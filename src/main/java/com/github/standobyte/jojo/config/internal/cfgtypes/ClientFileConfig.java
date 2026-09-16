package com.github.standobyte.jojo.config.internal.cfgtypes;

import java.io.File;

import com.github.standobyte.jojo.config.internal.ConfigNetworkFunctions;
import com.github.standobyte.jojo.config.internal.ConfigObjSerialization;
import com.github.standobyte.jojo.core.JojoMod;
import com.google.gson.JsonObject;

public class ClientFileConfig<C1, C2> extends ClientConfig<C1, C2> {
	protected File file;

	public ClientFileConfig(C1 localOnly, C2 broadcast, File mainDirectory, String modId) {
		super(localOnly, broadcast);
		String filePrefix = modId.equals(JojoMod.MOD_ID) ? "" : (modId + "_");
		this.file = new File(mainDirectory, ConfigObjSerialization.DIRECTORY + filePrefix + "client_settings.json");
	}

	@Override
	public void sendToServer() {
		if (ConfigNetworkFunctions.clientIsConnectedToAServer()) {
			ConfigNetworkFunctions.clSendPlayerBroadcastConfigToServer(true, 
					JojoMod.MOD_ID, this);
		}
	}
	
	@Override
	public void loadFromFileSystem() {
		if (exists()) {
			JsonObject json = CommonFileConfig.deserializeObj(file);
			if (json != null) {
				if (localOnly != null) {
					localOnly.fromJson(json);
				}
				
				if (broadcast != null) {
					JsonObject broadcastJson = json.getAsJsonObject("broadcasted");
					if (broadcastJson == null) broadcastJson = new JsonObject();
					broadcast.fromJson(broadcastJson);
				}
			}
		}
	}

	@Override
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
	
}
