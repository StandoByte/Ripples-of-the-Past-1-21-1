package com.github.standobyte.jojo.mechanics.standarrow;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.powersystem.standpower.StandUtil;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class StandArrowEntity extends AbstractArrow {
	private static final EntityDataAccessor<Byte> LOYALTY = SynchedEntityData.defineId(StandArrowEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> FOIL = SynchedEntityData.defineId(StandArrowEntity.class, EntityDataSerializers.BOOLEAN);

    private ArrowLoyalty loyaltyLogic = new ArrowLoyalty();
    
    public StandArrowEntity(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    public StandArrowEntity(LivingEntity owner, Level level, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(ModEntityTypes.STAND_ARROW.get(), owner.getX(), owner.getEyeY() - (double)0.1F, owner.getZ(), level, pickupItemStack, firedFromWeapon);
        this.setOwner(owner);
    	updateDataFromItem(pickupItemStack);
    }
    
    public StandArrowEntity(Level level, double x, double y, double z, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(ModEntityTypes.STAND_ARROW.get(), x, y, z, level, pickupItemStack, firedFromWeapon);
    	updateDataFromItem(pickupItemStack);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.STAND_ARROW.get());
    }

    @Override
    protected void setPickupItemStack(ItemStack pickupItemStack) {
    	super.setPickupItemStack(pickupItemStack);
    	updateDataFromItem(pickupItemStack);
    }
    
    protected void updateDataFromItem(ItemStack item) {
    	this.entityData.set(LOYALTY, ArrowLoyalty.getLoyaltyFromItem(this, item));
    	this.entityData.set(FOIL, item.hasFoil());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    	super.defineSynchedData(builder);
    	builder.define(LOYALTY, (byte)0);
    	builder.define(FOIL, false);
    }
    
    public int getLoyaltyLevel() {
    	return entityData.get(LOYALTY);
    }


    @Override
    public void tick() {
    	loyaltyLogic.tickPre(this, getLoyaltyLevel(), inGroundTime);
    	super.tick();
    }

    @Override
    protected EntityHitResult findHitEntity(Vec3 pos, Vec3 nextPos) {
    	return loyaltyLogic.canHitEntity() ? super.findHitEntity(pos, nextPos) : null;
    }
    
    
    // copypaste from AbstractArrow, but without discarding the arrow entity
    @Override
    protected void onHitEntity(EntityHitResult result) {
    	Entity target = result.getEntity();
    	Level level = level();
    	
    	double baseDamage = getBaseDamage();
    	Entity shooter = getOwner();
    	LivingEntity shooterLiving = shooter instanceof LivingEntity __ ? __ : null;
    	DamageSource damageSource = damageSources().arrow(this, shooter != null ? shooter : this);
    	
    	if (getWeaponItem() != null && level instanceof ServerLevel serverLevel) {
    		baseDamage = EnchantmentHelper.modifyDamage(serverLevel, 
    				getWeaponItem(), target, damageSource, (float) baseDamage);
    	}

    	int damage = Mth.ceil(Mth.clamp(getDeltaMovement().length() * baseDamage, 0, 2.147483647E9));
    	//if (this.getPierceLevel() > 0) {
    	//	if (this.piercingIgnoreEntityIds == null) {
    	//		this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
    	//	}
    	//
    	//	if (this.piercedAndKilledEntities == null) {
    	//		this.piercedAndKilledEntities = Lists.newArrayListWithCapacity(5);
    	//	}
    	//
    	//	if (this.piercingIgnoreEntityIds.size() >= this.getPierceLevel() + 1) {
    	//		this.discard();
    	//		return;
    	//	}
    	//
    	//	this.piercingIgnoreEntityIds.add(target.getId());
    	//}

    	if (this.isCritArrow()) {
    		damage = (int)Math.min((long)random.nextInt(damage / 2 + 2) + (long)damage, 2147483647L);
    	}

    	if (shooterLiving != null) {
    		shooterLiving.setLastHurtMob(target);
    	}

    	boolean dodge = target.getType() == EntityType.ENDERMAN;
    	int prevTargetFireTicks = target.getRemainingFireTicks();
    	if (this.isOnFire() && !dodge) {
    		target.igniteForSeconds(5.0F);
    	}

    	if (target.hurt(damageSource, (float)damage)) {
    		if (dodge) {
    			return;
    		}

    		if (target instanceof LivingEntity targetLiving) {
    			//if (!this.level().isClientSide && this.getPierceLevel() <= 0) {
    			//	targetLiving.setArrowCount(targetLiving.getArrowCount() + 1);
    			//}

    			doKnockback(targetLiving, damageSource);
    			if (level() instanceof ServerLevel serverLevel) {
    				EnchantmentHelper.doPostAttackEffectsWithItemSource(serverLevel, targetLiving, damageSource, this.getWeaponItem());
    			}

    			doPostHurtEffects(targetLiving);
    			if (targetLiving != shooter && targetLiving instanceof Player && shooter instanceof ServerPlayer shooterPlayer && !this.isSilent()) {
    				shooterPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
    			}

    			//if (!target.isAlive() && this.piercedAndKilledEntities != null) {
    			//	this.piercedAndKilledEntities.add(targetLiving);
    			//}
    			//
    			//if (!this.level().isClientSide && shooter instanceof ServerPlayer serverplayer) {
    			//	if (this.piercedAndKilledEntities != null && this.shotFromCrossbow()) {
    			//		CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverplayer, this.piercedAndKilledEntities);
    			//	} else if (!target.isAlive() && this.shotFromCrossbow()) {
    			//		CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverplayer, Arrays.asList(target));
    			//	}
    			//}
    		}

    		playSound(getHitGroundSoundEvent(), 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
    		//if (getPierceLevel() <= 0) {
    		//	discard();
    		//}
    	} else {
    		target.setRemainingFireTicks(prevTargetFireTicks);
    	}
    	
    	deflect(ProjectileDeflection.REVERSE, target, shooter, false);
    	setDeltaMovement(getDeltaMovement().scale(0.2));
    	//if (!level.isClientSide() && getDeltaMovement().lengthSqr() < 1.0E-7) {
    	//	if (pickup == AbstractArrow.Pickup.ALLOWED) {
    	//		spawnAtLocation(this.getPickupItem(), 0.1F);
    	//	}
    	//	discard();
    	//}
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
    	super.doPostHurtEffects(target);
    	Level level = level();
    	if (!level.isClientSide()) {
    		ItemStack arrowItem = getPickupItem();
    		if (target.isAlive()) {
    			if (!StandUtil.isEntityStandUser(target)) {
    				boolean gaveStand = StandArrowItem.giveStand(level, target);
    				if (!StandArrowItem.isInvulnerable(target)) {
    					StandArrowItem.dealDamageFromArrow(target, arrowItem, 
    							this, getOwner(), false, gaveStand);
    				}
    			}
    		}
    		
    		ServerLevel serverLevel = (ServerLevel) level;
    		arrowItem.hurtAndBreak(1, serverLevel, target, itemType -> {
    			this.discard();
    			StandArrowItem.onBreakArrow(serverLevel, null, null, getBoundingBox().getCenter(), itemType);
    		});
    		setPickupItemStack(arrowItem);
    	}
    }

    @Override
    public void playerTouch(Player player) {
    	if (loyaltyLogic.canPlayerPickUp(this, player, getLoyaltyLevel())) {
    		super.playerTouch(player);
    	}
    }

    @Override
    public void tickDespawn() {
    	if (pickup != AbstractArrow.Pickup.ALLOWED) {
    		super.tickDespawn();
    	}
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
    	super.addAdditionalSaveData(compound);
    	loyaltyLogic.addAdditionalSaveData(compound);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
    	super.readAdditionalSaveData(compound);
    	updateDataFromItem(getPickupItem());
    	loyaltyLogic.readAdditionalSaveData(compound);
    }

}
