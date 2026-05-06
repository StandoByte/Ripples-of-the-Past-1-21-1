package com.github.standobyte.jojo.powersystem.standpower.entity;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.network.s2c.StandEntitySoundPacket;
import com.github.standobyte.jojo.network.s2c.TrSetStandEntityPacket;
import com.github.standobyte.jojo.powersystem.MovesetBuilder;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.StandStats;
import com.github.standobyte.jojo.powersystem.standpower.datapack.StandTypeClass;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.github.standobyte.jojo.util.objects_java.DefaultedValue;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

// TODO EntityStandType stuff (arms-only summon, etc.)
// TODO stand hitbox size parameter (+the size to stretch the model to)
public class EntityStandType extends StandType {
	static {
		StandTypeClass.registerStandClass(EntityStandType.class, "entity", EntityStandType::new);
	}
	
	protected DefaultedValue<EntityType<? extends StandEntity>> entityType;
	
	public EntityStandType(StandStats stats, MovesetBuilder moveset, 
			ResourceLocation id) {
		this(stats, moveset, ModEntityTypes.HUMANOID_STAND.get(), id);
	}
	
	public EntityStandType(StandStats stats, MovesetBuilder moveset, 
			EntityType<? extends StandEntity> standEntityType, 
			ResourceLocation id) {
		super(stats, moveset, id);
		Objects.requireNonNull(standEntityType);
		this.entityType = new DefaultedValue<>(standEntityType);
	}
	
	@Override
	public JsonObject makeConfigTemplate() {
		JsonObject json = super.makeConfigTemplate();
		json.addProperty("entityType", EntityType.getKey(this.entityType.defaultValue).toString());
		return json;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void applyConfig(JsonElement json) {
		super.applyConfig(json);
		JsonObject config = json.getAsJsonObject();
		Optional.ofNullable(config.get("entityType"))
			.map(JsonElement::getAsString)
			.flatMap(id -> EntityType.byString(id))
			.ifPresent(entityType -> {
				this.entityType.value = (EntityType<? extends StandEntity>) entityType;
			});
	}

	@Override
	public void restoreDefaults() {
		super.restoreDefaults();
		entityType.reset();
	}
	
	
//	@Override
//	public void toggleSummon(LivingEntity user, StandPower standPower) {
//		if (!standPower.isSummoned()) {
//			summon(standPower.getUser(), standPower);
//		}
//		else {
//			StandEntity standEntity = (StandEntity) standPower.getSummonedStand();
//			if (standEntity.isArmsOnlyMode()) {
//				standEntity.fullSummonFromArms();
//				triggerAdvancement(standPower, standPower.getSummonedStand());
//			}
//			else {
//				unsummon(standPower.getUser(), standPower);
//			}
//		}
//	}

	@Override
	public boolean summon(LivingEntity user, StandPower standPower) {
		return summon(user, standPower, entity -> {}, true);
	}

	public boolean summon(LivingEntity user, StandPower standPower, Consumer<StandEntity> beforeTheSummon, boolean addToWorld) {
		if (!standPower.canUsePower()) {
			return false;
		}
//		if (!withoutNameVoiceLine && !user.isShiftKeyDown()) {
//			SoundEvent shout = summonShoutSupplier.get();
//			if (shout != null) {
//				JojoModUtil.sayVoiceLine(user, shout);
//			}
//		}
//		triggerAdvancement(standPower, standPower.getStandManifestation());
		
		Level level = user.level();
		if (!level.isClientSide()) {
			if (!standPower.isSummoned()) {
				StandEntity standEntity = entityType.value.create(level/*, EntitySpawnReason.NATURAL*/)
						.withStandType(this);
				standEntity.copyPosition(user);
				standEntity.copyStandUserRotation(user);
				standPower.setSummonedStand(standEntity);
				beforeTheSummon.accept(standEntity);
				
				if (addToWorld) {
					finalizeStandSummonFromAction(user, standPower, standEntity, true);
				}
				return true;
			}
			
//			standEntity.onStandSummonServerSide();
		}
		
		return false;
	}
	
	public void finalizeStandSummonFromAction(LivingEntity user, StandPower standPower, StandEntity standEntity, boolean addToWorld) {
		Level level = user.level();
		if (!level.isClientSide() && !standEntity.isAddedToLevel()) {
			if (addToWorld) {
				level.addFreshEntity(standEntity);
				PacketDistributor.sendToPlayersTrackingEntityAndSelf(user, new StandEntitySoundPacket(standEntity, ModSoundEvents.STAND_SUMMON, 1, 1));
				PacketDistributor.sendToPlayersTrackingEntityAndSelf(user, new TrSetStandEntityPacket(user.getId(), standEntity.getId()));
//				triggerAdvancement(standPower, standPower.getSummonedStand());
			}
			else {
				forceUnsummon(user, standPower);
			}
		}
	}
	
//	protected void triggerAdvancement(StandPower standPower, SummonedStand stand) {
//		if (stand instanceof StandEntity && !((StandEntity) stand).isArmsOnlyMode()) {
//			super.triggerAdvancement(standPower, stand);
//		}
//	}

	@Override
	public void unsummon(LivingEntity user, StandPower standPower) {
		if (!user.level().isClientSide()) {
			StandEntity standEntity = ((StandEntity) standPower.getSummonedStand());
			if (standEntity != null) {
				standEntity.onUnsummonUserInput();
			}
		}
	}

	@Override
	public void forceUnsummon(LivingEntity user, StandPower standPower) {
		if (!user.level().isClientSide()) {
			StandEntity standEntity = standPower.getSummonedStandEntity();
			if (standEntity != null) {
				standPower.setSummonedStand(null);
				standEntity.remove(Entity.RemovalReason.DISCARDED);
			}
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(user, new TrSetStandEntityPacket(user.getId(), 0));
		}
//		else if (user.is(ClientUtil.getClientPlayer())) {
//			StandUtil.setManualControl(ClientUtil.getClientPlayer(), false, false);
//		}
	}
	
	public EntityType<?> getEntityType() {
		return entityType.value;
	}
	
}
