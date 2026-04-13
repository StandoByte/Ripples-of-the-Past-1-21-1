package com.github.standobyte.jojo.adventure.npc.ai.player_crutch;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.sensing.Sensing;
import net.minecraft.world.level.Level;

public class Sensing2 extends Sensing {
	protected LivingEntity entity;
	protected final IntSet seen = new IntOpenHashSet();
	protected final IntSet unseen = new IntOpenHashSet();

	public Sensing2(_MobAIVanillaClassesHelper playerAi, LivingEntity entity) {
		super(entity instanceof Mob mob ? mob : null);
		this.entity = entity;
	}

	@Override
	public void tick() {
		this.seen.clear();
		this.unseen.clear();
	}

	@Override
	public boolean hasLineOfSight(Entity entity) {
		int i = entity.getId();
		if (this.seen.contains(i)) {
			return true;
		} else if (this.unseen.contains(i)) {
			return false;
		} else {
			Level level = this.entity.level();
			level.getProfiler().push("hasLineOfSight");
			boolean flag = this.entity.hasLineOfSight(entity);
			level.getProfiler().pop();
			if (flag) {
				this.seen.add(i);
			} else {
				this.unseen.add(i);
			}

			return flag;
		}
	}

}
