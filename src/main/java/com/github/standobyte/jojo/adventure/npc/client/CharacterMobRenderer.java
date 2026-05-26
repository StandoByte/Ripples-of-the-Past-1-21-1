package com.github.standobyte.jojo.adventure.npc.client;

import java.text.DecimalFormat;

import com.github.standobyte.jojo.adventure.npc.PowerUserMobEntity;
import com.github.standobyte.jojo.adventure.npc.debug.NpcFlags;
import com.github.standobyte.jojo.client.entityrender.replace_player_model.ReplacePlayerModel;
import com.github.standobyte.jojo.client.standskin.text.StandNameSetColor;
import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.mechanics.resolve.ResolveCounter;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.v1_21_4_stuff.renderstate.HumanoidRenderState;
import com.github.standobyte.v1_21_4_stuff.renderstate.RenderStateCrutches;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.BeeStingerLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

public class CharacterMobRenderer<T extends PowerUserMobEntity> extends LivingEntityRenderer<T, EntityModel<T>> {
	protected EntityModel<T> regularPlayerModel;
	protected EntityModel<T> slimPlayerModel;
	protected HumanoidRenderState reusedState = new HumanoidRenderState();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public CharacterMobRenderer(Context context) {
		super(context, new PlayerModel<T>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
		this.regularPlayerModel = this.model;
		this.slimPlayerModel = new PlayerModel<T>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);

		this.addLayer(new HumanoidArmorLayer(this,
				new HumanoidArmorModel(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
				new HumanoidArmorModel(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
				context.getModelManager()));
		this.addLayer(new PlayerItemInHandLayer(this, context.getItemInHandRenderer()));
		this.addLayer(new CustomHeadLayer(this, context.getModelSet(), context.getItemInHandRenderer()));
        this.addLayer(new ArrowLayer(context, this));
        this.addLayer(new BeeStingerLayer(this));
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		ResourceLocation replacementTexture = ReplacePlayerModel.getTexture(entity);
		if (replacementTexture != null) {
			return replacementTexture;
		}
		return entity.clientStuff.getTexture(entity);
	}

	@Override
	public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		PlayerSkin.Model modelType = entity.clientStuff.getModelType(entity);
		this.model = switch (modelType) {
			case WIDE -> regularPlayerModel;
			case SLIM -> slimPlayerModel;
		};
		
		PlayerModel replacementModel = ReplacePlayerModel.getModel(entity);
		if (replacementModel != null) {
			this.model = replacementModel;
		}
		
		RenderStateCrutches.beforeLivingRender(entity, reusedState, this, entityRenderDispatcher, partialTick);
		super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
		RenderStateCrutches.afterLivingRender();
		
		renderDummyStuff(entity, partialTick, poseStack, buffer, packedLight, entityRenderDispatcher);
	}

	@Override
	protected boolean shouldShowName(T entity) {
		return super.shouldShowName(entity) && entity.hasCustomName();
	}

	public void renderDummyStuff(T entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, EntityRenderDispatcher entityRenderDispatcher) {
		poseStack.pushPose();
		//poseStack.translate(0, -0.25, 0);

		if (Minecraft.renderNames()) {
			if (entity.getFlag(NpcFlags.SHOW_POWER_VARIABLES)) {
				StandPower stand = StandPower.get(entity);
				ResolveCounter resolve = ResolveCounter.getIfEnabled(entity);
				if (stand != null && stand.hasPower()) {
					poseStack.translate(0, 0.25, 0);
					float staminaRatio = stand.getStamina() / stand.getMaxStamina();
					float staminaCondition = 0.25F + Math.min(staminaRatio * 1.5F, 0.75F);
					int color = FastColor.ARGB32.colorFromFloat(1, 1 - staminaCondition, staminaCondition, 0f);
					renderNameTag(entity, 
							Component.translatable("Stamina: %s", 
									Component.translatable(String.format("%.2f%%", staminaRatio * 100)).withStyle(style -> style.withColor(color))), 
							poseStack, buffer, packedLight, partialTick);
				}
				
				if (resolve != null) {
					poseStack.translate(0, 0.25, 0);
					float resolveRatio = resolve.getResolveBarFill();
					renderNameTag(entity, 
							Component.translatable(String.format("Resolve: %.2f%%", resolveRatio * 100)), 
							poseStack, buffer, packedLight, partialTick);
				}

				if (stand != null && stand.hasPower()) {
					poseStack.translate(0, 0.25, 0);
					Component standName = stand.getName();
					standName = StandNameSetColor.fromSkin(stand, standName, false);
					renderNameTag(entity, 
							standName, 
							poseStack, buffer, packedLight, partialTick);
				}
			}
			
			if (entity.getFlag(NpcFlags.SHOW_HUNGER)) {
				poseStack.translate(0, 0.25, 0);
				int hunger = entity.getFoodLevel();
				float saturation = entity.getSaturationLevel();
				renderNameTag(entity, 
						Component.translatable("🍖 " + hunger + "/20 (" + (int) saturation + ")"), 
						poseStack, buffer, packedLight, partialTick);
			}
			
			if (entity.getFlag(NpcFlags.SHOW_HP)) {
				DecimalFormat format = new DecimalFormat("#.##");
				poseStack.translate(0, 0.25, 0);
				String hp = format.format(entity.getHealth());
				String maxHp = format.format(entity.getMaxHealth());
				renderNameTag(entity, 
						Component.translatable("❤ " + hp + "/" + maxHp), 
						poseStack, buffer, packedLight, partialTick);
			}
		}

		poseStack.popPose();
	}

	@Override
	protected void scale(T livingEntity, PoseStack poseStack, float partialTickTime) {
		boolean usingPlayerModel = true;
		if (usingPlayerModel) {
			poseStack.scale(
					ClientUtil.PLAYER_RENDER_SCALE, 
					ClientUtil.PLAYER_RENDER_SCALE, 
					ClientUtil.PLAYER_RENDER_SCALE);
		}
	}

}
