package com.github.standobyte.jojo.subsystems;

import java.util.Comparator;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.textsymbols.sprite.IconGlyphInfo;
import com.github.standobyte.jojo.client.textsymbols.sprite.IconGlyphsCache;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public class StoryPart {
	@Nullable protected final TextColor nameColor;
	public static final float WIDTH_DEFAULT = 16;
	public static final float HEIGHT_DEFAULT = 16;
	protected final float iconWidth;
	protected final float iconHeight;
	
	protected Component iconAsText;
	protected Component nameWithIcon;
	protected ResourceLocation icon;
	protected char clientGlyphIndex = 0;

	public StoryPart() {
		this(null);
	}

	public StoryPart(@Nullable TextColor nameColor) {
		this(nameColor, WIDTH_DEFAULT, HEIGHT_DEFAULT);
	}

	public StoryPart(@Nullable TextColor nameColor, float iconWidth, float iconHeight) {
		this.nameColor = nameColor;
		this.iconWidth = iconWidth;
		this.iconHeight = iconHeight;
	}
	
	public void initNameAndIcon(ResourceKey<StoryPart> key) {
		MutableComponent iconAndName = null;
		if (FMLEnvironment.dist == Dist.CLIENT) {
			ResourceLocation id = key.location();
			this.icon = id.withPath(path -> "textures/story_part/" + path + ".png");
			this.clientGlyphIndex = IconGlyphsCache.makeCharCodeFor(
					new IconGlyphInfo(new GuiIcon(this.icon, this.iconWidth, this.iconHeight), this.iconWidth / 2, this.iconHeight / 2));
			this.iconAsText = Component.literal(Character.toString(this.clientGlyphIndex));
		}
		else {
			this.iconAsText = Component.empty();
		}
		iconAndName = iconAsText.copy();

		ResourceLocation id = key.location();
		String tlKey = id.getNamespace() + ".story_part." + id.getPath();
		MutableComponent name = Component.translatable(tlKey);
		if (this.nameColor != null) {
			name.withStyle(style -> style.withColor(this.nameColor));
		}
		iconAndName.append(name);

		this.nameWithIcon = iconAndName;
	}
	
	public Component getPartName() {
		return nameWithIcon;
	}
	
	public ResourceLocation getPartIcon() {
		return icon;
	}
	
	public Component getPartIconAsText() {
		return iconAsText;
	}


	public static final Codec<StoryPart> DIRECT_CODEC = RecordCodecBuilder.create(
			builder -> builder.group(
					TextColor.CODEC.optionalFieldOf("name_color", TextColor.fromRgb(0xFFFFFF)).forGetter(part -> part.nameColor),
					Codec.FLOAT.optionalFieldOf("icon_width", WIDTH_DEFAULT).forGetter(part -> part.iconWidth),
					Codec.FLOAT.optionalFieldOf("icon_height", HEIGHT_DEFAULT).forGetter(part -> part.iconHeight))
			.apply(builder, StoryPart::new));


	public static final Codec<Holder<StoryPart>> REG_CODEC = RegistryFixedCodec.create(JojoRegistries.STORY_PARTS_REG_KEY);



	@Nullable
	public static Holder<StoryPart> getStoryPart(StandInstance standInstance, HolderLookup.Provider registries) {
		if (FMLEnvironment.dist == Dist.CLIENT) {
			StandSkin skin = StandSkinsLoader.getInstance().getSkin(standInstance);
			if (skin != null) {
				return skin.getStoryPart(registries);
			}
		}
		return null;
	}


	public static final Comparator<ResourceLocation> ID_COMPARATOR = (id1, id2) -> {
		if (id2 == null) return -1;
		if (id1 == null) return 1;
		return id1.compareTo(id2);
	};
	
	@Nullable
	public static ResourceLocation getId(Holder<?> holder) {
		if (holder == null) return null;
		var key = holder.getKey();
		return key != null ? key.location() : null;
	}
	
	public static final Comparator<Holder<StoryPart>> COMPARATOR = Comparator.comparing(StoryPart::getId, ID_COMPARATOR);
	
}
