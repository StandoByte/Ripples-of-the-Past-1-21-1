package com.github.standobyte.jojo.mc.entity.projectile;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModEntityTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

public class ThrownNuggetBearingEntity extends ThrowableItemProjectile implements IEntityWithComplexSpawn {
	protected int dropOffTick = 10;
	@Nullable protected ProjectilePiercing ironBearingPierce;

	public ThrownNuggetBearingEntity(EntityType<? extends ThrownNuggetBearingEntity> entityType, Level level) {
		super(entityType, level);
	}

	public ThrownNuggetBearingEntity(Level level, LivingEntity owner, ItemStack item) {
		super(ModEntityTypes.NUGGET_BEARING.get(), owner, level/*, item*/);
		setItem(item); // will be redundant
	}

	public ThrownNuggetBearingEntity(Level level, double x, double y, double z, ItemStack item) {
		super(ModEntityTypes.NUGGET_BEARING.get(), x, y, z, level/*, item*/);
		setItem(item); // will be redundant
	}

	@Override
	protected Item getDefaultItem() {
		return Items.IRON_NUGGET;
	}

	public static float shotVelocity(ItemStack item) {
		float velocity = 1.5f;
		if (item.is(Tags.Items.NUGGETS_GOLD)) {
			velocity *= 1.5f;
		}
		return velocity;
	}

	@Override
	public void tick() {
		if (tickCount == dropOffTick) {
			setDeltaMovement(getDeltaMovement().scale(0.75));
		}
		super.tick();
	}

	@Override
	protected double getDefaultGravity() {
		return tickCount > dropOffTick ? 0.05 : 0;
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		super.onHitEntity(result);
		if (!level().isClientSide()) {
			ItemStack item = getItem();
			float damage = tickCount < dropOffTick ? 5 : 3;
			if (item.is(Tags.Items.NUGGETS_GOLD)) {
				damage *= 1.5f;
			}
			Entity target = result.getEntity();
			target.hurt(this.damageSources().thrown(this, this.getOwner()), damage);
			boolean remove = true;
			
			boolean pierce = item.is(Tags.Items.NUGGETS_IRON);
			if (pierce) {
				if (ironBearingPierce == null) {
					ironBearingPierce = new ProjectilePiercing(3);
				}
				remove = !ironBearingPierce.pierceEntity(result, 3);
			}
			
			if (remove) {
				this.discard();
			}
		}
	}

	@Override
	protected void onHitBlock(BlockHitResult result) {
		super.onHitBlock(result);
		Level level = level();
		if (level.isClientSide()) {
			BlockPos blockPos = result.getBlockPos();
			BlockState blockState = level().getBlockState(blockPos);
			SoundType sounds = blockState.getSoundType(level, blockPos, this);
			SoundEvent sound = sounds.getBreakSound();
			Vec3 pos = result.getLocation();
			level.playLocalSound(pos.x, pos.y, pos.z, sound, this.getSoundSource(), 
					sounds.volume * 0.4f, sounds.pitch * (0.85f + 0.3f * random.nextFloat()), false);
		}
		else {
			ItemStack item = this.getItem();
			if (!item.isEmpty()) {
				Vec3 pos = result.getLocation();
				pos = pos.add(this.getDeltaMovement().normalize().scale(-0.25));
				ItemEntity itemEntity = new ItemEntity(level, pos.x, pos.y, pos.z, item, 0, 0, 0);
				itemEntity.setItem(item);
				Entity owner = this.getOwner();
				if (owner != null) itemEntity.setThrower(owner);
				level.addFreshEntity(itemEntity);
			}
			this.discard();
		}
	}
	
	@Override
    protected boolean canHitEntity(Entity target) {
    	return super.canHitEntity(target) && !(ironBearingPierce != null && ironBearingPierce.alreadyHit(target));
    }


	@Override
	public void addAdditionalSaveData(CompoundTag nbt) {
		super.addAdditionalSaveData(nbt);
		nbt.putInt("age", tickCount);
		if (ironBearingPierce != null) {
			nbt.put("pierced", ironBearingPierce.toNBT());
		}
	}

	@Override
	public void readAdditionalSaveData(CompoundTag nbt) {
		super.addAdditionalSaveData(nbt);
		tickCount = nbt.getInt("age");
		if (nbt.contains("pierced")) {
			ironBearingPierce = ProjectilePiercing.fromNBT(nbt.get("pierced"));
		}
	}

	@Override
	public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
		buffer.writeVarInt(tickCount);
	}

	@Override
	public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
		tickCount = additionalData.readVarInt();
	}

}
