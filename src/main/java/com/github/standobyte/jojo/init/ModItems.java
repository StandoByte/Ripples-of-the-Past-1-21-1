package com.github.standobyte.jojo.init;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Function;

import com.github.standobyte.jojo.DebugItem;
import com.github.standobyte.jojo.adventure.npc.debug.CharacterTestItem;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.clothes.ClothesItem;
import com.github.standobyte.jojo.mechanics.clothes.mannequin.MannequinItem;
import com.github.standobyte.jojo.mechanics.standarrow.StandArrowItem;
import com.github.standobyte.jojo.mechanics.standarrow.StandArrowShardItem;
import com.github.standobyte.jojo.mechanics.standdisc.StandDiscItem;
import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.google.common.collect.ImmutableMap;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// TODO datagen for crafting recipes, advancements and loot tables
public final class ModItems {
	public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, JojoMod.MOD_ID);
	public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(JojoMod.MOD_ID);

	public static final DeferredItem<Item> DEBUG_ITEM = ITEMS.registerItem("debug_item", DebugItem::new, new Item.Properties());
	public static final DeferredItem<Item> CHARACTER_TEST = ITEMS.registerItem("character_test", CharacterTestItem::new, new Item.Properties());

	public static final DeferredItem<Item> STAND_DISC = ITEMS.registerItem("stand_disc", StandDiscItem::new, new Item.Properties().stacksTo(1));

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

	public static final DeferredItem<Item> STONE_MASK = ITEMS.registerItem("stone_mask", 
			props -> new BlockItem(ModBlocks.STONE_MASK.get(), props), new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> STONE_MASK_PROTOTYPE = ITEMS.registerItem("stone_mask_prototype", 
			props -> new BlockItem(ModBlocks.STONE_MASK_PROTOTYPE.get(), props), new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> STONE_MASK_AJA_SOCKET = ITEMS.registerItem("stone_mask_aja_socket", 
			props -> new BlockItem(ModBlocks.STONE_MASK_AJA_SOCKET.get(), props), new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> STONE_MASK_AJA = ITEMS.registerItem("stone_mask_aja", 
			props -> new BlockItem(ModBlocks.STONE_MASK_AJA.get(), props), new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> STONE_MASK_SUPER_AJA = ITEMS.registerItem("stone_mask_super_aja", 
			props -> new BlockItem(ModBlocks.STONE_MASK_SUPER_AJA.get(), props), new Item.Properties().stacksTo(1));
	public static final Map<DyeColor, DeferredItem<Item>> WOODEN_COFFIN = register16colorsItem("wooden_coffin", 
			dye -> {
				Item.Properties builder = new Item.Properties().stacksTo(1);
				return new BlockItem(ModBlocks.WOODEN_COFFIN.get(dye).get(), builder);
			});
	public static final DeferredItem<Item> AJA_STONE = ITEMS.registerItem("aja_stone", Item::new, new Item.Properties().stacksTo(16));
	public static final DeferredItem<Item> SUPER_AJA_STONE = ITEMS.registerItem("super_aja_stone", Item::new, new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> KNIFE = ITEMS.registerItem("knife", Item::new, new Item.Properties().stacksTo(16));
	public static final DeferredItem<Item> LUCK_SWORD = ITEMS.registerItem("luck_sword", Item::new, new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> LUCK_PLUCK_SWORD = ITEMS.registerItem("luck_pluck_sword", Item::new, new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> GLOVES = ITEMS.registerItem("gloves", Item::new, new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> OIL = ITEMS.registerItem("oil", Item::new, new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> FIREBOMB = ITEMS.registerItem("firebomb", Item::new, new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> CLACKERS = ITEMS.registerItem("clackers", Item::new, new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> SOAP = ITEMS.registerItem("soap", Item::new, new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> GLOVES_SOAP = ITEMS.registerItem("gloves_soap", Item::new, new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> TOMMY_GUN = ITEMS.registerItem("tommy_gun", Item::new, new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> METAL_BALL_CROSSBOW = ITEMS.registerItem("metal_ball_crossbow", Item::new, new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> METAL_BALL = ITEMS.registerItem("metal_ball", Item::new, new Item.Properties().stacksTo(16));
	public static final DeferredItem<Item> WEDDING_RING_OF_DEATH = ITEMS.registerItem("wedding_ring_of_death", Item::new, new Item.Properties());
	public static final DeferredItem<Item> WEARABLE_UV_LAMPS = ITEMS.registerItem("wearable_uv_lamps", Item::new, new Item.Properties().stacksTo(1));
	
	public static final DeferredItem<Item> ORIENTAL_POISON = ITEMS.registerItem("oriental_poison", Item::new, new Item.Properties());
	public static final DeferredItem<Item> SQUID_INK_PASTA = ITEMS.registerItem("squid_ink_pasta", Item::new, new Item.Properties().stacksTo(16));
	
	public static final DeferredItem<Item> POLAROID = ITEMS.registerItem("polaroid", 
			props -> new BlockItem(ModBlocks.POLAROID.get(), props), new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> PHOTO = ITEMS.registerItem("photo", Item::new, new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> PHOTO_ALBUM = ITEMS.registerItem("photo_album", Item::new, new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> PHOTO_FRAME = ITEMS.registerItem("photo_frame", 
			props -> new BlockItem(ModBlocks.PHOTO_FRAME.get(), props), new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> WALKMAN = ITEMS.registerItem("walkman", Item::new, new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> CASSETTE_BLANK = ITEMS.registerItem("cassette_blank", Item::new, new Item.Properties());
	public static final DeferredItem<Item> CASSETTE_RECORDED = ITEMS.registerItem("cassette_recorded", Item::new, new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> TAROT_CARD = ITEMS.registerItem("tarot_card", Item::new, new Item.Properties().stacksTo(78));
//	public static final DeferredItem<BlockItem> CRYSTAL_BALL = ITEMS.registerSimpleBlockItem("crystal_ball", ModBlocks.CRYSTAL_BALL, new Item.Properties());
	
	public static final DeferredItem<Item> CRYSTAL_BALL = ITEMS.registerItem("crystal_ball", props -> new BlockItem(ModBlocks.CRYSTAL_BALL.get(), props), new Item.Properties());
	
	public static final DeferredItem<Item> PLAYING_CARD = ITEMS.registerItem("playing_card", Item::new, new Item.Properties().stacksTo(52));
	public static final DeferredItem<Item> POKER_CHIP = ITEMS.registerItem("poker_chip", Item::new, new Item.Properties());
	public static final DeferredItem<Item> ROAD_ROLLER = ITEMS.registerItem("road_roller", Item::new, new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> OIL_TANKER = ITEMS.registerItem("oil_tanker", Item::new, new Item.Properties().stacksTo(1));
	
	public static final DeferredItem<Item> NAIL_CLIPPERS = ITEMS.registerItem("nail_clippers", Item::new, new Item.Properties().stacksTo(1));
	
	public static final DeferredItem<Item> LADYBUG_BROOCH = ITEMS.registerItem("ladybug_brooch", Item::new, new Item.Properties());
	public static final DeferredItem<Item> MISTA_REVOLVER = ITEMS.registerItem("revolver", Item::new, new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> LIGHTER = ITEMS.registerItem("lighter", 
			props -> new BlockItem(ModBlocks.LIGHTER.get(), props), new Item.Properties().stacksTo(1));
	
	public static final DeferredItem<Item> STONE_PENDANT = ITEMS.registerItem("stone_pendant", Item::new, new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> EXPLOSIVE_BRACELET = ITEMS.registerItem("explosive_bracelet", Item::new, new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> HARPOON = ITEMS.registerItem("harpoon", Item::new, new Item.Properties().stacksTo(1));
	
	public static final DeferredItem<Item> DODODODEDADADA = ITEMS.registerItem("dodododedadada", Item::new, new Item.Properties().stacksTo(1));
	public static final DeferredItem<Item> OBLADI_OBLADA = ITEMS.registerItem("obladi_oblada", Item::new, new Item.Properties().stacksTo(1));
	
	public static final DeferredItem<Item> GUCCI_BAG = ITEMS.registerItem("gucci_bag", Item::new, new Item.Properties().stacksTo(1));


	public static final DeferredItem<BlockItem> SEWING_MACHINE = ITEMS.registerSimpleBlockItem("sewing_machine", 
			ModBlocks.SEWING_MACHINE, new Item.Properties());

	public static final DeferredItem<Item> SEWING_NEEDLE = ITEMS.registerSimpleItem("sewing_needle");

	public static final DeferredItem<Item> MANNEQUIN = ITEMS.registerItem("mannequin", props -> new MannequinItem(props, false), new Item.Properties().stacksTo(16));

	public static final DeferredItem<Item> MANNEQUIN_SLIM = ITEMS.registerItem("mannequin_slim", props -> new MannequinItem(props, true), new Item.Properties().stacksTo(16));

	public static final DeferredItem<ClothesItem> CLOTHES_BASE_ITEM = ITEMS.registerItem("clothes", props -> new ClothesItem(props));

	@Deprecated
	public static Comparator<StandInstance> discsOrder(HolderLookup.Provider registries) {
		return ModItemsCreativeTab.discsOrder(registries);
	}

	public static final DeferredHolder<CreativeModeTab, ModItemsCreativeTab> MAIN_TAB = CREATIVE_MODE_TABS.register(JojoMod.MOD_ID + "_main", () -> {
		ModItemsCreativeTab tab = (ModItemsCreativeTab) CreativeModeTab.builder()
		.title(Component.translatable("itemGroup." + JojoMod.MOD_ID + "_main"))
		.icon(() -> DEBUG_ITEM.value().getDefaultInstance())
		.displayItems((CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) -> {
			output.accept(STONE_MASK);
			output.accept(WOODEN_COFFIN.get(DyeColor.RED));
			output.accept(KNIFE);
			//output.accept(ORIENTAL_POISON);
			output.accept(GLOVES);
			//output.accept(LUCK_SWORD);
			//output.accept(LUCK_PLUCK_SWORD);
			//output.accept(OIL);
			//output.accept(FIREBOMB);
			//output.accept(CLACKERS);
			output.accept(SOAP);
			output.accept(GLOVES_SOAP);
			output.accept(TOMMY_GUN);
			output.accept(METAL_BALL_CROSSBOW);
			output.accept(METAL_BALL);
			output.accept(AJA_STONE);
			output.accept(SUPER_AJA_STONE);
			output.accept(STONE_MASK_PROTOTYPE);
			output.accept(STONE_MASK_AJA_SOCKET);
			output.accept(STONE_MASK_AJA);
			output.accept(STONE_MASK_SUPER_AJA);
			//output.accept(WEDDING_RING_OF_DEATH);
			//output.accept(WEARABLE_UV_LAMPS);
			output.accept(SQUID_INK_PASTA);
			
			output.accept(STAND_ARROW);
			output.accept(STAND_ARROW_BEETLE);
			output.accept(STAND_ARROW_METEORITE);
			output.accept(STAND_ARROW_SHARD);
			output.accept(METEORIC_IRON);
			output.accept(METEORITE_CORE);
			output.accept(METEORIC_SCRAP);
			output.accept(METEORIC_INGOT);
			output.accept(POLAROID);
			//output.accept(PHOTO_ALBUM);
			//output.accept(PHOTO_FRAME);
			output.accept(WALKMAN);
			output.accept(CASSETTE_BLANK);
			// TODO (walkman) add dyed cassettes to creative tab
			output.accept(CASSETTE_RECORDED);
			//output.accept(TAROT_CARD);
			//output.accept(CRYSTAL_BALL);
			//output.accept(PLAYING_CARD);
			//output.accept(POKER_CHIP);
			//output.accept(ROAD_ROLLER);
			//output.accept(OIL_TANKER);
			//output.accept(NAIL_CLIPPERS);
			output.accept(LADYBUG_BROOCH);
			//output.accept(MISTA_REVOLVER);
			//output.accept(LIGHTER);
			//output.accept(STONE_PENDANT);
			//output.accept(EXPLOSIVE_BRACELET);
			//output.accept(HARPOON);
			//output.accept(DODODODEDADADA);
			//output.accept(OBLADI_OBLADA);
			//output.accept(GUCCI_BAG);
			
			output.accept(SEWING_MACHINE);
			output.accept(SEWING_NEEDLE);
			output.accept(MANNEQUIN);
			output.accept(MANNEQUIN_SLIM);
		})
		.withTabFactory(ModItemsCreativeTab::new).build();
		
		Collections.addAll(tab.unfinishedItems, 
				STONE_MASK,
				WOODEN_COFFIN.get(DyeColor.RED),
				KNIFE,
				ORIENTAL_POISON,
				GLOVES,
				LUCK_SWORD,
				LUCK_PLUCK_SWORD,
				OIL,
				FIREBOMB,
				CLACKERS,
				SOAP,
				GLOVES_SOAP,
				TOMMY_GUN,
				METAL_BALL_CROSSBOW,
				METAL_BALL,
				AJA_STONE,
				SUPER_AJA_STONE,
				STONE_MASK_PROTOTYPE,
				STONE_MASK_AJA_SOCKET,
				STONE_MASK_AJA,
				STONE_MASK_SUPER_AJA,
				WEDDING_RING_OF_DEATH,
				WEARABLE_UV_LAMPS,
				SQUID_INK_PASTA,
				POLAROID,
				PHOTO_ALBUM,
				PHOTO_FRAME,
				WALKMAN,
				CASSETTE_BLANK,
				CASSETTE_RECORDED,
				TAROT_CARD,
				CRYSTAL_BALL,
				PLAYING_CARD,
				POKER_CHIP,
				ROAD_ROLLER,
				OIL_TANKER,
				NAIL_CLIPPERS,
				LADYBUG_BROOCH,
				MISTA_REVOLVER,
				LIGHTER,
				STONE_PENDANT,
				EXPLOSIVE_BRACELET,
				HARPOON,
				DODODODEDADADA,
				OBLADI_OBLADA,
				GUCCI_BAG
				);
		
		return tab;
	});


	public static <I extends Item> Map<DyeColor, DeferredItem<I>> register16colorsItem(
			String idMain, Function<DyeColor, I> supplier) {
		ImmutableMap.Builder<DyeColor, DeferredItem<I>> colorMap = ImmutableMap.builder();
		for (DyeColor dye : DyeColor.values()) {
			DeferredItem<I> registryObject = ITEMS.register(idMain + "_" + dye.getName().toLowerCase(), () -> supplier.apply(dye));
			colorMap.put(dye, registryObject);
		}
		return colorMap.build();
	}

	public static <I extends Block> Map<DyeColor, DeferredBlock<I>> register16colorsBlock(
			String idMain, Function<DyeColor, I> supplier) {
		ImmutableMap.Builder<DyeColor, DeferredBlock<I>> colorMap = ImmutableMap.builder();
		for (DyeColor dye : DyeColor.values()) {
			DeferredBlock<I> registryObject = ModBlocks.BLOCKS.register(idMain + "_" + dye.getName().toLowerCase(), () -> supplier.apply(dye));
			colorMap.put(dye, registryObject);
		}
		return colorMap.build();
	}

}
