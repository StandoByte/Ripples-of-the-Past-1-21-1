package com.github.standobyte.jojo.config.internal.cfgtypes;

import java.io.File;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.config.internal.ConfigNetworkFunctions;
import com.github.standobyte.jojo.config.internal.ConfigObjSerialization;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.util.functions.JSONUtil;
import com.google.gson.JsonObject;

public class ClientFileConfig<C1, C2> {
	@Nullable public final ConfigObjSerialization<C1> localOnly;
	@Nullable public final ConfigObjSerialization<C2> broadcast;
	protected File file;

	public ClientFileConfig(C1 localOnly, C2 broadcast, File mainDirectory, String modId) {
		this.localOnly = ConfigObjSerialization.create(localOnly);
		this.broadcast = ConfigObjSerialization.create(broadcast);
		
		String filePrefix = modId.equals(JojoMod.MOD_ID) ? "" : (modId + "_");
		this.file = new File(mainDirectory, "config/jojo_rotp/" + filePrefix + "client_settings.json");
	}
	
	public void sendToServer() {
		if (ConfigNetworkFunctions.clientIsConnectedToAServer()) {
			ConfigNetworkFunctions.clSendPlayerBroadcastConfigToServer(true, 
					JojoMod.MOD_ID, this);
		}
	}
	
	public boolean exists() {
		return localOnly != null || broadcast != null;
	}
	
	public void loadFromFileSystem() {
		if (exists()) {
			CommonFileConfig.deserialize(file, reader -> {
				JsonObject json = JSONUtil.parse(reader);
				if (localOnly != null) {
					localOnly.fromJson(json);
				}
				
				if (broadcast != null) {
					JsonObject broadcastJson = json.getAsJsonObject("broadcasted");
					if (broadcastJson == null) broadcastJson = new JsonObject();
					broadcast.fromJson(broadcastJson);
				}
			});
		}
	}
	
	public void saveToFileSystem() {
		if (exists()) {
			CommonFileConfig.serialize(file, writer -> {
				JsonObject json = localOnly != null ? localOnly.toJson() : new JsonObject();
				if (broadcast != null) {
					json.add("broadcasted", broadcast.toJson());
				}
				CommonFileConfig.GSON.toJson(json, writer);
			});
		}
	}
	
	public void reset() {
		if (localOnly != null) {
			localOnly.reset();
		}
		if (broadcast != null) {
			broadcast.reset();
		}
	}
	
}
