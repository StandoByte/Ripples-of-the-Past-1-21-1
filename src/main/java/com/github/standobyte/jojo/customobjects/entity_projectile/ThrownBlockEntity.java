package com.github.standobyte.jojo.customobjects.entity_projectile;

import java.util.Optional;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.particle.CustomParticlesHelper;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.subsystems.itemtracking.OriginalItemPosComponent;
import com.github.standobyte.jojo.subsystems.target.ActionTarget;
import com.github.standobyte.jojo.subsystems.target.ActionTarget.TargetType;
import com.github.standobyte.jojo.util.functions.NBTUtil;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;
import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDHealAbility;
import com.github.standobyte.jojoimpl.stands.crazydiamond.brokenblocks.EntityMadeFromBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ThrownBlockEntity extends ModdedProjectileEntity implements EntityMadeFromBlock {
    protected static final EntityDataAccessor<Boolean> CRAZY_D_RESTORED = SynchedEntityData.defineId(ThrownBlockEntity.class, EntityDataSerializers.BOOLEAN);
    protected BlockState blockState;
    protected Optional<BlockPos> originBlockPos = Optional.empty();
    protected int crazyDRestoreTick = 1;
    
    public static ThrownBlockEntity fromItem(ServerLevel level, LivingEntity shooter, ItemStack item) {
    	BlockState blockState = null;
    	BlockPos blockPos = null;
    	
    	if (!item.isEmpty()) {
    		if (item.getItem() instanceof BlockItem blockItem) {
    			blockState = blockItem.getBlock().defaultBlockState();
    		}
    		
    		@Nullable OriginalItemPosComponent originalPos = item.get(ModItemDataComponents.ORIGINAL_POS);
    		if (originalPos != null) {
    			blockPos = originalPos.blockPos();
    		}
    	}
    	
    	return new ThrownBlockEntity(shooter, level, blockState, blockPos);
    }
    
    public ThrownBlockEntity(LivingEntity shooter, Level level, BlockState blockState, BlockPos originBlockPos) {
        super(ModEntityTypes.THROWN_BLOCK.get(), shooter, level);
        this.blockState = blockState;
        this.originBlockPos = Optional.ofNullable(originBlockPos).or(() -> Optional.of(this.blockPosition()));
    }

    public ThrownBlockEntity(EntityType<? extends ThrownBlockEntity> entityType, Level level) {
        super(entityType, level);
    }
    
    public BlockState getBlock() {
        if (blockState == null) {
            blockState = Blocks.COBBLESTONE.defaultBlockState();
        }
        return blockState;
    }
    
    public Optional<BlockPos> getOriginBlockPos() {
    	return originBlockPos;
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
    
    @Override
    protected boolean hurtTarget(Entity target, @Nullable LivingEntity owner) {
        if (super.hurtTarget(target, owner)) {
        	// TODO effects on hit
//            if (isGlass() && target instanceof LivingEntity livingTarget) {
//                if (random.nextFloat() < glassShardBleedingChance(livingTarget)) {
//                    glassShardBleeding(livingTarget);
//                }
//            }
            
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
    	if (!level().isClientSide()) {
    		sendClientBreakPacket(targetType, hitTarget);
    	}
    }
    
    @Override
    public void clientBreakProjectile(ActionTarget target) {
    	Level level = level();
        if (blockState != null) {
            Vec3 position = position();
            SoundType soundType = blockState.getSoundType(level, BlockPos.containing(position), this);
            SoundEvent sound = soundType.getBreakSound();
            if (sound != null) {
                level.playLocalSound(position.x, position.y, position.z, 
                        sound, SoundSource.BLOCKS, 
                        (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F, false);
            }
            
            CustomParticlesHelper.addBlockDestroyParticles(blockState, BlockPos.containing(position), position);
        }
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
        entityData.set(CRAZY_D_RESTORED, nbt.getBoolean("CDRestore"));
    }
    
    @Override
    protected void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        NBTUtil.put(nbt, "Block", blockState, BlockState.CODEC);
        originBlockPos.ifPresent(blockPos -> NBTUtil.put(nbt, "OriginPos", blockPos, BlockPos.CODEC));
        nbt.putBoolean("CDRestore", isCrazyDRestored());
    }

    

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeInt(Block.getId(getBlock()));
        NetworkUtil.writeOptional(originBlockPos, buffer, BlockPos.STREAM_CODEC);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        this.blockState = Block.stateById(additionalData.readInt());
        this.originBlockPos = NetworkUtil.readOptional(additionalData, BlockPos.STREAM_CODEC);
    }
    
}
