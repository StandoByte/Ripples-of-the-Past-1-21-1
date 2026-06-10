package com.github.standobyte.jojo.adventure.character;

import javax.annotation.Nonnull;

import com.github.standobyte.jojo.adventure.npc.PowerUserMobEntity;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.entityattachment.ComponentUtil;
import com.github.standobyte.jojo.entityattachment.SynchronizablePlayerData;
import com.github.standobyte.jojo.entityattachment.TickingEntityData;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.util.functions.NBTUtil;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.network.PacketDistributor;

// why do "character" (of a story) and "character" (as in a symbol) have the same word? English is so stupid sometimes lol
@EventBusSubscriber(modid = JojoMod.MOD_ID)
public class CharacterPersonData implements SynchronizablePlayerData, TickingEntityData, INBTSerializable<CompoundTag> {
	public final LivingEntity entity;
	
	@Nonnull public CharacterSpecies species = CharacterSpecies.HUMAN;
	
	public CharacterPersonData(LivingEntity entity) {
		this.entity = entity;
		addSynchronization(entity);
		addTicking(entity);
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
		nbt.put("Species", species.toNBT());
		return nbt;
	}

	@Override
	public void deserializeNBT(Provider provider, CompoundTag nbt) {
		this.age = nbt.getLong("Age");
		this.species = NBTUtil.getCompoundOptional(nbt, "Species")
				.map(CharacterSpecies::fromNBT).orElse(CharacterSpecies.HUMAN);
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
