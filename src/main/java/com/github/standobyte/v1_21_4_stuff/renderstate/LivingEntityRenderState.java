package com.github.standobyte.v1_21_4_stuff.renderstate;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;

public class LivingEntityRenderState extends EntityRenderState {
    public float bodyRot;
    public float yRot;
    public float xRot;
    public float deathTime;
    public float walkAnimationPos;
    public float walkAnimationSpeed;
    public float scale = 1.0F;
    public float ageScale = 1.0F;
    public boolean isUpsideDown;
    public boolean isFullyFrozen;
    public boolean isBaby;
    public boolean isInWater;
    public boolean isAutoSpinAttack;
    public boolean hasRedOverlay;
    public boolean isInvisibleToPlayer;
    public boolean appearsGlowing;
    @Nullable
    public Direction bedOrientation;
    @Nullable
    public Component customName;
    public Pose pose = Pose.STANDING;
    public ItemStack headItem = ItemStack.EMPTY;
    public float wornHeadAnimationPos;
    @Nullable
    public SkullBlock.Type wornHeadType;
    @Nullable
    public ResolvableProfile wornHeadProfile;

    public boolean hasPose(Pose pose) {
        return this.pose == pose;
    }
    
    public static void extract(LivingEntity entity, LivingEntityRenderState reusedState, 
    		LivingEntityRenderer<?, ?> renderer, EntityRenderDispatcher entityRenderDispatcher, float partialTick) {
    	EntityRenderState.extract(entity, reusedState, renderer, entityRenderDispatcher, partialTick);
        float f = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);
        reusedState.bodyRot = solveBodyRot(entity, f, partialTick);
        reusedState.yRot = Mth.wrapDegrees(f - reusedState.bodyRot);
        reusedState.xRot = getXRot(entity, partialTick);
        reusedState.customName = entity.getCustomName();
        reusedState.isUpsideDown = isEntityUpsideDown(entity);
        if (reusedState.isUpsideDown) {
            reusedState.xRot *= -1.0F;
            reusedState.yRot *= -1.0F;
        }

        if (!entity.isPassenger() && entity.isAlive()) {
            reusedState.walkAnimationPos = entity.walkAnimation.position(partialTick);
            reusedState.walkAnimationSpeed = entity.walkAnimation.speed(partialTick);
        } else {
            reusedState.walkAnimationPos = 0.0F;
            reusedState.walkAnimationSpeed = 0.0F;
        }

        if (entity.getVehicle() instanceof LivingEntity livingentity) {
            reusedState.wornHeadAnimationPos = livingentity.walkAnimation.position(partialTick);
        } else {
            reusedState.wornHeadAnimationPos = reusedState.walkAnimationPos;
        }

        reusedState.scale = entity.getScale();
        reusedState.ageScale = entity.getAgeScale();
        reusedState.pose = entity.getPose();
        reusedState.bedOrientation = entity.getBedOrientation();
        if (reusedState.bedOrientation != null) {
            reusedState.eyeHeight = entity.getEyeHeight(Pose.STANDING);
        }

        label48: {
            reusedState.isFullyFrozen = entity.isFullyFrozen();
            reusedState.isBaby = entity.isBaby();
            reusedState.isInWater = entity.isInWater() || entity.isInFluidType((fluidType, height) -> entity.canSwimInFluidType(fluidType));
            reusedState.isAutoSpinAttack = entity.isAutoSpinAttack();
            reusedState.hasRedOverlay = entity.hurtTime > 0 || entity.deathTime > 0;
            ItemStack itemstack = entity.getItemBySlot(EquipmentSlot.HEAD);
            if (itemstack.getItem() instanceof BlockItem blockitem && blockitem.getBlock() instanceof AbstractSkullBlock abstractskullblock) {
                reusedState.wornHeadType = abstractskullblock.getType();
                reusedState.wornHeadProfile = itemstack.get(DataComponents.PROFILE);
                reusedState.headItem = itemstack.copy();
                break label48;
            }

            reusedState.wornHeadType = null;
            reusedState.wornHeadProfile = null;
            reusedState.headItem = itemstack.copy();
        }

        reusedState.deathTime = entity.deathTime > 0 ? (float)entity.deathTime + partialTick : 0.0F;
        Minecraft minecraft = Minecraft.getInstance();
        reusedState.isInvisibleToPlayer = reusedState.isInvisible && entity.isInvisibleTo(minecraft.player);
        reusedState.appearsGlowing = minecraft.shouldEntityAppearGlowing(entity);
    }

    public static float solveBodyRot(LivingEntity entity, float yHeadRot, float partialTick) {
        if (entity.getVehicle() instanceof LivingEntity livingentity) {
            float yBodyRot = Mth.rotLerp(partialTick, livingentity.yBodyRotO, livingentity.yBodyRot);
            float maxRotAbs = 85.0F;
            float f1 = Mth.clamp(Mth.wrapDegrees(yHeadRot - yBodyRot), -maxRotAbs, maxRotAbs);
            yBodyRot = yHeadRot - f1;
            if (Math.abs(f1) > 50.0F) {
                yBodyRot += f1 * 0.2F;
            }

            return yBodyRot;
        } else {
            return Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        }
    }
    
    public static boolean isEntityUpsideDown(LivingEntity entity) {
        if (entity instanceof Player || entity.hasCustomName()) {
            String s = ChatFormatting.stripFormatting(entity.getName().getString());
            if ("Dinnerbone".equals(s) || "Grumm".equals(s)) {
                return !(entity instanceof Player) || ((Player)entity).isModelPartShown(PlayerModelPart.CAPE);
            }
        }

        return false;
    }
}
