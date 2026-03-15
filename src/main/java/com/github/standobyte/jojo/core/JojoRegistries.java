package com.github.standobyte.jojo.core;

import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;

import com.github.standobyte.jojo.entityattachment.custom_effect.EntityCustomEffectType;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSet;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.StoryCharacter;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.entityaction.type.SpecialEntityActionType;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPowerType;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.github.standobyte.jojo.subsystems.StoryPart;
import com.github.standobyte.jojo.util.reflection.CommonReflection;
import com.github.standobyte.jojoimpl.powers.hamon.HamonSkill;
import com.github.standobyte.jojoimpl.powers.hamon.HamonTechnique;

import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ModifyRegistriesEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public final class JojoRegistries {
	public static final ResourceKey<Registry<AbilityType<?>>> ABILITY_TYPES_REG_KEY = ResourceKey.createRegistryKey(
			JojoMod.resLoc("ability_type"));
	
	public static final Registry<AbilityType<?>> ABILITY_TYPES_REG = new RegistryBuilder<>(ABILITY_TYPES_REG_KEY)
			.create();
	
	
	public static final ResourceKey<Registry<PlayerPowerType<?>>> PLAYER_POWER_TYPES_REG_KEY = ResourceKey.createRegistryKey(
			JojoMod.resLoc("player_power_type"));
	
	public static final Registry<PlayerPowerType<?>> PLAYER_POWER_TYPES_REG = new RegistryBuilder<>(PLAYER_POWER_TYPES_REG_KEY)
			.sync(true)
			.create();
	
	
	public static final ResourceKey<Registry<HamonSkill>> HAMON_SKILLS_REG_KEY = ResourceKey.createRegistryKey(
			JojoMod.resLoc("hamon_skill"));
	
	public static final Registry<HamonSkill> HAMON_SKILLS_REG = new RegistryBuilder<>(HAMON_SKILLS_REG_KEY)
			.sync(true)
			.create();
	
	
	public static final ResourceKey<Registry<HamonTechnique>> HAMON_TECHNIQUES_REG_KEY = ResourceKey.createRegistryKey(
			JojoMod.resLoc("hamon_technique"));
	
	public static final Registry<HamonTechnique> HAMON_TECHNIQUES_REG = new RegistryBuilder<>(HAMON_TECHNIQUES_REG_KEY)
			.sync(true)
			.create();
	
	public static final ResourceKey<Registry<EntityCustomEffectType<?>>> ENTITY_CUSTOM_EFFECTS_REG_KEY = ResourceKey.createRegistryKey(
			JojoMod.resLoc("custom_effects"));
	
	public static final Registry<EntityCustomEffectType<?>> ENTITY_CUSTOM_EFFECTS_REG = new RegistryBuilder<>(ENTITY_CUSTOM_EFFECTS_REG_KEY)
			.sync(true)
			.create();
	public static final Registry<EntityCustomEffectType<?>> STAND_EFFECTS_REG = ENTITY_CUSTOM_EFFECTS_REG;
	
	
	public static final ResourceKey<Registry<StandType>> DEFAULT_STANDS_REG_KEY = ResourceKey.createRegistryKey(
			JojoMod.resLoc("stand_type"));
	
	public static final Registry<StandType> DEFAULT_STANDS_REG = new RegistryBuilder<>(DEFAULT_STANDS_REG_KEY)
			.sync(true)
			.create();
	
	
	public static final ResourceKey<Registry<SpecialEntityActionType>> NON_POWER_ACTIONS = ResourceKey.createRegistryKey(
			JojoMod.resLoc("special_action"));
	
	public static final Registry<SpecialEntityActionType> NON_POWER_ACTIONS_REG = new RegistryBuilder<>(NON_POWER_ACTIONS)
			.sync(true)
			.create();
	
	
	public static final ResourceKey<Registry<StoryCharacter>> STORY_CHARACTERS_REG_KEY = ResourceKey.createRegistryKey(
			ResourceLocation.fromNamespaceAndPath("ripples_clothes", "character"));
	
	
	public static final ResourceKey<Registry<ClothesSet>> CLOTHES_SETS_REG_KEY = ResourceKey.createRegistryKey(
			ResourceLocation.fromNamespaceAndPath("ripples_clothes", "clothes"));
	
	
	public static final ResourceKey<Registry<StoryPart>> STORY_PARTS_REG_KEY = ResourceKey.createRegistryKey(
			ResourceLocation.fromNamespaceAndPath("ripples_clothes", "story_part"));
	
	
	
	public static final DeferredRegister<AbilityType<?>> ABILITY_TYPES = DeferredRegister.create(JojoRegistries.ABILITY_TYPES_REG, JojoMod.MOD_ID);
	
	@SubscribeEvent
	public static void registerRegistries(NewRegistryEvent event) {
		event.register(ABILITY_TYPES_REG);
		event.register(PLAYER_POWER_TYPES_REG);
		event.register(HAMON_SKILLS_REG);
		event.register(HAMON_TECHNIQUES_REG);
		event.register(STAND_EFFECTS_REG);
		event.register(DEFAULT_STANDS_REG);
		event.register(NON_POWER_ACTIONS_REG);
	}
	
	
	static Consumer<RegistryBuilder<StoryCharacter>> storyCharactersCommon = builder -> {
		builder.onAdd((Registry<StoryCharacter> registry, int id, ResourceKey<StoryCharacter> key, StoryCharacter value) -> value.initName(key));
	};
	
	static Consumer<RegistryBuilder<ClothesSet>> clothesSetsCommon = builder -> {
		builder.onAdd((Registry<ClothesSet> registry, int id, ResourceKey<ClothesSet> key, ClothesSet value) -> value.initName(key));
		builder.onBake((Registry<ClothesSet> registry) -> ClothesSet.onBake(registry));
	};
	
	static Consumer<RegistryBuilder<StoryPart>> storyPartsCommon = builder -> {
		builder.onAdd((Registry<StoryPart> registry, int id, ResourceKey<StoryPart> key, StoryPart value) -> value.initNameAndIcon(key));
	};
	
	@SubscribeEvent
	public static void registerDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
		event.dataPackRegistry(STORY_CHARACTERS_REG_KEY,
				StoryCharacter.DIRECT_CODEC,
				StoryCharacter.DIRECT_CODEC,
				storyCharactersCommon
				.andThen(builder -> {
					builder.sync(true);
				})
		);
		
		event.dataPackRegistry(CLOTHES_SETS_REG_KEY,
				ClothesSet.DIRECT_CODEC,
				ClothesSet.DIRECT_CODEC,
				clothesSetsCommon
				.andThen(builder -> {
					builder.sync(true);
				})
		);
		
		event.dataPackRegistry(STORY_PARTS_REG_KEY,
				StoryPart.DIRECT_CODEC,
				StoryPart.DIRECT_CODEC,
				storyPartsCommon
		);
	}
	
	@SubscribeEvent
	public static void makeTheCallbacksForDataPackRegistriesAlsoWorkOnClientSideBecauseFuckMeIGuess(ModifyRegistriesEvent event) {
		List<RegistryDataLoader.RegistryData<?>> NETWORKABLE_REGISTRIES = CommonReflection.getDataPackNetworkableRegistries();
		ListIterator<RegistryDataLoader.RegistryData<?>> iter = NETWORKABLE_REGISTRIES.listIterator();
		while (iter.hasNext()) {
			RegistryDataLoader.RegistryData<?> registryData = iter.next();
			ResourceKey<? extends Registry<?>> registryKey = registryData.key();
			if (registryKey.equals(STORY_CHARACTERS_REG_KEY)) {
				iter.set(JojoRegistries.<StoryCharacter>withCallbacks(registryData, storyCharactersCommon));
			}
			else if (registryKey.equals(CLOTHES_SETS_REG_KEY)) {
				iter.set(JojoRegistries.<ClothesSet>withCallbacks(registryData, clothesSetsCommon));
			}
			else if (registryKey.equals(STORY_PARTS_REG_KEY)) {
				iter.set(JojoRegistries.<StoryPart>withCallbacks(registryData, storyPartsCommon));
			}
		}
	}
	
	public static <T> RegistryDataLoader.RegistryData<T> withCallbacks(RegistryDataLoader.RegistryData<?> dataWithoutCallbacksForWhoTheFuckKnowsWhatReason, Consumer<RegistryBuilder<T>> callbacks) {
		@SuppressWarnings("unchecked")
		RegistryDataLoader.RegistryData<T> cast = (RegistryDataLoader.RegistryData<T>) dataWithoutCallbacksForWhoTheFuckKnowsWhatReason;
		return new RegistryDataLoader.RegistryData<T>(
				cast.key(), 
				cast.elementCodec(), 
				cast.requiredNonEmpty(), 
				cast.registryBuilderConsumer() /* that's technically empty enyway, but whatever*/ 
					.andThen(callbacks));
	}
	
}
