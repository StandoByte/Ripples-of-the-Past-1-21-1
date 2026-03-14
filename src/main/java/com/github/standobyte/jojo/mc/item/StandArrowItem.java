package com.github.standobyte.jojo.mc.item;

import static com.github.standobyte.jojo.init.ModItems.discsOrder;

import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.github.standobyte.gameplay.standarrow.GiveStandToEntity;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.core.packet.fromserver.ItemBreakVisualsPacket;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.mc.entity.projectile.StandArrowEntity;
import com.github.standobyte.jojo.mechanics.StoryPart;
import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.github.standobyte.jojo.util.UtilFunctions;
import com.github.standobyte.jojo.util.mc.StatusEffectUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class StandArrowItem extends ArrowItem {
    // dur: 25 | 250; ench: 10 | 25
    private final int enchantability = 20;

    public StandArrowItem(Properties properties) {
        super(properties);
        DispenserBlock.registerProjectileBehavior(this);
    }

    // TODO bow/crossbow model override when shooting a stand arrow
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


//    /**
//     * @return  if the entity got the Stand Virus effect or a Stand
//     */
//    public static boolean onPiercedByArrow(Entity target, ItemStack arrowItem, Level level, Optional<Entity> arrowShooter) {
//        if (!level.isClientSide() && target instanceof LivingEntity) {
//            LivingEntity livingEntity = (LivingEntity) target;
//            if (livingEntity.hasEffect(ModStatusEffects.STAND_VIRUS)) {
//                return false;
//            }
//
//            if (livingEntity instanceof StandEntity) {
//                return false;
//            }
//            else if (livingEntity instanceof Player) {
//                Player player = (Player) livingEntity;
//                StandType standToGive = ModStands.STAR_PLATINUM.get(); // todo remove this test field
//                // todo return GeneralUtil.orElseFalse(IStandPower.getStandPowerOptional(livingEntity), standCap -> .. (it would also fail arrow piercing for stand users)
//                if (player.getAbilities().instabuild) { // instantly give a stand in creative
//                    return giveStandFromArrow(player, standToGive);
//                }
//                else {
//                    // todo standCap.getStandArrowHandler().startArrowEffectSetStand(standToGive);
//
//                    int virusEffectDuration = 600;
//                    if (virusEffectDuration > 0) {
//                        int effectLevel = 0;
//                        player.addEffect(new MobEffectInstance(ModStatusEffects.STAND_VIRUS,
//                                virusEffectDuration, effectLevel, false, false, true));
//                    }
//                    else { // instantly give a stand if there was no stand virus effect given
//                        return giveStandFromArrow(player, standToGive);
//                    }
//
//                    // todo rememberArrowShooter(livingEntity, arrowShooter, stack);
//                }
//
//                return true;
//            }
//            // if the target is a mob
//            else {
//                // todo virus inhibition ench
//                int effectLevel = 0;
//                livingEntity.addEffect(new MobEffectInstance(ModStatusEffects.STAND_VIRUS,
//                        600, effectLevel, false, false, true));
//            }
//        }
//        return false;
//    }
//
//    public static boolean giveStandFromArrow(LivingEntity entity, StandType standType) {
//        PowerClass.STAND.attachPower(entity);
//        StandPower stand = PowerClass.STAND.get(entity);
//        if (stand != null) {
//            stand.setStandInstance(Optional.of(new StandInstance(standType)));
//            return true;
//        }
//        return false;
//    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack arrow = player.getItemInHand(usedHand);

        if (!level.isClientSide() && GiveStandToEntity.onPiercedByArrow(level, player, arrow, null, player)) {
        	if (!isInvulnerable(player)) {
        		dealDamageFromArrow(player, arrow, false);
        	}
        	ServerLevel serverLevel = (ServerLevel) level;
			arrow.hurtAndBreak(1, serverLevel, player, itemType -> onBreakArrow(
					serverLevel, player, usedHand, null, null, itemType));
            return InteractionResultHolder.success(arrow);
        }
        return InteractionResultHolder.fail(arrow);
    }
    
    public static void onBreakArrow(ServerLevel level, 
    		@Nullable LivingEntity userEntity, @Nullable InteractionHand usedHand,
    		@Nullable Entity itemEntity, @Nullable Vec3 pos, 
    		Item itemConsumerArg) {
    	if (userEntity != null && usedHand != null) {
    		userEntity.onEquippedItemBroken(itemConsumerArg, UtilFunctions.getHandSlot(usedHand));
    		// TODO spawn arrow shards
    	}
    	else if (pos != null || itemEntity != null) {
    		if (pos == null) pos = itemEntity.getBoundingBox().getCenter();
    		// TODO spawn arrow shards
    		ItemBreakVisualsPacket packet = ItemBreakVisualsPacket.fromParams(itemEntity, pos, null);
    		if (packet != null) {
    			PacketDistributor.sendToPlayersTrackingChunk(level, AAAAAAAAAAAAAAAAAA(pos), packet);
    		}
    	}
    }
    
    public static ChunkPos AAAAAAAAAAAAAAAAAA(Vec3 pos) {
    	return new ChunkPos(((int) pos.x) >> 4, ((int) pos.z) >> 4);
    }

    // i'm tired of being angry
    public static boolean isInvulnerable(LivingEntity entity) {
    	return entity.isInvulnerable() || entity instanceof Player player && player.getAbilities().invulnerable;
    }
    
    public static void dealDamageFromArrow(LivingEntity entity, ItemStack arrowItem, boolean reducedDamage) {
    	int bleedingEffect = reducedDamage ? 1 : 2;
    	float dmgAmount = reducedDamage ? 12 : 16;

    	entity.addEffect(new MobEffectInstance(ModStatusEffects.BLEEDING, 
    			6000 /* it'll heal anyway */, bleedingEffect, false, false, true));
    	// TODO damage source
    	DamageSource dmgSource = entity.damageSources().playerAttack((Player) entity);
    	dmgAmount = Math.min(dmgAmount, entity.getHealth() - 1.0F);
    	entity.hurt(dmgSource, dmgAmount);
    	StandPower.get(entity).healingDamageFromArrow = true;
    }
    
    public static boolean healArrowDamage(LivingEntity entity) {
		if (entity.getHealth() < entity.getMaxHealth()) {
			entity.heal(0.1F);
		}
		
		MobEffectInstance bleeding = entity.getEffect(ModStatusEffects.BLEEDING);
		if (bleeding != null) {
			if (entity.tickCount % 40 == 39) {
				StatusEffectUtil.reduceEffect(entity, ModStatusEffects.BLEEDING, 0, 1);
			}
		}
		
		return entity.getHealth() < entity.getMaxHealth() || bleeding != null;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
    	addStandNamesToTooltip(tooltipComponents, context);
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
    
    public static void addStandNamesToTooltip(List<Component> tooltipComponents, TooltipContext context) {
        Player player = ClientProxy.getClientPlayer();
        if (player != null) {
            Stream<StandType> stands = GiveStandToEntity.getStandsForPlayer();
            stands.map(StandInstance::new)
            .sorted(discsOrder(context.registries()))
            .forEach(stand -> {
            	StandSkin defaultSkin = StandSkinsLoader.getInstance().getSkin(stand);
            	Component standName = stand.getStandName(true).plainCopy().withStyle(ChatFormatting.GRAY);
            	if (defaultSkin != null) {
            		Holder<StoryPart> storyPart = defaultSkin.getStoryPart(context.registries());
            		if (storyPart != null) {
            			Component partIcon = storyPart.value().getPartIconAsText();
            			standName = partIcon.copy().append(standName);
            		}
            	}
                tooltipComponents.add(standName);
            });
        }
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return enchantability;
    }
}
