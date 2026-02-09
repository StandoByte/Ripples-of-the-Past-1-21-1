package com.github.standobyte.jojo.mixin.container.open_as_entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.github.standobyte.jojo.mechanics.entity_like_player.opencontainer.OpenContainerAsEntity.ContainerExtension;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;

@Mixin(AbstractContainerMenu.class)
public class ContainerMenuMixin implements ContainerExtension {
	@Unique private Entity actualEntity;

	@Override
	public Entity jojo_ripples$getActualEntity() {
		return actualEntity;
	}

	@Override
	public void jojo_ripples$setActualEntity(Entity entity) {
		this.actualEntity = entity;
	}
	
}
