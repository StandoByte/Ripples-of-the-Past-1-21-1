package com.github.standobyte.jojo.powersystem.ability.condition;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.powersystem.ability.Ability;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public record ConditionCheck(boolean positive, boolean greenHighlight, @Nullable Component warning) {
	public static final ConditionCheck POSITIVE = new ConditionCheck(true, null);
	public static final ConditionCheck NEGATIVE = new ConditionCheck(false, null);
	
	public static ConditionCheck createNegative(Component warning) {
		return new ConditionCheck(false, warning);
	}
	
	public static ConditionCheck createNegative(String warningPostfix) {
		return new ConditionCheck(false, message(warningPostfix));
	}
	
	public static MutableComponent message(String warningPostfix) {
		return Component.translatable("jojo.message.action_condition." + warningPostfix);
	}
	
	public static ConditionCheck noMessage(boolean isPositive) {
		return isPositive ? POSITIVE : NEGATIVE;
	}
	
	private ConditionCheck(boolean positive, Component warning) {
		this(positive, false, warning);
	}
	
	@Deprecated /** @deprecated It's a record now, just use positive() */
	public boolean isPositive() {
		return positive;
	}
	
	@Deprecated /** @deprecated It's a record now, just use warning() */
	@Nullable
	public Component getWarning() {
		return warning;
	}
	
	public static void sendActionFailedMessage(@Nullable Ability ability, ConditionCheck result, LivingEntity user) {
		if (!user.level().isClientSide() /* && (ability == null || ability.sendsConditionMessage()) */) {
			Component message = result.warning();
			
			if (message != null && user instanceof ServerPlayer player) {
				player.displayClientMessage(message, true);
			}
		}
	}

}
