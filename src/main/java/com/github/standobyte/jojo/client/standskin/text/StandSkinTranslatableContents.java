package com.github.standobyte.jojo.client.standskin.text;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public class StandSkinTranslatableContents extends CustomLangTranslatableContents {
	public static final MapCodec<StandSkinTranslatableContents> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
					// If you call this "translate", the game will instead create a TranslatableContents object when sending the component over network
					Codec.STRING.fieldOf("mojankmoment").forGetter(contents -> contents.getKey()),
					Codec.STRING.lenientOptionalFieldOf("fallback").forGetter(contents -> Optional.ofNullable(contents.getFallback())),
					ARG_CODEC.listOf().optionalFieldOf("with").forGetter(contents -> _adjustArgs(contents.getArgs())),
					ResourceLocation.CODEC.fieldOf("standid").forGetter(contents -> contents.standId),
					ResourceLocation.CODEC.xmap(Optional::of, optional -> optional.orElse(null)).fieldOf("skin").forGetter(contents -> contents.selectedSkin))
			.apply(instance, StandSkinTranslatableContents::create));
	public static final ComponentContents.Type<StandSkinTranslatableContents> TYPE = new ComponentContents.Type<>(StandSkinTranslatableContents.CODEC, "jojo_ripples:standskin");

	private static Optional<List<Object>> _adjustArgs(Object[] args) {
		return args.length == 0 ? Optional.empty() : Optional.of(Arrays.asList(args));
	}

	public final ResourceLocation standId;
	public final Optional<ResourceLocation> selectedSkin;

	public StandSkinTranslatableContents(String key, String fallback, Object[] args, ResourceLocation standId, Optional<ResourceLocation> selectedSkin) {
		super(key, fallback, args);
		this.standId = standId;
		this.selectedSkin = selectedSkin;
	}

	private static StandSkinTranslatableContents create(String key, Optional<String> fallback, Optional<List<Object>> args,
			ResourceLocation standId, Optional<ResourceLocation> selectedSkin) {
		return new StandSkinTranslatableContents(key, fallback.orElse(null),
				args.<Object[]>map(list -> list.isEmpty() ? NO_ARGS : list.toArray()).orElse(NO_ARGS),
				standId, selectedSkin);
	}

	@Override
	public ComponentContents.Type<?> type() {
		return StandSkinTranslatableContents.TYPE;
	}


	/*
	 * null - haven't looked up the Stand skin yet
	 * empty optional - Stand skins don't have this line, use the vanilla Language as a fallback
	 */
	protected Optional<StandSkin> clCachedStandSkin = null;
	@Override
	public Language getLanguage() {
		if (FMLEnvironment.dist == Dist.CLIENT) {
			// Check if the skins have been reloaded, in which case the cached one is now irrelevant
			if (clCachedStandSkin != null) {
				StandSkin skin = clCachedStandSkin.orElse(null);
				if (skin != null && skin.isDiscarded) {
					clCachedStandSkin = null;
				}
			}
			
			// Retrieve the skin from the skins manager, and cache the object 
			// (or an empty optional, if the Stand skin doesn't have this translation entry)
			if (clCachedStandSkin == null) {
				StandSkinsLoader standSkinsLoader = StandSkinsLoader.getInstance();
				StandSkin standSkin = standSkinsLoader.getSkinFromId(this.standId, this.selectedSkin);
				StandSkin defaultStandSkin = standSkinsLoader.getDefaultSkin(this.standId);
				if (standSkin == null) {
					standSkin = defaultStandSkin;
				}
				StandSkin skinLang = standSkin != null ? standSkin.resolveLang(defaultStandSkin, this.getKey(), this.getFallback()) : null;
				clCachedStandSkin = Optional.ofNullable(skinLang);
			}
			
			// Already did the work retrieving the Stand skin, if it has the text - get the language
			if (clCachedStandSkin != null) {
				StandSkin skin = clCachedStandSkin.orElse(null);
				if (skin != null) {
					return skin.getLanguage();
				}
			}
		}
		
		// Otherwise return the vanilla one
		return Language.getInstance();
	}


	@Override
	public MutableComponent resolve(@Nullable CommandSourceStack nbtPathPattern, @Nullable Entity entity, int recursionDepth) throws CommandSyntaxException {
		Object[] argsResolved = new Object[this.args.length];

		for (int i = 0; i < argsResolved.length; i++) {
			Object arg = this.args[i];
			if (arg instanceof Component component) {
				argsResolved[i] = ComponentUtils.updateForEntity(nbtPathPattern, component, entity, recursionDepth);
			} else {
				argsResolved[i] = arg;
			}
		}

		return MutableComponent.create(new StandSkinTranslatableContents(key, fallback, argsResolved, standId, selectedSkin));
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		} else {
			if (other instanceof StandSkinTranslatableContents otherContents
					&& Objects.equals(this.getKey(), otherContents.getKey())
					&& Objects.equals(this.getFallback(), otherContents.getFallback())
					&& Arrays.equals(this.getArgs(), otherContents.getArgs())
					&& Objects.equals(this.selectedSkin, otherContents.selectedSkin)
					&& Objects.equals(this.standId, otherContents.standId)) {
				return true;
			}

			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(getKey(), getFallback(), selectedSkin, standId, getArgs());
	}

	@Override
	public String toString() {
		return "translation{"
				+ "key='" + getKey() + "'"
				+ (getFallback() != null ? ", fallback='" + getFallback() + "'" : "")
				+ ", args=" + Arrays.toString(getArgs())
				+ ", standId=" + standId.toString()
				+ ", standSkinId=" + selectedSkin.orElse(standId).toString()
				+ "}";
	}

}
