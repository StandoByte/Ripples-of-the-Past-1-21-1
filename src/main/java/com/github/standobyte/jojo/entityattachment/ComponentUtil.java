package com.github.standobyte.jojo.entityattachment;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

public class ComponentUtil {

	@Nullable // dawg i don't want to create optionals for this
	public static <T> T getExistingDataOrNull(IAttachmentHolder entity, Supplier<AttachmentType<T>> type) {
		return entity.hasData(type) ? entity.getData(type) : null;
	}
}
