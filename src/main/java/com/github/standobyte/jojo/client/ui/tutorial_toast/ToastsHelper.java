package com.github.standobyte.jojo.client.ui.tutorial_toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.ClientTickables;
import com.github.standobyte.jojo.client.ClientTickables.Tickable;
import com.github.standobyte.jojo.config.core.ModConfig;
import com.github.standobyte.jojo.config.interfaces.fileimpl.CommonFileConfig;
import com.github.standobyte.jojo.config.internal.ObjectConfigHelper;
import com.github.standobyte.jojo.util.functions.JSONUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;

@ApiStatus.Internal
public class ToastsHelper implements Tickable {
	protected static ToastsHelper instance;

	public static void init() {
		instance = new ToastsHelper();
		ClientTickables.add(instance);
	}
	
	public static ToastsHelper getInstance() {
		return instance;
	}
	

	protected File file;
	protected final Map<String, Toast2> registeredToasts = new HashMap<>();
	protected final List<TimedToast> timedToasts = new ArrayList<>();
	
	@Nullable protected Toast2 curToast;
	protected int toastCooldown;
	
	protected ToastsHelper() {
		file = new File(ModConfig.getMainDirectory(Dist.CLIENT), ObjectConfigHelper.DIRECTORY + "tutorial_completed.json");
	}
	
	public void register(String key, Toast2 toast) {
		registeredToasts.put(key, toast);
	}
	
	
	public static boolean showOneAtATimeWithCooldown(Toast2 toastToShow, Consumer<Toast2> showMethod) {
		ToastsHelper instance = getInstance();
		if (instance.curToast == null && instance.toastCooldown <= 0 && toastToShow.canShow()) {
			showMethod.accept(toastToShow);
			instance.curToast = toastToShow;
			return true;
		}
		return false;
	}
	
	
	public void loadFromFileSystem() {
		JsonElement json = CommonFileConfig.deserialize(file);
		if (json != null && json.isJsonArray()) {
			for (JsonElement element : json.getAsJsonArray()) {
				if (element.isJsonPrimitive()) {
					String toastName = element.getAsString();
					Toast2 toast = registeredToasts.get(toastName);
					if (toast != null) {
						toast.setCompleted(true);
					}
				}
			}
		}
	}
	
	public void saveToFileSystem() {
		Collection<String> shownToasts = new ArrayList<>();
		for (var toastEntry : registeredToasts.entrySet()) {
			if (toastEntry.getValue().isCompleted()) {
				shownToasts.add(toastEntry.getKey());
			}
		}
		
		CommonFileConfig.serialize(file, writer -> {
			JSONUtil.GSON.toJson(shownToasts, writer);
		});
	}
	
	
	public void hideAll() {
		for (Toast2 toast : registeredToasts.values()) {
			toast.hide();
		}
	}
	
	public void resetTutorial() {
		for (Toast2 toast : registeredToasts.values()) {
			toast.setCompleted(false);
			toast.setShownThisSession(false);
			CommonFileConfig.serialize(file, writer -> {
				JSONUtil.GSON.toJson(new JsonArray(), writer);
			});
		}
	}

	
	@Override
	public boolean tick() {
		if (!Minecraft.getInstance().isPaused()) {
			timedToasts.removeIf(TimedToast::updateProgress);
		}
		
        if (curToast != null && !curToast.isShowing()) {
        	toastCooldown = 200;
        	curToast = null;
        }
        else if (toastCooldown > 0) --toastCooldown;
        return true;
	}
}
