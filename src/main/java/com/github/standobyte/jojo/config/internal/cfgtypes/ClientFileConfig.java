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
	
	public void loadFromFileSystem() {
		if (localOnly != null || broadcast != null) {
			CommonFileConfig.deserialize(file, reader -> {
				JsonObject json = JSONUtil.parse(reader);
				if (localOnly != null) {
					localOnly.fromJson(json);
				}
				if (broadcast != null) {
					broadcast.fromJson(json);
				}
			});
		}
	}
	
	public void saveToFileSystem() {
		if (localOnly != null || broadcast != null) {
			CommonFileConfig.serialize(file, writer -> {
				JsonObject json;
				if (localOnly != null && broadcast != null) {
					json = localOnly.toJson();
					JSONUtil.merge(json, broadcast.toJson());
				}
				else {
					json = localOnly != null ? localOnly.toJson() : broadcast.toJson();
				}
				CommonFileConfig.GSON.toJson(json, writer);
			});
		}
	}
	
}
