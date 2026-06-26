package com.github.standobyte.jojo.powersystem.ability;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.powersystem.Moveset;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.PowerType;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public record AbilityId(@Nullable PowerClass<?> powerClass, ResourceLocation powerTypeId, String nameInMoveset) {
	
	static <A extends Ability> A makeDefaultAbilityInstance(AbilityType<A> abilityType) {
		return abilityType.createInstance(new AbilityId(null, null, abilityType.registryKey.toString()));
	}
	
	@Override
	public String toString() {
		if (powerClass != null) {
			return powerClass.toString() + "#" + powerTypeId.toString() + "/" + nameInMoveset;
		}
		else {
			return powerTypeId.toString() + "/" + nameInMoveset;
		}
	}
	
	public static AbilityId parse(String string) {
		int namePos = string.indexOf("/");
		if (namePos > 0) {
			PowerClass<?> powerClass = null;
			int powerClassPos = string.indexOf("#");
			if (powerClassPos > 0 && powerClassPos < namePos) {
				powerClass = PowerClass.fromName(string.substring(0, powerClassPos));
			}
			ResourceLocation powerTypeId = ResourceLocation.tryParse(string.substring(powerClassPos + 1, namePos));
			if (powerTypeId != null) {
				String name = string.substring(namePos + 1);
				return new AbilityId(powerClass, powerTypeId, name);
			}
		}
		return null;
	}


	public static class AbilityInputNetwork {
		private final int userId;
		private final SyncStrategy syncStrategy;
		private final PowerClass<?> powerClass;
		private final ResourceLocation powerTypeId;
		private final String abilityName;

		AbilityInputNetwork(SyncStrategy syncStrategy, int userId, PowerClass<?> powerClass, ResourceLocation powerTypeId, String abilityName) {
			this.userId = userId;
			this.syncStrategy = syncStrategy;
			this.powerClass = powerClass;
			this.powerTypeId = powerTypeId;
			this.abilityName = abilityName;
		}


		public static void encodeInput(FriendlyByteBuf buffer, LivingEntity user, @Nullable Ability ability) {
			encodeInput(buffer, user, ability != null ? ability.abilityId : null);
		}

		public static void encodeInput(FriendlyByteBuf buffer, LivingEntity user, @Nullable AbilityId abilityId) {
			if (abilityId == null) {
				buffer.writeEnum(SyncStrategy.NULL_ABILITY);
				return;
			}
			
			SyncStrategy strategy = SyncStrategy.FROM_POWER_TYPE_MOVESET;
			if (abilityId.powerTypeId == null) {
				strategy = SyncStrategy.DEFAULT_ABILITY_TYPE_INSTANCE;
			}
			else if (user != null) {
				// a bit of server computation as a tradeoff for less packet size (to not send the power type id)
				Power<?> power = abilityId.powerClass.get(user);
				if (power != null && power.hasPower() && abilityId.powerTypeId.equals(power.getPowerType().getId())) {
					strategy = SyncStrategy.FROM_PLAYER_MOVESET;
				}
			}
			buffer.writeEnum(strategy);
			
			switch (strategy) {
				case FROM_PLAYER_MOVESET -> {
					buffer.writeInt(user.getId());
					PowerClass.NETWORK_CODEC.encode(buffer, abilityId.powerClass());
					buffer.writeUtf(abilityId.nameInMoveset());
				}
				case FROM_POWER_TYPE_MOVESET -> {
					PowerClass.NETWORK_CODEC.encode(buffer, abilityId.powerClass());
					buffer.writeResourceLocation(abilityId.powerTypeId());
					buffer.writeUtf(abilityId.nameInMoveset());
				}
				case DEFAULT_ABILITY_TYPE_INSTANCE -> {
					buffer.writeUtf(abilityId.nameInMoveset());
				}
				default -> {}
			}
		}


		public static AbilityInputNetwork decodeInput(FriendlyByteBuf buffer) {
			SyncStrategy strategy = buffer.readEnum(SyncStrategy.class);
			return switch (strategy) {
				case NULL_ABILITY -> new AbilityInputNetwork(SyncStrategy.NULL_ABILITY, 0, null, null, null);
				case FROM_PLAYER_MOVESET -> {
					int userId = buffer.readInt();
					PowerClass<?> powerClass = PowerClass.NETWORK_CODEC.decode(buffer);
					String abilityName = buffer.readUtf();
					
					yield new AbilityInputNetwork(SyncStrategy.FROM_PLAYER_MOVESET, userId, powerClass, null, abilityName);
				}
				case FROM_POWER_TYPE_MOVESET -> {
					PowerClass<?> powerClass = PowerClass.NETWORK_CODEC.decode(buffer);
					ResourceLocation powerTypeId = buffer.readResourceLocation();
					String abilityName = buffer.readUtf();

					yield new AbilityInputNetwork(SyncStrategy.FROM_POWER_TYPE_MOVESET, 0, powerClass, powerTypeId, abilityName);
				}
				case DEFAULT_ABILITY_TYPE_INSTANCE -> {
					String abilityName = buffer.readUtf();
					
					yield new AbilityInputNetwork(SyncStrategy.DEFAULT_ABILITY_TYPE_INSTANCE, 0, null, null, abilityName);
				}
			};
		}
		
		public Ability getAbility(LivingEntity powerUser, @Nullable Level inCaseUserIsNotResolved) {
			Ability ability;
			return switch (syncStrategy) {
				case NULL_ABILITY -> null;
				case FROM_PLAYER_MOVESET -> {
					if (powerUser == null) {
						Entity entity = inCaseUserIsNotResolved.getEntity(userId);
						if (entity instanceof LivingEntity living) powerUser = living;
					}

					if (powerUser == null) throw new IllegalStateException("Failed to sync ability " + abilityName + " (needs user entity)");
					Power<?> userPower = powerClass.get(powerUser);
					if (userPower == null) throw new IllegalStateException("Failed to sync ability " + abilityName + " (user data not attached)");
					
					ability = userPower.getAbility(abilityName);
					if (ability == null) {
						if (userPower.hasPower()) throw new IllegalStateException("Failed to sync ability " + abilityName + " (ability not found in the moveset " + userPower.getPowerType().getId() + " of " + powerUser.getName().getString() + ")");
						else throw new IllegalStateException("Failed to sync ability " + abilityName + " (" + powerUser.getName().getString() + " has no moveset of " + userPower.getClass() + " class)");
					}
					
					yield ability;
				}
				case FROM_POWER_TYPE_MOVESET -> {
					PowerType powerType = powerClass.getPowerType(powerTypeId);
					if (powerType == null) throw new IllegalStateException("Failed to sync ability " + abilityName + " (moveset " + powerTypeId + " not found)");
					
					Moveset moveset = powerType.getBaseMoveset();
					ability = moveset.getAbility(abilityName);
					if (ability == null) throw new IllegalStateException("Failed to sync ability " + abilityName + " (ability not found in the moveset " + powerTypeId + ")");
					
					yield ability;
				}
				case DEFAULT_ABILITY_TYPE_INSTANCE -> {
					ResourceLocation abilityTypeId = ResourceLocation.parse(abilityName);
					AbilityType<?> abilityType = JojoRegistries.ABILITY_TYPES_REG.get(abilityTypeId);
					
					yield abilityType != null ? abilityType._defaultAbilityInstance : null;
				}
			};
		}
		
		enum SyncStrategy {
			NULL_ABILITY,
			DEFAULT_ABILITY_TYPE_INSTANCE,
			FROM_POWER_TYPE_MOVESET,
			FROM_PLAYER_MOVESET;
		}
	}
	
}
