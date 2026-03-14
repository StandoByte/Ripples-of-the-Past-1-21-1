package com.github.standobyte.jojo.client.ui.hud.marker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.github.standobyte.core_subsystems.entitydata.EntityAttachmentType;
import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.rendertype.CustomMultiBufferSource;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.effect.StandEffectInstance;
import com.github.standobyte.jojo.powersystem.standpower.effect.UserStandEffects;
import com.github.standobyte.jojo.util.MathUtil;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public abstract class MarkerRenderer {
	@Nullable protected GuiIcon icon;
	@Nullable protected String iconAbilityName;
	protected TextureAtlasSprite _abilityIconSprite;
	private final List<MarkerInstance> positions = new ArrayList<>();
	// somewhere between 1.21.2 and 1.21.4, the markers stop rendering through blocks except in fabulous mode
	protected boolean renderThroughBlocks = true;
	protected final Minecraft mc;
	
	protected int color = FastColor.ARGB32.colorFromFloat(1.0f, 1.0f, 1.0f, 1.0f);
	protected boolean useStandSkinColor = false;
	protected static int _curStandSkinColor = -1;
	
	public static void registerMarkerRenderer(MarkerRenderer markerRenderer) {
		MarkerRenderer.Handler.RENDERERS.add(markerRenderer);
	}
	

	public MarkerRenderer(int color, ResourceLocation iconTexture, Minecraft mc) {
		this(iconTexture, mc);
		this.color = color;
	}

	public MarkerRenderer(ResourceLocation iconTexture, Minecraft mc) {
		this(iconTexture != null ? new GuiIcon(iconTexture, 16, 16) : null, null, mc);
	}

	public MarkerRenderer(String iconAbilityName, Minecraft mc) {
		this(null, iconAbilityName, mc);
	}

	public MarkerRenderer(GuiIcon icon, String iconAbilityName, Minecraft mc) {
		this.icon = icon;
		this.iconAbilityName = iconAbilityName;
		this.mc = mc;
	}
	

	protected void render(PoseStack poseStack, Camera camera, float partialTick, StandSkin standSkin) {
		if (shouldRender()) {
			positions.clear();
			updatePositions(positions, partialTick);
			if (!positions.isEmpty()) {
				poseStack.pushPose();
				poseStack.mulPose(camera.rotation());
				poseStack.mulPose(Axis.ZP.rotationDegrees(camera.getRoll()));
				poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
				poseStack.mulPose(Axis.YP.rotationDegrees(camera.getYRot()));
				
				int color = getColor();
				_abilityIconSprite = null;
				if (iconAbilityName != null) {
					_abilityIconSprite = StandSkinsLoader.getInstance().abilityIcons.getAbilityIcon(iconAbilityName, standSkin);
				}

				positions.forEach(marker -> {
					if (renderThroughBlocks) {
						RenderSystem.disableDepthTest();
					} else {
						RenderSystem.enableDepthTest();
					}
					Vec3 diff = marker.pos.subtract(camera.getPosition())
							.yRot(camera.getYRot() * MathUtil.DEG_TO_RAD)
							.xRot(camera.getXRot() * MathUtil.DEG_TO_RAD)
							.zRot(camera.getRoll() * MathUtil.DEG_TO_RAD);
					renderAt(poseStack, marker, camera, diff, partialTick, standSkin, color);
				});
				RenderSystem.enableDepthTest();

				poseStack.popPose();
			}
		}
	}

	protected void renderAt(PoseStack poseStack, MarkerInstance marker, Camera camera, 
			Vec3 diff, float partialTick, StandSkin standSkin, int color) {
		poseStack.pushPose();

		double distance = diff.length();
		if (distance > 256) return;

		float scale = Math.min((float) Math.pow(2, (16 - Math.min(distance, 32)) / 16) * (float) distance / 256, 1);

		poseStack.translate(diff.x, diff.y, diff.z);
		poseStack.scale(-scale, -scale, 1);
		poseStack.scale(0.8f, 0.8f, 0.8f);

		poseStack.pushPose();
		poseStack.translate(-8, -28, 0);
		renderIcon(poseStack, marker, partialTick, standSkin);
		poseStack.popPose();
		renderBorder(poseStack, marker, partialTick, color);

		poseStack.pushPose();
		poseStack.translate(-8, -28, 0);
		RenderSystem.disableDepthTest();
		renderIconOnBorder(poseStack, marker, partialTick);
		RenderSystem.enableDepthTest();
		poseStack.popPose();

		poseStack.popPose();
	}

	protected void renderIcon(PoseStack poseStack, MarkerInstance marker, float partialTick, StandSkin standSkin) {
		if (this._abilityIconSprite != null) {
			BlitFloat.blit(poseStack, mc, _abilityIconSprite, 0, 0, 16, 16, 0, BlitFloat.NO_TINT);
		}
		else if (this.icon != null) {
			this.icon.render(poseStack, 0, 0);
		}
	}

	public static MultiBufferSource.BufferSource noDepthBuffers = null;
	public void renderItem(PoseStack poseStack, ItemStack item, float partialTick) {
		Minecraft mc = Minecraft.getInstance();
		MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
		
		BakedModel bakedmodel = mc.getItemRenderer().getModel(item, mc.level, null, 0);
		poseStack.pushPose();
		poseStack.translate(8, 8, 0);
		poseStack.scale(16, 16, 0.0625f);
		poseStack.scale(1, -1, -1);

		boolean flag = !bakedmodel.usesBlockLight();
		if (flag) {
			Lighting.setupForFlatItems();
		}
		if (renderThroughBlocks) {
			RenderSystem.disableDepthTest();
			if (noDepthBuffers == null) {
				RenderStateShard.OutputStateShard targetShard = new RenderStateShard.OutputStateShard(
						"item_no_depth", 
						() -> RenderSystem.disableDepthTest(), 
						() -> RenderSystem.enableDepthTest());
				noDepthBuffers = CustomMultiBufferSource.create(mc, targetShard);
			}
			bufferSource = noDepthBuffers;
		}

		mc.getItemRenderer().render(item, ItemDisplayContext.GUI, false, poseStack, bufferSource, 
				ClientUtil.MAX_LIGHT, OverlayTexture.NO_OVERLAY, bakedmodel);
		bufferSource.endBatch();
		
		if (!renderThroughBlocks) {
			RenderSystem.enableDepthTest();
		}
		if (flag) {
			if (mc.level.effects().constantAmbientLight()) {
				Lighting.setupNetherLevel();
			} else {
				Lighting.setupLevel();
			}
		}
		poseStack.popPose();
	}

	public static final GuiIcon MARKER_BORDER = new GuiIcon(JojoMod.resLoc("textures/hud/marker.png"), 32, 32);
	public static final GuiIcon MARKER_BORDER_OUTLINE = new GuiIcon(JojoMod.resLoc("textures/hud/marker_highlight.png"), 32, 32);

	protected void renderBorder(PoseStack poseStack, MarkerInstance marker, float partialTick, int color) {
        MARKER_BORDER.render(poseStack, -16, -32, color);
        if (marker.outlined) {
        	MARKER_BORDER_OUTLINE.render(poseStack, -16, -32);
        }
	}

	protected void renderIconOnBorder(PoseStack poseStack, MarkerInstance marker, float partialTick) {}

	protected abstract boolean shouldRender();
	protected abstract void updatePositions(List<MarkerInstance> list, float partialTick);

	protected static void fillWithStandEffectTargets(List<MarkerInstance> list, float partialTick, 
			EntityAttachmentType<? extends StandEffectInstance> standEffect, double range, Minecraft mc, boolean highlightLookedAt) {
		StandPower stand = ClientPowerCache.getPower(PowerClass.STAND);
		if (stand != null) {
			List<StandEffectInstance> targets = UserStandEffects.getEffectsInRange(stand, standEffect, range, mc.player).collect(Collectors.toList());
			Optional<StandEffectInstance> outlined = highlightLookedAt ? UserStandEffects.getTargetLookedAt(targets.stream(), mc.player) : Optional.empty();
			targets.forEach(effect -> {
				Entity target = effect.getTarget();
				if (target != null) {
					list.add(new MarkerInstance(
							entityMarkerPos(target, partialTick), 
							highlightLookedAt && outlined.map(outlinedEffect -> effect == outlinedEffect).orElse(false),
							Optional.of(effect)));
				}
			});
		}
	}
	
	public static Vec3 entityMarkerPos(Entity entity, float partialTick) {
		Vec3 position;
		if (entity.level().isClientSide()) {
			position = entity.getPosition(partialTick);
		}
		else {
			position = entity.position();
		}
		return position.add(0, entity.getBbHeight() + 0.25, 0);
	}
	
	public static Vec3 blockMarkerPos(BlockPos blockPos) {
		return Vec3.upFromBottomCenterOf(blockPos, 1.0);
	}
	
	protected int getColor() {
		if (useStandSkinColor && _curStandSkinColor != -1) {
			return _curStandSkinColor;
		}
		return color;
	}

	@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
	public static class Handler {
		private static Collection<MarkerRenderer> RENDERERS = new ArrayList<>();

		@SubscribeEvent(priority = EventPriority.LOW)
		public static void renderMarkers(RenderLevelStageEvent event) {
			if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
				Minecraft mc = Minecraft.getInstance();
				if (!mc.options.hideGui) {
					RenderSystem.disableDepthTest();
					// XXX (marker) fabulous graphics fix
//					if (mc.options.graphicsMode().get() == GraphicsStatus.FABULOUS) { // it just works
//						RenderSystem.enableTexture();
//					}

					float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
					StandSkin standSkin = StandSkinsLoader.getCurSkin();
					_curStandSkinColor = standSkin != null ? standSkin.getColor() : -1;
					RENDERERS.forEach(marker -> marker.render(event.getPoseStack(), event.getCamera(), partialTick, standSkin));

					mc.renderBuffers().bufferSource().endBatch();
					RenderSystem.enableDepthTest();
				}
			}
		}
	}



	protected static class MarkerInstance {
		protected Vec3 pos;
		protected boolean outlined;
		protected final Optional<? extends StandEffectInstance> standEffect;

		public MarkerInstance(Vec3 pos) {
			this(pos, false);
		}

		public MarkerInstance(Vec3 pos, boolean outlined) {
			this(pos, outlined, Optional.empty());
		}

		public MarkerInstance(Vec3 pos, boolean outlined, Optional<? extends StandEffectInstance> standEffect) {
			this.pos = pos;
			this.outlined = outlined;
			this.standEffect = standEffect;
		}
	}
}
