package com.github.standobyte.core_subsystems.entitydata;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.JojoRegistries;

import net.neoforged.neoforge.registries.DeferredRegister;

public class ModTickingEntityAttachments {
	public static final DeferredRegister<EntityAttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(JojoRegistries.ENTITY_ATTACHMENTS_REG, JojoMod.MOD_ID);

}
