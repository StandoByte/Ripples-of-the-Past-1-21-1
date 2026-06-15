package com.github.standobyte.jojoimpl.powers.pillarman;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.powersystem.playerpower.PlayerPowerData;
import com.github.standobyte.jojo.util.functions.NBTUtil;
import com.github.standobyte.jojo.util.functions_network.NetworkUtil;
import com.github.standobyte.jojo.util.objects_mc.SpecificAttributeModifier;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;

public class PillarmanData extends PlayerPowerData {
	@Nonnull private PillarmanStage stage = PillarmanStage.SANTANA;
	@Nullable private PillarmanMode mode = null;
	
	public PillarmanData() {
		super(PillarmanPowerType.PILLAR_MAN.get());
	}
	
	
	@Nonnull public PillarmanStage getStage() { return stage; }
	@Nullable public PillarmanMode getMode() { return mode; }
	
	public void setStage(@Nonnull PillarmanStage stage, LivingEntity user) {
		if (this.stage != stage && !user.level().isClientSide()) {
			SpecificAttributeModifier[] prevModifiers = PillarmanAttributeModifiers.getModifiersForStage(this.stage);
			SpecificAttributeModifier[] modifiers = PillarmanAttributeModifiers.getModifiersForStage(stage);
			for (SpecificAttributeModifier modifier : prevModifiers) {
				modifier.remove(user);
			}
			for (SpecificAttributeModifier modifier : modifiers) {
				modifier.addOrReplacePermanent(user);
			}
			
			this.stage = stage;
			syncOnUpdate(user);
		}
	}
	
	public void setMode(@Nullable PillarmanMode mode, LivingEntity user) {
		if (this.mode != mode && !user.level().isClientSide()) {
			this.mode = mode;
			syncOnUpdate(user);
		}
	}
	
	
	@Override
	public void addAttributeModifiers(LivingEntity user) {
		SpecificAttributeModifier[] modifiers = PillarmanAttributeModifiers.getModifiersForStage(stage);
		for (SpecificAttributeModifier modifier : modifiers) {
			modifier.addOrReplacePermanent(user);
		}
	}
	
	@Override
	public void removeAttributeModifiers(LivingEntity user) {
		for (SpecificAttributeModifier modifier : PillarmanAttributeModifiers.ULTIMATE_THING_MODIFIERS) {
			modifier.remove(user);
		}
		/* the ids repeat in AJA_BUFF_MODIFIERS, MODE_USER_MODIFIERS and SANTANA_MODIFIERS,
		 * so no need to remove those
		 */
	}
	

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag nbt = new CompoundTag();
		NBTUtil.putEnum(nbt, "stage", stage);
		if (mode != null) {
			NBTUtil.putEnum(nbt, "mode", mode);
		}
		return nbt;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		stage = NBTUtil.getEnum(nbt, "stage", PillarmanStage.class);
		if (stage == null) {
			stage = PillarmanStage.SANTANA;
		}
		mode = NBTUtil.getEnum(nbt, "mode", PillarmanMode.class);
	}

	@Override
	public void toBuf(FriendlyByteBuf buf, boolean isSentToTracking) {
		buf.writeEnum(stage);
		NetworkUtil.writeNullableSmallEnum(buf, mode);
	}

	@Override
	public void fromBuf(FriendlyByteBuf buf, boolean isSentToTracking) {
		stage = buf.readEnum(PillarmanStage.class);
		mode = NetworkUtil.readNullableSmallEnum(buf, PillarmanMode.class);
	}

}
