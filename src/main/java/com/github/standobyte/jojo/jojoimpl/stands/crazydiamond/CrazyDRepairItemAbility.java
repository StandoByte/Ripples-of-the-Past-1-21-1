package com.github.standobyte.jojo.jojoimpl.stands.crazydiamond;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.input.AbilityInputState;
import com.github.standobyte.jojo.client.input.InputHandler;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.ability.AbilityUsageGroup;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.util.MathUtil;
import com.github.standobyte.jojo.util.UtilFunctions;
import com.github.standobyte.jojo.util.mc.ContainerSlotInput;
import com.github.standobyte.jojo.util.mc.ItemUtil;
import com.github.standobyte.jojo.util.network.NetworkUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class CrazyDRepairItemAbility extends StandEntityAbility {

	public CrazyDRepairItemAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId, ItemRepair::new);
		usageGroup = AbilityUsageGroup.INVENTORY;
		setButtonHoldPhase(ActionPhase.PERFORM);
	}

	@Override
	public AbilityInputState cl_abilityInputState(Power<?> context) {
		AbilityInputState state = AbilityInputState.init();
		state.setFlag(AbilityInputState.ONLY_IN_CONTAINER, true);
		return state;
	}
	
	@Override
	public ConditionCheck checkSpecificConditions(Power<?> context) {
		if (context.getUser().level().isClientSide()) {
			Screen screen = Minecraft.getInstance().screen;
			if (screen instanceof AbstractContainerScreen invScreen) {
				boolean active = false;
				if (!InputHandler.inputsDisabled) {
					Slot hovered = invScreen.getSlotUnderMouse();
					if (hovered != null) {
						ItemStack item = hovered.getItem();
						// TODO (item repair) disable it when hovering over an item in a creative tab
						active = canBeRepaired(item);
					}
				}
				if (!active) {
					return ConditionCheck.NEGATIVE;
				}
			}
		}
		
		return super.checkSpecificConditions(context);
	}

	@Override
	public void writeExtraInput(FriendlyByteBuf serverboundBuf, LivingEntity user, boolean isClientPlayer) {
		if (isClientPlayer) {
			ContainerSlotInput hoveredItem = ContainerSlotInput.cl_HoveredSlot();
			NetworkUtil.writeOptionally(hoveredItem, serverboundBuf, ContainerSlotInput.STREAM_CODEC);
		}
	}


	// TODO (item repair) CD heal particles on the model of the item being repaired
	// TODO (item repair) sounds
	public static class ItemRepair extends EntityActionInstance {
		private Optional<ContainerSlotInput> inputInvSlot = Optional.empty();
		private ItemStack repairedStack = ItemStack.EMPTY;

		public ItemRepair(EntityActionType ability) {
			super(ability);
		}

		@Override
		public void extraClientInput(FriendlyByteBuf input) {
			inputInvSlot = NetworkUtil.readOptional(input, ContainerSlotInput.STREAM_CODEC);
		}

		@Override
		public void onActionSet(@Nullable EntityActionInstance prevAction) {
			if (inputInvSlot != null && inputInvSlot.isPresent() && powerUser.getEntity(level()) instanceof Player player) {
				repairedStack = ContainerSlotInput.getItem(inputInvSlot.get(), player);
			}
		}

		@Override
		public void actionTick() {
			Level level = level();
			if (!level.isClientSide()) {
				if (!canBeRepaired(repairedStack)) {
					setPhase(ActionPhase.RECOVERY, 0);
					syncPhaseChanges();
				}
				else if (performer instanceof StandEntity stand) {
					repairTick(powerUser.getEntityLiving(level), stand, repairedStack, curPhaseTick);
				}
			}
		}

		@Override
		public void onButtonStopHold() {
			if (getPhase() != ActionPhase.RECOVERY) {
				setPhaseStart(ActionPhase.RECOVERY);
				syncPhaseChanges();
			}
		}

		@Override
		public boolean canBeCancelledInto(EntityActionType cancellingAbility) {
			return true;
		}

	}


	public static boolean canBeRepaired(ItemStack itemStack) {
		if (itemStack == null || itemStack.isEmpty()) {
			return false;
		}

		if (itemStack.isDamaged() || itemStack.isEnchanted()
				|| itemStack.getItem() == Items.CHIPPED_ANVIL || itemStack.getItem() == Items.DAMAGED_ANVIL
				|| itemStack.getItem() == Items.COBBLESTONE) {
			return true;
		}

		ResourceLocation itemId = UtilFunctions.getItemId(itemStack);
		if (itemId.getPath().contains("cracked")) {
			ResourceLocation uncracked = itemId.withPath(path -> path.replace("cracked_", ""));
			if (BuiltInRegistries.ITEM.containsKey(uncracked)) {
				return true;
			}
		}

		return false;
	}
	
	public static class ItemRepairResult {
		static ItemRepairResult instance = new ItemRepairResult();
		
		public boolean isRepairing;
		public float uncraftLearnPoints;
	}

	public static ItemRepairResult repairTick(LivingEntity user, StandEntity standEntity, ItemStack itemStack, int taskTicks) {
		ItemRepairResult result = ItemRepairResult.instance;
		result.isRepairing = false;
		
		if (itemStack.isEmpty()) {
			result.uncraftLearnPoints = 0;
			return result;
		}
		
		int damage = 0;
		float multiplier = 1;
		ItemStack newStack = null;
		
		if (itemStack.getItem() == Items.CHIPPED_ANVIL) { 
			newStack = new ItemStack(Items.ANVIL);
			damage += 125;
		}
		else if (itemStack.getItem() == Items.DAMAGED_ANVIL) {
			newStack = new ItemStack(Items.CHIPPED_ANVIL);
			damage += 125;
		}
		else if (itemStack.getItem() == Items.COBBLESTONE) {
			newStack = new ItemStack(Items.STONE);
			damage += 1;
		}
		else if (itemStack.getItem() == Items.ENCHANTED_BOOK) {
			newStack = new ItemStack(Items.BOOK);
		}
		else {
			ResourceLocation itemId = UtilFunctions.getItemId(itemStack);
			if (itemId.getPath().contains("cracked")) {
				ResourceLocation uncracked = itemId.withPath(path -> path.replace("cracked_", ""));
				if (BuiltInRegistries.ITEM.containsKey(uncracked)) {
					damage += 1;
					newStack = new ItemStack(BuiltInRegistries.ITEM.get(uncracked));
				}
			}
		}
		
		switch (itemStack.getRarity()) {
		case UNCOMMON:
			multiplier += 0.5f;
			break;
		case RARE:
			multiplier += 1.5f;
			break;
		case EPIC:
			multiplier += 3;
			break;
		default:
			break;
		}
		
		if (itemStack.isDamageableItem()) {
			// TODO (item repair) retrieve the tool tier to give more learning points
//			if (itemStack.getItem() instanceof TieredItem) {
//				int level = ((TieredItem) itemStack.getItem()).getTier().getLevel();
//				multiplier += (float) level / 2;
//			}
			int damageToRestore = Math.min(itemStack.getDamageValue(), (int) (CrazyDHealAbility.crazyDRestorationSpeed(standEntity) * 40));
			damage += damageToRestore;
			itemStack.setDamageValue(itemStack.getDamageValue() - damageToRestore);
			itemStack.set(DataComponents.REPAIR_COST, 0);
			if (damageToRestore > 0) {
				result.isRepairing = true;
			}
		}
		boolean transformTick = isItemTransformationTick(taskTicks, standEntity);
		
		int xp = getFullExperienceAmount(itemStack);
		if (xp > 0) {
			if (damage > 0) {
				damage += xp * 5;
			}
			dropExperience(user, xp);
		}
		DataComponentType<ItemEnchantments> enchComponentType = EnchantmentHelper.getComponentType(itemStack);
		if (itemStack.has(enchComponentType)) {
			itemStack.set(enchComponentType, ItemEnchantments.EMPTY);
		}

		if (newStack != null) {
			result.isRepairing = true;
			if (transformTick) {
				itemStack.shrink(1);
				ItemUtil.giveItemTo(user, newStack, true);
			}
			else {
				damage = -1;
			}
		}

		result.uncraftLearnPoints = (float) damage * multiplier * UNCRAFT_LEARNING_RATE;
		return result;
	}
	private static final float UNCRAFT_LEARNING_RATE = 0.002f / 13f;

	public static boolean isItemTransformationTick(int taskTicks, StandEntity standEntity) {
		int ticks = (int) (10 / CrazyDHealAbility.crazyDRestorationSpeed(standEntity));
		return taskTicks % ticks == ticks - 1;
	}
	
	public static int dropExperience(LivingEntity entity, int xp) {
		Level level = entity.level();
		if (!level.isClientSide() && xp > 0) {
			int i1 = (int) Math.ceil((double)xp / 2.0);
			xp = i1 + level.random.nextInt(i1);
			
			int xpToDrop = xp;
			Vec3 pos = entity.position().add(new Vec3(
					0, 
					entity.getBbHeight() * (entity.isShiftKeyDown() ? 0.25 : 0.45), 
					entity.getBbWidth() * 0.7)
					.yRot(-entity.yBodyRot * MathUtil.DEG_TO_RAD));
			while (xpToDrop > 0) {
				int xpThisOrb = ExperienceOrb.getExperienceValue(xpToDrop);
				xpToDrop -= xpThisOrb;
				level.addFreshEntity(new ExperienceOrb(level, pos.x, pos.y, pos.z, xpThisOrb));
			}
			
			return xp;
		}
		return 0;
	}

	private static int getFullExperienceAmount(ItemStack item) {
		int xp = 0;

		ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(item);
		for (var entry : enchantments.entrySet()) {
			Holder<Enchantment> enchantment = entry.getKey();
			int enchLvl = entry.getIntValue();
			if (!enchantment.is(EnchantmentTags.CURSE)) {
				xp += enchantment.value().getMinCost(enchLvl);
			}
		}
		
		return xp;
	}

	// TODO (!!!!!) (item repair) "jojo.crazy_diamond_fix.warning"

}
