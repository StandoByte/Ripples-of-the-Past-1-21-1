package com.github.standobyte.jojo.mixin.container.open_as_non_player;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.github.standobyte.jojo.subsystems.entity_opencontainer.OpenContainerAsNonPlayer;
import com.github.standobyte.jojo.subsystems.entity_opencontainer.OpenContainerAsNonPlayer.ContainerOpenedAsNonPlayer;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;

@Mixin(AbstractContainerMenu.class)
public class ContainerMenuMixin implements ContainerOpenedAsNonPlayer {
	@Unique private Entity actualEntity;

	@Override
	public Entity jojo_ripples$getActualEntity() {
		return actualEntity;
	}

	@Override
	public void jojo_ripples$setActualEntity(Entity entity) {
		this.actualEntity = entity;
		OpenContainerAsNonPlayer.onSetActualEntity((AbstractContainerMenu) (Object) this, entity);
	}
	
}
