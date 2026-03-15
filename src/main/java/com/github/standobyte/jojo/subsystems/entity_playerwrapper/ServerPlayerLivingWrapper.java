package com.github.standobyte.jojo.subsystems.entity_playerwrapper;

import java.util.OptionalInt;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.mixin.entity_like_player.npc.LivingEntityAccessor;
import com.github.standobyte.jojo.subsystems.entity_opencontainer.OpenContainerAsNonPlayer;
import com.mojang.authlib.GameProfile;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayer;

/**
 * A fake player class that poses a LivingEntity and all its data as a Player, for item methods.
 * This is not meant to be added to the world, but to be used as a parameter for Item methods like Item#use(Level, Player, InteractionHand).
 * Friendship ended with object-oriented programming.
 */
public class ServerPlayerLivingWrapper extends FakePlayer implements EntityAsPlayerWrapper {

	public static ServerPlayerLivingWrapper create(LivingEntity actualEntity, @Nullable ServerPlayer playerStandUser) {
		ServerLevel level = (ServerLevel) (actualEntity.level());
		GameProfile gameProfile = new GameProfile(actualEntity.getUUID(), actualEntity.getName().getString());
		ServerPlayerLivingWrapper fakePl = new ServerPlayerLivingWrapper(actualEntity, playerStandUser, 
				level, gameProfile);

		fakePl.setUUID(actualEntity.getUUID());
		fakePl.setId(actualEntity.getId());

		copyData(actualEntity, fakePl);
		linkMutableData(actualEntity, fakePl);
		
		Inventory fakeInventory = fakePl.getInventory();
		fakeInventory.offhand.set(0, actualEntity.getOffhandItem());
		fakeInventory.selected = 0;
		fakeInventory.setItem(fakeInventory.selected, actualEntity.getMainHandItem());
		
		fakePl.updateUseItem();

		return fakePl;
	}


	protected LivingEntity actualEntity;
	protected @Nullable ServerPlayer playerStandUser;

	protected ServerPlayerLivingWrapper(LivingEntity actualEntity, @Nullable ServerPlayer playerStandUser, 
			ServerLevel level, GameProfile gameProfile) {
		super(level, gameProfile);
		this.actualEntity = actualEntity;
		this.playerStandUser = playerStandUser;
	}

	@Override
	public Entity getEntity() {
		return actualEntity;
	}

	public static void copyData(LivingEntity from, LivingEntity to) {
//		to.dimensions = from.dimensions;
//		to.eyeHeight = from.eyeHeight;
		to.setPos(from.position());
//		to.bb = from.bb;
//		to.onGround = from.onGround;
		to.setDeltaMovement(from.getDeltaMovement());
		to.setYRot(from.getYRot());
		to.setXRot(from.getXRot());
		to.yRotO = from.yRotO;
		to.xRotO = from.xRotO;
		to.yBodyRot = from.yBodyRot;
		to.yBodyRotO = from.yBodyRotO;
		to.yHeadRot = from.yHeadRot;
		to.yHeadRotO = from.yHeadRotO;
		to.horizontalCollision = from.horizontalCollision;
		to.verticalCollision = from.verticalCollision;
		to.verticalCollisionBelow = from.verticalCollisionBelow;
		to.minorHorizontalCollision = from.minorHorizontalCollision;
		to.hurtMarked = from.hurtMarked;
		to.moveDist = from.moveDist;
		to.flyDist = from.flyDist;
		to.fallDistance = from.fallDistance;
		
		to.tickCount = from.tickCount;

		// synched entity data:
//		DATA_LIVING_ENTITY_FLAGS
//		DATA_EFFECT_PARTICLES
//		DATA_EFFECT_AMBIENCE_ID
//		DATA_ARROW_COUNT_ID
//		DATA_STINGER_COUNT_ID
		to.setHealth(from.getHealth());
//		SLEEPING_POS_ID
	}

	public static void linkMutableData(LivingEntity from, LivingEntity to) {
		LivingEntityAccessor _from = (LivingEntityAccessor) from;
		LivingEntityAccessor _to = (LivingEntityAccessor) to;
		_to.setAttributes(_from.getAttributes());
		_to.setCombatTracker(_from.getCombatTracker());
		// FIXME !!!!!!!!!!!!!!!!!!!!!!!! THEY WILL TICK TWICE YOU DUMBASS
		_to.setActiveEffects(_from.getActiveEffects());
	}
	
	protected void updateUseItem() {
		this.useItem = actualEntity.getUseItem();
		this.useItemRemaining = actualEntity.getUseItemRemainingTicks();
	}

	@Override
	public ItemStack getItemBySlot(EquipmentSlot slot) {
		return actualEntity != null ? actualEntity.getItemBySlot(slot) : ItemStack.EMPTY;
	}

	@Override
	public void setItemSlot(EquipmentSlot slot, ItemStack item) {
		actualEntity.setItemSlot(slot, item);
	}
	
	public void checkInventoryChanges() {
		Inventory inventory = this.getInventory();
		actualEntity.setItemSlot(EquipmentSlot.OFFHAND, inventory.offhand.get(0));
		actualEntity.setItemSlot(EquipmentSlot.MAINHAND, inventory.getSelected());
		for (EquipmentSlot armorSlot : EquipmentSlot.values()) {
			if (armorSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
				actualEntity.setItemSlot(armorSlot, inventory.armor.get(armorSlot.getIndex()));
			}
		}
	}


    protected int containerCounter;
    protected int nextContainerCounter() { this.containerCounter = this.containerCounter % 100 + 1; return this.containerCounter; }
	@Override
	public OptionalInt openMenu(@Nullable MenuProvider menuProvider, @Nullable Consumer<RegistryFriendlyByteBuf> extraDataWriter) {
		OptionalInt containerId = OpenContainerAsNonPlayer.openMenu(this, actualEntity, playerStandUser, menuProvider, extraDataWriter, nextContainerCounter());
		if (containerId.isPresent()) return containerId;
		
		return super.openMenu(menuProvider, extraDataWriter);
	}
	
	
	@Override
    public void startUsingItem(InteractionHand hand) {
		actualEntity.startUsingItem(hand);
		updateUseItem();
	}
	
	@Override
    public void stopUsingItem() {
		actualEntity.stopUsingItem();
		updateUseItem();
	}
	
	@Override
    public ItemStack getUseItem() {
		return actualEntity.getUseItem();
	}
	
	@Override
    public int getUseItemRemainingTicks() {
		return actualEntity.getUseItemRemainingTicks();
	}

	
	@Override
	public ItemStack getProjectile(ItemStack shootable) {
		return actualEntity.getProjectile(shootable);
	}

	@Override
	public void teleportTo(double x, double y, double z) {
		actualEntity.teleportTo(x, y, z);
	}
	
//	@Override
//	public ServerPlayer teleport(TeleportTransition transition) {
//		actualEntity.teleport(transition);
//		return this;
//	}

	@Override
	public ItemCooldowns getCooldowns() {
		return playerStandUser != null ? playerStandUser.getCooldowns() : super.getCooldowns();
	}


	@Override
	public void displayClientMessage(Component message, boolean actionBar) {
		if (playerStandUser != null) {
			playerStandUser.displayClientMessage(message, actionBar);
		}
	}

	@Override
	public void awardStat(Stat<?> stat, int amount) {
		if (playerStandUser != null) {
			playerStandUser.awardStat(stat, amount);
		}
	}

	@Override
	public PlayerAdvancements getAdvancements() {
		return playerStandUser != null ? playerStandUser.getAdvancements() : super.getAdvancements();
	}

}
