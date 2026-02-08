package com.github.standobyte.jojoimpl.stands._helditems;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.init.core.ModContainers;
import com.github.standobyte.jojo.mechanics.externalcontainer.PlayerExternalContainers.MenuConstructor_;
import com.github.standobyte.jojo.mixin.container.ContainerMenuInvoker;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
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
	
	public Slot getLeftHandSlot() {
		boolean leftMainHand = standEntity.getMainArm() == HumanoidArm.LEFT;
		return slots.get(leftMainHand ? 0 : 1);
	}
	
	public Slot getRightHandSlot() {
		boolean leftMainHand = standEntity.getMainArm() == HumanoidArm.LEFT;
		return slots.get(leftMainHand ? 1 : 0);
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
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
		switch (clickType) {
			case THROW -> {
				if (this.getCarried().isEmpty() && slotId >= 0) {
					Slot slot = this.slots.get(slotId);
					int amountToDrop = button == 0 ? 1 : slot.getItem().getCount();
					ItemStack toDrop = slot.safeTake(amountToDrop, Integer.MAX_VALUE, player);
					InteractionHand hand = slot == getLeftHandSlot() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
					standEntity.drop(toDrop, hand);
				}
				return;
			}
			case SWAP -> {
				if (button == Inventory.SLOT_OFFHAND) {
					/* LivingEntity#handleHandSwap checks if the items held by the entity were swapped, 
					 * and sends a packet that literally just swaps the items on the client side, 
					 * without the data of actual items being sent.
					 * In this case, however, this conflicts with the regular container item synchronization
					 * (ContainerSynchronizer#sendInitialData in particular), causing a desync.
					 * So, while pressing the F key to swap main and off hand items held by the Stand, 
					 * we just disable that method and then update both items by calling LivingEntity#detectEquipmentUpdates() manually.
					 * Just fucking kill me already.
					 */
					boolean serverSide = !standEntity.level().isClientSide();
					HandSwapSyncFixCrutch desyncCrutch = serverSide ? (HandSwapSyncFixCrutch) standEntity : null;
					if (serverSide) {
						desyncCrutch.jojo_ripples$enableHandSwapCrutch();
					}
					
					Slot clickedSlot = this.slots.get(slotId);
					Slot otherSlot = this.slots.get(slotId == 0 ? 1 : 0);
					ItemStack clickedItem = clickedSlot.getItem();
					ItemStack otherItem = otherSlot.getItem();
					boolean update = false;
					if (!otherItem.isEmpty() || !clickedItem.isEmpty()) {
						if (!otherItem.isEmpty() && !clickedItem.isEmpty() && ItemStack.isSameItemSameComponents(clickedItem, otherItem)) {
							int itemStacksSum = otherItem.getCount() + clickedItem.getCount();
							int maxStackSize = otherSlot.getMaxStackSize(otherItem);
							if (itemStacksSum <= maxStackSize) {
								clickedItem.setCount(0);
								otherItem.setCount(itemStacksSum);
								update = true;
							}
							else if (otherItem.getCount() < maxStackSize) {
								clickedItem.shrink(maxStackSize - otherItem.getCount());
								otherItem.setCount(maxStackSize);
								update = true;
							}
						}
						else {
							otherSlot.setByPlayer(clickedItem);
							clickedSlot.setByPlayer(otherItem);
							update = true;
						}
						if (update) {
							otherSlot.setChanged();
							clickedSlot.setChanged();
						}
					}
					
					if (serverSide) {
						standEntity.detectEquipmentUpdates();
						desyncCrutch.jojo_ripples$disableHandSwapCrutch();
					}
					return;
				}
			}
			default -> {}
		}
		super.clicked(slotId, button, clickType, player);
    }

	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack itemMoved = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem() && player.inventoryMenu != null) {
			ItemStack itemInSlot = slot.getItem();
			itemMoved = itemInSlot.copy();
			if (!((ContainerMenuInvoker) player.inventoryMenu).invokeMoveItemStackTo(
					itemInSlot, InventoryMenu.INV_SLOT_START, InventoryMenu.USE_ROW_SLOT_END, false)) {
				return ItemStack.EMPTY;
			}

			if (itemInSlot.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (itemInSlot.getCount() == itemMoved.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, itemInSlot);
		}

		return itemMoved;
	}
	
	@Override
    public void synchronizeSlotToRemote(int slotIndex, ItemStack stack, Supplier<ItemStack> supplier) {
        if (!this.suppressRemoteUpdates) {
        	// it's called in LivingEntity every tick anyway, but whatever
        	standEntity.detectEquipmentUpdates();
        }
    }

	@Override
	public boolean stillValid(Player player) {
		return handsContainer.stillValid(player);
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		this.handsContainer.stopOpen(player);
	}
	
	
	public static interface HandSwapSyncFixCrutch {
		public void jojo_ripples$enableHandSwapCrutch();
		public void jojo_ripples$disableHandSwapCrutch();
	}

}
