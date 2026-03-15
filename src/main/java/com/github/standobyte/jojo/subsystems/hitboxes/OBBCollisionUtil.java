package com.github.standobyte.jojo.subsystems.hitboxes;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class OBBCollisionUtil {

    public static List<? extends Entity> getEntitiesInOBB(Level level, OrientedBoundingBox obb, Predicate<? super Entity> predicate){
        List<Entity> output = new ArrayList<>();
        for (Entity entity : getEntities(level)){
            if (predicate.test(entity)){
                AABB aabb = entity.getBoundingBox().inflate((double)entity.getPickRadius());
                if (obb.intersects(aabb) || obb.contains(entity.position().add(0, entity.getBbHeight() / 2F, 0))){
                    output.add(entity);
                }
            }
        }
        return output;
    }

    @Nullable
    public static BlockState getCollidingBlock(Level level, BlockPos blockPos){
        List<AABB> blockCollisions = level.getBlockState(blockPos).getShape(level, blockPos).toAabbs();
        if (!blockCollisions.isEmpty()) return level.getBlockState(blockPos);
        return null;
    }

    public static Iterable<Entity> getEntities(Level level){
        if (level.isClientSide()){
            return ((ClientLevel) level).entitiesForRendering();
        }
        else {
            return ((ServerLevel) level).getAllEntities();
        }
    }
}
