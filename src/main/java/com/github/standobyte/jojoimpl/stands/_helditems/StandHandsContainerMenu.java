package com.github.standobyte.jojoimpl.stands._helditems;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.init.core.ModContainers;
import com.github.standobyte.jojo.mechanics.externalcontainer.PlayerExternalContainers.MenuConstructor_;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.IContainerFactory;

public class StandHandsContainerMenu extends AbstractContainerMenu {
	public Container handsContainer;
	public StandEntity standEntity;

	public StandHandsContainerMenu(int containerId, Inventory playerInventory, StandEntity standEntity) {
		super(ModContainers.STAND_HANDS.get(), containerId);
		this.handsContainer = standEntity.handsPseudoInventory;
		this.standEntity = standEntity;
		this.addSlot(new Slot(handsContainer, 0, 0, 0));
		this.addSlot(new Slot(handsContainer, 1, 0, 0));
	}
	
	
	public static MenuConstructor_ createServerSide(StandEntity standEntity) {
		return new MenuConstructor_() {
			
			@Override
			public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
				return new StandHandsContainerMenu(containerId, playerInventory, standEntity);
			}
			
			@Override
		    public void writeClientSideData(AbstractContainerMenu _container, RegistryFriendlyByteBuf buffer) {
				StandHandsContainerMenu container = (StandHandsContainerMenu) _container;
				buffer.writeInt(container.standEntity.getId());
		    }
		};
	}
	
	public static final IContainerFactory<StandHandsContainerMenu> CLIENT_FACTORY = (int containerId, Inventory inv, RegistryFriendlyByteBuf data) -> {
		int entityId = data.readInt();
		Entity entity = ClientProxy.getEntityById(entityId);
		if (entity instanceof StandEntity standEntity) {
			StandHandsContainerMenu menu = new StandHandsContainerMenu(containerId, inv, standEntity);
			return menu;
		}
		return null;
	};


	@Override
	public boolean stillValid(Player player) {
		return handsContainer.stillValid(player);
	}

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.handsContainer.stopOpen(player);
	}

}
