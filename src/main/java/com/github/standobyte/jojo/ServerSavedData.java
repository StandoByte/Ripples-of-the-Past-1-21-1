package com.github.standobyte.jojo;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.subsystems.itemtracking.ItemTracking;
import com.github.standobyte.jojo.util.functions.NBTUtil;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class ServerSavedData extends SavedData {
    public ItemTracking itemsTracker = new ItemTracking(this);

	@Override
	public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
		CompoundTag nbt = new CompoundTag();
		nbt.put("ItemTrackers", itemsTracker.serializeNBT(registries));
		return nbt;
	}
	
    public static ServerSavedData load(CompoundTag nbt, HolderLookup.Provider registries) {
    	ServerSavedData data = new ServerSavedData();
    	NBTUtil.getElementOptional(nbt, "ItemTrackers", ListTag.class).ifPresent(
    			trackersNbt -> data.itemsTracker.deserializeNBT(registries, trackersNbt));
        return data;
    }
    
	
    protected static final String fileName = JojoMod.MOD_ID + "-server_data";
	public static ServerSavedData get(MinecraftServer server) {
		DimensionDataStorage storage = server.overworld().getDataStorage();
		return storage.computeIfAbsent(new SavedData.Factory<>(
				ServerSavedData::new, ServerSavedData::load), fileName);
	}

}
