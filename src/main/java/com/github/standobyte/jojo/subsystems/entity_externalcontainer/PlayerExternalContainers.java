package com.github.standobyte.jojo.subsystems.entity_externalcontainer;

import java.util.Collection;
import java.util.OptionalInt;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.entityattachment.TickingEntityData;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.packet.ExternalContainerClosePacket;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.packet.ExternalContainerOpenPacket;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.packet.ExternalContainerSyncSetContentPacket;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.packet.ExternalContainerSyncSetDataPacket;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.packet.ExternalContainerSyncSetSlotPacket;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerSynchronizer;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.IMenuProviderExtension;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FriendlyByteBufUtil;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class PlayerExternalContainers implements TickingEntityData {
	public final Player player;
	protected Int2ObjectMap<AbstractContainerMenu> containers = new Int2ObjectArrayMap<>();
	protected ContainerSynchronizer containerSynchronizer;

	public PlayerExternalContainers(Player player) {
		this.player = player;
		if (!player.level().isClientSide()) {
			containerSynchronizer = new ExternalContainerSyncher((ServerPlayer) player);
			addTicking(player);
		}
	}

	protected int containerCounter;
	protected int nextContainerCounter() {
		this.containerCounter = this.containerCounter % 65536 + 1;
		return this.containerCounter;
	}

	public OptionalInt openMenu(@Nullable MenuConstructor_ menu, @Nullable Consumer<RegistryFriendlyByteBuf> extraDataWriter) {
		if (player.level().isClientSide() || menu == null) {
			return OptionalInt.empty();
		}
		else {
			int containerId = nextContainerCounter();
			AbstractContainerMenu containerMenu = menu.createMenu(containerId, player.getInventory(), player);
			if (containerMenu == null) {
				return OptionalInt.empty();
			}
			else {
				byte[] extraData = FriendlyByteBufUtil.writeCustomData(
						buffer -> {
							menu.writeClientSideData(containerMenu, buffer);
							if (extraDataWriter != null) {
								extraDataWriter.accept(buffer);
							}
						},
						player.registryAccess());
				MenuType<?> syncMenuType = containerMenu.getType();
				PacketDistributor.sendToPlayer((ServerPlayer) player, new ExternalContainerOpenPacket(
						containerMenu.containerId, syncMenuType, extraData));
				// holy shit a Mojang abstraction that is actually kinda helpful
				containerMenu.setSynchronizer(containerSynchronizer);
				this.containers.put(containerMenu.containerId, containerMenu);
				NeoForge.EVENT_BUS.post(new PlayerContainerEvent.Open(player, containerMenu));
				return OptionalInt.of(containerMenu.containerId);
			}
		}
	}
	
	public static interface MenuConstructor_ extends MenuConstructor, IMenuProviderExtension {}

	public void closeMenu(int containerMenuId) {
		if (!player.level().isClientSide() && containers.remove(containerMenuId) != null) {
			PacketDistributor.sendToPlayer((ServerPlayer) player, new ExternalContainerClosePacket(containerMenuId));
		}
	}

	@Override
	public void tick() {
		if (!player.level().isClientSide()) {
			var iter = containers.int2ObjectEntrySet().iterator();
			while (iter.hasNext()) {
				var entry = iter.next();
				AbstractContainerMenu container = entry.getValue();
				if (!container.stillValid(player)) {
					iter.remove();
					PacketDistributor.sendToPlayer((ServerPlayer) player, new ExternalContainerClosePacket(entry.getIntKey()));
				}
			}
		}
		if (player.containerMenu != null) {
			containers.values().forEach(extContainer -> {
				tickSyncContainers(extContainer, player.containerMenu);
			});
		}
	}

	public AbstractContainerMenu getContainer(int containerId) {
		return containers.get(containerId);
	}

	public void clientAddContainer(AbstractContainerMenu menu) {
		containers.put(menu.containerId, menu);
	}

	public void clientRemoveContainer(int menuId) {
		/*AbstractContainerMenu menu = */containers.remove(menuId);
	}
	
	public Collection<AbstractContainerMenu> getAllContainers() {
		return containers.values();
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	public <T extends AbstractContainerMenu> T getContainerOfType(Class<T> clazz) {
		for (AbstractContainerMenu container : containers.values()) {
			if (clazz.isInstance(container)) {
				return (T) container;
			}
		}
		return null;
	}


	public static PlayerExternalContainers get(Player player) {
		return player.getData(ModDataAttachmentTypes.EXTERNAL_CONTAINERS);
	}


	public static class ExternalContainerSyncher implements ContainerSynchronizer {
		protected ServerPlayer player;

		public ExternalContainerSyncher(ServerPlayer player) {
			this.player = player;
		}

		@Override
		public void sendInitialData(AbstractContainerMenu container, NonNullList<ItemStack> items, ItemStack carriedItem, int[] initialData) {
			PacketDistributor.sendToPlayer(player, new ExternalContainerSyncSetContentPacket(container.containerId, container.incrementStateId(), items, carriedItem));

			for (int i = 0; i < initialData.length; i++) {
				this.broadcastDataValue(container, i, initialData[i]);
			}
		}

		@Override
		public void sendSlotChange(AbstractContainerMenu container, int slot, ItemStack itemStack) {
			PacketDistributor.sendToPlayer(player, new ExternalContainerSyncSetSlotPacket(container.containerId, container.incrementStateId(), slot, itemStack));
		}

		@Override
		public void sendCarriedChange(AbstractContainerMenu containerMenu, ItemStack stack) {
			PacketDistributor.sendToPlayer(player, new ExternalContainerSyncSetSlotPacket(-1, containerMenu.incrementStateId(), -1, stack));
		}

		@Override
		public void sendDataChange(AbstractContainerMenu container, int id, int value) {
			this.broadcastDataValue(container, id, value);
		}

		protected void broadcastDataValue(AbstractContainerMenu container, int id, int value) {
			PacketDistributor.sendToPlayer(player, new ExternalContainerSyncSetDataPacket(container.containerId, id, value));
		}
	}
	
	
	public static void click(AbstractContainerMenu container, int slotId, int mouseButton, ModdedContainerClickType clickType, Player player) {
		ItemStack prevCarried = container.getCarried();
		
		if (clickType.vanillaType != null) {
			container.clicked(slotId, mouseButton, clickType.vanillaType, player);
		}
		else {
			ModdedContainerClickType.clicked(container, slotId, mouseButton, clickType, player);
		}
		
		// if the carried item has changed, update it for the main player container too
		if (player.containerMenu != null && player.containerMenu != container) {
			ItemStack carried = container.getCarried();
			if (carried != prevCarried) {
				player.containerMenu.setCarried(carried);
			}
		}
	}
	
	protected static void tickSyncContainers(AbstractContainerMenu extContainer, AbstractContainerMenu mainContainer) {
		extContainer.setCarried(mainContainer.getCarried());
	}

}
