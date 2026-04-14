package com.github.standobyte.jojo.adventure.npc;

import com.github.standobyte.jojo.adventure.StandAsDiscSlot;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.init.ModContainers;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.mechanics.clothes.EntityClothesInventory;
import com.github.standobyte.jojo.mechanics.clothes.container.PlayerClothesMenu;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.PlayerExternalContainers.MenuConstructor_;

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
		
		// npc inventory
		for (Slot slot : PlayerClothesMenu.inventorySlots(charInventory, 8, -16)) {
			this.addSlot(slot);
		}
		this.addSlot(PlayerClothesMenu.offhandSlot(charInventory, character, -10, 42));
		for (Slot slot : PlayerClothesMenu.armorSlots(charInventory, character, -28, -12)) {
			this.addSlot(slot);
		}
		EntityClothesInventory npcClothes = character.getData(ModDataAttachmentTypes.HUMANOID_CLOTHES.get());
		for (Slot slot : PlayerClothesMenu.clothesSlots(npcClothes, character, -46, -12)) {
			this.addSlot(slot);
		}
		if (isDebug) {
			this.addSlot(new StandAsDiscSlot(character, -10, -12));
		}

		// player inventory
		Player player = inventory.player;
		for (Slot slot : PlayerClothesMenu.inventorySlots(inventory, 12, 84)) {
			this.addSlot(slot);
		}
		this.addSlot(PlayerClothesMenu.offhandSlot(inventory, player, -10, 142));
		for (Slot slot : PlayerClothesMenu.armorSlots(inventory, player, -28, 88)) {
			this.addSlot(slot);
		}
		EntityClothesInventory playerClothes = player.getData(ModDataAttachmentTypes.HUMANOID_CLOTHES.get());
		for (Slot slot : PlayerClothesMenu.clothesSlots(playerClothes, player, -46, 88)) {
			this.addSlot(slot);
		}
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
