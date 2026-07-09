package com.github.standobyte.jojo.client.standskin;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityrender.entities.SimpleEntityRenderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class TextureFromStandSkin {
	public final ResourceLocation texPath;
	public final boolean loadFromStandSkin;
	@Nullable public ResourceLocation defaultSkinId;
	
	public TextureFromStandSkin(ResourceLocation texPath, boolean loadFromStandSkin) {
		this.texPath = texPath;
		this.loadFromStandSkin = loadFromStandSkin;
	}

	public ResourceLocation getTextureLocation(Entity entity) {
		if (loadFromStandSkin) {
			StandSkin standSkin = SimpleEntityRenderer.getStandSkin(entity);
			if (standSkin != null) {
				return standSkin.getTexture(texPath);
			}
			if (defaultSkinId != null) {
				standSkin = StandSkinsLoader.getInstance().getDefaultSkin(defaultSkinId);
				if (standSkin != null) {
					return standSkin.getTexture(texPath);
				}
			}
		}
		
		return texPath;
	}
}
