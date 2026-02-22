package com.github.standobyte.jojo.client.entityrender.stand;

import java.util.List;
import java.util.function.Consumer;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.entityanim.AnimationSet;
import com.github.standobyte.jojo.client.entityanim.PreFrameEntityAnimCalc;
import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition;
import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition.AnimWithId;
import com.github.standobyte.jojo.client.entityanim.molang.AnimMolangQuery.AnimMolangVariables;
import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose;
import com.github.standobyte.jojo.client.entityrender.EntityActionRenderState;
import com.github.standobyte.jojo.client.entityrender.parsemodel.loader.RotpGeckoModelLoader;
import com.github.standobyte.jojo.client.shader.ModShaders;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.ui.jojomenu.StandInfoScreen;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.entityaction.ActionAnimIdentifier;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.util.java.LazyNullable;
import com.github.standobyte.v1_21_4_stuff.renderstate.ArmedEntityRenderState;
import com.github.standobyte.v1_21_4_stuff.renderstate.LivingEntityRenderState;
import com.github.standobyte.v1_21_4_stuff.renderstate.RenderStateCrutches;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class StandEntityRenderer<
				T extends StandEntity, 
				S extends StandEntityRenderState, 
				M extends StandEntityModel<T, S>> 
		extends LivingEntityRenderer<T, M> {
	@Deprecated(forRemoval = true)
	public final S reusedState = this.createRenderState();
	public final S outOfLevelRenderState = createRenderState();
	protected LazyNullable<M> missingSkinModel;

	public StandEntityRenderer(Context context) {
		this(context, 0);
	}

	public StandEntityRenderer(Context context, float shadowRadius) {
		super(context, null, shadowRadius);
		this.missingSkinModel = LazyNullable.of(() -> {
			LayerDefinition defaultModel = RotpGeckoModelLoader.getInstance().getModelDefinition(JojoMod.resLoc("stand_default"));
			return defaultModel != null ? createStandModel(defaultModel) : null;
		});
		this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
	}
	
	public final S createRenderState(T entity, float partialTick) {
		S s = this.reusedState;
		this.extractRenderState(entity, s, partialTick);
		return s;
	}

	/**
	 * If you extend StandEntityRenderer and put a sub-class of StandEntityRenderState as S, 
	 * don't forget to override this method too to actually create the new render state object.
	 */
	@SuppressWarnings("unchecked")
//	@Override // 1.21.2+
	public S createRenderState() {
		return (S) new StandEntityRenderState();
	}
	
	@SuppressWarnings("unchecked")
	public M createStandModel(LayerDefinition definition) {
		return (M) new StandEntityModel<>(definition.bakeRoot());
	}
	
	public static final ActionAnimIdentifier IDLE_ANIM = ActionAnimIdentifier.getOrCreate("idle", true);
	public static final ActionAnimIdentifier GRAB_IDLE_ANIM = ActionAnimIdentifier.getOrCreate("grab", true);
//	@Override // 1.21.2+
	public void extractRenderState(T entity, S renderState, float partialTick) {
//		super.extractRenderState(entity, renderState, partialTick); // 1.21.2+
		LivingEntityRenderState.extract(entity, renderState, this, entityRenderDispatcher, partialTick);
		ArmedEntityRenderState.extractArmedEntityRenderState(entity, renderState/*, this.itemModelResolver*/);
        StandEntityRenderState.extractStandRenderState(entity, renderState, partialTick);
		renderState.leftArmPose = HumanoidModel.ArmPose.EMPTY;
		renderState.rightArmPose = HumanoidModel.ArmPose.EMPTY;
		
		renderState.skin = getStandSkin(entity);
		renderState.visibleParts = HumanoidPart.ALL;
		EntityActionRenderState.extract(renderState.action, entity, partialTick);
		
		renderState.tint = -1;
		renderState.alpha = (float) entity.rangeEfficiency * entity.modelAlpha.lerp(partialTick);
		
		Minecraft mc = Minecraft.getInstance();
		renderState.mayObstructView = mc.options.getCameraType().isFirstPerson() && entity.isFollowingUser();
		if (renderState.mayObstructView) {
			Entity cameraEntity = mc.getCameraEntity();
			if (cameraEntity == null) cameraEntity = mc.player;
			renderState.mayObstructView &= cameraEntity != null && entity.getUser() == cameraEntity;
		}
	}
	
	public StandSkin getStandSkin(T entity) {
		return StandSkinsLoader.getInstance().getSkin(entity);
	}
	
	public void extractSkinMenuRenderState(S renderState, StandSkin skin, ResourceLocation standId, float ticks, int tint, MenuType menuType) {
		renderState.skin = skin;
		renderState.visibleParts = HumanoidPart.ALL;
		renderState.tint = tint;
		
		AnimFramePose pose = null;
		switch (menuType) {
			case STAND_SKINS -> {
				ActionAnimIdentifier animId = StandEntityRenderer.IDLE_ANIM;
				AnimWithId animWithId = PreFrameEntityAnimCalc.getStandAnim(skin, animId, StandEntityRenderer.IDLE_ANIM);
				RotpAnimDefinition anim = animWithId.anim;
				if (anim != null) {
					float seconds = anim.getAnimTime(ticks);
					AnimMolangVariables molangVars = AnimMolangVariables.set(0, 0, 0);
					pose = anim.calcAnimPose(molangVars, null, seconds, 1);
				}
			}
			case STAND_INFO -> {
				if (skin != null) {
					AnimationSet anims = skin.getAnimations();
					if (anims != null) {
						List<AnimFramePose> poses = anims.coolPoses;
						if (poses != null && !poses.isEmpty()) {
							pose = poses.get(StandInfoScreen.rand % poses.size());
						}
					}
				}
			}
		}
		renderState.action.pose = pose;
	}
	
	public enum MenuType {
		STAND_SKINS,
		STAND_INFO
	}
	
	
	protected static final ResourceLocation MISSING_TEXTURE = JojoMod.resLoc("textures/entity/stand_default.png");
//	@Override // 1.21.1+
	public ResourceLocation getTextureLocation(S renderState) {
		StandSkin standSkin = renderState.skin;
		ResourceLocation texture = standSkin != null ? standSkin.getStandTexture(MISSING_TEXTURE) : null;
		return texture != null ? texture : MISSING_TEXTURE;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ResourceLocation getTextureLocation(T entity) {
		if (RenderStateCrutches.currentEntityRenderState != null) {
			return getTextureLocation((S) RenderStateCrutches.currentEntityRenderState);
		}
		return MISSING_TEXTURE;
	}

//	@Override
//	protected int getModelTint(S renderState) {
//		return renderState.tint;
//	}

	protected M modelFrom(S renderState) {
		M model = getEntityModel(renderState);
		if (model == missingSkinModel.get() && renderState.skin != null) {
			renderState.tint = renderState.skin.getColor();
		}
		return model;
	}
	
	public M getEntityModel(T entity) {
		return getEntityModel(getStandSkin(entity));
	}
	
	public M getEntityModel(S renderState) {
		StandSkin standSkin = renderState.skin;
		return getEntityModel(standSkin);
	}
	
	public M getEntityModel(StandSkin standSkin) {
		M model = standSkin != null ? (M) standSkin.getStandModel(this) : null;
		if (model == null) {
			model = missingSkinModel.get();
		}
		return model;
	}
	
	
	public void renderForStandSkinUI(PoseStack poseStack, MultiBufferSource bufferSource, Consumer<S> extractRenderState) {
		S renderState = outOfLevelRenderState;
		extractRenderState.accept(renderState);
		preRender(renderState);

		M model = modelFrom(renderState);

		poseStack.pushPose();
		
		poseStack.translate(0, 1.5f, 0);
		poseStack.scale(1, -1, 1);
		poseStack.mulPose(Axis.YP.rotationDegrees(180));
		poseStack.scale(-1, 1, 1);
		
//		Optional<ResourceLocation> nonDefaultSkin = standSkin.getNonDefaultLocation();
		model.attackTime = 0;
		model.riding = false;
		model.young = false;

		model.setupAnim(renderState);

		ResourceLocation texture = getTextureLocation(renderState);
		RenderType renderType = model.renderType(texture);
		int packedLight = ClientUtil.MAX_LIGHT;
		if (renderType != null) {
			VertexConsumer vertexBuilder = bufferSource.getBuffer(renderType);
			int packedOverlay = OverlayTexture.NO_OVERLAY;
			model.renderToBuffer(poseStack, vertexBuilder, packedLight, packedOverlay, renderState.tint);

			for (RenderLayer<T, M> layerRenderer : this.layers) {
				if (layerRenderer instanceof StandModelLayerRenderer) {
					@SuppressWarnings("unchecked")
					StandModelLayerRenderer<T, S, M> standLayer = (StandModelLayerRenderer<T, S, M>) layerRenderer;
//					if (standLayer.shouldRender(null, nonDefaultSkin)) {
//						M layerModel = standLayer.getLayerModel(nonDefaultSkin);
//						RenderType layerRenderType = layerModel.renderType(standLayer.getLayerTexture(nonDefaultSkin));
//						VertexConsumer layerVertexBuilder = bufferSource.getBuffer(layerRenderType);
//						layerModel.attackTime = 0;
//						layerModel.riding = false;
//						layerModel.young = false;
//						layerModel.setupAnim(renderState);
//						layerModel.renderToBuffer(poseStack, layerVertexBuilder, packedLight, packedOverlay, renderState.tint);
//					}
				}
			}
		}

		poseStack.popPose();
		
		postRender();
	}
	
	// 1.21.2+
//	public void renderWithRenderState(Consumer<S> renderState, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
//		renderState.accept(outOfLevelRenderState);
//		render(outOfLevelRenderState, poseStack, bufferSource, light);
//	}
	
	
//	@Override // 1.21.2+
//	public void render(S renderState, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
//		setModelFrom(renderState);
//		if (this.model != null) {
//			super.render(renderState, poseStack, bufferSource, light);
//		}
//	}
	
	public void preRender(S renderState) {
		RenderStateCrutches.currentEntityRenderState = renderState;
		RenderStateCrutches.currentStandEntityRenderState = renderState;
	}
	
	public void postRender() {
		RenderStateCrutches.currentEntityRenderState = null;
		RenderStateCrutches.currentStandEntityRenderState = null;
	}

	@Override
	public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
		S s = this.createRenderState(entity, partialTicks);
		render(entity, s, entityYaw, partialTicks, poseStack, bufferSource, light);
	}

	public void render(T entity, S renderState, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
		preRender(renderState);

		this.model = modelFrom(renderState);
		
		if (renderState.mayObstructView) {
			ModShaders shaders = ModShaders.getInstance();
			if (shaders != null) {
				bufferSource = ModShaders.getInstance().firstPersonStandTranslucency.useBufferSource(bufferSource);
			}
		}
		
		if (this.model != null) {
			this.doRender(entity, entityYaw, partialTicks, poseStack, bufferSource, light);
		}
		postRender();
	}

    public void doRender(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
		super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, light);
    }

	@Override
	protected boolean shouldShowName(T entity/*, double distSqr*/) {
		return false;
	}

}
