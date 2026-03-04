package com.github.standobyte.jojo.util;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.packet.fromserver.StandSkinSoundPacket;
import com.github.standobyte.jojo.init.core.ModEntityAttributes;
import com.github.standobyte.jojo.mechanics.grab.LivingComponentGrab;
import com.github.standobyte.jojo.modcompat.JojoModsInteraction;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.powersystem.standpower.resolve.ResolveModeEffect;
import com.github.standobyte.jojo.util.mc.AttributeUtil;

import net.minecraft.core.Holder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;

public class StandUtil {

    public static LivingEntity getStandUser(LivingEntity entityMaybeStand) {
        if (entityMaybeStand instanceof StandEntity stand) {
            LivingEntity user = stand.getUser();
            if (user != null) return user;
        }
        return entityMaybeStand;
    }
    
    @Nullable
    public static StandEntity getSummonedStand(LivingEntity standUser) {
    	StandPower standPower = StandPower.get(standUser);
    	return standPower != null ? standPower.getSummonedStandEntity() : null;
    }

    public static StandEntity getSummonedStand(Power<?> standPower) {
    	StandPower _standPower = PowerClass.STAND.cast(standPower);
    	return _standPower != null ? _standPower.getSummonedStandEntity() : null;
    }
    
    public static class StandAndUserEntity {
    	protected static StandAndUserEntity instance = new StandAndUserEntity();
    	
    	@Nullable public LivingEntity standUser;
    	@Nullable public LivingEntity standEntity;
    }
    
    public static StandAndUserEntity getStandAndUser(LivingEntity someEntity) {
    	LivingEntity targetStandEntity = StandUtil.getSummonedStand(someEntity);
    	LivingEntity targetStandUser = someEntity == targetStandEntity ? StandUtil.getStandUser(targetStandEntity) : someEntity;
    	StandAndUserEntity obj = StandAndUserEntity.instance;
    	obj.standUser = targetStandUser;
    	obj.standEntity = targetStandEntity;
    	return obj;
    }
    
    public static boolean isEntityStandUser(LivingEntity entity) {
    	StandPower standData = StandPower.get(entity);
    	return standData != null && standData.hasPower() || JojoModsInteraction.entityHasStandFromAnotherMod(entity);
    }

    public static boolean entityCanSeeStands(LivingEntity entity) {
    	return entity.isSpectator()
    			|| isEntityStandUser(entity) /*|| player.hasEffect(ModStatusEffects.SPIRIT_VISION.get())*/;
    	// TODO spirit vision effect
    }

    public static boolean entityCanHearStands(Player player) {
    	return entityCanSeeStands(player);
    }

    @Nullable
	public static LivingEntity getStandGrabTarget(Power<?> power) {
		StandPower standPower = PowerClass.STAND.cast(power);
		if (standPower != null) {
			StandEntity standEntity = standPower.getSummonedStandEntity();
			if (standEntity != null) {
				return LivingComponentGrab.getEntityGrabbedBy(standEntity);
			}
		}
		
		return null;
	}
	
	public static boolean standIgnoresStaminaDebuff(LivingEntity standUser) {
		return ResolveModeEffect.getResolveEffectLvl(standUser) >= 0;
	}
	
	public static double staminaCondition(StandPower standPower) {
		return standIgnoresStaminaDebuff(standPower.getUser()) ? 1
				: 0.25 + Math.min((double) (standPower.getStamina() / standPower.getMaxStamina()) * 1.5, 0.75);
	}
	
	
	public static double getPhysicalStatValue(StandPower standPower, StandStat stat) {
		StandEntity standEntity = standPower.getSummonedStandEntity();
		LivingEntity user = standPower.getUser();
		if (standEntity != null) {
			return switch (stat) {
				case STRENGTH -> standEntity.getAttackDamage();
				case ATTACK_SPEED -> standEntity.getAttackSpeed();
				case DURABILITY -> standEntity.getDurability();
				case PRECISION -> standEntity.getPrecision();
			};
		}
		else if (user != null) {
			Holder<Attribute> attribute = switch (stat) {
				case STRENGTH -> ModEntityAttributes.STAND_STRENGTH;
				case ATTACK_SPEED -> ModEntityAttributes.STAND_SPEED;
				case DURABILITY -> ModEntityAttributes.STAND_DURABILITY;
				case PRECISION -> ModEntityAttributes.STAND_PRECISION;
			};
			return AttributeUtil.getValueOrDefault(user, attribute, 0) * staminaCondition(standPower);
		}
		
		else return 0;
	}
	
	public enum StandStat {
		STRENGTH,
		ATTACK_SPEED,
		DURABILITY,
		PRECISION
	}
	
	
	public static void broadcastSound(ServerLevel level, Vec3 pos, Holder<SoundEvent> sound, 
			boolean onlyForStandUsers, StandPower userPower, 
			SoundSource category, float volume, float pitch) {
		PlayLevelSoundEvent.AtPosition event = EventHooks.onPlaySoundAtPosition(level, pos.x, pos.y, pos.z, sound, category, volume, pitch);
		if (event.isCanceled() || event.getSound() == null) return;
		
		sound = event.getSound();
		category = event.getSource();
		volume = event.getNewVolume();
		pitch = event.getNewPitch();
		
		StandSkinSoundPacket packet = StandSkinSoundPacket.play(pos, sound, userPower, category, volume, pitch);
		double radius = sound.value().getRange(volume);
        Packet<?> vanillaPacket = new ClientboundCustomPayloadPacket(packet);
        PlayerList playerList = level.getServer().getPlayerList();
        ResourceKey<Level> dimension = level.dimension();
        for (ServerPlayer player : playerList.getPlayers()) {
        	if (player.level().dimension() == dimension && (!onlyForStandUsers || StandUtil.entityCanHearStands(player))) {
        		double diffX = pos.x - player.getX();
        		double diffY = pos.y - player.getY();
        		double diffZ = pos.z - player.getZ();
        		if (diffX * diffX + diffY * diffY + diffZ * diffZ < radius * radius) {
        			player.connection.send(vanillaPacket);
        		}
        	}
        }
	}

}
