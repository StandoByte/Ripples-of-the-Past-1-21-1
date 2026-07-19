package com.github.standobyte.jojo.modcompat.interfaces;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoMod;

import net.neoforged.fml.ModList;

public class OtherModInterfaces {

	public static InterfaceIris iris = InterfaceIris.DUMMY;


	public static void init() {
		OtherModInterfaces.<InterfaceIris>createIfModPresent("iris", 
				fullClassName("InterfaceImplIris"), "Iris",
				obj -> iris = obj);
	}
	
	private static String fullClassName(String className) {
		return "com.github.standobyte.jojo.modcompat.interfaces.donotcall." + className;
	}

	@Nullable
	public static <I> I createIfModPresent(String modId, 
			String modPresentClassName,
			String loggingModName,
			@Nullable Consumer<I> ifPresent) {
		if (ModList.get().isLoaded(modId)) {
			try {
				Class<? extends I> objClass = (Class<? extends I>) Class.forName(modPresentClassName);
				Constructor<? extends I> constructor = objClass.getConstructor();
				I instance = constructor.newInstance();
				JojoMod.getLogger().debug("{}: {} compatibility initialized.", JojoMod.MOD_ID, loggingModName);
				if (ifPresent != null) {
					ifPresent.accept(instance);
				}
				return instance;
			} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException e) {
				JojoMod.getLogger().error("{}: Failed to init compatibility with {}", JojoMod.MOD_ID, loggingModName, e);
			} catch (InvocationTargetException e) {
				JojoMod.getLogger().error("{}: Failed to init compatibility with {}", JojoMod.MOD_ID, loggingModName, e.getCause());
			}
		}
		else {
			JojoMod.getLogger().debug("{}: {} not found.", JojoMod.MOD_ID, loggingModName);
		}
		return null;
	}
}
