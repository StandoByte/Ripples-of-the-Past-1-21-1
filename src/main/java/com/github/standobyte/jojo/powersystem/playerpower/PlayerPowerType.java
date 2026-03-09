package com.github.standobyte.jojo.powersystem.playerpower;

import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.core.JojoRegistries;
import com.github.standobyte.jojo.powersystem.MovesetBuilder;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.PowerType;

import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.Lazy;

public abstract class PlayerPowerType<D extends PlayerPowerData> extends PowerType {
	private final ResourceLocation registryKey;

	public PlayerPowerType(ResourceLocation registryKey, MovesetBuilder abilitySet) {
		super(abilitySet);
		this.registryKey = registryKey;
	}
	
	@Override
	@Nonnull public abstract D newDataInstance();

	
	@Override
	public ResourceLocation getId() {
		return registryKey;
	}

    @Nullable
    public static PlayerPowerType<?> fromId(ResourceLocation id) {
        return JojoRegistries.PLAYER_POWER_TYPES_REG.get(id);
    }
	
	@Override
	public PowerClass<PlayerPower> getPowerClass() {
		return PowerClass.PLAYER_POWER;
	}
	
	protected Lazy<Component> name = Lazy.of(() -> Component.translatable(Util.makeDescriptionId("power", this.getId())));
	@Override
	public Component getName(Power<?> playerPowerData) {
		return name.get();
	}

    public static Stream<PlayerPowerType> getAllEnabledPlayerPowers() {
        return JojoRegistries.PLAYER_POWER_TYPES_REG.entrySet().stream().map(Map.Entry::getValue);
    }
}
