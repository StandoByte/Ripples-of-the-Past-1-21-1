package com.github.standobyte.gameplay.standarrow;

import com.github.standobyte.jojo.init.power.ModStands;
import com.github.standobyte.jojo.mc.statuseffect.RotpStatusEffect;
import com.github.standobyte.jojo.mc.statuseffect.StatusEffectApplicable;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.github.standobyte.jojo.util.StandUtil;
import com.github.standobyte.jojo.util.damage.DamageUtil;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class StandVirusEffect extends RotpStatusEffect implements StatusEffectApplicable {

    public StandVirusEffect(MobEffectCategory category, int color) {
        super(category, color);
        this.isUncurable = true;
    }

    @Override
    public boolean isApplicable(LivingEntity entity) {
        // todo add the opportunity for mobs to get a stand
        return !StandUtil.isEntityStandUser(entity) && (entity instanceof Player);
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (!livingEntity.level().isClientSide()) {
            float damage = baseDamage(amplifier);
            if (livingEntity instanceof Player) {
                Player player = (Player) livingEntity;

                boolean hasXpLevel = player.getAbilities().instabuild || player.experienceLevel > 0;
                boolean stopEffect = false;

                // todo add here xp levels counter that stops effect

                player.giveExperienceLevels(-1);
                if (hasXpLevel) {
                    damage /= 10;
                    if (damage > livingEntity.getHealth()) {
                        damage = 0.001F;
                    }
                }
                // todo replace "livingEntity.level().damageSources().cactus()" with custom damage source
                DamageUtil.hurtThroughInvulTicks(livingEntity, livingEntity.level().damageSources().cactus(), damage);
                if (stopEffect) {
                    livingEntity.removeEffect(Holder.direct(this));
                }
            }

            else if (livingEntity.getHealth() > damage) {
                DamageUtil.hurtThroughInvulTicks(livingEntity, livingEntity.level().damageSources().cactus(), damage);
            }
            else {
                livingEntity.removeEffect(Holder.direct(this));
            }
        }
        return true;
    }

    private static float baseDamage(int amplifier) {
        return 1.5f + amplifier * 2.0f;
    }

    @Override
    public void onRemoved(LivingEntity entity, MobEffectInstance instance) {
        if (!entity.level().isClientSide() && entity.isAlive()) {
            if (entity instanceof Player) {
                Player player = (Player) entity;
                StandPower.getOptional(player).ifPresent(
                        power -> {
                            StandType stand = ModStands.STAR_PLATINUM.get();

                            if (stand == null) {
                                // todo null stand error handling
                            }
//                            if (stand != null) {
//                                StandArrowItem.giveStandFromArrow(player, stand);
//                            }
                        });
            }
            else {
                // todo randomStandGiver for mobs
            }
        }
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }
}
