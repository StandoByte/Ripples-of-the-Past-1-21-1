package com.github.standobyte.jojo.adventure.npc;

import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

public class ClientHumanoidCharacterStuff {
	protected ResolvableProfile profile = null;

	public void onSetSkinSourceProfile(Optional<ResolvableProfile> profile, Entity entity) {
		synchronized (entity) {
			this.profile = profile.orElse(null);
		}
		if (this.profile != null && !this.profile.isResolved()) {
			this.profile.resolve().thenAcceptAsync(resolved -> {
				this.profile = resolved;
			}, SkullBlockEntity.CHECKED_MAIN_THREAD_EXECUTOR);
		}
	}
	
	public PlayerSkin getPlayerSkin(Entity entity) {
		if (profile != null) {
			SkinManager skinManager = Minecraft.getInstance().getSkinManager();
			return skinManager.getInsecureSkin(profile.gameProfile());
		}
		else {
			return DefaultPlayerSkin.get(entity.getUUID());
		}
	}

	public ResourceLocation getTexture(Entity entity) {
		return getPlayerSkin(entity).texture();
	}

	public PlayerSkin.Model getModelType(Entity entity) {
		return getPlayerSkin(entity).model();
	}

}
