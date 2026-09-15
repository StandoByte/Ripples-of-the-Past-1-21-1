package com.github.standobyte.jojo.config.stand;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.config.internal.cfgtypes.CommonFileConfig;
import com.github.standobyte.jojo.config.stand.datapack.DataDrivenStandsLoader;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.util.functions.JSONUtil;
import com.github.standobyte.jojo.util.functions.StringUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

@ApiStatus.Internal
public class StitchedStandDataFiles<F> {
	public static final String MAIN_FILE_NAME = "stand";
	
	protected F mainInfo;
	protected Map<List<String>, F> members = new HashMap<>();
	
	protected JsonObject resultingJson;
	
	public static <F> Map<ResourceLocation, StitchedStandDataFiles<F>> groupResources(
			Map<ResourceLocation, F> allResources, @Nullable String topDirectory) {
		if (topDirectory != null && !topDirectory.isEmpty()) topDirectory += "/";
		
		Map<ResourceLocation, StitchedStandDataFiles<F>> allGrouped = new HashMap<>();
		for (var entry : allResources.entrySet()) {
			ResourceLocation resourceKey = entry.getKey();
			String fileFullPath = resourceKey.getPath();
			if (topDirectory != null) {
				if (!fileFullPath.startsWith(topDirectory)) {
					continue;
				}
				fileFullPath = fileFullPath.substring(topDirectory.length());
			}
			
			F resource = entry.getValue();
			String[] path = fileFullPath.split("/");
			path[path.length - 1] = StringUtil.substrBack(path[path.length - 1], DataDrivenStandsLoader.FILE_EXT.length());
			
			ResourceLocation standId = ResourceLocation.fromNamespaceAndPath(resourceKey.getNamespace(), path[0]);
			StitchedStandDataFiles<F> stitchedResources = allGrouped.computeIfAbsent(standId, __ -> new StitchedStandDataFiles<>());
			
			if (path.length == 2) {
				if (path[1].equals(MAIN_FILE_NAME)) {
					stitchedResources.mainInfo = resource;
				}
				else {
					stitchedResources.members.put(Arrays.asList(path[1]), resource);
				}
			}
			else {
				String[] memberPath = new String[path.length - 1];
				System.arraycopy(path, 1, memberPath, 0, memberPath.length);
				stitchedResources.members.put(Arrays.asList(memberPath), resource);
			}
		}
		return allGrouped;
	}
	
	public JsonObject stitchJson(ResourceLocation standId) throws IOException {
		if (mainInfo != null) {
			resultingJson = readFile(mainInfo).getAsJsonObject();
		}
		else {
			resultingJson = new JsonObject();
		}
			
		for (var memberEntry : members.entrySet()) {
			JsonObject grandparentJson = null;
			JsonObject parentJson = resultingJson;
			String memberKey = null;
			for (String memberPath : memberEntry.getKey()) {
				if (grandparentJson != null) {
					parentJson = grandparentJson.getAsJsonObject(memberKey);
					if (parentJson == null) {
						parentJson = new JsonObject();
						grandparentJson.add(memberKey, parentJson);
					}
				}
				memberKey = memberPath;
				grandparentJson = parentJson;
			}
			
			JsonElement memberJson = readFile(memberEntry.getValue());
			JSONUtil.mergeWithObjMember(parentJson, memberKey, memberJson);
		}
		
		return resultingJson;
	}
	
	private JsonElement readFile(F resource) {
		return switch (resource) {
			case Resource datapackResource -> {
				try (Reader standMainRes = datapackResource.openAsReader()) {
					yield JsonParser.parseReader(standMainRes);
				}
				catch (IOException e) {
					JojoMod.getLogger().error("", e);
					throw new RuntimeException(e);
				}
			}
			case File fileName -> {
				yield CommonFileConfig.deserialize(fileName);
			}
			case Path filePath -> {
				yield CommonFileConfig.deserialize(filePath.toFile());
			}
			default -> throw new UnsupportedOperationException();
		};
	}
	
}
