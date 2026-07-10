package com.github.standobyte.v1_21_4_stuff.renderstate;

import com.github.standobyte.jojo.client.entityrender.RipplesPlayerRenderState;
import com.github.standobyte.jojo.client.entityrender.RipplesPlayerRenderState.RipplesRenderStateExtensionMixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;

public class HumanoidRenderState extends ArmedEntityRenderState implements RipplesRenderStateExtensionMixin {
    public float swimAmount;
    public float attackTime;
    public float speedValue = 1.0F;
    public float maxCrossbowChargeDuration;
    public int ticksUsingItem;
    public HumanoidArm attackArm = HumanoidArm.RIGHT;
    public InteractionHand useItemHand = InteractionHand.MAIN_HAND;
    public boolean isCrouching;
    public boolean isFallFlying;
    public boolean isVisuallySwimming;
    public boolean isPassenger;
    public boolean isUsingItem;
//    public float elytraRotX;
//    public float elytraRotY;
//    public float elytraRotZ;
    public ItemStack headEquipment = ItemStack.EMPTY;
    public ItemStack chestEquipment = ItemStack.EMPTY;
    public ItemStack legsEquipment = ItemStack.EMPTY;
    public ItemStack feetEquipment = ItemStack.EMPTY;

    public static void extractHumanoidRenderState(LivingEntity entity, HumanoidRenderState reusedState, float partialTick) {
        ArmedEntityRenderState.extractArmedEntityRenderState(entity, reusedState);
        reusedState.isCrouching = entity.isCrouching();
        reusedState.isFallFlying = entity.isFallFlying();
        reusedState.isVisuallySwimming = entity.isVisuallySwimming();
        reusedState.isPassenger = entity.isPassenger() && (entity.getVehicle() != null && entity.getVehicle().shouldRiderSit());
        reusedState.speedValue = 1.0F;
        if (reusedState.isFallFlying) {
            reusedState.speedValue = (float)entity.getDeltaMovement().lengthSqr();
            reusedState.speedValue /= 0.2F;
            reusedState.speedValue = reusedState.speedValue * reusedState.speedValue * reusedState.speedValue;
        }

        if (reusedState.speedValue < 1.0F) {
            reusedState.speedValue = 1.0F;
        }

        reusedState.attackTime = entity.getAttackAnim(partialTick);
        reusedState.swimAmount = entity.getSwimAmount(partialTick);
        reusedState.attackArm = getAttackArm(entity);
        reusedState.useItemHand = entity.getUsedItemHand();
        reusedState.maxCrossbowChargeDuration = (float)CrossbowItem.getChargeDuration(entity.getUseItem(), entity);
        reusedState.ticksUsingItem = entity.getTicksUsingItem();
        reusedState.isUsingItem = entity.isUsingItem();
//        reusedState.elytraRotX = entity.elytraAnimationState.getRotX(partialTick);
//        reusedState.elytraRotY = entity.elytraAnimationState.getRotY(partialTick);
//        reusedState.elytraRotZ = entity.elytraAnimationState.getRotZ(partialTick);
        reusedState.headEquipment = entity.getItemBySlot(EquipmentSlot.HEAD).copy();
        reusedState.chestEquipment = entity.getItemBySlot(EquipmentSlot.CHEST).copy();
        reusedState.legsEquipment = entity.getItemBySlot(EquipmentSlot.LEGS).copy();
        reusedState.feetEquipment = entity.getItemBySlot(EquipmentSlot.FEET).copy();
    }
    
    @Override
    public void clear(float ageInTicks) {
    	super.clear(ageInTicks);
        this.swimAmount = 0;
        this.attackTime = 0;
        this.speedValue = 1;
        this.maxCrossbowChargeDuration = 0;
        this.ticksUsingItem = 0;
        this.attackArm = HumanoidArm.RIGHT;
        this.useItemHand = InteractionHand.MAIN_HAND;
        this.isCrouching = false;
        this.isFallFlying = false;
        this.isVisuallySwimming = false;
        this.isPassenger = false;
        this.isUsingItem = false;
//        this.elytraRotX = 0;
//        this.elytraRotY = 0;
//        this.elytraRotZ = 0;
        this.headEquipment = ItemStack.EMPTY;
        this.chestEquipment = ItemStack.EMPTY;
        this.legsEquipment = ItemStack.EMPTY;
        this.feetEquipment = ItemStack.EMPTY;
    }

    private static HumanoidArm getAttackArm(LivingEntity entity) {
        HumanoidArm humanoidarm = entity.getMainArm();
        return entity.swingingArm == InteractionHand.MAIN_HAND ? humanoidarm : humanoidarm.getOpposite();
    }
    
    
	private final RipplesPlayerRenderState jojo_ripples$renderState = new RipplesPlayerRenderState();

	@Override
	public RipplesPlayerRenderState get() {
		return jojo_ripples$renderState;
	}
}
