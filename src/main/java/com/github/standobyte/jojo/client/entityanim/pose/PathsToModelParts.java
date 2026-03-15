package com.github.standobyte.jojo.client.entityanim.pose;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;

import com.github.standobyte.jojo.client.entityrender.ModelPartWithName;

import net.minecraft.client.model.geom.ModelPart;

@ApiStatus.Internal
public class PathsToModelParts {

	public static Map<String, ModelPartWithName[]> make(String rootName, ModelPart root/*, String... endPartNames*/) {
		Stack<ModelPartWithName> stack = new Stack<>();
		Set<String> toFind = null; //endPartNames.length > 0 ? new HashSet<>(Arrays.asList(endPartNames)) : null;
		Map<String, ModelPartWithName[]> destination = new HashMap<>();
		recursionMyBeloved(rootName, root, stack, destination, toFind);
		return destination;
	}
	
	private static void recursionMyBeloved(String partName, ModelPart part, Stack<ModelPartWithName> stack, Map<String, ModelPartWithName[]> destination, @Nullable Collection<String> toFind) {
		if (toFind != null && toFind.isEmpty()) return;
		
		stack.add(new ModelPartWithName(partName, part));
		if (toFind == null || toFind.remove(partName)) {
			destination.put(partName, stack.toArray(ModelPartWithName[]::new));
		}
		if (toFind == null || !toFind.isEmpty()) {
			for (var child : part.children.entrySet()) {
				recursionMyBeloved(child.getKey(), child.getValue(), stack, destination, toFind);
			}
		}
		stack.pop();
	}
	
	
	
}
