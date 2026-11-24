package com.github.standobyte.jojo.network.s2c;

import java.util.Optional;

import com.github.standobyte.jojo.PacketsRegister;
import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.customobjects.DamageSourceModified;
import com.github.standobyte.jojo.mixin.LivingEntityAccessor;
import com.github.standobyte.jojo.util.functions.BitwiseFlagUtil;

import net.minecraft.core.Holder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class DamageEventExtendedPacket implements CustomPacketPayload {
	private final int targetEntityId;
	private final Holder<DamageType> sourceType;
	private final int sourceCauseId;
	private final int sourceDirectId;
	private final Optional<Vec3> sourcePosition;
	
	private final boolean noHurtSound;
	private final boolean noFlinchModelAnim;
	private final float cameraTiltMult;
	
	public DamageEventExtendedPacket(Entity target, DamageSource dmgSource, 
			boolean noHurtSound, boolean noFlinchModelAnim, float cameraTiltMult) {
		this(
			target.getId(),
			dmgSource.typeHolder(),
			dmgSource.getEntity() != null ? dmgSource.getEntity().getId() : -1,
			dmgSource.getDirectEntity() != null ? dmgSource.getDirectEntity().getId() : -1,
			Optional.ofNullable(dmgSource.sourcePositionRaw()),
			
			noHurtSound, noFlinchModelAnim, cameraTiltMult
		);
	}
	
	public DamageEventExtendedPacket(int targetEntityId, Holder<DamageType> sourceType, 
			int sourceCauseId, int sourceDirectId, Optional<Vec3> sourcePosition,
			boolean noHurtSound, boolean noFlinchModelAnim, float cameraTiltMult) {
		this.targetEntityId = targetEntityId;
		this.sourceType = sourceType;
		this.sourceCauseId = sourceCauseId;
		this.sourceDirectId = sourceDirectId;
		this.sourcePosition = sourcePosition;
		
		this.noHurtSound = noHurtSound;
		this.noFlinchModelAnim = noFlinchModelAnim;
		this.cameraTiltMult = cameraTiltMult;
	}

	private static CustomPacketPayload.Type<DamageEventExtendedPacket> type;
	
	public static class Handler implements PacketsRegister.PacketOGHandler<DamageEventExtendedPacket> {
		
		public Handler(ResourceLocation packetId) { 
			type = new CustomPacketPayload.Type<>(packetId);
		}

		@Override
		public Type<DamageEventExtendedPacket> type() {
			return type;
		}

		@Override
		public void encode(DamageEventExtendedPacket packet, RegistryFriendlyByteBuf buf) {
			buf.writeVarInt(packet.targetEntityId);
			DamageType.STREAM_CODEC.encode(buf, packet.sourceType);
			writeOptionalEntityId(buf, packet.sourceCauseId);
			writeOptionalEntityId(buf, packet.sourceDirectId);
			buf.writeOptional(packet.sourcePosition, (_buf, vec) -> {
				_buf.writeDouble(vec.x());
				_buf.writeDouble(vec.y());
				_buf.writeDouble(vec.z());
			});
			
			buf.writeByte(BitwiseFlagUtil.packUpTo8Flags(
					packet.noHurtSound,
					packet.noFlinchModelAnim));
			buf.writeFloat(packet.cameraTiltMult);
		}

		@Override
		public DamageEventExtendedPacket decode(RegistryFriendlyByteBuf buf) {
			int targetEntityId = buf.readVarInt();
			Holder<DamageType> sourceType = DamageType.STREAM_CODEC.decode(buf);
			int sourceCauseId = readOptionalEntityId(buf);
			int sourceDirectId = readOptionalEntityId(buf);
			Optional<Vec3> sourcePosition = buf.readOptional(_buf -> new Vec3(_buf.readDouble(), _buf.readDouble(), _buf.readDouble()));
			
			boolean[] flags = BitwiseFlagUtil.unpack(buf.readByte(), 2);
			boolean noHurtSound = flags[0];
			boolean noFlinchModelAnim = flags[1];
			float cameraTiltMult = buf.readFloat();
			
			return new DamageEventExtendedPacket(
					targetEntityId,
					sourceType,
					sourceCauseId,
					sourceDirectId,
					sourcePosition,
					
					noHurtSound, noFlinchModelAnim, cameraTiltMult);
		}
		
		private static void writeOptionalEntityId(FriendlyByteBuf buffer, int optionalEntityId) {
			buffer.writeVarInt(optionalEntityId + 1);
		}

		private static int readOptionalEntityId(FriendlyByteBuf buffer) {
			return buffer.readVarInt() - 1;
		}

		@Override
		public void handle(DamageEventExtendedPacket payload, IPayloadContext context) {
			Entity entity = ClientProxy.getEntityById(payload.targetEntityId);
			if (entity instanceof LivingEntity targetEntity) {
				DamageSource damageSource;
				if (payload.sourcePosition.isPresent()) {
					damageSource = new DamageSource(payload.sourceType, payload.sourcePosition.get());
				} else {
					Entity causingEntity = ClientProxy.getEntityById(payload.sourceCauseId);
					Entity directEntity = ClientProxy.getEntityById(payload.sourceDirectId);
					damageSource = new DamageSource(payload.sourceType, directEntity, causingEntity);
				}
				
				DamageSourceModified _damageSource = (DamageSourceModified) damageSource;
				// technically these two are not necessary
				//if (payload.noHurtSound) _damageSource.jojo_ripples$setNoHurtSound(true);
				//if (payload.noFlinchModelAnim) _damageSource.jojo_ripples$setNoFlinchModelAnim(true);
				_damageSource.jojo_ripples$setCameraTiltMultiplier(payload.cameraTiltMult);
				
				if (!payload.noFlinchModelAnim) {
					targetEntity.walkAnimation.setSpeed(1.5F);
				}
				targetEntity.invulnerableTime = 20;
				targetEntity.hurtDuration = 10;
				targetEntity.hurtTime = targetEntity.hurtDuration;
				
				LivingEntityAccessor _targetEntity = (LivingEntityAccessor) targetEntity;
				if (!payload.noHurtSound) {
					SoundEvent hurtSound = _targetEntity.invokeGetHurtSound(damageSource);
					if (hurtSound != null) {
						RandomSource random = targetEntity.getRandom();
						targetEntity.playSound(hurtSound, 
								_targetEntity.invokeGetSoundVolume(), 
								(random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
					}
				}

				targetEntity.hurt(targetEntity.damageSources().generic(), 0.0F); // why do they do this anyway?
				_targetEntity.setLastDamageSource(damageSource);
				_targetEntity.setLastDamageStamp(targetEntity.level().getGameTime());
			}
		}
		
	}
	
	@Override
	public Type<? extends CustomPacketPayload> type() {
		return type;
	}
	
}
