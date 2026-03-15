package com.github.standobyte.jojo.entityattachment;

import com.github.standobyte.jojo.init.ModDataAttachmentTypes;

import net.minecraft.world.entity.Entity;

public interface TickingEntityData {
	/**
	 * Only ticks after the data is initialized (usually by calling {@link Entity#getData(net.neoforged.neoforge.attachment.AttachmentType)}).<br>
	 * If you need it to tick from the very moment the entity is created, initialize the attachment in something like a EntityEvent.EntityConstructing event handler.<br>
	 * If it, for example, just holds some kind of an integer timer that only ticks when you set it to a positive value, you're good.
	 */
	void tick();
	
	default void addTicking(Entity entity) {
		entity.getData(ModDataAttachmentTypes.DATA_EVENT_HELPER.get()).addTickingData(this);
	}
}
