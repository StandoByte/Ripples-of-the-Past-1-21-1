package com.github.standobyte.jojoimpl.stands.starplatinum;

import org.jetbrains.annotations.Nullable;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.sound.ClientsideSoundsHelper;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.powersystem.ability.AbilityId;
import com.github.standobyte.jojo.powersystem.ability.AbilityType;
import com.github.standobyte.jojo.powersystem.entityaction.ActionOBB;
import com.github.standobyte.jojo.powersystem.entityaction.ActionPhase;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.type.EntityActionType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntityAbility;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandOffsetFromUser;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandStatFormulas;
import com.github.standobyte.jojo.util.MathUtil;
import com.github.standobyte.jojo.util.hitboxes.ExtendableOBB;
import com.github.standobyte.jojo.util.hitboxes.OBBCollisionUtil;
import com.github.standobyte.jojo.util.hitboxes.OrientedBoundingBox;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class StarFingerAbility extends StandEntityAbility {

	public StarFingerAbility(AbilityType<?> abilityType, AbilityId abilityId) {
		super(abilityType, abilityId, StarFingerInstance::new);
		setDefaultPhaseLength(ActionPhase.WINDUP, 5);
		setDefaultPhaseLength(ActionPhase.PERFORM, 20);
		setDefaultPhaseLength(ActionPhase.RECOVERY, 20);
	}

    public static class StarFingerInstance extends EntityActionInstance implements ActionOBB {
        public StarFingerInstance(EntityActionType ability) {
            super(ability);
        }

        private ExtendableOBB starFingerBB;

        @Override
        public void onActionSet(@Nullable EntityActionInstance prevAction) {
            super.onActionSet(prevAction);
            setStandOffset(0, 1.5, StandOffsetFromUser.Rotations.HEAD_XY, true);
            OrientedBoundingBox obb = new OrientedBoundingBox(new Vec3(0, 1.35, 0), 0.125d, 0.125d, 0.8d, getPerformer().getYRot(), getPerformer().getXRot());
            this.starFingerBB = new ExtendableOBB(obb, 0.8F, (int) phasesLength.getFloat(ActionPhase.PERFORM), 10, new Vec3(0, 1.35, 0));
        }

        @Override
        public void actionPerformStart() {
            LivingEntity user = getPowerUser();
            StandPower standPower = StandPower.get(user);
            standPower.consumeStamina(25);
        }

        @Override
        public void actionTick() {
            if (getPhase() == ActionPhase.PERFORM && extendableOBB() != null){
                Vec3 pos = getPerformer().position();
                Vec3 offset = new Vec3(0.07, 1.5, 1)
                        .yRot(-getPerformer().yBodyRot * MathUtil.DEG_TO_RAD);
                this.extendableOBB().updatePosition(level(), pos, offset, getPerformer().getXRot(), getPerformer().getYRot());
                if (!level().isClientSide()){
                    Vec3 endPos = this.extendableOBB().rotatableHitbox().center.add(getPerformer().getLookAngle().scale(extendableOBB().rotatableHitbox().extent.length()));
                    OBBCollisionUtil.getEntitiesInOBB(level(), this.extendableOBB().rotatableHitbox(), entity -> entity != getPerformer() && entity != getPowerUser()).forEach(entity -> {
                        if (performer instanceof StandEntity stand) {
                            DamageSource dmgSource = makePunchDamageSource();
                            float dmgAmount = StandStatFormulas.getLightAttackDamage(stand.getAttackDamage());
                            if (standEntityAttack(stand, entity, dmgSource, dmgAmount)) {
                                this.extendableOBB().forceRetract(level(), getPerformer(), this.id);
                            }
                        }
                    });
                    HitResult result = level().clip(new ClipContext(extendableOBB().rotatableHitbox().center, endPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, CollisionContext.empty()));
                    if (result instanceof BlockHitResult blockHitResult){
                        BlockState blockCollision = OBBCollisionUtil.getCollidingBlock(level(), blockHitResult.getBlockPos());
                        if (blockCollision != null){
                            // TODO Add button, lever and other interactions
                            this.extendableOBB().forceRetract(level(), getPerformer(), this.id);
                        }
                    }

                }
                this.extendableOBB().tick();
                if (this.extendableOBB().isRetracted()){
                    setPhaseStart(ActionPhase.RECOVERY);
                    syncPhaseChanges();
                }
            }
        }

        @Override
        public void onSetPhase(ActionPhase newPhase) {
            Level level = level();
            if (newPhase == ActionPhase.PERFORM){
                if (level.isClientSide() && performer instanceof StandEntity stand) {
                    if (ClientGlobals.canHearStands){
                        level.playLocalSound(stand, ClientsideSoundsHelper.withStandSkin(
                                        ModSoundEvents.STAR_PLATINUM_STAR_FINGER.get(), stand),
                                stand.getSoundSource(), 1, 1);
                    }
                    level.playLocalSound(stand, ClientsideSoundsHelper.withStandSkin(
                                    ModSoundEvents.JOTARO_STAR_FINGER.get(), stand),
                            stand.getSoundSource(), 1, 1);
                }
            }
        }

        @Override
        public ExtendableOBB extendableOBB() {
            return starFingerBB;
        }
    }
}
