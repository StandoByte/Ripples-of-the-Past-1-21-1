package com.github.standobyte.jojo.client.firstperson;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.joml.Matrix4f;

import com.github.standobyte.jojo.client.entityanim.IHumanoidAnimModel;
import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose;
import com.github.standobyte.jojo.client.entityanim.pose.AnimatedEntity;
import com.github.standobyte.jojo.client.entityrender.stand.HumanoidPart;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityRenderState;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityRenderer;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.subsystems.entity_possessionv2.LivingComponentPossession;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.client.ClientEntityController;
import com.github.standobyte.jojo.util.functions.UtilFunctions;
import com.github.standobyte.v1_21_4_stuff.renderstate.EntityRenderState;
import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

@SuppressWarnings({ "unchecked", "rawtypes" }) // Silence, Java generics.
public class FirstPersonRender {
	static FirstPersonRender instance;

	public static void init() { instance = new FirstPersonRender(); }
	public static FirstPersonRender getInstance() { return instance; }

	protected final Minecraft mc;

	protected FirstPersonRender() {
		this.mc = Minecraft.getInstance();
	}


	/**
	 * @return true if the vanilla hand render should be canceled entirely
	 */
	public static boolean onFirstPersonRender(Minecraft mc, float partialTick, PoseStack poseStack, BufferSource bufferSource, int light) {
		Entity povEntity = mc.cameraEntity;
		if (!JojoMod.disableDevStuff() && povEntity instanceof LivingEntity livingEntity) {
			AnimFramePose rotpAnimPose = ((AnimatedEntity) povEntity).jojo_ripples$getModelPose(AnimatedEntity.PoseType.FINAL);
			if (rotpAnimPose != null) {
				return renderPlayer1stPersonAnim(mc, livingEntity, rotpAnimPose, partialTick, poseStack, bufferSource, light);
			}
		}

		ClientEntityController curController = ClientEntityController.getInstance();
		Entity possessed = LivingComponentPossession.getEntityPossessedBy(mc.player);

		boolean mechanicFromThisMod = 
				curController != null && curController.entity == povEntity
				|| possessed != null && possessed == povEntity;
		// That means it's not our problem to deal with, or else we could accidentally mess with other mods
		if (!mechanicFromThisMod) return false;

		EntityRenderer<?> renderer = mc.getEntityRenderDispatcher().getRenderer(povEntity);
		switch (renderer) {
			case StandEntityRenderer standRenderer -> {
				StandEntity entity = (StandEntity) povEntity;
				StandEntityRenderState renderState = standRenderer.createRenderState(entity, partialTick);
				renderState.visibleParts = HumanoidPart.reduce(renderState.visibleParts, HumanoidPart.ARMS_ONLY);
				renderState.mayObstructView = false;
				renderState.doScalingFromStandSkin = false;

				poseStack.pushPose();
				poseStack.mulPose(Axis.XP.rotationDegrees(renderState.xRot));
				poseStack.mulPose(Axis.YP.rotationDegrees(180 + renderState.bodyRot));
				poseStack.translate(0, -povEntity.getEyeHeight(), 0);
				// standRenderer.render(renderState, poseStack, bufferSource, packedLight);
				standRenderer.render(entity, renderState, 0, partialTick, poseStack, bufferSource, light);
				poseStack.popPose();

				bufferSource.endBatch();
			}
			case LivingEntityRenderer livingRenderer -> {
				// WHO WOULD HAVE THOUGHT LIVING ENTITIES OTHER THAN PLAYERS HAVE ARMS AND CAN HOLD ITEMS? NO WAY
				// The entirety of the code below is a copypaste from ItemInHandRenderer, with methods taking a LivingEntity argument instead of using Minecraft#player
				// AND THIS FUCKING SUCKS
				// OOP was a mistake.
				LivingEntity entity = (LivingEntity) povEntity;
				float swingAnim = entity.getAttackAnim(partialTick);
				InteractionHand swingOrMainHand = MoreObjects.firstNonNull(entity.swingingArm, InteractionHand.MAIN_HAND);
				float xRot = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());

				boolean renderMainHand;
				boolean renderOffHand;
				boolean hasBow = instance.mainHandItem.is(Items.BOW) || instance.offHandItem.is(Items.BOW);
				boolean hasCrossbow = instance.mainHandItem.is(Items.CROSSBOW) || instance.offHandItem.is(Items.CROSSBOW);
				if (!hasBow && !hasCrossbow) {
					renderMainHand = true;
					renderOffHand = true;
				} else if (entity.isUsingItem()) {
					ItemStack usedItem = entity.getUseItem();
					InteractionHand usedItemHand = entity.getUsedItemHand();
					if (!usedItem.is(Items.BOW) && !usedItem.is(Items.CROSSBOW)) {
						renderMainHand = true;
						renderOffHand = !(usedItemHand == InteractionHand.MAIN_HAND && instance.offHandItem.is(Items.CROSSBOW) && CrossbowItem.isCharged(instance.offHandItem));
					}
					else {
						switch (usedItemHand) {
							case MAIN_HAND -> {
								renderMainHand = true;
								renderOffHand = false;
							}
							case OFF_HAND -> {
								renderMainHand = false;
								renderOffHand = true;
							}
							default -> throw new AssertionError();
						}
					}
				} else {
					renderMainHand = true;
					renderOffHand = !(instance.mainHandItem.is(Items.CROSSBOW) && CrossbowItem.isCharged(instance.mainHandItem));
				}

				float xBob = Mth.lerp(partialTick, instance.xBobO, instance.xBob);
				float yBob = Mth.lerp(partialTick, instance.yBobO, instance.yBob);
				poseStack.mulPose(Axis.XP.rotationDegrees((entity.getViewXRot(partialTick) - xBob) * 0.1F));
				poseStack.mulPose(Axis.YP.rotationDegrees((entity.getViewYRot(partialTick) - yBob) * 0.1F));
				if (renderMainHand) {
					float swingProgress = swingOrMainHand == InteractionHand.MAIN_HAND ? swingAnim : 0.0F;
					float handHeight = 1.0F - Mth.lerp(partialTick, instance.oMainHandHeight, instance.mainHandHeight);
					if (!ClientHooks.renderSpecificFirstPersonHand(InteractionHand.MAIN_HAND, poseStack, bufferSource, light, partialTick, xRot, swingProgress, handHeight, instance.mainHandItem))
						instance.renderArmWithItem(entity, partialTick, xRot, InteractionHand.MAIN_HAND, 
								swingProgress, instance.mainHandItem, handHeight, poseStack, bufferSource, light,
								false);
				}

				if (renderOffHand) {
					float swingProgress = swingOrMainHand == InteractionHand.OFF_HAND ? swingAnim : 0.0F;
					float handHeight = 1.0F - Mth.lerp(partialTick, instance.oOffHandHeight, instance.offHandHeight);
					if (!ClientHooks.renderSpecificFirstPersonHand(InteractionHand.OFF_HAND, poseStack, bufferSource, light, partialTick, xRot, swingProgress, handHeight, instance.offHandItem))
						instance.renderArmWithItem(entity, partialTick, xRot, InteractionHand.OFF_HAND, 
								swingProgress, instance.offHandItem, handHeight, poseStack, bufferSource, light,
								false);
				}

				bufferSource.endBatch();
			}
			default -> {}
		}

		return true;
	}
	
	public static boolean renderPlayer1stPersonAnim(Minecraft mc, @Nonnull LivingEntity cameraEntity, @Nonnull AnimFramePose pose, 
			float partialTick, PoseStack poseStack, BufferSource bufferSource, int light) {
		if (mc.getEntityRenderDispatcher().getRenderer(cameraEntity) instanceof LivingEntityRenderer renderer
				&& renderer.getModel() instanceof HumanoidModel model) {
			poseStack.pushPose();
			
			if (cameraEntity instanceof LocalPlayer playerBob) {
				float f2 = Mth.lerp(partialTick, playerBob.xBobO, playerBob.xBob);
				float f3 = Mth.lerp(partialTick, playerBob.yBobO, playerBob.yBob);
				poseStack.mulPose(Axis.XP.rotationDegrees((cameraEntity.getViewXRot(partialTick) - f2) * 0.1F));
				poseStack.mulPose(Axis.YP.rotationDegrees((cameraEntity.getViewYRot(partialTick) - f3) * 0.1F));
			}
			
			model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
			model.leftArmPose = HumanoidModel.ArmPose.EMPTY;
			model.attackTime = 0.0F;
			model.crouching = false;
			model.swimAmount = 0.0F;
			model.young = false;
			model.setupAnim(cameraEntity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
			((IHumanoidAnimModel) model).jojo_ripples$setupHumanoidPose(pose);
			ResourceLocation texture = renderer.getTextureLocation(cameraEntity);
			
			poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
			poseStack.translate(0, 0.125, 0);
			
			model.head.visible = false;
			model.hat.visible = false;
			model.body.visible = false;
			if (!cameraEntity.isInvisible()) {
				model.renderToBuffer(poseStack, bufferSource.getBuffer(RenderType.entityTranslucent(texture)), light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
			}
			
			List<FirstPersonModelLayer> layers = ((LivingLayersAccess) renderer).jojo_ripples$firstPersonHandLayers();
			for (FirstPersonModelLayer layer : layers) {
				layer.renderFirstPersonAnimated(pose, poseStack, bufferSource, light, cameraEntity, renderer);
			}
			// FIXME (1st person anim) render held items and modded layers
			
			poseStack.popPose();
			bufferSource.endBatch();
			EntityRenderState.resetPose(model);
			return true;
		}
		
		return false;
	}

	protected ItemStack mainHandItem = ItemStack.EMPTY;
	protected ItemStack offHandItem = ItemStack.EMPTY;
	protected float mainHandHeight;
	protected float oMainHandHeight;
	protected float offHandHeight;
	protected float oOffHandHeight;
	protected float yBob;
	protected float xBob;
	protected float yBobO;
	protected float xBobO;

	public void tick() {
		Entity povEntity = mc.cameraEntity;
		if (povEntity != mc.player && povEntity instanceof LivingEntity entity) {
			this.oMainHandHeight = this.mainHandHeight;
			this.oOffHandHeight = this.offHandHeight;

			ItemStack mainHandItem = entity.getMainHandItem();
			ItemStack offHandItem = entity.getOffhandItem();
			if (ItemStack.matches(this.mainHandItem, mainHandItem)) {
				this.mainHandItem = mainHandItem;
			}

			if (ItemStack.matches(this.offHandItem, offHandItem)) {
				this.offHandItem = offHandItem;
			}

			boolean isHandsBusy = false; // LocalPlayer#isHandsBusy()
			if (isHandsBusy) {
				this.mainHandHeight = Mth.clamp(this.mainHandHeight - 0.4F, 0.0F, 1.0F);
				this.offHandHeight = Mth.clamp(this.offHandHeight - 0.4F, 0.0F, 1.0F);
			} else {
				float attackStrScale = entity instanceof Player player ? player.getAttackStrengthScale(1.0F) : 1;
				boolean requipM = ClientHooks.shouldCauseReequipAnimation(this.mainHandItem, mainHandItem, -1);
				boolean requipO = ClientHooks.shouldCauseReequipAnimation(this.offHandItem, offHandItem, -1);

				if (!requipM && this.mainHandItem != mainHandItem)
					this.mainHandItem = mainHandItem;
				if (!requipO && this.offHandItem != offHandItem)
					this.offHandItem = offHandItem;

				this.mainHandHeight += Mth.clamp((!requipM ? attackStrScale * attackStrScale * attackStrScale : 0.0F) - this.mainHandHeight, -0.4F, 0.4F);
				this.offHandHeight += Mth.clamp((float)(!requipO ? 1 : 0) - this.offHandHeight, -0.4F, 0.4F);
			}

			if (this.mainHandHeight < 0.1F) {
				this.mainHandItem = mainHandItem;
			}

			if (this.offHandHeight < 0.1F) {
				this.offHandItem = offHandItem;
			}

			this.yBobO = this.yBob;
			this.xBobO = this.xBob;
			this.xBob = this.xBob + (entity.getXRot() - this.xBob) * 0.5F;
			this.yBob = this.yBob + (entity.getYRot() - this.yBob) * 0.5F;
		}
		else {
			this.mainHandItem = ItemStack.EMPTY;
			this.offHandItem = ItemStack.EMPTY;
			this.oMainHandHeight = 1;
			this.mainHandHeight = 1;
			this.oOffHandHeight = 1;
			this.offHandHeight = 1;
		}
	}

	public void renderArmWithItem(LivingEntity entity, float partialTicks, float pitch, InteractionHand hand, 
			float swingProgress, ItemStack stack, float equippedProgress, PoseStack poseStack, MultiBufferSource buffer, int light,
			boolean renderOffHandIfEmpty) {
		boolean isScoping = entity.isUsingItem() && entity.getUseItem().is(Items.SPYGLASS);
		if (!isScoping) {
			boolean isMainHand = hand == InteractionHand.MAIN_HAND;
			HumanoidArm handSide = isMainHand ? entity.getMainArm() : entity.getMainArm().getOpposite();
			poseStack.pushPose();
			if (stack.isEmpty()) {
				if ((renderOffHandIfEmpty || isMainHand)/* && !entity.isInvisible()*/) {
					renderEntityArm(getLivingRenderer(entity), entity, 
							poseStack, buffer, light, equippedProgress, swingProgress, handSide);
				}
			} else if (stack.getItem() instanceof MapItem) {
				if (isMainHand && entity.getOffhandItem().isEmpty()) {
					renderTwoHandedMap(entity, poseStack, buffer, light, pitch, equippedProgress, swingProgress, stack);
				} else {
					renderOneHandedMap(entity, poseStack, buffer, light, equippedProgress, handSide, swingProgress, stack);
				}
			} else if (stack.getItem() instanceof CrossbowItem) {
				boolean rightHand = handSide == HumanoidArm.RIGHT;
				int i = rightHand ? 1 : -1;
				if (entity.isUsingItem() && entity.getUseItemRemainingTicks() > 0 && entity.getUsedItemHand() == hand) {
					applyItemArmTransform(poseStack, handSide, equippedProgress);
					poseStack.translate((float)i * -0.4785682F, -0.094387F, 0.05731531F);
					poseStack.mulPose(Axis.XP.rotationDegrees(-11.935F));
					poseStack.mulPose(Axis.YP.rotationDegrees((float)i * 65.3F));
					poseStack.mulPose(Axis.ZP.rotationDegrees((float)i * -9.785F));
					float f9 = (float)stack.getUseDuration(entity) - ((float)entity.getUseItemRemainingTicks() - partialTicks + 1.0F);
					float f13 = f9 / (float)CrossbowItem.getChargeDuration(stack, entity);
					if (f13 > 1.0F) {
						f13 = 1.0F;
					}

					if (f13 > 0.1F) {
						float f16 = Mth.sin((f9 - 0.1F) * 1.3F);
						float f3 = f13 - 0.1F;
						float f4 = f16 * f3;
						poseStack.translate(f4 * 0.0F, f4 * 0.004F, f4 * 0.0F);
					}

					poseStack.translate(f13 * 0.0F, f13 * 0.0F, f13 * 0.04F);
					poseStack.scale(1.0F, 1.0F, 1.0F + f13 * 0.2F);
					poseStack.mulPose(Axis.YN.rotationDegrees((float)i * 45.0F));
				} else {
					float f = -0.4F * Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);
					float f1 = 0.2F * Mth.sin(Mth.sqrt(swingProgress) * (float) (Math.PI * 2));
					float f2 = -0.2F * Mth.sin(swingProgress * (float) Math.PI);
					poseStack.translate((float)i * f, f1, f2);
					applyItemArmTransform(poseStack, handSide, equippedProgress);
					applyItemArmAttackTransform(poseStack, handSide, swingProgress);
					if (CrossbowItem.isCharged(stack) && swingProgress < 0.001F && isMainHand) {
						poseStack.translate((float)i * -0.641864F, 0.0F, 0.0F);
						poseStack.mulPose(Axis.YP.rotationDegrees((float)i * 10.0F));
					}
				}

				// A vanille method that takes a LivingEntity argument and not Player?? An endangered species.
				mc.getEntityRenderDispatcher().getItemInHandRenderer().renderItem(
						entity, stack, rightHand ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
								!rightHand, poseStack, buffer, light);
			} else {
				boolean rightHand = handSide == HumanoidArm.RIGHT;
				boolean customArmAnim = false; // IClientItemExtensions.of(stack).applyForgeHandTransform(poseStack, /*LocalPlayer*/ entity, handSide, stack, partialTicks, equippedProgress, swingProgress);
				if (!customArmAnim) {
					if (entity.isUsingItem() && entity.getUseItemRemainingTicks() > 0 && entity.getUsedItemHand() == hand) {
						int k = rightHand ? 1 : -1;
						switch (stack.getUseAnimation()) {
							case NONE:
								applyItemArmTransform(poseStack, handSide, equippedProgress);
								break;
							case EAT:
							case DRINK:
								applyEatTransform(poseStack, partialTicks, handSide, stack, entity);
								applyItemArmTransform(poseStack, handSide, equippedProgress);
								break;
							case BLOCK:
								applyItemArmTransform(poseStack, handSide, equippedProgress);
								break;
							case BOW:
								applyItemArmTransform(poseStack, handSide, equippedProgress);
								poseStack.translate((float)k * -0.2785682F, 0.18344387F, 0.15731531F);
								poseStack.mulPose(Axis.XP.rotationDegrees(-13.935F));
								poseStack.mulPose(Axis.YP.rotationDegrees((float)k * 35.3F));
								poseStack.mulPose(Axis.ZP.rotationDegrees((float)k * -9.785F));
								float f8 = (float)stack.getUseDuration(entity) - ((float)entity.getUseItemRemainingTicks() - partialTicks + 1.0F);
								float f12 = f8 / 20.0F;
								f12 = (f12 * f12 + f12 * 2.0F) / 3.0F;
								if (f12 > 1.0F) {
									f12 = 1.0F;
								}

								if (f12 > 0.1F) {
									float f15 = Mth.sin((f8 - 0.1F) * 1.3F);
									float f18 = f12 - 0.1F;
									float f20 = f15 * f18;
									poseStack.translate(f20 * 0.0F, f20 * 0.004F, f20 * 0.0F);
								}

								poseStack.translate(f12 * 0.0F, f12 * 0.0F, f12 * 0.04F);
								poseStack.scale(1.0F, 1.0F, 1.0F + f12 * 0.2F);
								poseStack.mulPose(Axis.YN.rotationDegrees((float)k * 45.0F));
								break;
							case SPEAR:
								applyItemArmTransform(poseStack, handSide, equippedProgress);
								poseStack.translate((float)k * -0.5F, 0.7F, 0.1F);
								poseStack.mulPose(Axis.XP.rotationDegrees(-55.0F));
								poseStack.mulPose(Axis.YP.rotationDegrees((float)k * 35.3F));
								poseStack.mulPose(Axis.ZP.rotationDegrees((float)k * -9.785F));
								float f7 = (float)stack.getUseDuration(entity) - ((float)entity.getUseItemRemainingTicks() - partialTicks + 1.0F);
								float f11 = f7 / 10.0F;
								if (f11 > 1.0F) {
									f11 = 1.0F;
								}

								if (f11 > 0.1F) {
									float f14 = Mth.sin((f7 - 0.1F) * 1.3F);
									float f17 = f11 - 0.1F;
									float f19 = f14 * f17;
									poseStack.translate(f19 * 0.0F, f19 * 0.004F, f19 * 0.0F);
								}

								poseStack.translate(0.0F, 0.0F, f11 * 0.2F);
								poseStack.scale(1.0F, 1.0F, 1.0F + f11 * 0.2F);
								poseStack.mulPose(Axis.YN.rotationDegrees((float)k * 45.0F));
								break;
							case BRUSH:
								applyBrushTransform(poseStack, partialTicks, handSide, stack, entity, equippedProgress);
								break;
							default: break;
						}
					} else if (entity.isAutoSpinAttack()) {
						applyItemArmTransform(poseStack, handSide, equippedProgress);
						int j = rightHand ? 1 : -1;
						poseStack.translate((float)j * -0.4F, 0.8F, 0.3F);
						poseStack.mulPose(Axis.YP.rotationDegrees((float)j * 65.0F));
						poseStack.mulPose(Axis.ZP.rotationDegrees((float)j * -85.0F));
					} else {
						float f5 = -0.4F * Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);
						float f6 = 0.2F * Mth.sin(Mth.sqrt(swingProgress) * (float) (Math.PI * 2));
						float f10 = -0.2F * Mth.sin(swingProgress * (float) Math.PI);
						int l = rightHand ? 1 : -1;
						poseStack.translate((float)l * f5, f6, f10);
						applyItemArmTransform(poseStack, handSide, equippedProgress);
						applyItemArmAttackTransform(poseStack, handSide, swingProgress);
					}
				}

				mc.getEntityRenderDispatcher().getItemInHandRenderer().renderItem(
						entity, stack, rightHand ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
								!rightHand, poseStack, buffer, light);
			}

			poseStack.popPose();
		}
	}

	public static void renderEntityArm(LivingEntityRenderer renderer, LivingEntity entity, 
			PoseStack poseStack, MultiBufferSource buffer, int light, float equippedProgress, float swingProgress, HumanoidArm handSide) {
		float f = handSide == HumanoidArm.RIGHT ? 1.0F : -1.0F;
		float f1 = Mth.sqrt(swingProgress);
		float f2 = -0.3F * Mth.sin(f1 * (float) Math.PI);
		float f3 = 0.4F * Mth.sin(f1 * (float) (Math.PI * 2));
		float f4 = -0.4F * Mth.sin(swingProgress * (float) Math.PI);
		poseStack.translate(f * (f2 + 0.64000005F), f3 + -0.6F + equippedProgress * -0.6F, f4 + -0.71999997F);
		poseStack.mulPose(Axis.YP.rotationDegrees(f * 45.0F));
		float f5 = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
		float f6 = Mth.sin(f1 * (float) Math.PI);
		poseStack.mulPose(Axis.YP.rotationDegrees(f * f6 * 70.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(f * f5 * -20.0F));
		poseStack.translate(f * -1.0F, 3.6F, 3.5F);
		poseStack.mulPose(Axis.ZP.rotationDegrees(f * 120.0F));
		poseStack.mulPose(Axis.XP.rotationDegrees(200.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(f * -135.0F));
		poseStack.translate(f * 5.6F, 0.0F, 0.0F);
		renderHand(renderer, entity, poseStack, buffer, light, handSide, entity.isInvisible());
	}

	public static void renderMapArm(LivingEntityRenderer renderer, LivingEntity entity, 
			PoseStack poseStack, MultiBufferSource buffer, int light, HumanoidArm handSide) {
		poseStack.pushPose();
		float f = handSide == HumanoidArm.RIGHT ? 1.0F : -1.0F;
		poseStack.mulPose(Axis.YP.rotationDegrees(92.0F));
		poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(f * -41.0F));
		poseStack.translate(f * 0.3F, -1.1F, 0.45F);
		renderHand(renderer, entity, poseStack, buffer, light, handSide, entity.isInvisible());

		poseStack.popPose();
	}

	public static void renderHand(LivingEntityRenderer renderer, LivingEntity entity, PoseStack poseStack, 
			MultiBufferSource buffer, int light, HumanoidArm handSide, boolean isInvisible) {
		if (!isInvisible && entity instanceof AbstractClientPlayer player && ClientHooks.renderSpecificFirstPersonArm(poseStack, buffer, light, player, handSide)) return;

		if (renderer.getModel() instanceof HumanoidModel humanoidModel) {
			HumanoidModel.ArmPose mainArmPose = getArmPose(entity, InteractionHand.MAIN_HAND);
			HumanoidModel.ArmPose offArmPose = getArmPose(entity, InteractionHand.OFF_HAND);
			if (mainArmPose.isTwoHanded()) {
				offArmPose = entity.getOffhandItem().isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
			}

			if (entity.getMainArm() == HumanoidArm.RIGHT) {
				humanoidModel.rightArmPose = mainArmPose;
				humanoidModel.leftArmPose = offArmPose;
			} else {
				humanoidModel.rightArmPose = offArmPose;
				humanoidModel.leftArmPose = mainArmPose;
			}

			humanoidModel.attackTime = 0.0F;
			humanoidModel.crouching = false;
			humanoidModel.swimAmount = 0.0F;
			humanoidModel.setupAnim(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
			ResourceLocation texture = renderer.getTextureLocation(entity);
			if (!isInvisible) {
				ModelPart rendererArm = switch (handSide) {
					case LEFT -> humanoidModel.leftArm;
					case RIGHT -> humanoidModel.rightArm;
				};
				rendererArm.visible = true;
				rendererArm.xRot = 0.0F;
				rendererArm.render(poseStack, buffer.getBuffer(RenderType.entitySolid(texture)), light, OverlayTexture.NO_OVERLAY);
				if (humanoidModel instanceof PlayerModel playerModel) {
					ModelPart rendererArmwear = switch (handSide) {
						case LEFT -> playerModel.leftSleeve;
						case RIGHT -> playerModel.rightSleeve;
					};
					playerModel.leftSleeve.visible = entity instanceof Player player ? player.isModelPartShown(
							handSide == HumanoidArm.LEFT ? PlayerModelPart.LEFT_SLEEVE : PlayerModelPart.RIGHT_SLEEVE) : true;
					rendererArmwear.xRot = 0.0F;
					rendererArmwear.render(poseStack, buffer.getBuffer(RenderType.entityTranslucent(texture)), light, OverlayTexture.NO_OVERLAY);
				}
			}
			
			renderLayers(renderer, entity, poseStack, buffer, light, handSide);
		}
	}
	
	public static void renderLayers(LivingEntityRenderer renderer, LivingEntity entity, PoseStack poseStack, 
			MultiBufferSource buffer, int light, HumanoidArm handSide) {
		List<FirstPersonModelLayer> layers = ((LivingLayersAccess) renderer).jojo_ripples$firstPersonHandLayers();
		for (FirstPersonModelLayer layer : layers) {
			layer.renderHandFirstPerson(handSide, poseStack, buffer, light, entity, renderer);
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

	public static void applyEatTransform(PoseStack poseStack, float partialTick, HumanoidArm arm, ItemStack stack, LivingEntity entity) {
		float f = (float)entity.getUseItemRemainingTicks() - partialTick + 1.0F;
		float f1 = f / (float)stack.getUseDuration(entity);
		if (f1 < 0.8F) {
			float f2 = Mth.abs(Mth.cos(f / 4.0F * (float) Math.PI) * 0.1F);
			poseStack.translate(0.0F, f2, 0.0F);
		}

		float f3 = 1.0F - (float)Math.pow((double)f1, 27.0);
		int i = arm == HumanoidArm.RIGHT ? 1 : -1;
		poseStack.translate(f3 * 0.6F * (float)i, f3 * -0.5F, f3 * 0.0F);
		poseStack.mulPose(Axis.YP.rotationDegrees((float)i * f3 * 90.0F));
		poseStack.mulPose(Axis.XP.rotationDegrees(f3 * 10.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees((float)i * f3 * 30.0F));
	}

	public static void applyBrushTransform(PoseStack poseStack, float partialTick, HumanoidArm arm, ItemStack stack, LivingEntity entity, float equippedProgress) {
		applyItemArmTransform(poseStack, arm, equippedProgress);
		float f = (float)(entity.getUseItemRemainingTicks() % 10);
		float f1 = f - partialTick + 1.0F;
		float f2 = 1.0F - f1 / 10.0F;
		float f7 = -15.0F + 75.0F * Mth.cos(f2 * 2.0F * (float) Math.PI);
		if (arm != HumanoidArm.RIGHT) {
			poseStack.translate(0.1, 0.83, 0.35);
			poseStack.mulPose(Axis.XP.rotationDegrees(-80.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
			poseStack.mulPose(Axis.XP.rotationDegrees(f7));
			poseStack.translate(-0.3, 0.22, 0.35);
		} else {
			poseStack.translate(-0.25, 0.22, 0.35);
			poseStack.mulPose(Axis.XP.rotationDegrees(-80.0F));
			poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
			poseStack.mulPose(Axis.ZP.rotationDegrees(0.0F));
			poseStack.mulPose(Axis.XP.rotationDegrees(f7));
		}
	}

	public static void applyItemArmAttackTransform(PoseStack poseStack, HumanoidArm hand, float swingProgress) {
		int i = hand == HumanoidArm.RIGHT ? 1 : -1;
		float f = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
		poseStack.mulPose(Axis.YP.rotationDegrees((float)i * (45.0F + f * -20.0F)));
		float f1 = Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);
		poseStack.mulPose(Axis.ZP.rotationDegrees((float)i * f1 * -20.0F));
		poseStack.mulPose(Axis.XP.rotationDegrees(f1 * -80.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees((float)i * -45.0F));
	}

	public static void applyItemArmTransform(PoseStack poseStack, HumanoidArm hand, float equippedProg) {
		int i = hand == HumanoidArm.RIGHT ? 1 : -1;
		poseStack.translate((float)i * 0.56F, -0.52F + equippedProg * -0.6F, -0.72F);
	}

	public static void renderOneHandedMap(LivingEntity entity, PoseStack poseStack, MultiBufferSource buffer, 
			int packedLight, float equippedProgress, HumanoidArm hand, float swingProgress, ItemStack stack) {
		float f = hand == HumanoidArm.RIGHT ? 1.0F : -1.0F;
		poseStack.translate(f * 0.125F, -0.125F, 0.0F);
//		if (!entity.isInvisible()) {
			poseStack.pushPose();
			poseStack.mulPose(Axis.ZP.rotationDegrees(f * 10.0F));
			renderEntityArm(getLivingRenderer(entity), entity, poseStack, buffer, packedLight, equippedProgress, swingProgress, hand);
			poseStack.popPose();
//		}

		poseStack.pushPose();
		poseStack.translate(f * 0.51F, -0.08F + equippedProgress * -1.2F, -0.75F);
		float f1 = Mth.sqrt(swingProgress);
		float f2 = Mth.sin(f1 * (float) Math.PI);
		float f3 = -0.5F * f2;
		float f4 = 0.4F * Mth.sin(f1 * (float) (Math.PI * 2));
		float f5 = -0.3F * Mth.sin(swingProgress * (float) Math.PI);
		poseStack.translate(f * f3, f4 - 0.3F * f2, f5);
		poseStack.mulPose(Axis.XP.rotationDegrees(f2 * -45.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(f * f2 * -30.0F));
		renderMap(poseStack, buffer, packedLight, stack);
		poseStack.popPose();
	}

	public static void renderTwoHandedMap(LivingEntity entity, PoseStack poseStack, MultiBufferSource buffer, 
			int packedLight, float pitch, float equippedProgress, float swingProgress, ItemStack stack) {
		LivingEntityRenderer renderer = getLivingRenderer(entity);
		if (renderer == null) return;
		
		float f = Mth.sqrt(swingProgress);
		float f1 = -0.2F * Mth.sin(swingProgress * (float) Math.PI);
		float f2 = -0.4F * Mth.sin(f * (float) Math.PI);
		poseStack.translate(0.0F, -f1 / 2.0F, f2);
		float f3 = calculateMapTilt(pitch);
		poseStack.translate(0.0F, 0.04F + equippedProgress * -1.2F + f3 * -0.5F, -0.72F);
		poseStack.mulPose(Axis.XP.rotationDegrees(f3 * -85.0F));
//		if (!entity.isInvisible()) {
			poseStack.pushPose();
			poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
			renderMapArm(renderer, entity, poseStack, buffer, packedLight, HumanoidArm.RIGHT);
			renderMapArm(renderer, entity, poseStack, buffer, packedLight, HumanoidArm.LEFT);
			poseStack.popPose();
//		}

		float f4 = Mth.sin(f * (float) Math.PI);
		poseStack.mulPose(Axis.XP.rotationDegrees(f4 * 20.0F));
		poseStack.scale(2.0F, 2.0F, 2.0F);
		renderMap(poseStack, buffer, packedLight, stack);
	}

	public static float calculateMapTilt(float pitch) {
		float f = 1.0F - pitch / 45.0F + 0.1F;
		f = Mth.clamp(f, 0.0F, 1.0F);
		return -Mth.cos(f * (float) Math.PI) * 0.5F + 0.5F;
	}

	public static final RenderType MAP_BACKGROUND = RenderType.text(ResourceLocation.withDefaultNamespace("textures/map/map_background.png"));
	public static final RenderType MAP_BACKGROUND_CHECKERBOARD = RenderType.text(
			ResourceLocation.withDefaultNamespace("textures/map/map_background_checkerboard.png"));
	public static void renderMap(PoseStack poseStack, MultiBufferSource buffer, int packedLight, ItemStack stack) {
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
		poseStack.scale(0.38F, 0.38F, 0.38F);
		poseStack.translate(-0.5F, -0.5F, 0.0F);
		poseStack.scale(0.0078125F, 0.0078125F, 0.0078125F);
		MapId mapid = stack.get(DataComponents.MAP_ID);
		MapItemSavedData mapitemsaveddata = MapItem.getSavedData(stack, instance.mc.level);
		VertexConsumer vertexconsumer = buffer.getBuffer(mapitemsaveddata == null ? MAP_BACKGROUND : MAP_BACKGROUND_CHECKERBOARD);
		Matrix4f matrix4f = poseStack.last().pose();
		vertexconsumer.addVertex(matrix4f, -7.0F, 135.0F, 0.0F).setColor(-1).setUv(0.0F, 1.0F).setLight(packedLight);
		vertexconsumer.addVertex(matrix4f, 135.0F, 135.0F, 0.0F).setColor(-1).setUv(1.0F, 1.0F).setLight(packedLight);
		vertexconsumer.addVertex(matrix4f, 135.0F, -7.0F, 0.0F).setColor(-1).setUv(1.0F, 0.0F).setLight(packedLight);
		vertexconsumer.addVertex(matrix4f, -7.0F, -7.0F, 0.0F).setColor(-1).setUv(0.0F, 0.0F).setLight(packedLight);
		if (mapitemsaveddata != null) {
			instance.mc.gameRenderer.getMapRenderer().render(poseStack, buffer, mapid, mapitemsaveddata, false, packedLight);
		}
	}

	@Nullable
	public static LivingEntityRenderer getLivingRenderer(LivingEntity entity) {
		return instance.mc.getEntityRenderDispatcher().getRenderer(entity) instanceof LivingEntityRenderer __ ? __ : null;
	}
	
	
	public static void renderOnEvent(RenderHandEvent event, LivingEntity entity) {
		LivingEntityRenderer renderer = getLivingRenderer(entity);
		if (renderer == null) return;
		
		InteractionHand hand = event.getHand();
		PoseStack poseStack = event.getPoseStack();
		MultiBufferSource bufferSource = event.getMultiBufferSource();
		int light = event.getPackedLight();
		float equipProgress = event.getEquipProgress();
		float swingProgress = event.getSwingProgress();
		
		poseStack.pushPose();
		FirstPersonRender.renderEntityArm(renderer, entity, 
				poseStack, bufferSource, light, equipProgress, swingProgress, 
				UtilFunctions.getHandSide(entity, hand));
		poseStack.popPose();
	}
}
