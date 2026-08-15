package com.github.standobyte.jojo.client.entityrender;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.entityanim.IHumanoidAnimModel;
import com.github.standobyte.jojo.client.entityanim.barrage.BarrageSwings;
import com.github.standobyte.jojo.mixin.client.model.LivingRendererAccessor;
import com.github.standobyte.jojo.mixininterface.LivingRendererLayers;
import com.github.standobyte.jojo.mixininterface.PlayerRendererInterface;
import com.github.standobyte.v1_21_4_stuff.renderstate.EntityRenderState;
import com.github.standobyte.v1_21_4_stuff.renderstate.HumanoidRenderState;
import com.github.standobyte.v1_21_4_stuff.renderstate.RenderStateCrutches;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.NeoForge;

// mostly Ctrl+C Ctrl+V from the vanilla renderer classes + functions called from mixins
public class RenderPlayerSpecial {

	public static <T extends LivingEntity, M extends EntityModel<T>> LivingEntityRenderer<T, M> getRenderer(T entity) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.getEntityRenderDispatcher().getRenderer(entity) instanceof LivingEntityRenderer renderer) {
			return renderer;
		}
		return null;
	}

	public static <T extends LivingEntity, M extends EntityModel<T>> void render(T entity, 
			float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		LivingEntityRenderer<T, M> renderer = getRenderer(entity);
		if (renderer == null) return;

		if (renderer instanceof PlayerRendererInterface modelReplacementFeature) modelReplacementFeature.jojo_ripples$setReplacementModel(entity);
		setPlayerModelProperties(entity, renderer);
		if (!prePlayerRender(entity, renderer, partialTick, poseStack, bufferSource, packedLight)) return;

		if (renderer instanceof PlayerRendererInterface renderer_) {
			RenderStateCrutches.beforeLivingRender(entity, renderer_.jojo_ripples$getReusedState(), renderer, 
					Minecraft.getInstance().getEntityRenderDispatcher(), partialTick);
		}
		if (!preLivingRender(entity, renderer, partialTick, poseStack, bufferSource, packedLight)) return;
		poseStack.pushPose();
		LivingRenderVariables variables = modelBS(entity, renderer, partialTick, poseStack, LivingRenderVariables.instance());

		M model = renderer.getModel();
		beforeVanillaModelAnim(model);
		setupModelAnim(entity, model, variables);
		afterVanillaModelAnim(model);

		RenderType renderType = getRenderType(entity, renderer);
		if (renderType != null) {
			setupBarrageSwings();
			renderMainModel(entity, renderer, partialTick, poseStack, bufferSource, renderType, packedLight);
		}
		renderLayers(entity, renderer, partialTick, poseStack, bufferSource, packedLight, variables);
		poseStack.popPose();

		BarrageSwings.setupToRender(null);
		//renderLeash(entity, renderer, partialTick, poseStack, bufferSource);
		//renderNameTag(entity, renderer, partialTick, poseStack, bufferSource, packedLight);
		postLivingRender(entity, renderer, partialTick, poseStack, bufferSource, packedLight);
		RenderStateCrutches.afterLivingRender();

		postPlayerRender(entity, renderer, partialTick, poseStack, bufferSource, packedLight);
		if (renderer instanceof PlayerRendererInterface modelReplacementFeature) modelReplacementFeature.jojo_ripples$restoreModel();
	}

	public static class LivingRenderVariables {
		public float limbSwing;
		public float limbSwingAmount;
		public float bob;
		public float modelHeadRot;
		public float xRot;

		private static LivingRenderVariables instance = new LivingRenderVariables();

		public static LivingRenderVariables instance() {
			return instance;
		}
	}




	public static <T extends LivingEntity, M extends EntityModel<T>> void setPlayerModelProperties(T entity, LivingEntityRenderer<T, M> renderer) {
		if (renderer instanceof PlayerRenderer playerRenderer && entity instanceof AbstractClientPlayer player) {
			//playerRenderer.setModelProperties(player);
			setModelProperties(playerRenderer.getModel(), player);
		}
	}

	public static void setModelProperties(PlayerModel playerModel, AbstractClientPlayer player) {
		if (player.isSpectator()) {
			playerModel.setAllVisible(false);
			playerModel.head.visible = true;
			playerModel.hat.visible = true;
		} else {
			ModelUtil.setAllVisibleSetupOuterLayer(playerModel, player);
			playerModel.crouching = player.isCrouching();
			HumanoidModel.ArmPose humanoidmodel$armpose = getArmPose(player, InteractionHand.MAIN_HAND);
			HumanoidModel.ArmPose humanoidmodel$armpose1 = getArmPose(player, InteractionHand.OFF_HAND);
			if (humanoidmodel$armpose.isTwoHanded()) {
				humanoidmodel$armpose1 = player.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
			}

			if (player.getMainArm() == HumanoidArm.RIGHT) {
				playerModel.rightArmPose = humanoidmodel$armpose;
				playerModel.leftArmPose = humanoidmodel$armpose1;
			} else {
				playerModel.rightArmPose = humanoidmodel$armpose1;
				playerModel.leftArmPose = humanoidmodel$armpose;
			}
		}
	}

	public static HumanoidModel.ArmPose getArmPose(LivingEntity entity, InteractionHand hand) {
		ItemStack stack = entity.getItemInHand(hand);
		if (stack.isEmpty()) {
			return HumanoidModel.ArmPose.EMPTY;
		} else {
			if (entity.getUsedItemHand() == hand && entity.getUseItemRemainingTicks() > 0) {
				UseAnim useAnim = stack.getUseAnimation();
				HumanoidModel.ArmPose armPose = switch (useAnim) {
					case BLOCK -> HumanoidModel.ArmPose.BLOCK;
					case BOW -> HumanoidModel.ArmPose.BOW_AND_ARROW;
					case SPEAR -> HumanoidModel.ArmPose.THROW_SPEAR;
					case CROSSBOW -> hand == entity.getUsedItemHand() ? HumanoidModel.ArmPose.CROSSBOW_CHARGE : null;
					case SPYGLASS -> HumanoidModel.ArmPose.BLOCK;
					case TOOT_HORN -> HumanoidModel.ArmPose.TOOT_HORN;
					case BRUSH -> HumanoidModel.ArmPose.BRUSH;
					default -> null;
				};
				if (armPose != null) {
					return armPose;
				}
			} else if (!entity.swinging && stack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(stack)) {
				return HumanoidModel.ArmPose.CROSSBOW_HOLD;
			}
			HumanoidModel.ArmPose forgeArmPose = IClientItemExtensions.of(stack).getArmPose(entity, hand, stack);
			if (forgeArmPose != null) return forgeArmPose;

			return HumanoidModel.ArmPose.ITEM;
		}
	}

	public static <T extends LivingEntity, M extends EntityModel<T>> boolean prePlayerRender(T entity, LivingEntityRenderer<T, M> renderer, 
			float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		return !(renderer instanceof PlayerRenderer playerRenderer && entity instanceof AbstractClientPlayer player
				&& NeoForge.EVENT_BUS.post(new RenderPlayerEvent.Pre(player, playerRenderer, partialTick, poseStack, bufferSource, packedLight)).isCanceled());
	}

	public static <T extends LivingEntity, M extends EntityModel<T>> boolean preLivingRender(T entity, LivingEntityRenderer<T, M> renderer, 
			float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		return !NeoForge.EVENT_BUS.post(new RenderLivingEvent.Pre<T, M>(entity, renderer, partialTick, poseStack, bufferSource, packedLight)).isCanceled();
	}

	public static <T extends LivingEntity, M extends EntityModel<T>> LivingRenderVariables modelBS(T entity, LivingEntityRenderer<T, M> renderer, 
			float partialTick, PoseStack poseStack, LivingRenderVariables variables) {
		LivingRendererAccessor<T, M> renderer_ = (LivingRendererAccessor<T, M>) renderer;
		M model = renderer.getModel();
		model.attackTime = entity.getAttackAnim(partialTick);
		boolean shouldSit = entity.isPassenger() && (entity.getVehicle() != null && entity.getVehicle().shouldRiderSit());
		model.riding = shouldSit;
		model.young = entity.isBaby();
		float entityBodyRot = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
		float entityHeadRot = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
		float modelHeadRot = entityHeadRot - entityBodyRot;
		if (shouldSit && entity.getVehicle() instanceof LivingEntity livingentity) {
			entityBodyRot = Mth.rotLerp(partialTick, livingentity.yBodyRotO, livingentity.yBodyRot);
			modelHeadRot = entityHeadRot - entityBodyRot;
			float f7 = Mth.wrapDegrees(modelHeadRot);
			if (f7 < -85.0F) {
				f7 = -85.0F;
			}

			if (f7 >= 85.0F) {
				f7 = 85.0F;
			}

			entityBodyRot = entityHeadRot - f7;
			if (f7 * f7 > 2500.0F) {
				entityBodyRot += f7 * 0.2F;
			}

			modelHeadRot = entityHeadRot - entityBodyRot;
		}

		float xRot = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
		if (LivingEntityRenderer.isEntityUpsideDown(entity)) {
			xRot *= -1.0F;
			modelHeadRot *= -1.0F;
		}

		modelHeadRot = Mth.wrapDegrees(modelHeadRot);
		if (entity.hasPose(Pose.SLEEPING)) {
			Direction direction = entity.getBedOrientation();
			if (direction != null) {
				float f3 = entity.getEyeHeight(Pose.STANDING) - 0.1F;
				poseStack.translate((float)(-direction.getStepX()) * f3, 0.0F, (float)(-direction.getStepZ()) * f3);
			}
		}

		float scale = entity.getScale();
		poseStack.scale(scale, scale, scale);
		//float bob = renderer.getBob(entity, partialTick);
		float bob = getBob(entity, partialTick);
		renderer_.invokeSetupRotations(entity, poseStack, bob, entityBodyRot, partialTick, scale);
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		renderer_.invokeScale(entity, poseStack, partialTick);
		poseStack.translate(0.0F, -1.501F, 0.0F);
		float limbSwingAmount = 0.0F;
		float limbSwing = 0.0F;
		if (!shouldSit && entity.isAlive()) {
			limbSwingAmount = entity.walkAnimation.speed(partialTick);
			limbSwing = entity.walkAnimation.position(partialTick);
			if (entity.isBaby()) {
				limbSwing *= 3.0F;
			}

			if (limbSwingAmount > 1.0F) {
				limbSwingAmount = 1.0F;
			}
		}

		model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);

		variables.limbSwing = limbSwing;
		variables.limbSwingAmount = limbSwingAmount;
		variables.bob = bob;
		variables.modelHeadRot = modelHeadRot;
		variables.xRot = xRot;
		return variables;
	}

	private static float getBob(LivingEntity entity, float partialTick) {
		return (float)entity.tickCount + partialTick;
	}

	public static void beforeVanillaModelAnim(EntityModel<?> model) {
		if (model instanceof IHumanoidAnimModel && RenderStateCrutches.currentEntityRenderState instanceof HumanoidRenderState) {
			EntityRenderState.resetPose(model);
		}
	}

	public static <T extends LivingEntity, M extends EntityModel<T>> void setupModelAnim(T entity, M model, LivingRenderVariables variables) {
		model.setupAnim(entity, variables.limbSwing, variables.limbSwingAmount, variables.bob, variables.modelHeadRot, variables.xRot);
	}

	public static void afterVanillaModelAnim(EntityModel<?> model) {
		if (model instanceof IHumanoidAnimModel humanoidModel && RenderStateCrutches.currentEntityRenderState instanceof HumanoidRenderState humanoidRS) {
			humanoidModel.jojo_ripples$setupHumanoidAnim(humanoidRS);
		}
	}

	@Nullable
	public static <T extends LivingEntity, M extends EntityModel<T>> RenderType getRenderType(T entity, LivingEntityRenderer<T, M> renderer) {
		Minecraft minecraft = Minecraft.getInstance();
		//boolean visible = renderer.isBodyVisible(entity);
		boolean visible = !entity.isInvisible();
		boolean visibleToSpectatorPlayer = !visible && !entity.isInvisibleTo(minecraft.player);
		boolean glowing = minecraft.shouldEntityAppearGlowing(entity);
		return ((LivingRendererAccessor<T, M>) renderer).invokeGetRenderType(entity, visible, visibleToSpectatorPlayer, glowing);
	}

	public static <T extends LivingEntity, M extends EntityModel<T>> void setupBarrageSwings() {
		if (RenderStateCrutches.currentEntityRenderState != null) {
			BarrageSwings barrageSwings = BarrageSwings.getBarrageSwings(RenderStateCrutches.currentEntityRenderState);
			if (barrageSwings != null && barrageSwings.hasSmthToRender()) {
				BarrageSwings.setupToRender(barrageSwings);
			}
		}
	}

	public static <T extends LivingEntity, M extends EntityModel<T>> void renderMainModel(T entity, LivingEntityRenderer<T, M> renderer, 
			float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, RenderType renderType, int packedLight) {
		VertexConsumer buffer = bufferSource.getBuffer(renderType);
		//float overlayProgress = renderer.getWhiteOverlayProgress(entity, partialTick);
		float overlayProgress = 0;
		int overlay = LivingEntityRenderer.getOverlayCoords(entity, overlayProgress);
		renderer.getModel().renderToBuffer(poseStack, buffer, packedLight, overlay, /*visibleToSpectatorPlayer ? 0x26FFFFFF :*//*похуй...*/ -1);
	}

	public static <T extends LivingEntity, M extends EntityModel<T>> void renderLayers(T entity, LivingEntityRenderer<T, M> renderer, 
			float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, LivingRenderVariables variables) {
		if (!entity.isSpectator()) {
			LivingRendererLayers<T, M> renderer_ = (LivingRendererLayers<T, M>) renderer;
			for (RenderLayer<T, M> renderLayer : renderer_.jojo_ripples$allLayers()) {
				if (renderer_.jojo_ripples$shouldRenderLayer(renderLayer, entity)) {
					renderLayer.render(poseStack, bufferSource, packedLight, entity, 
							variables.limbSwing, variables.limbSwingAmount, partialTick, variables.bob, variables.modelHeadRot, variables.xRot);
				}
			}
		}
	}

	//public static <T extends LivingEntity, M extends EntityModel<T>> void renderLeash(T entity, LivingEntityRenderer<T, M> renderer, 
	//		float partialTick, PoseStack poseStack, MultiBufferSource bufferSource) {
	//	if (entity instanceof Leashable leashable) {
	//		Entity leashHolder = leashable.getLeashHolder();
	//		if (leashHolder != null) {
	//			renderer.renderLeash(entity, partialTick, poseStack, bufferSource, leashHolder);
	//		}
	//	}
	//}

	//public static <T extends LivingEntity, M extends EntityModel<T>> void renderNameTag(T entity, LivingEntityRenderer<T, M> renderer, 
	//		float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
	//	RenderNameTagEvent event = new RenderNameTagEvent(entity, entity.getDisplayName(), renderer, poseStack, bufferSource, packedLight, partialTick);
	//	NeoForge.EVENT_BUS.post(event);
	//	if (event.canRender().isTrue() || event.canRender().isDefault() && renderer.shouldShowName(entity)) {
	//		renderer.renderNameTag(entity, event.getContent(), poseStack, bufferSource, packedLight, partialTick);
	//	}
	//}

	public static <T extends LivingEntity, M extends EntityModel<T>> void postLivingRender(T entity, LivingEntityRenderer<T, M> renderer, 
			float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		NeoForge.EVENT_BUS.post(new RenderLivingEvent.Post<T, M>(entity, renderer, partialTick, poseStack, bufferSource, packedLight));
	}

	public static <T extends LivingEntity, M extends EntityModel<T>> void postPlayerRender(T entity, LivingEntityRenderer<T, M> renderer, 
			float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		if (renderer instanceof PlayerRenderer playerRenderer && entity instanceof AbstractClientPlayer player) {
			NeoForge.EVENT_BUS.post(new RenderPlayerEvent.Post(player, playerRenderer, partialTick, poseStack, bufferSource, packedLight));
		}
	}

}
