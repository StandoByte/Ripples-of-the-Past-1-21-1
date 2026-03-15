package com.github.standobyte.jojo.powersystem.standpower.datapack;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.util.functions.JSONUtil;
import com.github.standobyte.jojo.util.functions.StringUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

@ApiStatus.Internal
public class StitchedStandDataFiles  {
	protected static final String MAIN_FILE = "stand";
	protected static final int PREV_PATH_DIRS = 2;
	
	protected Resource mainInfo;
	protected Map<List<String>, Resource> members = new HashMap<>();
	
	protected JsonObject resultingJson;
	
	public static Map<ResourceLocation, StitchedStandDataFiles> groupResources(Map<ResourceLocation, Resource> allResources) {
		Map<ResourceLocation, StitchedStandDataFiles> allGrouped = new HashMap<>();
		for (var entry : allResources.entrySet()) {
			ResourceLocation resourceKey = entry.getKey();
			Resource resource = entry.getValue();
			String[] path = resourceKey.getPath().split("/");
			if (path.length <= PREV_PATH_DIRS) continue;
			path[path.length - 1] = StringUtil.substrBack(path[path.length - 1], DataDrivenStandsLoader.FILE_EXT.length());
			
			ResourceLocation standId = ResourceLocation.fromNamespaceAndPath(resourceKey.getNamespace(), path[PREV_PATH_DIRS - 1]);
			StitchedStandDataFiles stitchedResources = allGrouped.computeIfAbsent(standId, __ -> new StitchedStandDataFiles());
			
			if (path.length == PREV_PATH_DIRS + 1) {
				if (MAIN_FILE.equals(path[PREV_PATH_DIRS])) {
					stitchedResources.mainInfo = resource;
				}
				else {
					stitchedResources.members.put(Arrays.asList(path[PREV_PATH_DIRS]), resource);
				}
			}
			else {
				String[] memberPath = new String[path.length - PREV_PATH_DIRS];
				System.arraycopy(path, PREV_PATH_DIRS, memberPath, 0, memberPath.length);
				stitchedResources.members.put(Arrays.asList(memberPath), resource);
			}
		}
		return allGrouped;
	}
	
	protected JsonObject stitchJson(ResourceLocation standId) throws IOException {
		if (mainInfo != null) {
			try (Reader standMainRes = mainInfo.openAsReader()) {
				resultingJson = JsonParser.parseReader(standMainRes).getAsJsonObject();
			}
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
			try (Reader memberRes = memberEntry.getValue().openAsReader()) {
				JsonElement memberJson = JsonParser.parseReader(memberRes);
				JSONUtil.mergeWithObjMember(parentJson, memberKey, memberJson);
			}
		}
		
		return resultingJson;
	}
}
