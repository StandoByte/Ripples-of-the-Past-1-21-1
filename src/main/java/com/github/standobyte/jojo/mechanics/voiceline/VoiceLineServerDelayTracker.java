package com.github.standobyte.jojo.mechanics.voiceline;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.mutable.MutableInt;

import com.github.standobyte.jojo.entityattachment.TickingEntityData;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;

public class VoiceLineServerDelayTracker implements TickingEntityData {
	private Map<Holder<SoundEvent>, MutableInt> delay = new HashMap<>();
	
	public VoiceLineServerDelayTracker(LivingEntity entity) {
		addTicking(entity);
	}
	
	@Override
	public void tick() {
		if (!delay.isEmpty()) {
			var entryIter = delay.entrySet().iterator();
			while (entryIter.hasNext()) {
				MutableInt timer = entryIter.next().getValue();
				if (timer.decrementAndGet() <= 0) {
					entryIter.remove();
				}
			}
		}
	}
	
	public boolean isOnDelay(Holder<SoundEvent> voiceLine) {
		MutableInt timer = delay.get(voiceLine);
		return timer != null && timer.intValue() > 0;
	}
	
	public void setDelay(Holder<SoundEvent> voiceLine, int delay) {
		this.delay.computeIfAbsent(voiceLine, __ -> new MutableInt()).setValue(delay);
	}
	
	
	public static VoiceLineServerDelayTracker get(LivingEntity entity) {
		return entity.getData(ModDataAttachmentTypes.VOICE_LINE_DELAY);
	}
	
}
