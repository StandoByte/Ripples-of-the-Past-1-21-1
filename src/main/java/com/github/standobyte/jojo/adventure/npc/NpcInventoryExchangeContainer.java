package com.github.standobyte.jojo.adventure.npc;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.init.ModContainers;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.PlayerExternalContainers.MenuConstructor_;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.IContainerFactory;

public class NpcInventoryExchangeContainer extends AbstractContainerMenu {
	public PowerUserMobEntity character;
	public Inventory charInventory;
	public boolean isDebug;
	//public final Container charClothes;

	public NpcInventoryExchangeContainer(int containerId, Inventory inventory, PowerUserMobEntity character, boolean isDebug) {
		super(ModContainers.NPC_INV_EXCHANGE.get(), containerId);
		this.charInventory = character.asPlayer().getInventory();
		//this.charClothes = character.getBodyArmorAccess();
		this.character = character;
		charInventory.startOpen(inventory.player);
		this.isDebug = isDebug;
		
		int x0 = 8;
		int y0 = 102;
		int NPC_INV_OFFSET = -100;
		
		int index;
		int x;
		int y;
		
		// npc inventory
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				index = col + row * 9 + 9;
				x = x0 + col * 18;
				y = y0 + row * 18 + -18 + NPC_INV_OFFSET;
				this.addSlot(new Slot(charInventory, index, x, y));
			}
		}
		for (int col = 0; col < 9; col++) {
			index = col;
			x = x0 + col * 18;
			y = y0 + 40 + NPC_INV_OFFSET;
			this.addSlot(new Slot(charInventory, index, x, y));
		}

		// player inventory
		for (int row = 0; row < 3; row++) {
			for (int col = 0; col < 9; col++) {
				index = col + row * 9 + 9;
				x = x0 + col * 18;
				y = y0 + row * 18 + -18;
				this.addSlot(new Slot(inventory, index, x, y));
			}
		}
		// hotbar
		for (int col = 0; col < 9; col++) {
			index = col;
			x = x0 + col * 18;
			y = y0 + 40;
			this.addSlot(new Slot(inventory, index, x, y));
		}
		// TODO offhand slot
		// TODO armor
		// TODO clothes
	}

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

	// FIXME quick move
	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem();
			itemstack = itemstack1.copy();
			int i = this.charInventory.getContainerSize() + 1;
			if (index < i) {
				if (!this.moveItemStackTo(itemstack1, i, this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (this.getSlot(1).mayPlace(itemstack1) && !this.getSlot(1).hasItem()) {
				if (!this.moveItemStackTo(itemstack1, 1, 2, false)) {
					return ItemStack.EMPTY;
				}
			} else if (this.getSlot(0).mayPlace(itemstack1)) {
				if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
					return ItemStack.EMPTY;
				}
			} else if (i <= 1 || !this.moveItemStackTo(itemstack1, 2, i, false)) {
				int j = i + 27;
				int k = j + 9;
				if (index >= j && index < k) {
					if (!this.moveItemStackTo(itemstack1, i, j, false)) {
						return ItemStack.EMPTY;
					}
				} else if (index >= i && index < j) {
					if (!this.moveItemStackTo(itemstack1, j, k, false)) {
						return ItemStack.EMPTY;
					}
				} else if (!this.moveItemStackTo(itemstack1, j, j, false)) {
					return ItemStack.EMPTY;
				}

				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}

		return itemstack;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.charInventory.stopOpen(player);
	}

}
