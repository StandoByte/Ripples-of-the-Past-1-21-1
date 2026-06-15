package com.github.standobyte.jojoimpl.powers.vampirism;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModEntityAttributes;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPowerData;
import com.github.standobyte.jojo.util.objects_mc.SpecificAttributeModifier;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class VampirismData extends PlayerPowerData {
	
	public VampirismData() {
		super(VampirismPowerType.VAMPIRISM.get());
	}
	

	public static final SpecificAttributeModifier SUN_BURN_DAMAGE_MODIFIER = new SpecificAttributeModifier(
			ModEntityAttributes.SUN_BURN_DAMAGE, new AttributeModifier(JojoMod.resLoc("vampire_sun_burn"), 
					4, AttributeModifier.Operation.ADD_VALUE));

	public static final SpecificAttributeModifier MAX_HEALTH_MODIFIER = new SpecificAttributeModifier(
			Attributes.MAX_HEALTH, new AttributeModifier(JojoMod.resLoc("vampire_max_health"), 
					40, AttributeModifier.Operation.ADD_VALUE));
	
	@Override
	public void addAttributeModifiers(LivingEntity user) {
		SUN_BURN_DAMAGE_MODIFIER.addOrReplacePermanent(user);
		MAX_HEALTH_MODIFIER.addOrReplacePermanent(user);
	}

	@Override
	public void removeAttributeModifiers(LivingEntity user) {
		SUN_BURN_DAMAGE_MODIFIER.remove(user);
		MAX_HEALTH_MODIFIER.remove(user);
	}
	

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag nbt = new CompoundTag();
		
		return nbt;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		
	}

	@Override
	public void toBuf(FriendlyByteBuf buf, boolean isSentToTracking) {
		
	}

	@Override
	public void fromBuf(FriendlyByteBuf buf, boolean isSentToTracking) {
		
	}

}
