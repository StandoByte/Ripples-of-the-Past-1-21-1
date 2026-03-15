package com.github.standobyte.jojo.client.entityrender.entities;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ModEntityTypeRenderers;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.clothes.mannequin.MannequinEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;

public class MannequinRenderer extends LivingEntityRenderer<MannequinEntity, MannequinModel> {
	public static final ResourceLocation DEFAULT_TEXTURE = JojoMod.resLoc("textures/entity/mannequin.png");
	public static final ResourceLocation STEVE_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");
	public static final ResourceLocation ALEX_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/player/slim/alex.png");
	private final MannequinModel mannequinModel;
	private final MannequinModel mannequinModelSlim;

    public MannequinRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new MannequinModel(ctx.bakeLayer(ModEntityTypeRenderers.MANNEQUIN)), 0.0F);
        this.mannequinModel = this.getModel();
		this.mannequinModelSlim = new MannequinModel(ctx.bakeLayer(ModEntityTypeRenderers.MANNEQUIN_SLIM));
        this.addLayer(
            new HumanoidArmorLayer<>(
                this,
                new MannequinModel(ctx.bakeLayer(ModelLayers.ARMOR_STAND_INNER_ARMOR)),
                new MannequinModel(ctx.bakeLayer(ModelLayers.ARMOR_STAND_OUTER_ARMOR)),
                ctx.getModelManager()
            )
        );
        this.addLayer(new ItemInHandLayer<>(this, ctx.getItemInHandRenderer()));
        this.addLayer(new ElytraLayer<>(this, ctx.getModelSet()));
        this.addLayer(new CustomHeadLayer<>(this, ctx.getModelSet(), ctx.getItemInHandRenderer()));
    }

	@Override
    public ResourceLocation getTextureLocation(MannequinEntity entity) {
		ItemStack headItem = entity.getItemBySlot(EquipmentSlot.HEAD);
		boolean hasSkull = headItem.is(Items.PLAYER_HEAD);
		ResolvableProfile playerProfile;
		if (hasSkull) {
			playerProfile = !headItem.isEmpty() ? headItem.get(DataComponents.PROFILE) : null;
		}
		else {
			playerProfile = null;
		}
		
		if (playerProfile != null) {
			ResourceLocation texture = Minecraft.getInstance().getSkinManager().getInsecureSkin(playerProfile.gameProfile()).texture();
			return texture;
		}
		if (hasSkull) {
			return entity.isSlim() ? ALEX_TEXTURE : STEVE_TEXTURE;
		}
		return DEFAULT_TEXTURE;
    }

	@Override
	public void render(MannequinEntity entity, float entityYaw, float partialTick, PoseStack matrixStack, MultiBufferSource bufferSource, int light) {
		this.model = entity.isSlim() ? mannequinModelSlim : mannequinModel;
		super.render(entity, entityYaw, partialTick, matrixStack, bufferSource, light);
	}

	@Override
    protected void setupRotations(MannequinEntity entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale) {
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yBodyRot));
        float f = (float)(entity.level().getGameTime() - entity.lastHit) + partialTick;
        if (f < 5.0F) {
            poseStack.mulPose(Axis.YP.rotationDegrees(Mth.sin(f / 1.5F * (float) Math.PI) * 3.0F));
        }
    }

	@Override
    protected boolean shouldShowName(MannequinEntity entity) {
        double d0 = this.entityRenderDispatcher.distanceToSqr(entity);
        float f = entity.isCrouching() ? 32.0F : 64.0F;
        return d0 >= (double)(f * f) ? false : entity.isCustomNameVisible();
    }

    @Nullable
	@Override
    protected RenderType getRenderType(MannequinEntity livingEntity, boolean bodyVisible, boolean translucent, boolean glowing) {
        if (!livingEntity.isMarker()) {
            return super.getRenderType(livingEntity, bodyVisible, translucent, glowing);
        } else {
            ResourceLocation resourcelocation = this.getTextureLocation(livingEntity);
            if (translucent) {
                return RenderType.entityTranslucent(resourcelocation, false);
            } else {
                return bodyVisible ? RenderType.entityCutoutNoCull(resourcelocation, false) : null;
            }
        }
    }
}
