package com.github.standobyte.jojo.mechanics.explorer_map_creation;

import com.github.standobyte.jojo.init.ModContainers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.MapItemColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;

public class CartographyExplorerMapContainerMenu extends AbstractContainerMenu {
	public static final Component VANILLA_CONTAINER_TITLE = Component.translatable("container.cartography_table");
	public static final Component EXPLORER_CONTAINER_TITLE = Component.translatable("jojo_ripples.container.explorer_cartography_table");
	public static final int MAP_SLOT = 0;
	public static final int RESULT_SLOT = 1;
	private static final int INV_SLOT_START = 2;
	private static final int INV_SLOT_END = 29;
	private static final int USE_ROW_SLOT_START = 29;
	private static final int USE_ROW_SLOT_END = 38;
	private final ContainerLevelAccess access;
	long lastSoundTime;

	public Slot inputMapSlot;
	public Slot resultMapSlot;

	public ExplorerMapComponents components = new ExplorerMapComponents();

	public final Container inputContainer = new SimpleContainer(1) {
		@Override
		public void setChanged() {
			slotsChanged(this);
			super.setChanged();
		}

		@Override
		public int getMaxStackSize() {
			return 1;
		}
	};

	private final ResultContainer resultContainer = new ResultContainer() {
		@Override
		public void setChanged() {
			slotsChanged(this);
			super.setChanged();
		}
	};

	public CartographyExplorerMapContainerMenu(int containerId, Inventory playerInventory) {
		this(containerId, playerInventory.player, playerInventory, ContainerLevelAccess.NULL);
	}

	public CartographyExplorerMapContainerMenu(int containerId, Player player, Inventory playerInventory, final ContainerLevelAccess access) {
		super(ModContainers.CARTOGRAPHY_TABLE_EXPLORER.get(), containerId);
		this.access = access;

		inputMapSlot = addSlot(new Slot(inputContainer, 0, 15, 15) {
			@Override
			public boolean mayPlace(ItemStack item) {
				return item.is(Items.MAP);
			}
		});

		resultMapSlot = addSlot(new Slot(resultContainer, 0, 145, 39) {
			@Override
			public boolean mayPlace(ItemStack item) {
				return false;
			}

			@Override
			public void onTake(Player player, ItemStack item) {
				inputMapSlot.remove(1);
				item.getItem().onCraftedBy(item, player.level(), player);
				access.execute((level, blockPos) -> {
					if (!level.isClientSide()) {
						ExplorerMapComponents.finalizeMapItem(item, (ServerLevel) level, 
								components.posX, components.posZ);
					}

					long l = level.getGameTime();
					if (lastSoundTime != l) {
						level.playSound(null, blockPos, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.BLOCKS, 1.0F, 1.0F);
						lastSoundTime = l;
					}
				});
				super.onTake(player, item);
			}
		});

		// remained from copypasting CartographyTableMenu
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for (int k = 0; k < 9; k++) {
			addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
		}

		BlockPos blockPos = player.blockPosition();
		components.posX = blockPos.getX();
		components.posZ = blockPos.getZ();
	}


	@Override
	public boolean stillValid(Player player) {
		return stillValid(access, player, Blocks.CARTOGRAPHY_TABLE);
	}

	@Override
	public void slotsChanged(Container inventory) {
		setupResultSlot();
	}

	private void setupResultSlot() {
		ItemStack inputItem = inputMapSlot.getItem();
		ItemStack outputItem = resultMapSlot.getItem();
		if (!inputItem.isEmpty()) {
			access.execute((level, blockPos) -> {
				ItemStack resultMapItem = new ItemStack(Items.FILLED_MAP);
				ExplorerMapComponents.updateMapItem(resultMapItem, 
						components.makeItemName(), components.posX, components.posZ, 
						components.destinationIcon, components.itemColor);
				resultContainer.setItem(0, resultMapItem);
				broadcastChanges();
			});
		}
		else if (!outputItem.isEmpty()) {
			resultContainer.removeItemNoUpdate(0);
		}
	}

	@Override
	public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
		return slot.container != resultContainer && super.canTakeItemForPickAll(stack, slot);
	}

	// remained from copypasting CartographyTableMenu
	@Override
	public ItemStack quickMoveStack(Player player, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack clickedItem = slot.getItem();
			itemstack = clickedItem.copy();
			if (index == RESULT_SLOT) {
				clickedItem.getItem().onCraftedBy(clickedItem, player.level(), player);
				if (!moveItemStackTo(clickedItem, INV_SLOT_START, USE_ROW_SLOT_END, true)) {
					return ItemStack.EMPTY;
				}

				slot.onQuickCraft(clickedItem, itemstack);
			}
			else if (index != MAP_SLOT) {
				if (clickedItem.is(Items.MAP)) {
					if (!moveItemStackTo(clickedItem, MAP_SLOT, MAP_SLOT + 1, false)) {
						return ItemStack.EMPTY;
					}
				}
				else {
					if (index >= INV_SLOT_START && index < INV_SLOT_END) {
						if (!moveItemStackTo(clickedItem, USE_ROW_SLOT_START, USE_ROW_SLOT_END, false)) {
							return ItemStack.EMPTY;
						}
					}
					else if (index >= USE_ROW_SLOT_START && index < USE_ROW_SLOT_END && 
							!moveItemStackTo(clickedItem, INV_SLOT_START, INV_SLOT_END, false)) {
						return ItemStack.EMPTY;
					}
				}
			}
			else if (!moveItemStackTo(clickedItem, INV_SLOT_START, USE_ROW_SLOT_END, false)) {
				return ItemStack.EMPTY;
			}

			if (clickedItem.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			}

			slot.setChanged();
			if (clickedItem.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, clickedItem);
			broadcastChanges();
		}

		return itemstack;
	}

	@Override
	public void removed(Player player) {
		super.removed(player);
		resultContainer.removeItemNoUpdate(0);
		access.execute((level, blockPos) -> clearContainer(player, inputContainer));
	}


	public boolean setTargetPosX(int posX) {
		if (components.posX != posX) {
			components.posX = posX;
			setupResultSlot();
			return true;
		}
		return false;
	}

	public boolean setTargetPosZ(int posZ) {
		if (components.posZ != posZ) {
			components.posZ = posZ;
			setupResultSlot();
			return true;
		}
		return false;
	}

	public boolean setTargetIcon(Holder<MapDecorationType> decorationType) {
		if (decorationType != null && !components.destinationIcon.is(decorationType)) {
			components.destinationIcon = decorationType;
			MapDecorationType value = decorationType.value();
			if (value.hasMapColor()) {
				components.itemColor = value.mapColor();
			}
			else {
				components.itemColor = MapItemColor.DEFAULT.rgb();
			}
			setupResultSlot();
			return true;
		}
		return false;
	}

	public boolean setMapItemColor(int color) {
		if (components.itemColor != color) {
			components.itemColor = color;
			setupResultSlot();
			return true;
		}
		return false;
	}

	public boolean setMapItemName(String name) {
		if (name != null && !components.itemName.equals(name)) {
			components.itemName = name;
			setupResultSlot();
			return true;
		}
		return false;
	}
}
