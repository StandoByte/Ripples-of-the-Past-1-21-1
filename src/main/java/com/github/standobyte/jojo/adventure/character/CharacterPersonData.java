package com.github.standobyte.jojo.adventure.character;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.adventure.npc.PowerUserMobEntity;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.entityattachment.ComponentUtil;
import com.github.standobyte.jojo.entityattachment.SynchronizablePlayerData;
import com.github.standobyte.jojo.entityattachment.TickingEntityData;
import com.github.standobyte.jojo.entityattachment.syncheddata.DataParameter;
import com.github.standobyte.jojo.entityattachment.syncheddata.SyncedDataHolderExtended;
import com.github.standobyte.jojo.entityattachment.syncheddata.SynchedDataHelper;
import com.github.standobyte.jojo.entityattachment.syncheddata.SynchedDataPacket;
import com.github.standobyte.jojo.entityattachment.syncheddata.SynchedDataPacketHandler;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPower;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPowerData;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.util.functions.NBTUtil;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class CharacterPersonData implements SynchronizablePlayerData, TickingEntityData, INBTSerializable<CompoundTag>, SyncedDataHolderExtended {
	public final LivingEntity entity;
	public final SynchedDataHelper synchedData;

	public static final DataParameter<String> NATIVE_SPECIES_NAME = DataParameter.defineId(
			CharacterPersonData.class, EntityDataSerializers.STRING, "human");
	@Nonnull private CharacterSpecies nativeSpecies = CharacterSpecies.HUMAN;
	@Nonnull private CharacterSpecies __species = CharacterSpecies.HUMAN;
	
	private static final String SYNCHED_PACKET_HANDLER_TYPE = "chr";
	static {
		SynchedDataPacket.Handler.specificHandlers.put(SYNCHED_PACKET_HANDLER_TYPE, new SynchedDataPacketHandler() {

			@Override
			public SynchedDataHelper getDataSyncHelper(Entity entity) {
				if (entity instanceof LivingEntity living && isCharacter(living)) {
					CharacterPersonData characterData = entity.getData(ModDataAttachmentTypes.CHARACTER_DATA);
					return characterData.synchedData;
				}
				return null;
			}

			@Override
			public SynchedDataHelper getOrCreateDataSyncHelper(Entity entity) {
				CharacterPersonData characterData = ComponentUtil.getExistingDataOrNull(entity, ModDataAttachmentTypes.CHARACTER_DATA);
				return characterData != null ? characterData.synchedData : null;
			}
			
		});
	}
	
	public CharacterPersonData(LivingEntity entity) {
		this.entity = entity;
		this.synchedData = new SynchedDataHelper(SYNCHED_PACKET_HANDLER_TYPE, this, () -> entity.level().isClientSide());
		addSynchronization(entity);
		addTicking(entity);
		addSynchedData(entity, synchedData);
	}

	@Override
	public void defineSynchedData(Builder builder) {
		NATIVE_SPECIES_NAME.define(builder);
	}

	@Override
	public <V> void onSyncedDataUpdated(V oldValue, V newValue, EntityDataAccessor<V> dataAccessor) {
		if (dataAccessor == NATIVE_SPECIES_NAME.param) {
			nativeSpecies = CharacterSpecies.fromName((String) newValue);
		}
	}

	
	public static long BABY_START_AGE = -384000;
	protected long prevAge = 0;
	protected long age = 0;
	
	public long getAge() {
		return age;
	}
	
	public void setAge(long age) {
		this.age = age;
		sync();
	}
	
	@Override
	public void tick() {
		this.age++;
		if (prevAge < 0 && age >= 0 || prevAge >= 0 && age < 0) {
			entity.refreshDimensions();
		}
		this.prevAge = age;
		
		tickCheckSpecies();
	}
	
	
	@ApiStatus.Internal
	public void __initializeCharacterSpecies(CharacterSpecies species) {
		synchedData.set(NATIVE_SPECIES_NAME.param, species.name);
	}
	
	private void tickCheckSpecies() {
		/* this doesn't need to run every tick, but trying to optimize every single time
		 * is making my head explode at this point, we'll roll with that for now
		 */
		PlayerPower playerPower = PlayerPower.get(entity);
		StandPower standPower = StandPower.get(entity);
		CharacterSpecies species = CharacterSpecies.replaceSpeciesIfIncompatible(nativeSpecies, 
				playerPower != null ? (PlayerPowerData) playerPower.getCurTypeData() : null, 
				standPower != null ? standPower.getPowerType() : null);
		if (this.__species != species) {
			if (!entity.level().isClientSide()) {
				CharacterSpecies.updateSpeciesAttributes(entity, __species, species);
			}
			this.__species = species;
		}
	}
	
	public CharacterSpecies getSpecies() {
		return __species;
	}

	
	
	public void toBuf(RegistryFriendlyByteBuf buf) {
		buf.writeLong(age);
	}
	
	public void fromBuf(RegistryFriendlyByteBuf buf) {
		age = buf.readLong();
	}
	


	@Override
	public void syncToTracking(ServerPlayer trackingPlayer) {
		PacketDistributor.sendToPlayer(trackingPlayer, new TrCharacterDataPacket(entity.getId(), this));
	}

	@Override
	public void syncToPlayer(ServerPlayer entityAsPlayer) {
		PacketDistributor.sendToPlayer(entityAsPlayer, new TrCharacterDataPacket(entity.getId(), this));
	}
	
	public void sync() {
		if (entity.isAddedToLevel()) {
			PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new TrCharacterDataPacket(entity.getId(), this));
		}
	}

	@Override
	public void onPlayerClone(Player newPlayer, boolean wasDeath) {
		CharacterPersonData newData = newPlayer.getData(ModDataAttachmentTypes.CHARACTER_DATA);
		newData.age = this.age;
	}
	
	@Override
	public CompoundTag serializeNBT(Provider provider) {
		CompoundTag nbt = new CompoundTag();
		nbt.putLong("Age", age);
		nbt.put("Species", nativeSpecies.toNBT());
		return nbt;
	}

	@Override
	public void deserializeNBT(Provider provider, CompoundTag nbt) {
		this.age = nbt.getLong("Age");
		NBTUtil.getOptional(nbt, "Species", CharacterSpecies::fromNBT).ifPresent(species -> {
			synchedData.set(NATIVE_SPECIES_NAME.param, species.name);	
		});
	}
	


	@SubscribeEvent
	public static void attachOnEntityCreated(EntityJoinLevelEvent event) {
		if (event.getEntity() instanceof LivingEntity living
				&& isCharacter(living)
				&& !living.hasData(ModDataAttachmentTypes.CHARACTER_DATA)) {
			living.getData(ModDataAttachmentTypes.CHARACTER_DATA);
		}
	}
	
	public static CharacterPersonData _attach(IAttachmentHolder entity) {
		if (entity instanceof LivingEntity living) {
			return new CharacterPersonData(living);
		}
		return null;
	}
	
	public static boolean isCharacter(LivingEntity entity) {
		return entity instanceof Player || entity instanceof PowerUserMobEntity;
	}
	
	public static CharacterPersonData get(LivingEntity characterEntity) {
		return ComponentUtil.getExistingDataOrNull(characterEntity, ModDataAttachmentTypes.CHARACTER_DATA);
	}
	
}
