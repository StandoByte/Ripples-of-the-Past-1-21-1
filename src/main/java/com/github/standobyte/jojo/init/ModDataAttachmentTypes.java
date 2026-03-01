package com.github.standobyte.jojo.init;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.KnockbackCollisionImpact;
import com.github.standobyte.jojo.mechanics.ServerBlockDestroyTracker;
import com.github.standobyte.jojo.mechanics.clothes.EntityClothesInventory;
import com.github.standobyte.jojo.mechanics.entity_like_player.puppetcontrol.EntityComponentController;
import com.github.standobyte.jojo.mechanics.externalcontainer.PlayerExternalContainers;
import com.github.standobyte.jojo.mechanics.grab.LivingComponentGrab;
import com.github.standobyte.jojo.mechanics.possessionv2.LivingComponentPossession;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInputState;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPower;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.effect.StandEffectsTarget;
import com.github.standobyte.jojo.util.entitycomponent.DataEventListeners;
import com.github.standobyte.jojoimpl.stands.crazydiamond.brokenblocks.BrokenBlocksChunkData;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

public final class ModDataAttachmentTypes {
	public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, JojoMod.MOD_ID);
	
	
	// Entity

	@ApiStatus.Internal
	public static final Supplier<AttachmentType<DataEventListeners>> DATA_EVENT_HELPER = ATTACHMENT_TYPES.register("event_listener", 
			() -> AttachmentType.builder(DataEventListeners::new).build());
	
	public static final Supplier<AttachmentType<StandPower>> STAND_POWER = ATTACHMENT_TYPES.register("stand_power", 
			() -> AttachmentType.serializable(entity -> PowerClass._tryAttach(entity, StandPower::new)).build());

	public static final Supplier<AttachmentType<PlayerPower>> PLAYER_POWER = ATTACHMENT_TYPES.register("player_power", 
			() -> AttachmentType.serializable(entity -> PowerClass._tryAttach(entity, PlayerPower::new)).build());
	
	public static final Supplier<AttachmentType<LivingComponentAction>> LIVING_ACTION = ATTACHMENT_TYPES.register("living_action", 
			() -> AttachmentType.serializable(LivingComponentAction::create).build());

	@ApiStatus.Internal
	public static final Supplier<AttachmentType<EntityActionInputState>> ENTITY_ABILITY_INPUT = ATTACHMENT_TYPES.register("player_ability_input", 
			() -> AttachmentType.builder(obj -> obj instanceof LivingEntity entity ? new EntityActionInputState(entity) : null).build());
	
	public static final Supplier<AttachmentType<EntityClothesInventory>> HUMANOID_CLOTHES = ATTACHMENT_TYPES.register("humanoid_clothes", 
			() -> AttachmentType.serializable(obj -> obj instanceof LivingEntity entity ? new EntityClothesInventory(entity) : null).build());
	
	
	public static final Supplier<AttachmentType<LivingComponentGrab>> LIVING_GRAB = ATTACHMENT_TYPES.register("living_grab", 
			() -> AttachmentType.builder(obj -> obj instanceof LivingEntity entity ? new LivingComponentGrab(entity) : null).build());
	
	public static final Supplier<AttachmentType<KnockbackCollisionImpact>> KB_IMPACT = ATTACHMENT_TYPES.register("kb_impact", 
			() -> AttachmentType.builder(obj -> obj instanceof Entity entity ? new KnockbackCollisionImpact(entity) : null).build());

	public static final Supplier<AttachmentType<EntityComponentController>> CONTROLLER = ATTACHMENT_TYPES.register("controller_player", 
			() -> AttachmentType.builder(obj -> obj instanceof Entity entity ? new EntityComponentController(entity) : null).build());

	public static final Supplier<AttachmentType<LivingComponentPossession>> ENTITY_POSSESSION = ATTACHMENT_TYPES.register("possession", 
			() -> AttachmentType.builder(obj -> obj instanceof Entity entity ? new LivingComponentPossession(entity) : null).build());
	
	
	public static final Supplier<AttachmentType<StandEffectsTarget>> STAND_EFFECTS_TARGET = ATTACHMENT_TYPES.register("stand_effects_target", 
			() -> AttachmentType.builder(obj -> obj instanceof LivingEntity entity ? new StandEffectsTarget(entity) : null).build());
	
	public static final Supplier<AttachmentType<PlayerExternalContainers>> EXTERNAL_CONTAINERS = ATTACHMENT_TYPES.register("external_containers", 
			() -> AttachmentType.builder(entity -> entity instanceof Player player ? new PlayerExternalContainers(player) : null).build());
	
	
	// Level
	
	public static final Supplier<AttachmentType<ServerBlockDestroyTracker>> BLOCK_DESTROY = ATTACHMENT_TYPES.register("block_destroy",
			() -> AttachmentType.builder(obj -> obj instanceof ServerLevel level ? new ServerBlockDestroyTracker(level) : null).build());
	
	
	// Chunk
	
	public static final Supplier<AttachmentType<BrokenBlocksChunkData>> BROKEN_BLOCKS = ATTACHMENT_TYPES.register("broken_blocks", 
			() -> AttachmentType.serializable(obj -> obj instanceof LevelChunk chunk ? new BrokenBlocksChunkData(chunk) : null).build());
	
}
