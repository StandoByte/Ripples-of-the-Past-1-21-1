package com.github.standobyte.jojoimpl.powers.vampirism.abilities;

import com.github.standobyte.jojo.util.functions.DamageUtil;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class VampFreezeAbility {

	public static DamageSource freezeDamageSource(Entity vampireEntity) {
		Holder<DamageType> type = DamageUtil.type(vampireEntity.level(), DamageTypes.FREEZE);
		return new DamageSource(type, vampireEntity, vampireEntity, null) {

			@Override
			public Component getLocalizedDeathMessage(LivingEntity deadEntity) {
				Entity causingEntity = this.getEntity();
				Entity directEntity = this.getDirectEntity();
				String tlKey = "death.attack." + this.type().msgId();
				if (causingEntity == null && directEntity == null) {
					return Component.translatable(tlKey, deadEntity.getDisplayName());
				} else {
					Entity killer = causingEntity == null ? directEntity : causingEntity;
					Component killerName = killer.getDisplayName();
					return Component.translatable(tlKey + ".player", deadEntity.getDisplayName(), killerName);
				}
			}
		};
	}
}
