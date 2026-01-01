package com.github.standobyte.jojo.powersystem.standpower.effect;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.init.ModDataAttachmentTypes;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.attachment.AttachmentType;

public class StandEffectsTarget {
	private List<StandEffectInstance> standEffectsTargetedBy = new LinkedList<>();
	
	public StandEffectsTarget(LivingEntity entity) {}


	public void addEffectTargetedBy(StandEffectInstance instance) {
		this.standEffectsTargetedBy.add(instance);
	}

	public void removeEffectTargetedBy(StandEffectInstance instance) {
		this.standEffectsTargetedBy.remove(instance);
	}

	
	@ApiStatus.Internal
	public static StandEffectsTarget getList(LivingEntity targetEntity) {
		return targetEntity.getData(ModDataAttachmentTypes.STAND_EFFECTS_TARGET.get());
	}

	public static Stream<StandEffectInstance> getEffectsReadOnly(LivingEntity targetEntity) {
		AttachmentType<StandEffectsTarget> type = ModDataAttachmentTypes.STAND_EFFECTS_TARGET.get();
		return targetEntity.hasData(type) ? targetEntity.getData(type).standEffectsTargetedBy.stream() : Stream.empty();
	}
}
