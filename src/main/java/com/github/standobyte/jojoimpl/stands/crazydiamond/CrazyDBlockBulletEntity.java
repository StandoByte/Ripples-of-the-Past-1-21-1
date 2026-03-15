package com.github.standobyte.jojoimpl.stands.crazydiamond;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.sound.ClientsideSoundsHelper;
import com.github.standobyte.jojo.customobjects.entity_projectile.ModdedProjectileEntity;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModParticles;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.subsystems.target.ActionTarget.TargetType;
import com.github.standobyte.jojo.util.functions.DamageUtil;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;
import com.github.standobyte.jojo.util.objects_mc.EntityResolver;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class CrazyDBlockBulletEntity extends ModdedProjectileEntity {
	protected Block block;
	protected ResourceLocation blockTex = null;
	protected boolean soundStarted = false;
	protected EntityResolver homingTarget = new EntityResolver();
	public float homingStaminaCost;

	public CrazyDBlockBulletEntity(LivingEntity shooter, Level level) {
		super(ModEntityTypes.CD_BLOCK_BULLET.get(), shooter, level);
	}

	public CrazyDBlockBulletEntity(EntityType<? extends CrazyDBlockBulletEntity> type, Level level) {
		super(type, level);
	}

	public void setTarget(Entity target) {
		this.homingTarget.setEntity(target);
	}

	public void setBlock(Block block) {
		this.block = block;
	}

	public Block getBlock() {
		return block;
	}

	public ResourceLocation getBlockTex() {
		return blockTex;
	}

	public void setBlockTex(ResourceLocation texture) {
		this.blockTex = texture;
	}


	@Override
	public int ticksLifespan() {
		return 6000;
	}
	
	@Override
	protected void moveProjectile() {
		super.moveProjectile();
		Entity target = this.homingTarget.getEntity(level());
		
		if (target != null) {
			if (!target.isAlive()) {
				this.homingTarget.setEntity(null);
			}
			else if (tickCount >= 10) {
				Vec3 targetPos = target.getBoundingBox().getCenter();
				Vec3 vecToTarget = targetPos.subtract(this.position());
				setDeltaMovement(vecToTarget.normalize().scale(this.getDeltaMovement().length()));
				StandPower standPower = userStandPower.get();
				if (standPower != null) {
					standPower.consumeStamina(homingStaminaCost, true);
				}
				Level level = level();
				if (level.isClientSide()) {
					if (ClientGlobals.canSeeStands) {
						CrazyDHealAbility.addParticlesAround(this);

						target.getBoundingBox().clip(position(), targetPos).ifPresent(pos -> {
							level().addParticle(ModParticles.CD_RESTORATION.get(), 
									pos.x + (random.nextDouble() - 0.5) * 0.25, 
									pos.y + (random.nextDouble() - 0.5) * 0.25, 
									pos.z + (random.nextDouble() - 0.5) * 0.25, 
									0, 0, 0);
						});
					}
					if (!soundStarted && ClientGlobals.canHearStands && getOwner() instanceof StandEntity stand) {
						level.playLocalSound(this, ClientsideSoundsHelper.withStandSkin(
								ModSoundEvents.CRAZY_DIAMOND_FIX_STARTED.get(), stand), 
								stand.getSoundSource(), 1, 1);
						
						// TODO loop the sound
						level.playLocalSound(this, ClientsideSoundsHelper.withStandSkin(
								ModSoundEvents.CRAZY_DIAMOND_FIX_LOOP.get(), stand), 
								stand.getSoundSource(), 1, 1);
						
						soundStarted = true;
					}
				}
			}
		}
	}

	@Override
	protected float getBaseDamage() {
		return 4;
	}

	@Override
	protected boolean hurtTarget(Entity target, LivingEntity owner) {
		if (block == Blocks.MAGMA_BLOCK) {
			return DamageUtil.dealDamageAndSetOnFire(target, 
					entity -> super.hurtTarget(target, owner), 80, true);
		}
		boolean hurt = super.hurtTarget(target, owner);

		if (hurt) {
			if (block == Blocks.NOTE_BLOCK) {
				NoteBlockInstrument[] instruments = NoteBlockInstrument.values();
				NoteBlockInstrument instrument = instruments[random.nextInt(instruments.length)];
				target.playSound(instrument.getSoundEvent().value(), 
						5.0F, (float) Math.pow(2.0D, (double) (random.nextInt(24) - 12) / 12.0D));
			}

			else if (target instanceof LivingEntity targetLiving) {
				if (block != null && block.builtInRegistryHolder().is(BlockTags.ICE)) {
					int duration = 60;
					int amplifier;
					if (block == Blocks.BLUE_ICE)			amplifier = 2;
					else if (block == Blocks.PACKED_ICE)	amplifier = 1;
					else 									amplifier = 0;
					// TODO freeze effect
//					targetLiving.addEffect(new MobEffectInstance(ModStatusEffects.FREEZE.get(), duration, amplifier, false, false, true));
				}
			}
		}
		return hurt;
	}

	@Override
	protected void breakProjectile(TargetType targetType, HitResult hitTarget) {
		Level level = level();
		if (!level.isClientSide() && block instanceof InfestedBlock) {
			Silverfish silverfish = EntityType.SILVERFISH.create(level);
			silverfish.moveTo(getX(), getY(0.5), getZ(), 0.0F, 0.0F);
			level.addFreshEntity(silverfish);
			silverfish.spawnAnim();
		}
		super.breakProjectile(targetType, hitTarget);
	}

	@Override
	protected float getMaxHardnessBreakable() {
		return 1.0F;
	}

	@Override
	public boolean standDamage() {
		return false;
	}

//	public static final Codec<BlockState> BLOCK_STATE_CODEC = Codec.withAlternative(
//			BlockState.CODEC, BuiltInRegistries.BLOCK.byNameCodec(), Block::defaultBlockState);
	public static final Codec<Block> BLOCK_CODEC = BuiltInRegistries.BLOCK.byNameCodec();
	@Override
	protected void addAdditionalSaveData(CompoundTag nbt) {
		super.addAdditionalSaveData(nbt);
		homingTarget.saveNbt(nbt, "HomingTarget");
		if (block != null) BLOCK_CODEC.encode(block, NbtOps.INSTANCE, nbt);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag nbt) {
		super.readAdditionalSaveData(nbt);
		homingTarget.loadNbt(nbt, "HomingTarget");
		block = BLOCK_CODEC.decode(NbtOps.INSTANCE, nbt).mapOrElse(Pair::getFirst, __ -> null);
	}

	@Override
	public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
		super.writeSpawnData(buffer);
		NetworkUtil.writeOptionally(block, buffer, NetworkUtil.registryCodec(Registries.BLOCK));
		homingTarget.updateEntity(level());
		homingTarget.writeNetwork(buffer);
	}

	@Override
	public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
		super.readSpawnData(additionalData);
		NetworkUtil.readOptional(additionalData, NetworkUtil.registryCodec(Registries.BLOCK)).ifPresent(this::setBlock);
		homingTarget.readNetwork(additionalData);
	}
}
