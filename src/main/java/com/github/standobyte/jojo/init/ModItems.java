package com.github.standobyte.jojo.init;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import com.github.standobyte.jojo.DebugItem;
import com.github.standobyte.jojo.adventure.npc.debug.CharacterTestItem;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.mechanics.clothes.ClothesItem;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesDataComponent;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesPiece;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSlotType;
import com.github.standobyte.jojo.mechanics.clothes.mannequin.MannequinItem;
import com.github.standobyte.jojo.mechanics.standarrow.StandArrowItem;
import com.github.standobyte.jojo.mechanics.standarrow.StandArrowShardItem;
import com.github.standobyte.jojo.mechanics.standdisc.StandDiscItem;
import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.github.standobyte.jojo.subsystems.StoryPart;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// TODO datagen for crafting recipes, advancements and loot tables
@EventBusSubscriber(modid = JojoMod.MOD_ID)
public final class ModItems {
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, JojoMod.MOD_ID);
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(JojoMod.MOD_ID);

	public static final DeferredItem<Item> DEBUG_ITEM = ITEMS.registerItem("debug_item", DebugItem::new, new Item.Properties());
	public static final DeferredItem<Item> CHARACTER_TEST = ITEMS.registerItem("character_test", CharacterTestItem::new, new Item.Properties());

	public static final DeferredItem<Item> STAND_DISC = ITEMS.registerItem("stand_disc", StandDiscItem::new, new Item.Properties().stacksTo(1));

	public static final DeferredItem<BlockItem> SEWING_MACHINE = ITEMS.registerSimpleBlockItem("sewing_machine", 
			ModBlocks.SEWING_MACHINE, new Item.Properties());

	public static final DeferredItem<Item> SEWING_NEEDLE = ITEMS.registerSimpleItem("sewing_needle");

	public static final DeferredItem<Item> MANNEQUIN = ITEMS.registerItem("mannequin", props -> new MannequinItem(props, false), new Item.Properties().stacksTo(16));

	public static final DeferredItem<Item> MANNEQUIN_SLIM = ITEMS.registerItem("mannequin_slim", props -> new MannequinItem(props, true), new Item.Properties().stacksTo(16));

	public static final DeferredItem<ClothesItem> CLOTHES_BASE_ITEM = ITEMS.registerItem("clothes", props -> new ClothesItem(props));

	public static final DeferredItem<Item> STAND_ARROW = ITEMS.registerItem("stand_arrow", props -> new StandArrowItem(props), 
			new Item.Properties().stacksTo(1).rarity(Rarity.RARE).durability(5));
	public static final DeferredItem<Item> STAND_ARROW_BEETLE = ITEMS.registerItem("stand_arrow_beetle", props -> new StandArrowItem(props), 
			new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
	public static final DeferredItem<Item> STAND_ARROW_METEORITE = ITEMS.registerItem("stand_arrow_meteorite", props -> new StandArrowItem(props), 
			new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON).durability(25));

	public static final DeferredItem<Item> STAND_ARROW_SHARD = ITEMS.register("stand_arrow_shard", () -> new StandArrowShardItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
	public static final DeferredItem<BlockItem> METEORIC_IRON = ITEMS.register("meteoric_iron", props -> new BlockItem(ModBlocks.METEORIC_IRON.get(), new Item.Properties()));
	public static final DeferredItem<BlockItem> METEORITE_CORE = ITEMS.register("meteorite_core", props -> new BlockItem(ModBlocks.METEORITE_CORE.get(), new Item.Properties().rarity(Rarity.UNCOMMON)));
	public static final DeferredItem<Item> METEORIC_SCRAP = ITEMS.register("meteoric_scrap", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
	public static final DeferredItem<Item> METEORIC_INGOT = ITEMS.register("meteoric_ingot", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

	public static Comparator<StandInstance> discsOrder(HolderLookup.Provider registries) {
		return Comparator
				.comparingInt((StandInstance stand) -> stand.getStandType().discCategoryPriority)
				.thenComparing((StandInstance stand) -> StoryPart.getStoryPart(stand, registries), StoryPart.COMPARATOR)
				.thenComparingInt((StandInstance stand) -> stand.getStandType().discStoryPartPriority);
	}

	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register(JojoMod.MOD_ID + "_main", () -> CreativeModeTab.builder()
			.title(Component.translatable("itemGroup." + JojoMod.MOD_ID + "_main"))
			.icon(() -> DEBUG_ITEM.value().getDefaultInstance())
			.displayItems((CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) -> {
				// most of the mod's items
				output.accept(STAND_ARROW.get());
				output.accept(STAND_ARROW_BEETLE.get());
				output.accept(STAND_ARROW_METEORITE.get());
				output.accept(STAND_ARROW_SHARD.get());
				output.accept(METEORIC_IRON.get());
				output.accept(METEORITE_CORE.get());
				output.accept(METEORIC_SCRAP.get());
				output.accept(METEORIC_INGOT.get());

				Stream<StandType> stands = StandType.getAllEnabledStands();
				stands
				.map(StandInstance::new)
				.sorted(discsOrder(parameters.holders()))
				.map(StandDiscItem::withStand)
				.forEach(item -> output.accept(item, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS));
			}).build());

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void addToModCreativeTabLast(BuildCreativeModeTabContentsEvent event) {
		if (event.getTabKey() == MAIN_TAB.getKey()) {
			// items related to clothes
			event.accept(SEWING_MACHINE.get());
			event.accept(SEWING_NEEDLE.get());
			event.accept(MANNEQUIN.get());
			event.accept(MANNEQUIN_SLIM.get());

			// the clothes items themselves
			ClothesItem clothesFactory = CLOTHES_BASE_ITEM.get();

			event.getParameters().holders()
			.lookup(JojoRegistries.CLOTHES_SETS_REG_KEY)
			.ifPresent(
					clothesSets -> clothesSets.listElements()
					.flatMap(setHolder -> {
						List<ClothesDataComponent> components = new ArrayList<>(ClothesSlotType.values().length);
						for (ClothesSlotType slot : ClothesSlotType.values()) {
							ClothesDataComponent component = ClothesItem.makeItemComponent(setHolder, slot);
							if (component != null) {
								components.add(new ClothesDataComponent(setHolder, slot, ClothesPiece.SubClothingPiece.FULL));
							}
						}
						return components.stream();
					})
					.map(clothesFactory::makeClothesPieceStack)
					.forEach(item -> event.accept(item, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS)));
		}
	}

}
