package com.github.standobyte.core_subsystems.entitydata;

import com.github.standobyte.gameplay.standarrow.StandVirusData;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.JojoRegistries;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModTickingEntityAttachments {
	public static final DeferredRegister<EntityAttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(JojoRegistries.ENTITY_ATTACHMENTS_REG, JojoMod.MOD_ID);


	public static final DeferredHolder<EntityAttachmentType<?>, EntityAttachmentType<StandVirusData>> STAND_VIRUS = ATTACHMENT_TYPES.register(
			"stand_virus", key -> new EntityAttachmentType<>(key, StandVirusData::new));
}
