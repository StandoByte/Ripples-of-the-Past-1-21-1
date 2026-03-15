package com.github.standobyte.jojo.util.functions;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientProxy;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class UtilFunctions {

	public static Iterable<Entity> getEntities(Level level) {
		if (level instanceof ServerLevel serverLevel) {
			return serverLevel.getAllEntities();
		}
		if (level.isClientSide()) {
			return ClientProxy.getEntities(level);
		}
		throw new IllegalArgumentException();
	}
	
	@Nullable
	public static ResourceLocation getItemId(ItemStack item) {
		Holder<Item> holder = item.getItemHolder();
        return holder.unwrapKey().map(key -> key.location()).orElse(null);
	}

	public static HumanoidArm getHandSide(LivingEntity entity, InteractionHand hand) {
		return hand == InteractionHand.MAIN_HAND ? entity.getMainArm() : entity.getMainArm().getOpposite();
	}

	public static InteractionHand getHand(LivingEntity entity, HumanoidArm handSide) {
		return entity.getMainArm() == handSide ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
	}
	
	public static EquipmentSlot getHandSlot(InteractionHand hand) {
		return switch (hand) { case MAIN_HAND -> EquipmentSlot.MAINHAND; case OFF_HAND -> EquipmentSlot.OFFHAND; };
	}
	


    protected static float lerpRotation(float currentRotation, float targetRotation) {
        while (targetRotation - currentRotation < -180.0F) {
            currentRotation -= 360.0F;
        }

        while (targetRotation - currentRotation >= 180.0F) {
            currentRotation += 360.0F;
        }

        return Mth.lerp(0.2F, currentRotation, targetRotation);
    }
	
	public static void wrapYRotationAngles(LivingEntity entity) {
		float yRot = entity.getYRot();
		float firstPersonArmBobDiff = 0;
		while (yRot < -180) { yRot += 360; entity.setYRot(yRot); entity.yRotO += 360; firstPersonArmBobDiff += 360; }
		while (yRot >= 180) { yRot -= 360; entity.setYRot(yRot); entity.yRotO -= 360; firstPersonArmBobDiff -= 360; }

		while (yRot - entity.yBodyRot < -180.0F) {
			entity.yBodyRot -= 360;
			entity.yBodyRotO -= 360;
		}
		while (yRot - entity.yBodyRot >= 180.0F) {
			entity.yBodyRot += 360;
			entity.yBodyRotO += 360;
		}

		while (yRot - entity.yHeadRot < -180.0F) {
			entity.yHeadRot -= 360;
			entity.yHeadRotO -= 360;
		}
		while (yRot - entity.yHeadRot >= 180.0F) {
			entity.yHeadRot += 360;
			entity.yHeadRotO += 360;
		}

		if (entity.level().isClientSide()) {
			wrapClientSide(entity, firstPersonArmBobDiff);
		}
	}
	
	private static void wrapClientSide(LivingEntity entity, float diff) {
		if (entity instanceof LocalPlayer needsToBeInAnotherMethod) {
			needsToBeInAnotherMethod.yBob += diff;
			needsToBeInAnotherMethod.yBobO += diff;
		}
	}
	
}
