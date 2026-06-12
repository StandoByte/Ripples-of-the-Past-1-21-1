package com.github.standobyte.jojo.mechanics;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.JojoModLivingVariables;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.customobjects.StatusEffectApplicable;
import com.github.standobyte.jojo.customobjects.StatusEffectModified;
import com.github.standobyte.jojo.init.ModDamageTypes;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.network.s2c.BloodParticlesPacket;
import com.github.standobyte.jojo.util.functions.AttributeUtil;
import com.github.standobyte.jojo.util.functions.DamageUtil;
import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDBloodCutterAbility;
import com.github.standobyte.jojoimpl.stands.crazydiamond.DriedBloodDropsEffect;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class BleedingEffect extends StatusEffectModified implements StatusEffectApplicable {
	public static final float HP_REDUCTION_PER_LVL = 4;
	public static final ResourceLocation ATTRIBUTE_MODIFIER_ID = JojoMod.resLoc("effect.bleeding");

	public BleedingEffect(MobEffectCategory type, int liquidColor) {
		super(type, liquidColor);
		addAttributeModifier(Attributes.MAX_HEALTH, ATTRIBUTE_MODIFIER_ID, -HP_REDUCTION_PER_LVL, AttributeModifier.Operation.ADD_VALUE);
		disableCreeperLinger = true;
	}

//	@Override
//	public void onAdded(LivingEntity entity, MobEffectInstance instance, @Nullable Entity source) {
//		super.onAdded(entity, instance, source);
//	}

	@Override
	public boolean isApplicable(LivingEntity entity) {
		return JojoDefinitions.canBleed(entity);
	}


	@EventBusSubscriber(modid = JojoMod.MOD_ID)
	public static class EventHandler {
		
		@SubscribeEvent(priority = EventPriority.HIGHEST)
		public static void changePotionAmplifier(MobEffectEvent.Added event) {
			MobEffectInstance effectInstance = event.getEffectInstance();
			if (effectInstance.getEffect().is(ModStatusEffects.BLEEDING)) {
				int amplifier = BleedingEffect.limitAmplifier(event.getEntity(), effectInstance.getAmplifier());
				if (amplifier != effectInstance.getAmplifier() && amplifier >= 0) {
					effectInstance.amplifier = amplifier;
				}
			}
		}
		
		// can't use onAdded for this, as we need the previous effect instance as part of the context
		@SubscribeEvent(priority = EventPriority.LOWEST)
		public static void onPotionAdded(MobEffectEvent.Added event) {
			LivingEntity entity = event.getEntity();
			Level level = entity.level();
			
			if (!level.isClientSide()) {
				MobEffectInstance effectInstance = event.getEffectInstance();
				if (effectInstance.getEffect().is(ModStatusEffects.BLEEDING)) {
					int effectLvl = effectInstance.getAmplifier();
					MobEffectInstance prevEffect = event.getOldEffectInstance();
					if (prevEffect == null || prevEffect.getAmplifier() < effectLvl) {
						CrazyDBloodCutterAbility.onBleedingAdded(entity);

						level.broadcastDamageEvent(entity, DamageUtil.make(level, ModDamageTypes.BLEED_OUT_DEATH));

						Vec3 particlesPos = JojoModLivingVariables.get(entity).bleedingParticlesPos;
						if (particlesPos == null) {
							particlesPos = entity.getBoundingBox().getCenter();
						}
						splashBlood(entity.level(), particlesPos, effectLvl + 1, 
								HP_REDUCTION_PER_LVL * (effectLvl + 1), 
								OptionalInt.of(effectLvl), entity);
					}
				}
			}
		}
	}


	public static int limitAmplifier(LivingEntity entity, int amplifier) {
		return Math.min(amplifier, Math.max(
				(int) (entity.getAttributeBaseValue(Attributes.MAX_HEALTH) / HP_REDUCTION_PER_LVL) - 2, 
				(int) (getMaxHealthWithoutBleeding(entity) / HP_REDUCTION_PER_LVL) - 2));
	}

	public static float getMaxHealthWithoutBleeding(LivingEntity entity) {
		return (float) AttributeUtil.calcValueWithoutModifiers(entity.getAttribute(Attributes.MAX_HEALTH), ATTRIBUTE_MODIFIER_ID);
	}


	public static void setNextParticlesPos(LivingEntity entity, Vec3 pos) {
		JojoModLivingVariables.get(entity).bleedingParticlesPos = pos;
	}

	public static boolean splashBlood(Level level, Vec3 splashPos, double radius, 
			float bleedAmount, OptionalInt bleedingEffectLvl, @Nullable LivingEntity ownerEntity) {
		if (level.isClientSide()) {
			return false;
		}

		AABB aabb = new AABB(splashPos.subtract(radius, radius, radius), splashPos.add(radius, radius, radius));
		List<Vec3> particlePos = new ArrayList<>();
		List<LivingEntity> entitiesAround = level.getEntitiesOfClass(LivingEntity.class, aabb, 
				EntitySelector.ENTITY_STILL_ALIVE.and(EntitySelector.NO_SPECTATORS)
				.and(entity -> {
					return level.clip(new ClipContext(splashPos, entity.getBoundingBox().getCenter(), 
							ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity))
							.getType() == HitResult.Type.MISS;
				}));
		for (LivingEntity entity : entitiesAround) {
			if (dropBloodOnEntity(ownerEntity, entity, bleedAmount)) {
				particlePos.add(entity.getEyePosition(1.0F));
			}
		}

		// XXX blood splash on stone mask block
		BlockPos blockPos = BlockPos.containing(splashPos);
//		BlockPos.betweenClosedStream(
//				BlockPos.containing(splashPos.subtract(radius, radius, radius)),
//				BlockPos.containing(splashPos.add(radius, radius, radius)))
//		.filter(pos -> level.getBlockState(pos).getBlock() == ModBlocks.STONE_MASK.get())
//		.forEach(pos -> {
//			BlockState blockState = level.getBlockState(pos);
//			level.playSound(null, pos, ModSoundEvents.STONE_MASK_ACTIVATION.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
//			switch (blockState.getValue(HorizontalFaceBlock.FACE)) {
//			case FLOOR:
//				BlockEntity tileEntity = level.getBlockEntity(pos);
//				if (tileEntity instanceof StoneMaskTileEntity stoneMask) {
//					stoneMask.activate();
//				}
//				particlePos.add(Vec3.atBottomCenterOf(pos));
//				break;
//			default:
//				Block.popResource(level, pos, StoneMaskBlock.getItemFromBlock(level, pos, blockState));
//				level.removeBlock(pos, false);
//				particlePos.add(Vec3.atCenterOf(pos));
//				break;
//			}
//		});

		ServerLevel serverLevel = (ServerLevel) level;
		ChunkPos chunkPos = new ChunkPos(blockPos);
		if (!particlePos.isEmpty()) {
			int count = Math.min((int) (bleedAmount * 5), 50);
			particlePos.forEach(posTo -> {
				PacketDistributor.sendToPlayersTrackingChunk(serverLevel, chunkPos, 
						new BloodParticlesPacket(splashPos, posTo, 0.375f, count, ownerEntity != null ? ownerEntity.getId() : -1));
			});
		}
		else {
			bleedingEffectLvl.ifPresent(effectLvl -> {
				float speed = (Math.min(effectLvl, 3) + 1) * 0.09375f;
				int lvl = (effectLvl + 1);
				int count = 10 * lvl * lvl * lvl;
				PacketDistributor.sendToPlayersTrackingChunk(serverLevel, chunkPos, 
						new BloodParticlesPacket(splashPos, speed, count, ownerEntity != null ? ownerEntity.getId() : -1));
			});
		}

		return !particlePos.isEmpty();
	}

	private static boolean dropBloodOnEntity(@Nullable LivingEntity bleedingEntity, LivingEntity nearbyEntity, float bleedAmount) {
		boolean dropped = false;

//		ItemStack headArmor = nearbyEntity.getItemBySlot(EquipmentSlot.HEAD);
//		if (headArmor.getItem() instanceof StoneMaskItem && applyStoneMask(nearbyEntity, headArmor)) {
//			dropped = true;
//		}

		boolean crazyDMarker = DriedBloodDropsEffect.onPossibleBloodSplash(dropped, bleedingEntity, nearbyEntity, bleedAmount);
		dropped |= crazyDMarker;

		return dropped;
	}

//	public static boolean applyStoneMask(LivingEntity entity, ItemStack headStack) {
//		if (entity.level().getDifficulty() == Difficulty.PEACEFUL) {
//			JojoModUtil.sendOverlayMsg(entity, Component.translatable("jojo.chat.message.stone_mask_peaceful"));
//			return false;
//		}
//		if (entity instanceof Player player) {
//			return INonStandPower.getNonStandPowerOptional(player).map(power -> {
//				//Prevents aja-stone mask to work on non pillar men
//				Optional<PillarmanData> pillarmanOptional = power.getTypeSpecificData(ModPowers.PILLAR_MAN.get());
//
//				if (headStack.getItem() == ModItems.AJA_STONE_MASK.get()) {
//					if (!pillarmanOptional.isPresent()) {
//						if (entity instanceof ServerPlayerEntity) {
//							ModCriteriaTriggers.MASK_SUICIDE.get().trigger((ServerPlayerEntity) entity);
//						}
//						entity.hurt(DamageUtil.STONE_MASK, 1000);
//						return false;
//					} else {
//						PillarmanData pillarman = pillarmanOptional.get();
//						if (pillarmanOptional.get().getEvolutionStage() < 4) {
//							pillarman.setEvolutionStage(4);
//							//Gives a random Mode
//							switch (entity.getRandom().nextInt(3)) {
//							case 0:
//								pillarman.setMode(PillarmanMode.WIND);
//								break;
//							case 1:
//								pillarman.setMode(PillarmanMode.HEAT);
//								break;
//							case 2:
//								pillarman.setMode(PillarmanMode.LIGHT);
//								break;
//							}
//							applyMaskEffect(entity, headStack);
//							return true;
//						}
//					}
//				}
//				else /*if (headStack.getItem() == ModItems.STONE_MASK.get())*/ {
//					if (pillarmanOptional.isPresent()) {
//						PillarmanData pillarman = pillarmanOptional.get();
//						if (pillarman.getEvolutionStage() < 2) {
//							pillarman.setEvolutionStage(2);
//							applyMaskEffect(entity, headStack);
//							return true;
//						}
//					}
//					else if (power.getTypeSpecificData(ModPowers.VAMPIRISM.get()).map(
//							vamp -> !vamp.isVampireAtFullPower()).orElse(false) || power.givePower(ModPowers.VAMPIRISM.get())) {
//						if (power.getType() == ModPowers.VAMPIRISM.get()) {
//							power.getTypeSpecificData(ModPowers.VAMPIRISM.get()).get().setVampireFullPower(true);
//							applyMaskEffect(entity, headStack);
//							return true;
//						}
//					}
//				}
//				return false;
//			}).orElse(false);
//		}
//		return false;
//	}
//
//	private static void applyMaskEffect(LivingEntity entity, ItemStack headStack) {
//		entity.level().playSound(null, entity, ModSoundEvents.STONE_MASK_ACTIVATION_ENTITY.get(), entity.getSoundSource(), 1.0F, 1.0F);
//		StoneMaskItem.setActivatedArmorTexture(headStack); // note: add light beams on stone mask activation
//		headStack.hurtAndBreak(1, entity, stack -> {});
//	}
	
	public static final double[] bleedOutPerTick = new double[] {
			1.0 / 25600, 
			1.0 / 6400, 
			1.0 / 1600, 
			1.0 / 400
	};
	public static double bleedOutPerTick(int effectLvl) {
		return bleedOutPerTick[Math.min(effectLvl, 3)];
	}
	public static double bloodRegainPerTick = 1.0 / 5000;
}
