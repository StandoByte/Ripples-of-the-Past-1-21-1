package com.github.standobyte.jojo.mechanics.entity_like_player.opencontainer;

import java.util.OptionalInt;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientProxy;

import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FriendlyByteBufUtil;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.network.payload.AdvancedOpenScreenPayload;

public class OpenContainerAsEntity {

	// yaaaaaay copypasting
	public static OptionalInt openMenu(ServerPlayer entityWrapperAsPlayer, LivingEntity wrappedEntity, ServerPlayer actualPlayer, 
			@Nullable MenuProvider menu, @Nullable Consumer<RegistryFriendlyByteBuf> extraDataWriter, int containerId) {
		if (menu == null || actualPlayer == null) {
			return OptionalInt.empty();
		}
		else {
			if (actualPlayer.containerMenu != actualPlayer.inventoryMenu) {
				if (menu.shouldTriggerClientSideContainerClosingOnOpen()) {
					actualPlayer.closeContainer();
				}
				else {
					actualPlayer.doCloseContainer();
				}
			}

			AbstractContainerMenu containerMenu = menu.createMenu(containerId, actualPlayer.getInventory(), actualPlayer);
			if (containerMenu == null) {
				if (actualPlayer.isSpectator()) {
					actualPlayer.displayClientMessage(Component.translatable("container.spectatorCantOpen").withStyle(ChatFormatting.RED), true);
				}

				return OptionalInt.empty();
			}
			else {
				ContainerExtension containerMenu_ = (ContainerExtension) containerMenu;
				containerMenu_.jojo_ripples$setActualEntity(wrappedEntity);
				
				// Neo: Support sending additional arbitrary data to menu factories on the client-side
				byte[] extraData = FriendlyByteBufUtil.writeCustomData(
						buffer -> {
							menu.writeClientSideData(containerMenu, buffer);
							if (extraDataWriter != null) {
								extraDataWriter.accept(buffer);
							}
							writeServer(buffer, containerMenu, containerMenu_);
						},
						actualPlayer.registryAccess());
				actualPlayer.connection.send(new AdvancedOpenScreenPayload(containerMenu.containerId,
						containerMenu.getType(), menu.getDisplayName(), extraData));
				actualPlayer.initMenu(containerMenu);
				actualPlayer.containerMenu = containerMenu;
				NeoForge.EVENT_BUS.post(new PlayerContainerEvent.Open(actualPlayer, actualPlayer.containerMenu));
				return OptionalInt.of(containerMenu.containerId);
			}
		}
	}
	
	public static void writeServer(RegistryFriendlyByteBuf buf, AbstractContainerMenu container, ContainerExtension alsoContainer) {
		Entity entity = alsoContainer.jojo_ripples$getActualEntity();
		buf.writeInt(entity != null ? entity.getId() : -1);
	}
	
	public static void readClient(RegistryFriendlyByteBuf buf, AbstractContainerMenu containerMenu) {
		if (buf.readableBytes() > 0) {
			ContainerExtension containerMenu_ = (ContainerExtension) containerMenu;
			Entity actualEntity = ClientProxy.getEntityById(buf.readInt());
			containerMenu_.jojo_ripples$setActualEntity(actualEntity);
		}
	}
	
	
	public static interface ContainerExtension {
		public Entity jojo_ripples$getActualEntity();
		public void jojo_ripples$setActualEntity(Entity entity);
	}
	
}
