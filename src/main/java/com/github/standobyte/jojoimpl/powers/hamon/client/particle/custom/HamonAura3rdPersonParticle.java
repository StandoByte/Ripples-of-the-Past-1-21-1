package com.github.standobyte.jojoimpl.powers.hamon.client.particle.custom;

import com.github.standobyte.jojo.client.config.ClientModSettings;
import com.github.standobyte.jojo.core.NotYetImplemented;
import com.github.standobyte.jojoimpl.powers.hamon.client.particle.HamonAuraParticle;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class HamonAura3rdPersonParticle extends HamonAuraParticle {
	private final LivingEntity user;
	private final AbstractClientPlayer userAsPlayer;
	private Vec3 userPositionPrev;

	protected HamonAura3rdPersonParticle(ClientLevel level, LivingEntity entity, 
			double x, double y, double z, double xda, double yda, double zda,
			SpriteSet sprites) {
		super(level, x, y, z, xda, yda, zda, sprites);
		this.user = entity;
		this.userPositionPrev = entity.position();
		this.userAsPlayer = user instanceof AbstractClientPlayer player ? player : null;
	}

	@Override
	public void render(VertexConsumer vertexBuilder, Camera camera, float partialTick) {
		if (!ClientModSettings.getSettingsReadOnly().thirdPersonHamonAura) return;

		if (user != null) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.cameraEntity == user && mc.options.getCameraType() == CameraType.FIRST_PERSON) {
				return;
			}
		}

		Vec3 playerAnimPos = userAsPlayer != null ? getBodyPos(userAsPlayer, partialTick) : Vec3.ZERO;
		x += playerAnimPos.x;
		y += playerAnimPos.y;
		z += playerAnimPos.z;
		xo += playerAnimPos.x;
		yo += playerAnimPos.y;
		zo += playerAnimPos.z;

		super.render(vertexBuilder, camera, partialTick);

		x -= playerAnimPos.x;
		y -= playerAnimPos.y;
		z -= playerAnimPos.z;
		xo -= playerAnimPos.x;
		yo -= playerAnimPos.y;
		zo -= playerAnimPos.z;
	}
	
	public static Vec3 getBodyPos(AbstractClientPlayer player, float partialTick) {
//		return PlayerAnimationHandler.getPlayerAnimator().getBodyPos(player, partialTick);
		throw new NotYetImplemented();
	}

	@Override
	public void tick() {
		super.tick();
		if (user != null) {
			Vec3 offset = user.position().subtract(userPositionPrev);
			move(offset.x, offset.y, offset.z);
			this.userPositionPrev = user.position();
		}
	}


	public static HamonAuraParticle createCustomParticle(SpriteSet sprites, ClientLevel level, 
			LivingEntity entity, double x, double y, double z) {
		HamonAuraParticle particle = new HamonAura3rdPersonParticle(level, entity, x, y, z, 0, 0, 0, sprites);
		return particle;
	}
}
