package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mc.entity.BlockShardEntity;
import com.github.standobyte.jojo.mc.entity.projectile.ThrownNuggetBearingEntity;
import com.github.standobyte.jojo.mechanics.clothes.mannequin.MannequinEntity;
import com.github.standobyte.jojo.mechanics.entity_like_player.npc.PowerUserMobEntity;
import com.github.standobyte.jojo.mechanics.standarrow.StandArrowEntity;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDBlockBulletEntity;
import com.github.standobyte.jojoimpl.stands.crazydiamond.CrazyDBloodCutterEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public final class ModEntityTypes {
	public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, JojoMod.MOD_ID);
	
	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(CHARACTER.get(), Player.createAttributes().add(Attributes.FOLLOW_RANGE, 16.0).build());
		event.put(HUMANOID_STAND.get(), StandEntity.createAttributes().build());
		event.put(MANNEQUIN.get(), ArmorStand.createAttributes().build());
	}

	
	public static final DeferredHolder<EntityType<?>, EntityType<PowerUserMobEntity>> CHARACTER = ENTITY_TYPES.register("character", key -> 
			EntityType.Builder.<PowerUserMobEntity>of(PowerUserMobEntity::new, MobCategory.MISC)
			.sized(0.6F, 1.8F)
			.eyeHeight(1.62F)
			.vehicleAttachment(Player.DEFAULT_VEHICLE_ATTACHMENT)
			.clientTrackingRange(32)
			.updateInterval(2)
			.build(createIDFor(key)));
	
	public static final DeferredHolder<EntityType<?>, EntityType<StandEntity>> HUMANOID_STAND = ENTITY_TYPES.register("humanoid_stand", key -> 
			EntityType.Builder.of(StandEntity::new, MobCategory.MISC)
			.noSave()
			.noSummon()
			.sized(0.6F, 1.8F)
			.eyeHeight(1.62F)
			.vehicleAttachment(Player.DEFAULT_VEHICLE_ATTACHMENT)
			.clientTrackingRange(32)
			.updateInterval(2)
			.build(createIDFor(key)));
	
	public static final DeferredHolder<EntityType<?>, EntityType<MannequinEntity>> MANNEQUIN = ENTITY_TYPES.register("mannequin", key -> 
			EntityType.Builder.<MannequinEntity>of(MannequinEntity::new, MobCategory.MISC)
			.sized(0.5F, 1.975F)
			.eyeHeight(1.7775F)
			.clientTrackingRange(10)
			.build(createIDFor(key)));

	public static final DeferredHolder<EntityType<?>, EntityType<BlockShardEntity>> BLOCK_SHARD = ENTITY_TYPES.register("block_shard", key ->
			EntityType.Builder.<BlockShardEntity>of(BlockShardEntity::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
			.build(createIDFor(key)));

	public static final DeferredHolder<EntityType<?>, EntityType<ThrownNuggetBearingEntity>> NUGGET_BEARING = ENTITY_TYPES.register("nugget_bearing", key -> 
			EntityType.Builder.<ThrownNuggetBearingEntity>of(ThrownNuggetBearingEntity::new, MobCategory.MISC)
//			.noLootTable()
			.sized(0.125F, 0.125F)
			.clientTrackingRange(4)
			.updateInterval(10)
			.build(createIDFor(key)));

	public static final DeferredHolder<EntityType<?>, EntityType<CrazyDBlockBulletEntity>> CD_BLOCK_BULLET = ENTITY_TYPES.register("cd_block_bullet", key -> 
			EntityType.Builder.<CrazyDBlockBulletEntity>of(CrazyDBlockBulletEntity::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
//			.noSummon()
			.setUpdateInterval(10)
			.build(createIDFor(key)));

	public static final DeferredHolder<EntityType<?>, EntityType<CrazyDBloodCutterEntity>> CD_BLOOD_CUTTER = ENTITY_TYPES.register("cd_blood_cutter", key -> 
			EntityType.Builder.<CrazyDBloodCutterEntity>of(CrazyDBloodCutterEntity::new, MobCategory.MISC)
			.sized(0.5F, 0.5F)
//			.noSummon()
			.setUpdateInterval(10)
			.build(createIDFor(key)));

	public static final DeferredHolder<EntityType<?>, EntityType<StandArrowEntity>> STAND_ARROW = ENTITY_TYPES.register("stand_arrow", key ->
			EntityType.Builder.<StandArrowEntity>of(StandArrowEntity::new, MobCategory.MISC)
			.sized(0.5F, 0.5F).eyeHeight(0.13F)
			.clientTrackingRange(4)
			.updateInterval(20)
			.build(createIDFor(key)));



//	public static ResourceKey<EntityType<?>> createIDFor(ResourceLocation key) {
//		return ResourceKey.create(Registries.ENTITY_TYPE, key);
//	}
	
	public static String createIDFor(ResourceLocation key) {
		return key.toString();
	}
	
}
