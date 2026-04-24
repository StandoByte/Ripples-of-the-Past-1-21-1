package com.github.standobyte.jojo.subsystems.timestop;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.concurrent.ThreadLocalRandom;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class TimeStopEventHandler {
    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        Level level = event.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        var attachmentType = ModDataAttachmentTypes.TIME_STOP_TRACKER.get();
        if (serverLevel.hasData(attachmentType)) {
            serverLevel.getData(attachmentType).tickPost();
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) {
            return;
        }

        var attachmentType = ModDataAttachmentTypes.TIME_STOP_TRACKER.get();
        if (!serverLevel.hasData(attachmentType)) {
            return;
        }

        TimeStopLevelTracker tracker = serverLevel.getData(attachmentType);
        if (tracker.shouldFreeze(event.getEntity())) {
//            if (event.getEntity() instanceof LivingEntity livingEntity) {
//                MobEffectInstance debugEffect = switch (ThreadLocalRandom.current().nextInt(3)) {
//                    case 0 -> new MobEffectInstance(MobEffects.REGENERATION, 40, 0, false, true, true);
//                    case 1 -> new MobEffectInstance(MobEffects.POISON, 40, 0, false, true, true);
//                    default -> new MobEffectInstance(MobEffects.HUNGER, 40, 0, false, true, true);
//                };
//                livingEntity.addEffect(debugEffect);
//            }
            event.setCanceled(true);
        }
    }
}
