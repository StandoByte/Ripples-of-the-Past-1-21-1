package com.github.standobyte.jojo;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.subsystems.itemtracking.ItemTracking;
import com.github.standobyte.jojo.util.functions.NBTUtil;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

/**
 * Don't forget to call {@link SavedData#setDirty()} when changing the values, 
 * otherwise the change won't be saved to the disk.
 */
public class ServerSavedData extends SavedData {
    public ItemTracking itemsTracker = new ItemTracking(this);
    private Object2IntMap<String> intCounters = new Object2IntOpenHashMap<>();
    private Object2BooleanMap<String> bools = new Object2BooleanOpenHashMap<>();
    
    
    public void setInt(String key, int value) {
    	int oldValue = intCounters.put(key, value);
    	if (oldValue != value) {
    		setDirty();
    	}
    }
    
    public int getInt(String key, int defaultValue) {
    	return intCounters.getOrDefault(key, defaultValue);
    }
    
    
    public void setBoolean(String key, boolean value) {
    	if (bools.put(key, value) != value) {
    		setDirty();
    	}
    }
    
    public boolean getBoolean(String key, boolean defaultValue) {
    	return bools.getOrDefault(key, defaultValue);
    }
    

	@Override
	public CompoundTag save(CompoundTag nbt, HolderLookup.Provider registries) {
		nbt.put("ItemTrackers", itemsTracker.serializeNBT(registries));
		
		if (!intCounters.isEmpty()) {
			CompoundTag intsNbt = new CompoundTag();
			for (var entry : intCounters.object2IntEntrySet()) {
				intsNbt.putInt(entry.getKey(), entry.getIntValue());
			}
			nbt.put("ints", intsNbt);
		}
		
		if (!bools.isEmpty()) {
			CompoundTag boolsNbt = new CompoundTag();
			for (var entry : bools.object2BooleanEntrySet()) {
				boolsNbt.putBoolean(entry.getKey(), entry.getBooleanValue());
			}
			nbt.put("bools", boolsNbt);
		}
		
		return nbt;
	}
	
    public static ServerSavedData load(CompoundTag nbt, HolderLookup.Provider registries) {
    	ServerSavedData data = new ServerSavedData();
    	NBTUtil.getElementOptional(nbt, "ItemTrackers", ListTag.class).ifPresent(
    			trackersNbt -> data.itemsTracker.deserializeNBT(registries, trackersNbt));
    	
    	NBTUtil.getCompoundOptional(nbt, "ints").ifPresent(intsNbt -> {
    		for (String key : intsNbt.getAllKeys()) {
    			data.intCounters.put(key, intsNbt.getInt(key));
    		}
    	});
    	
    	NBTUtil.getCompoundOptional(nbt, "bools").ifPresent(boolsNbt -> {
    		for (String key : boolsNbt.getAllKeys()) {
    			data.bools.put(key, boolsNbt.getBoolean(key));
    		}
    	});
        return data;
    }
    
	
    protected static final String fileName = JojoMod.MOD_ID + "-server_data";
	public static ServerSavedData get(MinecraftServer server) {
		DimensionDataStorage storage = server.overworld().getDataStorage();
		return storage.computeIfAbsent(new SavedData.Factory<>(
				ServerSavedData::new, ServerSavedData::load), fileName);
	}

}
