package com.github.standobyte.jojo.client.standskin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ResourcePathChecker;
import com.github.standobyte.jojo.client.entityanim.AnimVariantsList;
import com.github.standobyte.jojo.client.entityanim.AnimationSet;
import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition;
import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose;
import com.github.standobyte.jojo.client.entityrender.BabyModelVariant;
import com.github.standobyte.jojo.client.entityrender.LoadedModel;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityModel;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityRenderState;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityRenderer;
import com.github.standobyte.jojo.client.sound.bgmloop.BgmTrackInfo;
import com.github.standobyte.jojo.client.standskin.sound.CustomPathSound;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.subsystems.StoryPart;
import com.github.standobyte.jojo.util.objects_mc.WeightsList;

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
	@Nullable protected final StandSkinColor color;
	protected final Optional<ResourceLocation> storyPart;
	
	protected Map<ResourceLocation, LayerDefinition> models = new HashMap<>();
	protected LayerDefinition standModel;
	protected Map<ResourceLocation, Optional<LoadedModel>> createdModelsCache = new HashMap<>();
	protected BabyModelVariant<StandEntityModel<?, ?>> createdStandModelCache;
	
	protected Map<ResourceLocation, AnimationSet> animations = new HashMap<>();
	protected AnimationSet standEntityAnims;
	
	protected float[] renderScale;
	
	protected Map<ResourceLocation, WeighedSoundEvents> soundEvents = new HashMap<>();
	protected Map<ResourceLocation, ResourceLocation> existingSounds = new HashMap<>();
	protected Map<ResourceLocation, Sound> remappedSound = new HashMap<>();
	
	protected WeightsList<BgmTrackInfo> resolveBGM;
	
	protected Optional<GuiIcon> standIcon;
	protected final Map<ResourceLocation, ResourcePathChecker> remapPathCache = new HashMap<>();
	
	public StandSkin(ResourceLocation skinId, ResourceLocation standId, @Nullable StandSkinColor color, Optional<ResourceLocation> storyPart) {
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
	
	protected void withScale(float width, float height) {
		this.renderScale = new float[] {
				ClientUtil.PLAYER_RENDER_SCALE * width / ClientUtil.DEFAULT_STAND_WIDTH,
				ClientUtil.PLAYER_RENDER_SCALE * height / ClientUtil.DEFAULT_STAND_HEIGHT
		};
	}
	
	protected void withSoundEvents(Map<ResourceLocation, WeighedSoundEvents> soundEvents) {
		this.soundEvents = soundEvents;
	}
	
	protected void withSounds(Map<ResourceLocation, ResourceLocation> sounds) {
		this.existingSounds = sounds;
	}
	
	protected void withResolveBGM(WeightsList<BgmTrackInfo> tracks) {
		this.resolveBGM = tracks;
	}

	
	@Deprecated
	public int getColor() {
		return getColors().primary();
	}
	
	public StandSkinColor getColors() {
		if (this.color != null) {
			return this.color;
		}
		if (this != defaultSkin && defaultSkin != null && defaultSkin.color != null) {
			return defaultSkin.color;
		}
		return StandSkinColor.FALLBACK;
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
	
	public <M extends Model> M getModel(ResourceLocation modelPath, Function<LayerDefinition, M> newModelFactory) {
		LoadedModel model = getModel(modelPath);
		return model != null ? model.getMainModel(newModelFactory) : null;
	}
	
	public LoadedModel getModel(ResourceLocation modelPath) {
		Optional<LoadedModel> cached = createdModelsCache.get(modelPath);
		if (cached == null) {
			LayerDefinition modelDefinition = models.get(modelPath);
			LoadedModel modelCast = modelDefinition != null ? new LoadedModel(modelDefinition) : null;
			cached = Optional.ofNullable(modelCast);
			createdModelsCache.put(modelPath, cached);
		}
		
		if (cached.isPresent()) {
			return cached.get();
		}
		
		if (this != defaultSkin && defaultSkin != null) {
			return defaultSkin.getModel(modelPath);
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public 
		<T extends StandEntity, 
		S extends StandEntityRenderState, 
		M extends StandEntityModel<T, S>> 
	M getStandModel(StandEntityRenderer<T, S, M> newModelFactory, boolean baby) {
		if (this.createdStandModelCache != null) {
			return (M) createdStandModelCache.get(baby);
		}
		if (this.standModel != null) {
			LayerDefinition standModel = this.standModel;
			this.createdStandModelCache = new BabyModelVariant<>(standModel, newModelFactory::createStandModel);
			return (M) createdStandModelCache.get(baby);
		}
		
		if (this != defaultSkin && defaultSkin != null) {
			return defaultSkin.getStandModel(newModelFactory, baby);
		}
		
		return null;
	}
	
	public LayerDefinition getModelDef(ResourceLocation modelId) {
		LayerDefinition model = models.get(modelId);
		if (model != null || this == defaultSkin) {
			return model;
		}
		
		if (defaultSkin != null && defaultSkin != null) {
			return defaultSkin.getModelDef(modelId);
		}
		return null;
	}
	
	public LayerDefinition getStandModelDef() {
		if (standModel != null) {
			return standModel;
		}

		if (defaultSkin != null && defaultSkin != null) {
			return defaultSkin.standModel;
		}
		return null;
	}
	
	public AnimVariantsList getStandAnimations(String name) {
		return getAnimations(skin -> skin.standEntityAnims, name);
	}
	
	public RotpAnimDefinition getStandAnimation(ActionAnimIdentifier animId) {
		return getAnimation(skin -> skin.standEntityAnims, animId);
	}
	
	public AnimVariantsList getAnimations(ResourceLocation modelId, String name) {
		return getAnimations(skin -> skin.animations != null ? skin.animations.get(modelId) : null, name);
	}
	
	public RotpAnimDefinition getAnimation(ResourceLocation modelId, ActionAnimIdentifier animId) {
		return getAnimation(skin -> skin.animations != null ? skin.animations.get(modelId) : null, animId);
		
	}
	
	protected AnimVariantsList getAnimations(Function<StandSkin, AnimationSet> getAnimSet, String name) {
		AnimationSet animSet = getAnimSet.apply(this);
		if (animSet != null) {
			AnimVariantsList anims = animSet.getAnimVariants(name);
			if (anims != null) {
				return anims;
			}
		}
		
		if (this != defaultSkin && defaultSkin != null) {
			return defaultSkin.getAnimations(getAnimSet, name);
		}
		return null;
	}
	
	protected RotpAnimDefinition getAnimation(Function<StandSkin, AnimationSet> getAnimSet, ActionAnimIdentifier animId) {
		AnimationSet animSet = getAnimSet.apply(this);
		if (animSet != null) {
			RotpAnimDefinition anims = animSet.getNamedAnim(animId);
			if (anims != null) {
				return anims;
			}
		}
		
		if (this != defaultSkin && defaultSkin != null) {
			return defaultSkin.getAnimation(getAnimSet, animId);
		}
		return null;
	}
	
	@Deprecated(forRemoval = true)
	public RotpAnimDefinition getAnimation(ResourceLocation modelId, Function<AnimationSet, RotpAnimDefinition> getAnim) {
		AnimationSet animSet = animations != null ? animations.get(modelId) : null;
		if (animSet != null) {
			RotpAnimDefinition anims = getAnim.apply(animSet);
			if (anims != null) {
				return anims;
			}
		}
		
		if (this != defaultSkin && defaultSkin != null) {
			return defaultSkin.getAnimation(modelId, getAnim);
		}
		return null;
	}

	@Deprecated(forRemoval = true)
	public RotpAnimDefinition getStandAnimation(Function<AnimationSet, RotpAnimDefinition> getAnim) {
		AnimationSet animSet = standEntityAnims;
		if (animSet != null) {
			RotpAnimDefinition anim = getAnim.apply(animSet);
			if (anim != null) {
				return anim;
			}
		}
		
		if (this != defaultSkin && defaultSkin != null) {
			return defaultSkin.getStandAnimation(getAnim);
		}
		return null;
	}
	
	@Deprecated
	public AnimationSet getAnimations() {
		return standEntityAnims;
	}
	
	protected List<AnimFramePose> _posesCache;
	public List<AnimFramePose> getAllPoses() {
		_posesCache = null;
		if (_posesCache == null) {
			AnimationSet animSet;
			Map<String, AnimVariantsList> allAnimsVariants = new HashMap<>();
			if (this != defaultSkin && defaultSkin != null) {
				animSet = defaultSkin.standEntityAnims;
				if (animSet != null) {
					for (var entry : animSet.namedAnimations.entrySet()) {
						allAnimsVariants.put(entry.getKey(), entry.getValue());
					}
				}
			}

			animSet = this.standEntityAnims;
			if (animSet != null) {
				for (var entry : animSet.namedAnimations.entrySet()) {
					allAnimsVariants.put(entry.getKey(), entry.getValue());
				}
			}
			
			_posesCache = new ArrayList<>();
			for (AnimVariantsList animVariants : allAnimsVariants.values()) {
				if (animVariants.poses != null && !animVariants.poses.isEmpty()) {
					_posesCache.addAll(animVariants.poses.values());
				}
			}
		}
		return _posesCache;
	}
	
	protected static final float[] DEFAULT_SCALE = new float[] { 1, 1 };
	public float[] getModelScale() {
		if (renderScale != null) {
			return renderScale;
		}
		if (this != defaultSkin && defaultSkin.renderScale != null) {
			return defaultSkin.renderScale;
		}
		return DEFAULT_SCALE;
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
		
		if (this != defaultSkin && defaultSkin != null) {
			return defaultSkin.overrideSound(sound);
		}
		return null;
	}
	
	@Nullable
	public WeightsList<BgmTrackInfo> getResolveBGM() {
		if (this.resolveBGM != null) {
			return this.resolveBGM;
		}
		if (this != defaultSkin && defaultSkin != null && defaultSkin.resolveBGM != null) {
			return defaultSkin.resolveBGM;
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
