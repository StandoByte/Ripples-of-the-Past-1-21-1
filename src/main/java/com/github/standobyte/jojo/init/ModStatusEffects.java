package com.github.standobyte.jojo.init;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.mc.statuseffect.StandVirusEffect;
import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mc.statuseffect.BleedingEffect;
import com.github.standobyte.jojo.mc.statuseffect.ResolveModeEffect;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class ModStatusEffects {
	public static final DeferredRegister<MobEffect> STATUS_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, JojoMod.MOD_ID);


	public static final Set<Holder<? extends MobEffect>> TRACKED_EFFECTS = new HashSet<>();

	public static final Set<Holder<? extends MobEffect>> RESOLVE_EFFECTS = new HashSet<>();

	public static final DeferredHolder<MobEffect, ResolveModeEffect> RESOLVE = STATUS_EFFECTS.register("resolve", 
			id -> new ResolveModeEffect(MobEffectCategory.BENEFICIAL, 0xC6151F));

	public static final DeferredHolder<MobEffect, BleedingEffect> BLEEDING = STATUS_EFFECTS.register("bleeding", 
			id -> new BleedingEffect(MobEffectCategory.HARMFUL, 0x990000));

	public static final DeferredHolder<MobEffect, StandVirusEffect> STAND_VIRUS = STATUS_EFFECTS.register("stand_virus",
			id -> new StandVirusEffect(MobEffectCategory.HARMFUL, 0xC10019).setUncurable());
	

	@SubscribeEvent
	public static void afterRegister(FMLCommonSetupEvent event) {
		TRACKED_EFFECTS.add(RESOLVE);
		TRACKED_EFFECTS.add(BLEEDING);
		TRACKED_EFFECTS.add(STAND_VIRUS);

		RESOLVE_EFFECTS.add(RESOLVE);
	}
	
	
	@Nullable
	public static MobEffectInstance maxDurationResolveEffect(LivingEntity entity) {
		return entity.getActiveEffectsMap().entrySet().stream()
				.filter(effect -> ModStatusEffects.RESOLVE_EFFECTS.contains(effect.getKey()))
				.max(Comparator.comparingInt(effect -> effect.getValue().getDuration()))
				.map(Map.Entry::getValue)
				.orElse(null);
	}
	
	public static boolean isInResolveEffect(LivingEntity entity) {
		return entity.getActiveEffectsMap().entrySet().stream()
				.filter(effect -> ModStatusEffects.RESOLVE_EFFECTS.contains(effect.getKey()))
				.findAny().isPresent();
	}
	
	
	@ApiStatus.Internal
	public static void trackAddEffect(MobEffectInstance effectInstance, LivingEntity entity) {
		if (!entity.level().isClientSide() && TRACKED_EFFECTS.contains(effectInstance.getEffect())) {
			((ServerChunkCache) entity.getCommandSenderWorld().getChunkSource()).broadcast(entity, 
					new ClientboundUpdateMobEffectPacket(entity.getId(), effectInstance, false));
		}
	}

	@ApiStatus.Internal
	public static void trackRemoveEffect(Holder<MobEffect> effect, LivingEntity entity) {
		if (!entity.level().isClientSide() && TRACKED_EFFECTS.contains(effect)) {
			((ServerChunkCache) entity.getCommandSenderWorld().getChunkSource()).broadcast(entity, 
					new ClientboundRemoveMobEffectPacket(entity.getId(), effect));
		}
	}

	@SubscribeEvent
	public static void syncTrackedEffects(PlayerEvent.StartTracking event) {
		if (event.getTarget() instanceof LivingEntity tracked) {
			ServerPlayer player = (ServerPlayer) event.getEntity();
			for (MobEffectInstance effectInstance : tracked.getActiveEffectsMap().values()) {
				if (TRACKED_EFFECTS.contains(effectInstance.getEffect())) {
					player.connection.send(new ClientboundUpdateMobEffectPacket(tracked.getId(), effectInstance, false));
				}
			}
		}
	}


//	@SubscribeEvent(priority = EventPriority.LOWEST)
//	public static void onPotionAdded(MobEffectEvent.Added event) {
//		LivingEntity entity = event.getEntity();
//		MobEffectInstance effectInstance = event.getEffectInstance();
////		EntityStandType.giveEffectSharedWithStand(entity, effectInstance);
//
//		if (!entity.level().isClientSide()) {
//			trackAddEffect(effectInstance, entity);
////			if (entity instanceof ServerPlayer) {
////				Holder<MobEffect> effect = effectInstance.getEffect();
////				if (effect == ModStatusEffects.RESOLVE.get()) {
////					PacketManager.sendToClient(new ResolveEffectStartPacket(effectInstance.getAmplifier()), (ServerPlayerEntity) entity);
////				}
////			}
////			if (effectInstance.getEffect() == ModStatusEffects.BLEEDING.get()) {
////				int effectLvl = effectInstance.getAmplifier();
////				MobEffectInstance prevEffect = entity.getEffect(effectInstance.getEffect());
////				if (prevEffect == null || prevEffect.getAmplifier() < effectLvl) {
////					BleedingEffect.onAddedBleeding(entity, effectLvl);
////				}
////			}
//		}
//	}
//
//	@SubscribeEvent(priority = EventPriority.LOWEST)
//	public static void trackedPotionRemoved(MobEffectEvent.Remove event) {
////		EntityStandType.removeEffectSharedWithStand(event.getEntityLiving(), event.getPotion());
//
//		LivingEntity entity = event.getEntity();
//		MobEffectInstance effect = event.getEffectInstance();
//		if (effect != null) {
//			trackRemoveEffect(effect.getEffect(), entity);
//		}
//	}
//
//    @SubscribeEvent(priority = EventPriority.LOWEST)
//    public static void trackedPotionExpired(MobEffectEvent.Expired event) {
////        EntityStandType.removeEffectSharedWithStand(event.getEntityLiving(), event.getPotionEffect().getEffect());
//        
//        LivingEntity entity = event.getEntity();
//		MobEffectInstance effect = event.getEffectInstance();
//		trackRemoveEffect(effect.getEffect(), entity);
//    }
}
