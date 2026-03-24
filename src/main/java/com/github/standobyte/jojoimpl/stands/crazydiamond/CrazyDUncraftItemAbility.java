package com.github.standobyte.jojoimpl.stands.crazydiamond;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.util.functions.ItemUtil;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;
import com.github.standobyte.jojo.util.objects_mc.ContainerSlotInput;
import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDRepairItemAbility.ItemRepairResult;
import com.mojang.datafixers.util.Pair;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.level.Level;

public class CrazyDUncraftItemAbility extends StandEntityAbility {

	public CrazyDUncraftItemAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId, ItemUncraft::new);
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
	public void writeExtraInput(FriendlyByteBuf serverboundBuf, LivingEntity user, boolean isClientPlayer) {
		if (isClientPlayer) {
			ContainerSlotInput hoveredItem = ContainerSlotInput.cl_HoveredSlot();
			NetworkUtil.writeOptionally(hoveredItem, serverboundBuf, ContainerSlotInput.STREAM_CODEC);
		}
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


	// TODO (item repair) CD heal particles on the model of the item being repaired
	// TODO (item repair) sounds
	public static class ItemUncraft extends EntityActionInstance {
		public Optional<ContainerSlotInput> inputInvSlot = Optional.empty();
		public ItemStack repairedStack = ItemStack.EMPTY;

		public ItemUncraft(EntityActionType ability) {
			super(ability);
		}

		@Override
		public void extraClientInput(FriendlyByteBuf input) {
			inputInvSlot = NetworkUtil.readOptional(input, ContainerSlotInput.STREAM_CODEC);
		}

		@Override
		public void onActionSet(@Nullable EntityActionInstance prevAction) {
			if (inputInvSlot != null && inputInvSlot.isPresent() && powerUser.getEntity(level()) instanceof Player player) {
				repairedStack = inputInvSlot.get().getItem(player);
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
					LivingEntity user = getPowerUser();
					int tick = curPhaseTick;
					boolean isEnchantedBook = repairedStack.has(DataComponents.STORED_ENCHANTMENTS);
					ItemRepairResult repairProgress = CrazyDRepairItemAbility.repairTick(user, stand, repairedStack, tick);
					if (isEnchantedBook || (!repairProgress.isRepairing && CrazyDRepairItemAbility.isItemTransformationTick(tick, stand))) {
						convertTo(repairedStack, level, null, performer.getRandom(), true).ifPresent(itemsAndCount -> {
							boolean gaveIngredients = false;
							for (ItemStack ingredient : itemsAndCount.getFirst()) {
								if (!ingredient.isEmpty()) {
									ItemUtil.giveItemTo(user, ingredient, true);
									gaveIngredients = true;
								}
							}
							if (gaveIngredients) {
								repairedStack.shrink(itemsAndCount.getSecond());
							}
						});
						StandPower userPower = stand.getUserPower();
						if (userPower != null) {
							userPower.addExp(0.02f);
						}
					}
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
		return !itemStack.isEmpty();
	}

	public static final Optional<Pair<ItemStack[], Integer>> EXISTS = Optional.of(Pair.of(new ItemStack[0], 0));
	public static Optional<Pair<ItemStack[], Integer>> convertTo(ItemStack item, Level level, 
			@Nullable Predicate<Recipe<?>> additionalCondition, RandomSource random, boolean createItems) {
		if (item.isEmpty()) return Optional.empty();

		if (item.getItem() == Items.ENCHANTED_BOOK)		return createItems ? Optional.of(Pair.of(new ItemStack[]{new ItemStack(Items.BOOK)}, 1)) : EXISTS;
		// TODO (1.16.5) (uncrafting) free up the map id
		// if (item.getItem() == Items.FILLED_MAP)		return createItems ? Optional.of(Pair.of(new ItemStack[]{new ItemStack(Items.MAP)}, 1))  : EXISTS;

		WrittenBookContent writtenBookContent = item.get(DataComponents.WRITTEN_BOOK_CONTENT);
		if (writtenBookContent != null) {
			if (createItems) {
				ItemStack writableBook = new ItemStack(Items.WRITABLE_BOOK);
				writableBook.applyComponents(item.getComponents());
				writableBook.remove(DataComponents.WRITTEN_BOOK_CONTENT);
				return Optional.of(Pair.of(new ItemStack[]{writableBook}, 1));
			} else {
				return EXISTS;
			}
		}

		HolderLookup.Provider trims = level.registryAccess();
		// TODO (1.16.5) (uncrafting) revert brewing recipes
		return groupByPredicatesOrdered(
				level.getRecipeManager().getRecipes().stream().map(RecipeHolder::value), Util.make(new ArrayList<>(), list -> {
					// TODO (1.16.5) (uncrafting) revert nbt recipes (including netherite armor)
					list.add(recipe -> recipe instanceof SmithingRecipe);
					list.add(recipe -> recipe instanceof AbstractCookingRecipe);
					list.add(recipe -> recipe instanceof StonecutterRecipe);
					list.add(recipe -> recipe instanceof CraftingRecipe);
					list.add(recipe -> true);
				}), recipe -> outputMatches(recipe, item, trims) && !bannedItem(item, level, trims) && (additionalCondition == null || additionalCondition.test(recipe)), false)
				.values().stream().filter(list -> !list.isEmpty()).findFirst()
				.flatMap(recipesOfPreferredType -> {
					Recipe<?> randomRecipe = recipesOfPreferredType.get(random.nextInt(recipesOfPreferredType.size()));
					ItemStack[] ingredients = getIngredients(randomRecipe, random);
					if (ingredients.length == 0) return Optional.empty();
					return Optional.of(Pair.of(ingredients, randomRecipe.getResultItem(trims).getCount()));
				});
	}

	public static <T> LinkedHashMap<Predicate<T>, List<T>> groupByPredicatesOrdered(Stream<T> elements, List<Predicate<T>> predicates, 
			@Nullable Predicate<T> commonCondition, boolean elementRepeats) {
		LinkedHashMap<Predicate<T>, List<T>> map = Util.make(new LinkedHashMap<>(), m -> {
			predicates.forEach(key -> m.put(key, new ArrayList<>()));
		});
		elements.forEach(element -> {
			if (commonCondition == null || commonCondition.test(element)) {
				for (Predicate<T> predicate : predicates) {
					if (predicate.test(element)) {
						map.get(predicate).add(element);
						if (!elementRepeats) {
							break;
						}
					}
				}
			}
		});
		return map;
	}

	public static boolean outputMatches(Recipe<?> recipe, ItemStack stack, HolderLookup.Provider trims) {
		return recipe.getResultItem(trims).getItem() == stack.getItem() && recipe.getResultItem(trims).getCount() <= stack.getCount();
	}

	public static boolean bannedItem(ItemStack stack, Level level, HolderLookup.Provider trims) {
		return level.getRecipeManager().getRecipes().stream().map(RecipeHolder::value).anyMatch(recipe -> 
		recipe.getResultItem(trims).getItem() == stack.getItem() && recipe instanceof BlastingRecipe);
	}

	public static ItemStack[] getIngredients(Recipe<?> recipe, RandomSource random) {
		List<Ingredient> ingredients = recipe.getIngredients();
		ItemStack[] stacks = new ItemStack[ingredients.size()];

		for (int i = 0; i < ingredients.size(); i++) {
			ItemStack[] matchingStacks = ingredients.get(i).getItems();
			stacks[i] = matchingStacks.length > 0 ? matchingStacks[random.nextInt(matchingStacks.length)].copy() : ItemStack.EMPTY;
		}

		return stacks;
	}

}
