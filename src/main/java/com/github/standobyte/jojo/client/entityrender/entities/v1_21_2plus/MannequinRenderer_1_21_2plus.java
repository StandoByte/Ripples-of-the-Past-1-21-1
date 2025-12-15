package com.github.standobyte.jojo.client.entityrender.entities.v1_21_2plus;

//import javax.annotation.Nullable;
//
//import com.github.standobyte.jojo.client.entityrender.ModEntityRenderers;
//import com.github.standobyte.jojo.core.JojoMod;
//import com.github.standobyte.jojo.mechanics.clothes.mannequin.MannequinEntity;
//import com.mojang.blaze3d.vertex.PoseStack;
//import com.mojang.math.Axis;
//
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.model.geom.ModelLayers;
//import net.minecraft.client.renderer.MultiBufferSource;
//import net.minecraft.client.renderer.RenderType;
//import net.minecraft.client.renderer.entity.EntityRendererProvider;
//import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
//import net.minecraft.client.renderer.entity.LivingEntityRenderer;
//import net.minecraft.client.renderer.entity.layers.ElytraLayer;
//import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
//import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
//import net.minecraft.client.renderer.entity.layers.WingsLayer;
//import net.minecraft.core.component.DataComponents;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.util.Mth;
//import net.minecraft.world.entity.EquipmentSlot;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.Items;

public class MannequinRenderer_1_21_2plus /*extends LivingEntityRenderer<MannequinEntity, MannequinModel_1_21_2plus>*/ {
//	public static final ResourceLocation DEFAULT_TEXTURE = JojoMod.resLoc("textures/entity/mannequin.png");
//	public static final ResourceLocation STEVE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");
//	public static final ResourceLocation ALEX_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/player/slim/alex.png");
//	private final MannequinModel_1_21_2plus bigModel;
//	private final MannequinModel_1_21_2plus bigModelSlim;
//	private final MannequinModel_1_21_2plus smallModel;
//	private final MannequinModel_1_21_2plus smallModelSlim;
//
//	public MannequinRenderer_1_21_2plus(EntityRendererProvider.Context ctx) {
//		super(ctx, new MannequinModel_1_21_2plus(ctx.bakeLayer(ModEntityRenderers.MANNEQUIN)), 0.0F);
//		this.bigModel = this.getModel();
//		this.bigModelSlim = new MannequinModel_1_21_2plus(ctx.bakeLayer(ModEntityRenderers.MANNEQUIN_SLIM));
//		this.smallModel = new MannequinModel_1_21_2plus(ctx.bakeLayer(ModEntityRenderers.MANNEQUIN_SMALL));
//		this.smallModelSlim = new MannequinModel_1_21_2plus(ctx.bakeLayer(ModEntityRenderers.MANNEQUIN_SLIM_SMALL));
//		// XXX (mannequin) switch between wide and slim layer variants
//		this.addLayer(
//			new HumanoidArmorLayer<>(
//				this,
//				new MannequinModel_1_21_2plus(ctx.bakeLayer(ModelLayers.ARMOR_STAND_INNER_ARMOR)),
//				new MannequinModel_1_21_2plus(ctx.bakeLayer(ModelLayers.ARMOR_STAND_OUTER_ARMOR)),
//				new MannequinModel_1_21_2plus(ctx.bakeLayer(ModelLayers.ARMOR_STAND_SMALL_INNER_ARMOR)),
//				new MannequinModel_1_21_2plus(ctx.bakeLayer(ModelLayers.ARMOR_STAND_SMALL_OUTER_ARMOR)),
//				ctx.getEquipmentRenderer()
//			)
//		);
////		this.addLayer(new ItemInHandLayer<>(this));
////		this.addLayer(new WingsLayer<>(this, ctx.getModelSet(), ctx.getEquipmentRenderer()));
//		this.addLayer(new ItemInHandLayer<>(this, ctx.getItemInHandRenderer()));
//		this.addLayer(new ElytraLayer<>(this, ctx.getModelSet()));
//	}
//
//	@Override
//	public ResourceLocation getTextureLocation(MannequinRenderState_1_21_2plus renderState) {
//		if (renderState.playerProfile != null) {
//			ResourceLocation texture = Minecraft.getInstance().getSkinManager().getInsecureSkin(renderState.playerProfile.gameProfile()).texture();
//			return texture;
//		}
//		if (renderState.hasSkull) {
//			return renderState.isSlim ? ALEX_TEXTURE : STEVE_TEXTURE;
//		}
//		return DEFAULT_TEXTURE;
//	}
//
//	@Override
//	public MannequinRenderState_1_21_2plus createRenderState() {
//		return new MannequinRenderState_1_21_2plus();
//	}
//
//	@Override
//	public void extractRenderState(MannequinEntity entity, MannequinRenderState_1_21_2plus renderState, float partialTick) {
//		super.extractRenderState(entity, renderState, partialTick);
//		HumanoidMobRenderer.extractHumanoidRenderState(entity, renderState, partialTick, this.itemModelResolver);
//		renderState.yRot = Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot());
//		renderState.isMarker = entity.isMarker();
//		renderState.isSmall = entity.isSmall();
//		renderState.showArms = entity.showArms();
//		renderState.showBasePlate = entity.showBasePlate();
//		renderState.bodyPose = entity.getBodyPose();
//		renderState.headPose = entity.getHeadPose();
//		renderState.leftArmPose = entity.getLeftArmPose();
//		renderState.rightArmPose = entity.getRightArmPose();
//		renderState.leftLegPose = entity.getLeftLegPose();
//		renderState.rightLegPose = entity.getRightLegPose();
//		renderState.wiggle = (float)(entity.level().getGameTime() - entity.lastHit) + partialTick;
//		
//		renderState.isSlim = entity.isSlim();
//		ItemStack headItem = entity.getItemBySlot(EquipmentSlot.HEAD);
//		renderState.hasSkull = headItem.is(Items.PLAYER_HEAD);
//		// XXX (mannequin) don't render the player head item itself
//		if (renderState.hasSkull) {
//			renderState.playerProfile = !headItem.isEmpty() ? headItem.get(DataComponents.PROFILE) : null;
//		}
//		else {
//			renderState.playerProfile = null;
//		}
//	}
//
//	@Override
//	public void render(MannequinRenderState_1_21_2plus renderState, PoseStack matrixStack, MultiBufferSource bufferSource, int light) {
//		if (renderState.isSlim) this.model = renderState.isSmall ? this.smallModelSlim : this.bigModelSlim;
//		else 					this.model = renderState.isSmall ? this.smallModel : this.bigModel;
//		super.render(renderState, matrixStack, bufferSource, light);
//	}
//
//	@Override
//	protected void setupRotations(MannequinRenderState_1_21_2plus renderState, PoseStack matrixStack, float bodyRot, float scale) {
//		matrixStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyRot));
//		if (renderState.wiggle < 5.0F) {
//			matrixStack.mulPose(Axis.YP.rotationDegrees(Mth.sin(renderState.wiggle / 1.5F * (float) Math.PI) * 3.0F));
//		}
//	}
//
//	@Override
//	protected boolean shouldShowName(MannequinEntity entity, double distSqr) {
//		return entity.isCustomNameVisible();
//	}
//
//	@Nullable
//	@Override
//	protected RenderType getRenderType(MannequinRenderState_1_21_2plus renderState, boolean isVisible, boolean renderTranslucent, boolean appearsGlowing) {
//		if (!renderState.isMarker) {
//			return super.getRenderType(renderState, isVisible, renderTranslucent, appearsGlowing);
//		} else {
//			ResourceLocation resourcelocation = this.getTextureLocation(renderState);
//			if (renderTranslucent) {
//				return RenderType.entityTranslucent(resourcelocation, false);
//			} else {
//				return isVisible ? RenderType.entityCutoutNoCull(resourcelocation, false) : null;
//			}
//		}
//	}
}
