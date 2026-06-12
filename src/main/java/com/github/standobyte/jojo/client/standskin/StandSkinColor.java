package com.github.standobyte.jojo.client.standskin;

import com.github.standobyte.jojo.util.functions.JSONUtil;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public record StandSkinColor(
		int primary, 
		int secondary, 
		int text, 
		int text_white_bg /*using the same case as in the jsons*/, 
		int disc,
		int stats,
		int aura) {

	public StandSkinColor(int color) {
		this(color, color, color, color, color, color, color);
	}
	
	public static StandSkinColor fromJson(JsonElement json) {
		if (json.isJsonObject()) {
			JsonObject jsonObj = json.getAsJsonObject();
			int primary = parseColor(JSONUtil.getRequired("primary", jsonObj));
			int secondary = JSONUtil.getOr("secondary", jsonObj, StandSkinColor::parseColor, 
					primary);
			int text = JSONUtil.getOr("text", jsonObj, StandSkinColor::parseColor, 
					primary);
			int textWhiteBg = JSONUtil.getOr("text_white_bg", jsonObj, StandSkinColor::parseColor, 
					text);
			int disc = JSONUtil.getOr("disc", jsonObj, StandSkinColor::parseColor, 
					primary);
			int stats = JSONUtil.getOr("stats", jsonObj, StandSkinColor::parseColor, 
					primary);
			int aura = JSONUtil.getOr("aura", jsonObj, StandSkinColor::parseColor, 
					secondary);
			return new StandSkinColor(primary, secondary, text, textWhiteBg, disc, stats, aura);
		}
		else {
			int color = parseColor(json);
			return new StandSkinColor(color);
		}
	}

	public static int parseColor(JsonElement colorElement) {
		return 0xff000000 | JSONUtil.parseColor(colorElement);
	}
	
	
	public static final StandSkinColor FALLBACK = new StandSkinColor(
			0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFF000000, 0xFFFFFFFF, 0xFFFF60FF, 0xFFFF60FF);
	
}
