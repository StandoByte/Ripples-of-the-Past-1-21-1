package com.github.standobyte.jojo.subsystems.entity_playerwrapper;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;

public class RemoteClientPlayerLivingWrapper extends RemotePlayer implements EntityAsPlayerWrapper {

	public static RemoteClientPlayerLivingWrapper create(LivingEntity actualEntity) {
		ClientLevel level = (ClientLevel) (actualEntity.level());
		GameProfile gameProfile = new GameProfile(actualEntity.getUUID(), actualEntity.getName().getString());
		RemoteClientPlayerLivingWrapper fakePl = new RemoteClientPlayerLivingWrapper(actualEntity, level, gameProfile);

		fakePl.setUUID(actualEntity.getUUID());
		fakePl.setId(actualEntity.getId());

		ServerPlayerLivingWrapper.copyData(actualEntity, fakePl);
		ServerPlayerLivingWrapper.linkMutableData(actualEntity, fakePl);
		
		Inventory fakeInventory = fakePl.getInventory();
		fakeInventory.offhand.set(0, actualEntity.getOffhandItem());
		fakeInventory.selected = 0;
		fakeInventory.setItem(fakeInventory.selected, actualEntity.getMainHandItem());
		
		fakePl.updateUseItem();

		return fakePl;
	}


	protected LivingEntity actualEntity;
	
	public RemoteClientPlayerLivingWrapper(LivingEntity actualEntity, ClientLevel clientLevel, GameProfile gameProfile) {
		super(clientLevel, gameProfile);
		this.actualEntity = actualEntity;
	}

	@Override
	public Entity getEntity() {
		return actualEntity;
	}
	
	protected void updateUseItem() {
		this.useItem = actualEntity.getUseItem();
		this.useItemRemaining = actualEntity.getUseItemRemainingTicks();
	}

}
