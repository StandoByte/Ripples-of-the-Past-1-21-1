package com.github.standobyte.jojoimpl.powers.vampirism.client.render;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.core.JojoMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class VampireEyesLayer<T extends LivingEntity, M extends PlayerModel<T>> extends RenderLayer<T, M> {
	public static final ResourceLocation TEXTURE = JojoMod.resLoc("textures/entity/layer/vampire_eyes.png");

	public VampireEyesLayer(RenderLayerParent<T, M> renderer) {
		super(renderer);
	}

	@Override
	public void render(PoseStack matrixStack, MultiBufferSource buffer, int packedLight, 
			T entity, float walkAnimPos, float walkAnimSpeed, float partialTick, 
			float ticks, float headYRotation, float headXRotation) {
		if (!entity.isInvisible()) {
			//boolean eyesEnabled = PlayerPower.getOptional(entity).filter(power -> {
			//	if (power.hasPower()) {
			//		PlayerPowerData curTypeData = power.getCurTypeData(null).get();
			//		PlayerPowerType<?> powerType = curTypeData.getPowerType();
			//		if (powerType == ModPlayerPowers.VAMPIRISM.get()) {
			//			return ((VampirismData) curTypeData).isHighOnBlood();
			//		}
			//		if (powerType == ModPlayerPowers.ZOMBIE.get()) {
			//			return ((ZombieData) curTypeData).isDisguiseEnabled();
			//		}
			//	}
			//	return false;
			//}).isPresent();
			boolean eyesEnabled = false;
			
			if (/*eyesEnabled && */entity instanceof Player player) {
				eyesEnabled = JojoMod.config.getPlayerBroadcast(player).vampireGlowingEyes_tmp.getAsBoolean();
			}
			if (!eyesEnabled) return;
			
			M model = getParentModel();
			ResourceLocation texture = getTexture(model, entity);
			if (texture == null) return;
			VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.entityTranslucent(texture));
			model.renderToBuffer(matrixStack, vertexBuilder, ClientUtil.MAX_LIGHT, 
					LivingEntityRenderer.getOverlayCoords(entity, 0.0F), 0xFFFFFFFF);
		}
	}

	@Nullable
	private ResourceLocation getTexture(EntityModel<?> model, LivingEntity entity) {
		return TEXTURE;
	} 
}
