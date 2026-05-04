package com.github.standobyte.jojoimpl.stands.theworld.timestop.level;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/* because on the client side, the time stopping player may be outside of the tracking range,
 * and since the time stop effects are tied to that player's StandPower, in this case the client won't know about them
 */
public class TimeStopClientLevelTracker {
	public Int2ObjectMap<TimeStopClientLevelInstance> activeEffects = new Int2ObjectArrayMap<>();
	public ResourceKey<Level> dimension;
	
	
	
	public void clear() {
		this.activeEffects.clear();
		this.dimension = null;
	}

}
