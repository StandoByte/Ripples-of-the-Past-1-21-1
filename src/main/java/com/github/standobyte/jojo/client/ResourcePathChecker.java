package com.github.standobyte.jojo.client;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

public class ResourcePathChecker {
	private static final Map<ResourceLocation, ResourcePathChecker> ALL = new HashMap<>();
	public final ResourceLocation path;
	private boolean resourceExists;
	private boolean checked = false;
	
	private ResourcePathChecker(ResourceLocation path) {
		this.path = path;
	}
	
	public static ResourcePathChecker getOrCreate(ResourceLocation path) {
		return ALL.computeIfAbsent(path, ResourcePathChecker::new);
	}
	
	public ResourceLocation or(ResourceLocation orDefault) {
		return resourceExists() ? path : orDefault;
	}
	
	public ResourceLocation or(Supplier<ResourceLocation> orDefault) {
		return resourceExists() ? path : orDefault.get();
	}
	
	public boolean resourceExists() {
		if (!checked) {
			resourceExists = resourceExists(path);
			checked = true;
		}
		return resourceExists;
	}
	
	public static boolean resourceExists(ResourceLocation location) {
		return Minecraft.getInstance().getResourceManager().getResource(location).isPresent();
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj instanceof ResourcePathChecker) {
			return this.path.equals(((ResourcePathChecker) obj).path);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return path.hashCode();
	}
	
	
	public static class ResourceReloadNotifier implements PreparableReloadListener {

		@Override
		public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager,
		        ProfilerFiller preparationsProfiler, ProfilerFiller reloadProfiler,
				Executor backgroundExecutor, Executor gameExecutor) {
			return barrier.wait(null)
					.thenRunAsync(() -> ALL.values().forEach(path -> path.checked = false));
		}

	}

}
