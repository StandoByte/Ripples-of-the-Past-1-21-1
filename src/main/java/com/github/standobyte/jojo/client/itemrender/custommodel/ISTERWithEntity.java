package com.github.standobyte.jojo.client.itemrender.custommodel;

import javax.annotation.Nullable;

import net.minecraft.world.entity.LivingEntity;

public interface ISTERWithEntity {
	void setEntity(@Nullable LivingEntity entity);
}
