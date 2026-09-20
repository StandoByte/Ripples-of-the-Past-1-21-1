package com.github.standobyte.jojo.config.interfaces.fileimpl;

import java.io.File;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.config.core.ConfigId;
import com.github.standobyte.jojo.config.interfaces.ClientConfig;
import com.github.standobyte.jojo.config.internal.ConfigNetworkFunctions;
import com.github.standobyte.jojo.config.internal.ObjectConfigHelper;
import com.google.gson.JsonObject;

public class ClientFileConfig<C1, C2> implements ClientConfig<C1, C2> {
	@Nullable private final ObjectConfigHelper<C1> localOnly;
	@Nullable private final ObjectConfigHelper<C2> broadcast;
	private File file;

	public ClientFileConfig(ObjectConfigHelper<C1> localOnly, ObjectConfigHelper<C2> broadcast, 
			File mainDirectory, String modId) {
		this.localOnly = localOnly;
		this.broadcast = broadcast;
		String filePrefix = CommonFileConfig.makeFilePrefix(modId);
		this.file = new File(mainDirectory, ObjectConfigHelper.DIRECTORY + filePrefix + "client_settings.json");
	}

	@Override
	public C1 getLocalOnlyObj() {
		return localOnly.configObj;
	}

	@Override
	public C2 getBroadcastObj() {
		return broadcast.configObj;
	}
	
	@Override
	public boolean isDefault() {
		return (localOnly == null || localOnly.isDefault())
				&& (broadcast == null || broadcast.isDefault());
	}
	
	@Override
	public void reset() {
		if (localOnly != null) {
			localOnly.reset();
		}
		if (broadcast != null) {
			broadcast.reset();
		}
	}

	@Override
	public void sendBroadcastToServer(ConfigId configId, boolean sendNonDefault) {
		if (broadcast != null && (sendNonDefault || !broadcast.isDefault())) {
			ConfigNetworkFunctions.clSendPlayerBroadcastConfigToServer(configId, broadcast::toBuf);
		}
	}
	
	@Override
	public void loadFromFileSystem() {
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

	@Override
	public void saveToFileSystem() {
		CommonFileConfig.serialize(file, writer -> {
			JsonObject json = localOnly != null ? localOnly.toJson() : new JsonObject();
			if (broadcast != null) {
				json.add("broadcasted", broadcast.toJson());
			}
			CommonFileConfig.GSON.toJson(json, writer);
		});
	}
	
}
