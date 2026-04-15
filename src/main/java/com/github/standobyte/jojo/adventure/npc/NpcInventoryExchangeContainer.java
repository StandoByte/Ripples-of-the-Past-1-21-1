package com.github.standobyte.jojo.adventure.npc;

import com.github.standobyte.jojo.adventure.StandAsDiscSlot;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.init.ModContainers;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.mechanics.clothes.EntityClothesInventory;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.PlayerExternalContainers.MenuConstructor_;
import com.github.standobyte.jojo.util.functions.ContainerMenuUtil;
import com.github.standobyte.jojo.util.functions.ContainerMenuUtil.SlotIndices;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.IContainerFactory;

// TODO (NPC inventory AI) "forced" equipment
public class NpcInventoryExchangeContainer extends AbstractContainerMenu {
	public PowerUserMobEntity character;
	public Inventory charInventory;
	public boolean isDebug;
	//public final Container charClothes;

	public NpcInventoryExchangeContainer(int containerId, Inventory inventory, PowerUserMobEntity character, boolean isDebug) {
		super(ModContainers.NPC_INV_EXCHANGE.get(), containerId);
		this.charInventory = character.asPlayer().getInventory();
		this.character = character;
		charInventory.startOpen(inventory.player);
		this.isDebug = isDebug;

		// player inventory
		Player player = inventory.player;
		
		for (Slot slot : ContainerMenuUtil.armorSlots(inventory, player, -28, 88, playerSlots)) {
			this.addSlot(slot);
		}

		EntityClothesInventory playerClothes = player.getData(ModDataAttachmentTypes.HUMANOID_CLOTHES.get());
		for (Slot slot : ContainerMenuUtil.clothesSlots(playerClothes, player, -46, 88, playerSlots)) {
			this.addSlot(slot);
		}
		
		this.addSlot(ContainerMenuUtil.offhandSlot(inventory, player, -10, 142, playerSlots));
		
		for (Slot slot : ContainerMenuUtil.inventorySlots(inventory, 12, 84, playerSlots)) {
			this.addSlot(slot);
		}
		
		// npc inventory
		for (Slot slot : ContainerMenuUtil.armorSlots(charInventory, character, -28, -12, charSlots)) {
			this.addSlot(slot);
		}

		EntityClothesInventory npcClothes = character.getData(ModDataAttachmentTypes.HUMANOID_CLOTHES.get());
		for (Slot slot : ContainerMenuUtil.clothesSlots(npcClothes, character, -46, -12, charSlots)) {
			this.addSlot(slot);
		}
		
		this.addSlot(ContainerMenuUtil.offhandSlot(charInventory, character, -10, 42, charSlots));

		for (Slot slot : ContainerMenuUtil.inventorySlots(charInventory, 8, -16, charSlots)) {
			this.addSlot(slot);
		}
		
		if (isDebug) {
			this.addSlot(new StandAsDiscSlot(character, -10, -12));
		}
	}
	
	public SlotIndices playerSlots = new SlotIndices(this);
	public SlotIndices charSlots = new SlotIndices(this);

	public static MenuConstructor_ createServerSide(PowerUserMobEntity character, boolean isDebug) {
		return new MenuConstructor_() {

			@Override
			public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
				return new NpcInventoryExchangeContainer(containerId, playerInventory, character, isDebug);
			}

			@Override
			public void writeClientSideData(AbstractContainerMenu _container, RegistryFriendlyByteBuf buffer) {
				buffer.writeBoolean(isDebug);
				NpcInventoryExchangeContainer container = (NpcInventoryExchangeContainer) _container;
				buffer.writeInt(container.character.getId());
			}
		};
	}

	public static final IContainerFactory<NpcInventoryExchangeContainer> CLIENT_FACTORY = (int containerId, Inventory inv, RegistryFriendlyByteBuf data) -> {
		boolean isDebug = data.readBoolean();
		int entityId = data.readInt();
		Entity entity = ClientProxy.getEntityById(entityId);
		if (entity instanceof PowerUserMobEntity character) {
			NpcInventoryExchangeContainer menu = new NpcInventoryExchangeContainer(containerId, inv, character, isDebug);
			return menu;
		}
		return null;
	};

	@Override
	public boolean stillValid(Player player) {
		return this.charInventory.stillValid(player)
				&& this.character.isAlive()
				&& player.canInteractWithEntity(this.character, 4.0);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.charInventory.stopOpen(player);
	}

}
