package com.github.standobyte.jojo.client.standskin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityanim.AnimationSet;
import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityModel;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityRenderState;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityRenderer;
import com.github.standobyte.jojo.client.standskin.sound.CustomPathSound;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.client.utils.ResourcePathChecker;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.mechanics.StoryPart;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class StandSkin {
	public final ResourceLocation skinId;
	public final ResourceLocation standTypeId;
	protected StandSkin defaultSkin;
	public final boolean isDefault;
	public final Optional<ResourceLocation> nonDefaultId;
	protected final ResourcePathChecker standTexture;
	protected final OptionalInt color;
	protected final Optional<ResourceLocation> storyPart;
	
	protected Map<ResourceLocation, LayerDefinition> models = new HashMap<>();
	protected LayerDefinition standModel;
	protected Map<ResourceLocation, Optional<Model>> createdModelsCache = new HashMap<>();
	protected Optional<StandEntityModel<?, ?>> createdStandModelCache;
	
	protected Map<ResourceLocation, AnimationSet> animations = new HashMap<>();
	protected AnimationSet standEntityAnims;
	
	protected Map<ResourceLocation, WeighedSoundEvents> soundEvents = new HashMap<>();
	protected Map<ResourceLocation, ResourceLocation> existingSounds = new HashMap<>();
	protected Map<ResourceLocation, Sound> remappedSound = new HashMap<>();
	
	protected Optional<GuiIcon> standIcon;
	protected final Map<ResourceLocation, ResourcePathChecker> remapPathCache = new HashMap<>();
	
	public StandSkin(ResourceLocation skinId, ResourceLocation standId, OptionalInt color, Optional<ResourceLocation> storyPart) {
		this.skinId = skinId;
		this.standTypeId = standId;
		this.standTexture = remapAssetPath(ResourceLocation.fromNamespaceAndPath(
				standId.getNamespace(), 
				"textures/entity/" + standId.getPath() + ".png"));
		this.isDefault = skinId.equals(standId);
		this.nonDefaultId = isDefault ? Optional.empty() : Optional.of(skinId);
		this.color = color;
		this.storyPart = storyPart;
	}
	
	protected void withModels(Map<ResourceLocation, LayerDefinition> models) {
		Objects.requireNonNull(models);
		this.models = models;
		this.standModel = models.get(standTypeId);
	}
	
	protected void withAnimations(Map<ResourceLocation, AnimationSet.Builder> animations) {
		Objects.requireNonNull(animations);
		this.animations = new HashMap<>();
		this.standEntityAnims = null;
		animations.forEach((key, animsBuilder) -> {
			if (!animsBuilder.isEmpty()) {
				AnimationSet anims = animsBuilder.build();
				this.animations.put(key, anims);
				if (key.equals(standTypeId)) {
					this.standEntityAnims = anims;
				}
			}
		});
	}
	
	protected void withSoundEvents(Map<ResourceLocation, WeighedSoundEvents> soundEvents) {
		this.soundEvents = soundEvents;
	}
	
	protected void withSounds(Map<ResourceLocation, ResourceLocation> sounds) {
		this.existingSounds = sounds;
	}

	
	public int getColor() {
		if (this.color.isPresent()) {
			return this.color.getAsInt();
		}
		if (this != defaultSkin && defaultSkin != null && defaultSkin.color.isPresent()) {
			return defaultSkin.color.getAsInt();
		}
		return 0xffffff;
	}
	
	@Nullable
	public Holder<StoryPart> getStoryPart(HolderLookup.Provider registries) {
		ResourceLocation storyPartId = null;
		if (this.storyPart.isPresent()) {
			storyPartId = this.storyPart.get();
		}
		if (this != defaultSkin && defaultSkin != null && defaultSkin.storyPart.isPresent()) {
			storyPartId = defaultSkin.storyPart.get();
		}
		
		if (storyPartId != null) {
			ResourceLocation id = storyPartId;
			Holder<StoryPart> storyPart = registries.lookup(JojoRegistries.STORY_PARTS_REG_KEY)
					.flatMap(registry -> registry.get(ResourceKey.create(JojoRegistries.STORY_PARTS_REG_KEY, id)))
					.filter(Holder.Reference::isBound)
					.orElse(null);
			return storyPart;
		}
		return null;
	}
	
	public ResourceLocation getTexture(ResourceLocation path) {
		return getTexture(path, path);
	}
	
	public ResourceLocation getTexture(ResourceLocation path, ResourceLocation defaultTex) {
		ResourcePathChecker remapped = remapAssetPath(path);
		if (this != defaultSkin && defaultSkin != null) {
			return remapped.or(() -> defaultSkin.getTexture(path, defaultTex));
		}
		else {
			return remapped.or(defaultTex);
		}
	}
	
	public ResourceLocation getStandTexture(ResourceLocation defaultTex) {
		if (this != defaultSkin && defaultSkin != null) {
			return this.standTexture.or(() -> defaultSkin.getStandTexture(defaultTex));
		}
		else {
			return this.standTexture.or(defaultTex);
		}
	}
	
	@SuppressWarnings("unchecked")
	public <M extends Model> M getModel(ResourceLocation modelPath, Function<LayerDefinition, M> newModelFactory) {
		Optional<Model> cached = createdModelsCache.get(modelPath);
		if (cached != null) {
			return (M) cached.orElse(null);
		}
		LayerDefinition modelDefinition = models.get(modelPath);
		if (modelDefinition != null) {
			cached = Optional.ofNullable(newModelFactory.apply(modelDefinition));
			createdModelsCache.put(modelPath, cached);
			return (M) cached.orElse(null);
		}
		
		if (this != defaultSkin) {
			return defaultSkin.getModel(modelPath, newModelFactory);
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public 
		<T extends StandEntity, 
		S extends StandEntityRenderState, 
		M extends StandEntityModel<T, S>> 
	M getStandModel(StandEntityRenderer<T, S, M> newModelFactory) {
		if (this.createdStandModelCache != null) {
			return (M) createdStandModelCache.orElse(null);
		}
		if (this.standModel != null) {
			LayerDefinition standModel = this.standModel;
			M model = newModelFactory.createStandModel(standModel);
			this.createdStandModelCache = Optional.ofNullable(model);
			return model;
		}
		
		if (this != defaultSkin) {
			return defaultSkin.getStandModel(newModelFactory);
		}
		
		return null;
	}
	
	public LayerDefinition getModelDef(ResourceLocation modelId) {
		LayerDefinition model = models.get(modelId);
		if (model != null || this == defaultSkin) {
			return model;
		}
		
		if (defaultSkin != null) {
			return defaultSkin.getModelDef(modelId);
		}
		return null;
	}
	
	public LayerDefinition getStandModelDef() {
		if (standModel != null) {
			return standModel;
		}

		if (defaultSkin != null) {
			return defaultSkin.standModel;
		}
		return null;
	}
	
	public RotpAnimDefinition getAnimation(ResourceLocation modelId, Function<AnimationSet, RotpAnimDefinition> getAnim) {
		AnimationSet anims = this.animations.get(modelId);
		if (anims != null || this == defaultSkin) {
			RotpAnimDefinition anim = getAnim.apply(anims);
			if (anim != null) {
				return anim;
			}
		}
		
		if (defaultSkin != null) {
			return defaultSkin.getAnimation(modelId, getAnim);
		}
		return null;
	}
	
	public RotpAnimDefinition getStandAnimation(Function<AnimationSet, RotpAnimDefinition> getAnim) {
		if (this.standEntityAnims != null) {
			RotpAnimDefinition anim = getAnim.apply(this.standEntityAnims);
			if (anim != null) {
				return anim;
			}
		}
		
		if (defaultSkin != null && defaultSkin.standEntityAnims != null) {
			return getAnim.apply(defaultSkin.standEntityAnims);
		}
		return null;
	}
	
	public GuiIcon getStandIcon() {
		if (standIcon == null) {
			ResourcePathChecker iconPath = remapAssetPath(standTypeId.withPath("textures/stand_icon.png"));
			boolean createIcon = this == defaultSkin || iconPath.resourceExists();
			this.standIcon = createIcon ? Optional.of(new GuiIcon(iconPath.path, 16, 16)) : null;
		}
		if (standIcon != null) {
			return standIcon.get();
		}
		if (defaultSkin != null && this != defaultSkin) {
			return defaultSkin.getStandIcon();
		}
		throw new IllegalStateException();
	}
	
//	public WeighedSoundEvents getSoundEvent(SoundEvent soundEvent) {
//		return getSoundEvent(soundEvent.location());
//	}
	
	public WeighedSoundEvents getSoundEvent(ResourceLocation soundEventLocation) {
		WeighedSoundEvents sound = this.soundEvents.get(soundEventLocation);
		if (sound != null) {
			return sound;
		}
		
		if (this != defaultSkin && defaultSkin != null) {
			return defaultSkin.getSoundEvent(soundEventLocation);
		}
		return null;
	}
	
	public Sound overrideSound(Sound sound) {
		ResourceLocation key = sound.getLocation();
		Sound cached = remappedSound.get(key);
		if (cached != null) {
			return cached;
		}
		ResourceLocation path = existingSounds.get(key);
		if (path != null) {
			return remappedSound.compute(key, (__, ___) -> new CustomPathSound(sound, path));
		}
		
		if (this != defaultSkin) {
			return defaultSkin.overrideSound(sound);
		}
		return null;
	}
	
	
	public ResourcePathChecker remapAssetPath(ResourceLocation path) {
		return remapPathCache.computeIfAbsent(path, asset -> ResourcePathChecker.getOrCreate(StandSkinsLoader.remap(asset, skinId)));
	}
	
	
	public MutableComponent translatable(String key) {
		return Component.translatable(key);
	}

	public MutableComponent translatable(String key, Object... args) {
		return Component.translatable(key, args);
	}
	
}
