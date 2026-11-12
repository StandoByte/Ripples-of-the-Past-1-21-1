package com.github.standobyte.jojo.client.standskin.sprites;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.ability.Ability;

import net.minecraft.Util;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceList;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

public class AbilityIconSprites implements AutoCloseable {
	public static final String DIR_NAME = "ability";
	
	protected final TextureAtlas textureAtlas;
	protected final Set<MetadataSectionSerializer<?>> metadataSections;

	public AbilityIconSprites(TextureManager textureManager) {
		this(textureManager, JojoMod.resLoc("abilities"), SpriteLoader.DEFAULT_METADATA_SECTIONS);
	}

	public AbilityIconSprites(TextureManager textureManager, ResourceLocation atlasId) {
		this(textureManager, atlasId, SpriteLoader.DEFAULT_METADATA_SECTIONS);
	}

	public AbilityIconSprites(TextureManager textureManager, ResourceLocation atlasId, Set<MetadataSectionSerializer<?>> metadataSections) {
		this.textureAtlas = new TextureAtlas(atlasId);
		textureManager.register(this.textureAtlas.location(), this.textureAtlas);
		this.metadataSections = metadataSections;
	}


	public TextureAtlasSprite getAbilityIconWithContext(Ability ability, Power<?> context, @Nullable StandSkin curStandSkin) {
		return getAbilityIcon(ability.getSpriteName(context), curStandSkin);
	}

	public TextureAtlasSprite getAbilityIcon(String abilitySpriteName, @Nullable StandSkin curStandSkin) {
		Map<ResourceLocation, TextureAtlasSprite> texturesByName = textureAtlas.texturesByName;
		ResourceLocation spritePath;

		if (curStandSkin != null) {
			spritePath = skinSpritePath(curStandSkin.skinId, abilitySpriteName);
			if (texturesByName.containsKey(spritePath)) return texturesByName.get(spritePath);
			
			if (!curStandSkin.isDefault) {
				spritePath = skinSpritePath(curStandSkin.standTypeId, abilitySpriteName);
				if (texturesByName.containsKey(spritePath)) return texturesByName.get(spritePath);
			}
		}

		spritePath = defaultSpritePath(abilitySpriteName);
		if (texturesByName.containsKey(spritePath)) return texturesByName.get(spritePath);
		
		return textureAtlas.missingSprite;
	}

	protected static ResourceLocation skinSpritePath(ResourceLocation skinId, String abilityName) {
		return memoize1.apply(skinId, abilityName);
	}
	protected static final BiFunction<ResourceLocation, String, ResourceLocation> memoize1 = Util.memoize(
			(ResourceLocation skinId, String abilityName) -> {
				return ResourceLocation.fromNamespaceAndPath(
						skinId.getNamespace(), 
						skinId.getPath() + "/" + abilityName);
			});

	protected static ResourceLocation defaultSpritePath(String abilityName) { 
		return memoize2.apply(abilityName);
	}
	protected static final Function<String, ResourceLocation> memoize2 = Util.memoize(
			(String abilityName) -> {
				return JojoMod.resLoc(DIR_NAME + "/" + abilityName);
			});


	@ApiStatus.Internal
	public static class Preps {
		protected AbilityIconSprites atlasHolder;
		protected SpriteLoader.Preparations prepsDone;
		protected Map<ResourceLocation, ResourceLocation> skinSprites = new HashMap<>();

		public Preps(AbilityIconSprites atlasHolder) {
			this.atlasHolder = atlasHolder;
		}

		public void addSkinSprite(ResourceLocation skinId, String spriteName, ResourceLocation filePath) {
			skinSprites.put(skinSpritePath(skinId, spriteName), filePath);
		}

		public CompletableFuture<SpriteLoader.Preparations> stitch(ResourceManager resourceManager, Executor executor) {
			SpriteLoader loader = SpriteLoader.create(atlasHolder.textureAtlas);
			SpriteResourceLoader resourceLoader = SpriteResourceLoader.create(atlasHolder.metadataSections);

			int mipLevel = 0;

			return CompletableFuture.supplyAsync(() -> {
				List<SpriteSource> sources = new ArrayList<>();
				// sprites from all the stand skins
				for (var spriteEntry : skinSprites.entrySet()) {
					ResourceLocation id = spriteEntry.getKey();
					ResourceLocation filePath = spriteEntry.getValue();
					sources.add(new SpriteSourceWithoutFileListerShit(id, filePath));
				}
				// and the default sprites that are not from the stand skins
				sources.add(new DirectoryLister(DIR_NAME, DIR_NAME + "/"));
				return new SpriteSourceList(sources).list(resourceManager);
			}).thenCompose((List<Function<SpriteResourceLoader, SpriteContents>> factories) -> {
				return SpriteLoader.runSpriteSuppliers(resourceLoader, factories, executor);
			}).thenApply((List<SpriteContents> contents) -> {
				return loader.stitch(contents, mipLevel, executor);
			}).thenCompose(preps -> {
				this.prepsDone = preps;
				return preps.waitForUpload();
			});
		}

		public void apply(ResourceManager resourceManager, ProfilerFiller profiler) {
			atlasHolder.apply(prepsDone, resourceManager, profiler);
		}

	}


	@ApiStatus.Internal
	public void apply(SpriteLoader.Preparations preparations, ResourceManager resourceManager, ProfilerFiller profiler) {
		profiler.startTick();
		profiler.push("upload");
		this.textureAtlas.upload(preparations);
		profiler.pop();
		profiler.endTick();
	}


	@Override
	public void close() {
		this.textureAtlas.clearTextureData();
	}
}
