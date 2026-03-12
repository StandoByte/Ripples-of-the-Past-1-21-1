package com.github.standobyte.jojo.mc.item;

import static com.github.standobyte.jojo.init.ModItems.discsOrder;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.init.power.ModStands;
import com.github.standobyte.jojo.mc.entity.projectile.StandArrowEntity;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class StandArrowItem extends ArrowItem {
    // dur: 25 | 250; ench: 10 | 25
    private final int enchantability = 20;

    public StandArrowItem(Properties properties) {
        super(properties);
    }

    @Override
    public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity shooter, @Nullable ItemStack weapon) {
        return new StandArrowEntity(shooter, level, ammo, weapon);
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack arrowItem, Direction dispenserDir) {
    	StandArrowEntity arrow = new StandArrowEntity(level, pos.x(), pos.y(), pos.z(), arrowItem.copyWithCount(1), null);
        arrow.pickup = AbstractArrow.Pickup.ALLOWED;
        return arrow;
    }


    /**
     * @return  if the entity got the Stand Virus effect or a Stand
     */
    public static boolean onPiercedByArrow(Entity target, ItemStack arrowItem, Level level, Optional<Entity> arrowShooter) {
        if (!level.isClientSide() && target instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) target;
            if (livingEntity.hasEffect(ModStatusEffects.STAND_VIRUS)) {
                return false;
            }

            if (livingEntity instanceof StandEntity) {
                return false;
            }
            else if (livingEntity instanceof Player) {
                Player player = (Player) livingEntity;
                StandType standToGive = ModStands.STAR_PLATINUM.get(); // todo remove this test field
                // todo return GeneralUtil.orElseFalse(IStandPower.getStandPowerOptional(livingEntity), standCap -> .. (it would also fail arrow piercing for stand users)
                if (player.getAbilities().instabuild) { // instantly give a stand in creative
                    return giveStandFromArrow(player, standToGive);
                }
                else {
                    // todo standCap.getStandArrowHandler().startArrowEffectSetStand(standToGive);

                    int virusEffectDuration = 600;
                    if (virusEffectDuration > 0) {
                        int effectLevel = 0;
                        player.addEffect(new MobEffectInstance(ModStatusEffects.STAND_VIRUS,
                                virusEffectDuration, effectLevel, false, false, true));
                    }
                    else { // instantly give a stand if there was no stand virus effect given
                        return giveStandFromArrow(player, standToGive);
                    }

                    // todo rememberArrowShooter(livingEntity, arrowShooter, stack);
                }

                return true;
            }
            // if the target is a mob
            else {
                // todo virus inhibition ench
                int effectLevel = 0;
                livingEntity.addEffect(new MobEffectInstance(ModStatusEffects.STAND_VIRUS,
                        600, effectLevel, false, false, true));
            }
        }
        return false;
    }

    public static boolean giveStandFromArrow(LivingEntity entity, StandType standType) {
        PowerClass.STAND.attachPower(entity);
        StandPower stand = PowerClass.STAND.get(entity);
        if (stand != null) {
            stand.setStandInstance(Optional.of(new StandInstance(standType)));
            return true;
        }
        return false;
    }


    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
    	addStandNamesToTooltip(tooltipComponents, context);
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
    
    public static void addStandNamesToTooltip(List<Component> tooltipComponents, TooltipContext context) {
        Player player = ClientProxy.getClientPlayer();
        if (player != null) {
            Stream<StandType> stands = StandType.getAllEnabledStands();
            stands.map(StandInstance::new)
            .sorted(discsOrder(context.registries())) // <- It's not a bug when experimental stands are shown at the bottom of the list
            .forEach(stand -> {
                Component partIconAndName = Component.literal(
                        Character.toString(StandSkinsLoader.getInstance().getSkin(stand).getStoryPart(context.registries()).value().getPartName().getString().charAt(0)))
                        .append(stand.getStandName(true).plainCopy().withStyle(ChatFormatting.GRAY));
                tooltipComponents.add(partIconAndName);
            });
        }
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return enchantability;
    }
}
