package com.github.standobyte.jojo.subsystems.entity_externalcontainer._stand;

import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.init.ModContainers;
import com.github.standobyte.jojo.mixin.container.ContainerMenuInvoker;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.PlayerExternalContainers;
import com.github.standobyte.jojo.subsystems.entity_externalcontainer.PlayerExternalContainers.MenuConstructor_;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.IContainerFactory;

public class StandHandsContainerMenu extends AbstractContainerMenu {
	public Container handsContainer;
	public StandEntity standEntity;
	protected Slot mainHandSlot;
	protected Slot offHandSlot;

	public StandHandsContainerMenu(int containerId, Inventory playerInventory, StandEntity standEntity) {
		super(ModContainers.STAND_HANDS.get(), containerId);
		this.handsContainer = standEntity.handsPseudoInventory;
		this.standEntity = standEntity;
		this.addSlot(mainHandSlot = new Slot(handsContainer, 0, 0, 0));
		this.addSlot(offHandSlot = new Slot(handsContainer, 1, 0, 0));
	}
	
	public Slot getLeftHandSlot() { return standEntity.getMainArm() == HumanoidArm.LEFT ? mainHandSlot : offHandSlot; }
	public Slot getRightHandSlot() { return standEntity.getMainArm() == HumanoidArm.LEFT ? offHandSlot : mainHandSlot; }
	public Slot getMainHandSlot() { return mainHandSlot; }
	public Slot getOffHandSlot() { return offHandSlot; }
	
	
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
						desyncCrutch.jojo_ripples$disableHandSwapCheck();
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
						desyncCrutch.jojo_ripples$reenableHandSwapCheck();
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
		Slot clickedSlot = this.slots.get(index);
		if (clickedSlot != null && clickedSlot.hasItem() && player.inventoryMenu != null) {
			ItemStack itemInSlot = clickedSlot.getItem();
			itemMoved = itemInSlot.copy();
			if (!((ContainerMenuInvoker) player.inventoryMenu).invokeMoveItemStackTo(
					itemInSlot, InventoryMenu.INV_SLOT_START, InventoryMenu.USE_ROW_SLOT_END, false)) {
				return ItemStack.EMPTY;
			}

			if (itemInSlot.isEmpty()) {
				clickedSlot.setByPlayer(ItemStack.EMPTY);
			} else {
				clickedSlot.setChanged();
			}

			if (itemInSlot.getCount() == itemMoved.getCount()) {
				return ItemStack.EMPTY;
			}

			clickedSlot.onTake(player, itemInSlot);
		}

		return itemMoved;
	}
	

	public void handleCtrlClickAKAStandQuickMove(AbstractContainerMenu mainContainer, int slotId, int mouseButton, Player player) {
		Slot clickedSlot = slotId > 0 && slotId < mainContainer.slots.size() ? mainContainer.getSlot(slotId) : null;
		if (clickedSlot != null) {
			StandHandsContainerMenu standHandsContainer = PlayerExternalContainers.get(player)
					.getContainerOfType(StandHandsContainerMenu.class);
			if (standHandsContainer != null) {
				ItemStack clickedItem = clickedSlot.getItem();
				if (clickedItem.isEmpty()) {
					// take held items from stand and put them to the clicked empty slot
					quickTakeFromStand(player, mainContainer, slotId, mouseButton);
				}
				else {
					// give the clicked item to the stand
					if (clickedSlot.mayPickup(player)) {
						ItemStack movedItem = quickGiveToStand(player, mainContainer, slotId, mouseButton);
						if (movedItem.isEmpty() && mouseButton == 0) {
							/* couldn't find stand hand slots that are empty or stackable, 
							 * instead swap the clicked item with the stand's main hand item
							 */
							Slot standMainHandSlot = getMainHandSlot();
							ItemStack standMainHandItem = standMainHandSlot.getItem();
							if (clickedSlot.mayPlace(standMainHandItem)) {
								standMainHandSlot.setByPlayer(clickedItem);
								clickedSlot.setByPlayer(standMainHandItem);
							}
						}
						// the quick move handling was copypasted from AbstractContainerMenu's doClick, but this seems redundant
						// else while (!movedItem.isEmpty() && ItemStack.isSameItem(clickedSlot.getItem(), movedItem)) {
						// 	movedItem = quickGiveToStand(player, mainContainer, slotId, mouseButton);
						// }
					}
				}
			}
		}
	}
	
	public ItemStack quickGiveToStand(Player player, AbstractContainerMenu mainContainer, int index, int mouseButton) {
		ItemStack itemPrev = ItemStack.EMPTY;
		Slot clickedSlot = mainContainer.getSlot(index);
		if (clickedSlot != null && clickedSlot.hasItem()) {
			ItemStack itemInSlot = clickedSlot.getItem();
			itemPrev = itemInSlot.copy();
			boolean rightClick = mouseButton == 1;
			ItemStack itemToMove;
			
			if (rightClick) {
				itemToMove = itemInSlot.split(1);
			}
			else {
				itemToMove = itemInSlot;
			}

			boolean offHandFirst = false;
			boolean quickMoved = this.moveItemStackTo(itemToMove, 0, 2, offHandFirst);
			if (itemToMove != itemInSlot /* when rightClick is true */ 
					&& !itemToMove.isEmpty() && ItemStack.isSameItem(itemToMove, itemInSlot)) {
				itemInSlot.setCount(itemInSlot.getCount() + itemToMove.getCount());
			}
			if (!quickMoved) {
				return ItemStack.EMPTY;
			}

			if (itemInSlot.isEmpty()) {
				clickedSlot.setByPlayer(ItemStack.EMPTY);
			}

			if (itemInSlot.getCount() == itemPrev.getCount()) {
				return ItemStack.EMPTY;
			}
			
			clickedSlot.setChanged();
		}

		return itemPrev;
	}

	public boolean quickTakeFromStand(Player player, AbstractContainerMenu mainContainer, int index, int mouseButton) {
		boolean moved = false;
		Slot clickedSlot = mainContainer.getSlot(index);
		if (clickedSlot != null) {
			boolean rightClick = mouseButton == 1 /* RMB */;
			for (int i = slots.size() - 1; i >= 0; i--) {
				Slot standSlot = slots.get(i);
				ItemStack itemToMove = standSlot.getItem();
				if (standSlot.hasItem() && (clickedSlot.mayPlace(itemToMove) || clickedSlot instanceof ArmorSlot armorSlot && armorSlot.slot == EquipmentSlot.HEAD /* the funny part of that one bug */)) {
					ItemStack destItem = clickedSlot.getItem();
					if (rightClick) {
						if (destItem.isEmpty()) {
							clickedSlot.setByPlayer(itemToMove.split(1));
							standSlot.setChanged();
							moved = true;
							break;
						}
					}
					else {
						if (destItem.isEmpty()) {
							standSlot.setByPlayer(ItemStack.EMPTY);
							clickedSlot.setByPlayer(itemToMove);
							moved = true;
						}
						else if (ItemStack.isSameItemSameComponents(itemToMove, destItem)) {
							int amountSum = destItem.getCount() + itemToMove.getCount();
							int maxAmount = clickedSlot.getMaxStackSize(destItem);
							if (amountSum <= maxAmount) {
								itemToMove.setCount(0);
								destItem.setCount(amountSum);
								clickedSlot.setChanged();
								standSlot.setChanged();
								moved = true;
							}
							else if (destItem.getCount() < maxAmount) {
								itemToMove.shrink(maxAmount - destItem.getCount());
								destItem.setCount(maxAmount);
								clickedSlot.setChanged();
								standSlot.setChanged();
								moved = true;
							}
						}
					}
				}
			}
		}
		
		return moved;
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
		public void jojo_ripples$disableHandSwapCheck();
		public void jojo_ripples$reenableHandSwapCheck();
	}

}
