package com.github.standobyte.jojo.subsystems.timestop;

import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class TimeStopLevelTracker {
    private static final AtomicInteger COUNTER = new AtomicInteger();

    private ServerLevel level;
    private Map<Integer, TimeStopInstance> instanceMap = new HashMap<>();

    public TimeStopLevelTracker(ServerLevel level) {this.level = level;}

    public static TimeStopLevelTracker get(ServerLevel level) {
        return level.getData(ModDataAttachmentTypes.TIME_STOP_TRACKER.get());
    }

    public Optional<TimeStopInstance> getUserTimeStopInstance(LivingEntity user) {
        return instanceMap.values().stream()
                .filter(instance -> instance.isOwnedBy(user))
                .findFirst();
    }

    public TimeStopInstance startTimeStop(@Nullable LivingEntity user, ChunkPos userPos, int ChunkRange, int durationTicks) {
        if (user != null) {
            getUserTimeStopInstance(user).ifPresent(instance -> {
                instanceMap.remove(instance.getId());
            });
        }

        TimeStopInstance timeStopInstance = new TimeStopInstance(user, userPos, ChunkRange, durationTicks);
        instanceMap.put(timeStopInstance.getId(), timeStopInstance);
        return timeStopInstance;
    }

    public void tickPost() {
        var iter = instanceMap.entrySet().iterator();
        while (iter.hasNext()) {
            TimeStopInstance instance = iter.next().getValue();

            if (!instance.isOwnerAlive(level) || instance.tick()) {
                iter.remove();
            }
        }
    }

    private boolean canMoveInStoppedTime(Entity entity, TimeStopInstance instance) {
        if (instance.isOwnedBy(entity)) {
            return true;
        }

        if (entity instanceof StandEntity stand) {
            LivingEntity user = stand.getUser();
            if (user != null && instance.isOwnedBy(user)) {
                return true;
            }
        }

//        if (entity instanceof Projectile projectile) {
//            Entity owner = projectile.getOwner();
//            if (owner != null && instance.isOwnedBy(owner)) {
//                return true;
//            }
//        }

        return false;
    }

    private Optional<TimeStopInstance> getStrongestInstanceFor(Entity entity) {
        return instanceMap.values().stream()
                .filter(instance -> instance.contains(entity))
                .max(Comparator.comparingInt(TimeStopInstance::getTicksLeft));
    }

    public boolean shouldFreeze(Entity entity) {
        TimeStopInstance instance = getStrongestInstanceFor(entity).orElse(null);
        if (instance == null) {
            return false;
        }

        return !canMoveInStoppedTime(entity, instance);
    }

    public boolean hasActiveInstance(int id) {
        return instanceMap.containsKey(id);
    }

    public void stopTimeStop(int id) {
        instanceMap.remove(id);
    }
}
