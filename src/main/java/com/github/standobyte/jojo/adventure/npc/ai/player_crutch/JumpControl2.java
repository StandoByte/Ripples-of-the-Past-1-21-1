package com.github.standobyte.jojo.adventure.npc.ai.player_crutch;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.JumpControl;

public class JumpControl2 extends JumpControl {
	protected LivingEntity entity;

	public JumpControl2(_MobAIVanillaClassesHelper playerAi, LivingEntity entity) {
		super(entity instanceof Mob mob ? mob : null);
		this.entity = entity;
	}

	@Override
	public void tick() {
		entity.setJumping(this.jump);
	}

}
