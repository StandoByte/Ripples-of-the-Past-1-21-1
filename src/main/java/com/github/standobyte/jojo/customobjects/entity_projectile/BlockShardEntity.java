package com.github.standobyte.jojo.customobjects.entity_projectile;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.particle.CustomParticlesHelper;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModStatusEffects;
import com.github.standobyte.jojo.subsystems.target.ActionTarget.TargetType;
import com.github.standobyte.jojo.util.functions.NBTUtil;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;
import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDBlockBulletAbility;
import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDHealAbility;
import com.github.standobyte.jojoimpl.stands.crazydiamond.brokenblocks.EntityMadeFromBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BlockShardEntity extends ModdedProjectileEntity implements EntityMadeFromBlock {
    protected static final EntityDataAccessor<Boolean> CRAZY_D_RESTORED = SynchedEntityData.defineId(BlockShardEntity.class, EntityDataSerializers.BOOLEAN);
    protected BlockState blockState;
    protected Optional<BlockPos> originBlockPos = Optional.empty();
    protected boolean isGlass;
    protected int crazyDRestoreTick = 1;
    
    public BlockShardEntity(LivingEntity shooter, Level level, BlockState blockState, BlockPos originBlockPos) {
        super(ModEntityTypes.BLOCK_SHARD.get(), shooter, level);
        this.blockState = blockState;
        this.originBlockPos = Optional.ofNullable(originBlockPos);
        this.isGlass = CrazyDBlockBulletAbility.isGlassBlock(blockState, level, originBlockPos);
    }

    public BlockShardEntity(EntityType<? extends BlockShardEntity> entityType, Level level) {
        super(entityType, level);
    }
    
    public BlockState getBlock() {
        if (blockState == null) {
            blockState = Blocks.COBBLESTONE.defaultBlockState();
        }
        return blockState;
    }
    
    // XXX block shards collision if they're frozen in place
//    @Override
//    public boolean canBeCollidedWith() {
//        return !canUpdate();
//    }
    
    @Override
    public int ticksLifespan() {
        return 100;
    }

    // TODO (1.16.5) damage based on the block hardness
    @Override
    protected float getBaseDamage() {
        return 2.5f;
    }
    
    public boolean isGlass() {
        return isGlass;
    }
    
    @Override
    protected boolean hurtTarget(Entity target, @Nullable LivingEntity owner) {
        if (super.hurtTarget(target, owner)) {
            if (isGlass() && target instanceof LivingEntity livingTarget) {
                if (random.nextFloat() < glassShardBleedingChance(livingTarget)) {
                    glassShardBleeding(livingTarget);
                }
            }
            
            return true;
        }
        return false;
    }

    @Override
    protected float getMaxHardnessBreakable() {
        return 0;
    }

    @Override
    public boolean standDamage() {
        return false;
    }
    
    @Override
    protected boolean constVelocity() {
        return false;
    }
    
    @Override
    protected double getGravityAcceleration() {
        return 0.05;
    }
    
    @Override
    protected boolean hasGravity() {
        return true;
    }
    
    @Override
    public boolean crazyDRestore(BlockPos blockPos) {
        entityData.set(CRAZY_D_RESTORED, true);
        return true;
    }
    
    protected boolean isCrazyDRestored() {
        return entityData.get(CRAZY_D_RESTORED);
    }
    
    @Override
    protected void moveProjectile() {
        if (isCrazyDRestored()) {
        	Level level = level();
            originBlockPos.ifPresent(target -> {
                if (crazyDRestoreTick-- == 0 && !level.isClientSide()) {
                    remove(RemovalReason.DISCARDED);
                    return;
                }
                
                Vec3 targetPos = Vec3.atCenterOf(target);
                Vec3 vecToTarget = targetPos.subtract(this.position());
                setDeltaMovement(vecToTarget.scale(0.5));
                if (level.isClientSide()) {
                    if (ClientGlobals.canSeeStands) {
                        CrazyDHealAbility.addParticlesAround(this);
                    }
                }
            });
        }
        super.moveProjectile();
    }
    
    @Override
    protected void breakProjectile(TargetType targetType, HitResult hitTarget) {
    	Level level = level();
        if (level.isClientSide() && blockState != null) {
            Vec3 position = position();
            SoundType soundType = blockState.getSoundType(level, BlockPos.containing(position), this);
            SoundEvent sound = soundType.getBreakSound();
            if (sound != null) {
                level.playLocalSound(position.x, position.y, position.z, 
                        sound, SoundSource.BLOCKS, 
                        (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F, false);
            }
            
            CustomParticlesHelper.addBlockShardBreakParticles(position, blockState);
        }
        super.breakProjectile(targetType, hitTarget);
    }
    
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CRAZY_D_RESTORED, false);
    }
    
    

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        blockState = NBTUtil.getOptional(nbt, "Block", BlockState.CODEC).orElse(Blocks.COBBLESTONE.defaultBlockState());
        originBlockPos = NBTUtil.getOptional(nbt, "OriginPos", BlockPos.CODEC);
        isGlass = nbt.getBoolean("Glass");
        entityData.set(CRAZY_D_RESTORED, nbt.getBoolean("CDRestore"));
    }
    
    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        NBTUtil.put(nbt, "Block", blockState, BlockState.CODEC);
        originBlockPos.ifPresent(blockPos -> NBTUtil.put(nbt, "OriginPos", blockPos, BlockPos.CODEC));
        nbt.putBoolean("Glass", isGlass);
        nbt.putBoolean("CDRestore", isCrazyDRestored());
    }

    

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeInt(Block.getId(getBlock()));
        buffer.writeBoolean(isGlass);
        NetworkUtil.writeOptional(originBlockPos, buffer, BlockPos.STREAM_CODEC);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        this.blockState = Block.stateById(additionalData.readInt());
        this.isGlass = additionalData.readBoolean();
        this.originBlockPos = NetworkUtil.readOptional(additionalData, BlockPos.STREAM_CODEC);
    }
    
    
    public static float glassShardBleedingChance(LivingEntity entity) {
        float armorCover = entity.getArmorCoverPercentage();
        return Math.max(1 - armorCover, 0.05f);
    }
    
    public static void glassShardBleeding(LivingEntity entity) {
        entity.addEffect(new MobEffectInstance(ModStatusEffects.BLEEDING, 100, 0, false, false, true));
    }

}
