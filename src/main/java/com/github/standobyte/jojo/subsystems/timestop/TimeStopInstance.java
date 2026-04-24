package com.github.standobyte.jojo.subsystems.timestop;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nullable;
import java.util.UUID;

public class TimeStopInstance {
    private static int nextId = 0;
    private final int id;
    @Nullable
    private final UUID ownerUuid;
    private final ChunkPos centerChunk;
    private final int chunkRange;
    private final int startingTicks;

    private int ticksLeft;

    public TimeStopInstance(@Nullable LivingEntity owner, ChunkPos centerChunk, int chunkRange, int durationTicks) {
        this.id = nextId++;
        this.ownerUuid = owner != null ? owner.getUUID() : null;
        this.centerChunk = centerChunk;
        this.chunkRange = chunkRange;
        this.startingTicks = durationTicks;
        this.ticksLeft = durationTicks;
    }

    public int getId() {
        return id;
    }

    public int getTicksLeft() {
        return ticksLeft;
    }

    public int getStartingTicks() {
        return startingTicks;
    }

    public ChunkPos getCenterChunk() {
        return centerChunk;
    }

    public int getChunkRange() {
        return chunkRange;
    }


    @Nullable
    public UUID getOwnerUuid() {
        return ownerUuid;
    }


    public boolean tick() {
        if (ticksLeft > 0) {ticksLeft--;}
        return ticksLeft <= 0;
    }

    public boolean isExpired() {
        return ticksLeft <= 0;
    }

    public boolean isInRange(ChunkPos chunkPos) {
        if (chunkRange <= 0) {return true;}

        return Math.abs(centerChunk.x - chunkPos.x) <= chunkRange
                && Math.abs(centerChunk.z - chunkPos.z) <= chunkRange;
    }

    public boolean contains(Entity entity) {
        return isInRange(entity.chunkPosition());
    }

    public boolean isOwnedBy(Entity entity) {
        return ownerUuid != null && entity.getUUID().equals(ownerUuid);
    }

    public boolean isOwnerAlive(ServerLevel level) {
        LivingEntity owner = getOwner(level);
        return owner != null && owner.isAlive();
    }

    public LivingEntity getOwner(ServerLevel serverLevel) {
        if (ownerUuid == null) {return null;}

        Entity entity = serverLevel.getEntity(ownerUuid);

        return entity instanceof LivingEntity ? (LivingEntity) entity : null;
    }
}
