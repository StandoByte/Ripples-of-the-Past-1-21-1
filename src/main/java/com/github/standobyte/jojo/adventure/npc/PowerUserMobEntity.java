package com.github.standobyte.jojo.adventure.npc;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.adventure.character.CharacterPersonData;
import com.github.standobyte.jojo.adventure.npc.ai.NpcCombatAiPrototype;
import com.github.standobyte.jojo.adventure.npc.ai.inventory.ItemManageAI;
import com.github.standobyte.jojo.adventure.npc.debug.NpcFlags;
import com.github.standobyte.jojo.init.ModDataAttachmentTypes;
import com.github.standobyte.jojo.init.ModEntityDataSerializers;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.mixin.entity_like_player.npc.PlayerAccessor;
import com.github.standobyte.jojo.subsystems.entity_playerwrapper.EntityAsPlayerWrapper;
import com.github.standobyte.jojo.subsystems.entity_playerwrapper.RemoteClientPlayerLivingWrapper;
import com.github.standobyte.jojo.subsystems.entity_playerwrapper.ServerPlayerLivingWrapper;
import com.github.standobyte.jojo.util.functions.BitwiseFlagUtil;
import com.github.standobyte.jojo.util.functions.NBTUtil;
import com.mojang.authlib.properties.PropertyMap;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.entity.XpOrbTargetingEvent;

// TODO (character mob) player mechanics
/*
 * ...
 * swimming, crawling, sneaking
 * ...
 * ender chest
 * elytra flight, mace (their code fucking sucks)
 * ...
 * using items and containers...
⣀⣠⣤⣤⣤⣤⢤⣤⣄⣀⣀⣀⣀⡀⡀⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄
⠄⠉⠹⣾⣿⣛⣿⣿⣞⣿⣛⣺⣻⢾⣾⣿⣿⣿⣶⣶⣶⣄⡀⠄⠄⠄
⠄⠄⠠⣿⣷⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣯⣿⣿⣿⣿⣿⣿⣆⠄⠄
⠄⠄⠘⠛⠛⠛⠛⠋⠿⣷⣿⣿⡿⣿⢿⠟⠟⠟⠻⠻⣿⣿⣿⣿⡀⠄
⠄⢀⠄⠄⠄⠄⠄⠄⠄⠄⢛⣿⣁⠄⠄⠒⠂⠄⠄⣀⣰⣿⣿⣿⣿⡀
⠄⠉⠛⠺⢶⣷⡶⠃⠄⠄⠨⣿⣿⡇⠄⡺⣾⣾⣾⣿⣿⣿⣿⣽⣿⣿
⠄⠄⠄⠄⠄⠛⠁⠄⠄⠄⢀⣿⣿⣧⡀⠄⠹⣿⣿⣿⣿⣿⡿⣿⣻⣿
⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠉⠛⠟⠇⢀⢰⣿⣿⣿⣏⠉⢿⣽⢿⡏
⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠠⠤⣤⣴⣾⣿⣿⣾⣿⣿⣦⠄⢹⡿⠄
⠄⠄⠄⠄⠄⠄⠄⠄⠒⣳⣶⣤⣤⣄⣀⣀⡈⣀⢁⢁⢁⣈⣄⢐⠃⠄
⠄⠄⠄⠄⠄⠄⠄⠄⠄⣰⣿⣛⣻⡿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡯⠄⠄
⠄⠄⠄⠄⠄⠄⠄⠄⠄⣬⣽⣿⣻⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠁⠄⠄
⠄⠄⠄⠄⠄⠄⠄⠄⠄⢘⣿⣿⣻⣛⣿⡿⣟⣻⣿⣿⣿⣿⡟⠄⠄⠄
⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠛⢛⢿⣿⣿⣿⣿⣿⣿⣷⡿⠁⠄⠄⠄
⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠄⠉⠉⠉⠉⠈⠄⠄⠄⠄⠄⠄
 */
public class PowerUserMobEntity extends Mob implements EntityAsPlayerWrapper, HasCustomInventoryScreen {
	public static final EntityDataAccessor<Optional<ResolvableProfile>> DATA_PROFILE = SynchedEntityData.defineId(PowerUserMobEntity.class, 
			ModEntityDataSerializers.RESOLVABLE_PROFILE_OPTIONAL.get());
	public static final EntityDataAccessor<Long> NPC_FLAGS = SynchedEntityData.defineId(PowerUserMobEntity.class, EntityDataSerializers.LONG);
	public static final EntityDataAccessor<Integer> SYNC_HUNGER = SynchedEntityData.defineId(PowerUserMobEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Float> SYNC_SATURATION = SynchedEntityData.defineId(PowerUserMobEntity.class, EntityDataSerializers.FLOAT);
	public ClientHumanoidCharacterStuff clientStuff;
	public EntityAsPlayerWrapper playerWrapper;
	public Lazy<CharacterPersonData> characterData;

	public PowerUserMobEntity(EntityType<? extends PowerUserMobEntity> entityType, Level level) {
		super(entityType, level);
		if (!level.isClientSide()) {
			this.playerWrapper = ServerPlayerLivingWrapper.create(this, null);
		}
		else {
			this.playerWrapper = RemoteClientPlayerLivingWrapper.create(this);
			this.clientStuff = level.isClientSide() ? new ClientHumanoidCharacterStuff() : null;
		}
		Player fakePlayerEntity = this.playerWrapper.asPlayer();
		PlayerAccessor fakePlayerEntityAccess = (PlayerAccessor) fakePlayerEntity;
		fakePlayerEntityAccess.setInventory(new MobAsPlayerInventory(fakePlayerEntity, this));
		
		characterData = Lazy.of(() -> this.getData(ModDataAttachmentTypes.CHARACTER_DATA));
		this.setPersistenceRequired();
	}

	public PowerUserMobEntity(Level level) {
		this(ModEntityTypes.CHARACTER.get(), level);
	}
	
	@Override
    public boolean shouldShowName() {
    	boolean qwe = super.shouldShowName();
    	return qwe;
    }
	
	@Override
	public Entity getEntity() {
		return this;
	}
	
	@Override
	public Player asPlayer() {
		return (Player) playerWrapper;
	}
	
	
	public static boolean isMobPlayerLike(Entity entity) {
		return entity.getType() == ModEntityTypes.CHARACTER.get();
	}
	
	public static Player getWrapperFakePlayer(Entity actualEntity) {
		if (actualEntity instanceof PowerUserMobEntity characterMob) {
			return characterMob.playerWrapper.asPlayer();
		}
		return null;
	}
	

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_PROFILE, Optional.empty());
		builder.define(NPC_FLAGS, 0L);
		builder.define(SYNC_HUNGER, 0);
		builder.define(SYNC_SATURATION, 0f);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);
		if (key == DATA_PROFILE) {
			if (level().isClientSide()) {
				clientStuff.onSetSkinSourceProfile(this.entityData.get(DATA_PROFILE), this);
			}
		}
	}

	@Override
	public void tick() {
		super.tick();
        this.updateSwingTime();
		if (playerWrapper != null) {
			Player asPlayer = playerWrapper.asPlayer();
			if (asPlayer instanceof ServerPlayer asServerPlayer) {
				asPlayer.setHealth(this.getHealth());
				
				asServerPlayer.doTick();
				
				this.setHealth(asPlayer.getHealth());
				FoodData foodData = asPlayer.getFoodData();
				entityData.set(SYNC_HUNGER, foodData.getFoodLevel());
				entityData.set(SYNC_SATURATION, foodData.getSaturationLevel());
			}
			else {
				asPlayer.tick();
			}
			ServerPlayerLivingWrapper.copyData(this, asPlayer);
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag nbt) {
		super.addAdditionalSaveData(nbt);
		
		if (playerWrapper != null) {
			Player player = playerWrapper.asPlayer();
			
			CompoundTag playerData = new CompoundTag();
			playerData.putFloat("XpP", player.experienceProgress);
			playerData.putInt("XpLevel", player.experienceLevel);
			playerData.putInt("XpTotal", player.totalExperience);
//			playerData.putInt("XpSeed", player.enchantmentSeed);
			
			ListTag inventoryNbt = player.getInventory().save(new ListTag());
			playerData.put("Inventory", inventoryNbt);
			
			player.getFoodData().addAdditionalSaveData(playerData);
			
			nbt.put("Player", playerData);
		}
		
		entityData.get(DATA_PROFILE).ifPresent(profile -> {
			ResolvableProfile.CODEC.encodeStart(NbtOps.INSTANCE, profile).ifSuccess(profileNbt -> {
				nbt.put("Skin", profileNbt);
			});
		});
		
		nbt.putLong("Flags", entityData.get(NPC_FLAGS));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag nbt) {
		super.readAdditionalSaveData(nbt);
		
		if (playerWrapper != null) {
			Player player = playerWrapper.asPlayer();
			
			CompoundTag playerData = NBTUtil.getCompoundOptional(nbt, "Player").orElse(null);
			if (playerData != null) {
				player.experienceProgress = playerData.getFloat("XpP");
				player.experienceLevel = playerData.getInt("XpLevel");
				player.totalExperience = playerData.getInt("XpTotal");
//				player.enchantmentSeed = playerData.getInt("XpSeed");

				ListTag inventoryNbt = playerData.getList("Inventory", Tag.TAG_COMPOUND);
				if (!inventoryNbt.isEmpty()) {
					player.getInventory().load(inventoryNbt);
				}
				
				player.getFoodData().readAdditionalSaveData(playerData);
			}
		}
		
		NBTUtil.getCompoundOptional(nbt, "Skin").ifPresent(profileNbt -> {
			ResolvableProfile.CODEC.parse(NbtOps.INSTANCE, profileNbt).ifSuccess(profile -> {
				entityData.set(DATA_PROFILE, Optional.of(profile));
			});
		});
		
		entityData.set(NPC_FLAGS, nbt.getLong("Flags"));
	}

    public static AttributeSupplier.Builder createAttributes() {
    	return Player.createAttributes()
    			// okay, so, WHY IN THE FLYING FUCK IS THEIR FUCKING CODE *SO* DAMN INCONSISTENT
    			// players have 0.1, but the mob is walking SO slowly with that
    			// the mobs' regular speed is 0.25, but that it still not as fast as player's walking speed
                .add(Attributes.MOVEMENT_SPEED, 0.3)
    			.add(Attributes.FOLLOW_RANGE, 16.0);
    }
    
    
    public CharacterPersonData getCharacterData() {
    	return characterData.get();
    }
	
	// Prototype AI
	
	protected ItemManageAI foo = new ItemManageAI();
	protected NpcCombatAiPrototype bar = new NpcCombatAiPrototype(this);
	
	@Override
	protected void customServerAiStep() {
		if (playerWrapper != null) {
			Player asPlayer = playerWrapper.asPlayer();
			if (!getFlag(NpcFlags.DISABLE_AI_EQUIPMENT)) {
				foo.customServerAiStep(this, asPlayer);
			}
		}
		bar.tick();
	}


	// Disabling any form of despawning

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Override
	public boolean requiresCustomPersistence() {
		return true;
	}

	@Override
	protected boolean shouldDespawnInPeaceful() {
		return false;
	}

	@Override
	public boolean isPersistenceRequired() {
		return true;
	}

	@Override
	public void checkDespawn() {}


	// XP stuff

	@Override
	protected int getBaseExperienceReward() {
		if (playerWrapper != null) {
			int i = playerWrapper.asPlayer().experienceLevel * 7;
			return i > 100 ? 100 : i;
		}
		return 0;
	}
	
	@Override
    protected boolean isAlwaysExperienceDropper() {
    	return true;
    }
	
	// XP orbs themselves are picked up when we call ServerPlayer#doTick() in this entity's tick method

	public static void attractXpToCharacter(XpOrbTargetingEvent event) {
		ExperienceOrb xpOrb = event.getXpOrb();
		Vec3 pos = xpOrb.position();
		double maxDist = event.getScanDistance();
		Level level = xpOrb.level();
		AABB searchBox = new AABB(
				pos.x - maxDist, pos.y - maxDist, pos.z - maxDist, 
				pos.x + maxDist, pos.y + maxDist, pos.z + maxDist);
		List<Entity> mobCharacters = level.getEntities((Entity) null, searchBox, entity -> PowerUserMobEntity.isMobPlayerLike(entity));
		if (!mobCharacters.isEmpty()) {
			Entity nearestMob = null;
			double nearestMobDistSqr = -1;
			double maxDistSqr = maxDist * maxDist;
			for (Entity mob : mobCharacters) {
				double distSqr = mob.distanceToSqr(xpOrb);
				if ((distSqr < 0.0 || distSqr < maxDistSqr)
						&& (nearestMobDistSqr == -1.0 || distSqr < nearestMobDistSqr)) {
					nearestMob = mob;
					nearestMobDistSqr = distSqr;
				}
			}
			
			if (nearestMob != null) {
				Player evenNearerPlayer = level.getNearestPlayer(pos.x, pos.y, pos.z, 
						Math.sqrt(nearestMobDistSqr), EntitySelector.NO_CREATIVE_OR_SPECTATOR);
				event.setFollowingPlayer(evenNearerPlayer != null ? evenNearerPlayer : PowerUserMobEntity.getWrapperFakePlayer(nearestMob));
			}
		}
	}
	
	// Item dropping

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
	}

	@Override
	protected void dropEquipment() {
		super.dropEquipment();
		
		if (playerWrapper != null) {
			Player fakePlayer = playerWrapper.asPlayer();
			Inventory inventory = fakePlayer.getInventory();

			((PlayerAccessor) fakePlayer).invokeDestroyVanishingCursedItems();
			inventory.dropAll();
			LeFunnyNPCEasterEggs.dropLootOnDeath(this, fakePlayer, true, false);
		}
	}

	@Override
	protected void hurtArmor(DamageSource damageSource, float damage) {
		this.doHurtEquipment(damageSource, damage, new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD});
	}

	@Override
	protected void hurtHelmet(DamageSource damageSource, float damageAmount) {
		this.doHurtEquipment(damageSource, damageAmount, new EquipmentSlot[]{EquipmentSlot.HEAD});
	}

	@Override
	protected void hurtCurrentlyUsedShield(float damage) {
		if (this.useItem.canPerformAction(ItemAbilities.SHIELD_BLOCK)) {
			if (damage >= 3.0F) {
				int i = 1 + Mth.floor(damage);
				InteractionHand interactionhand = this.getUsedItemHand();
				if (this.level() instanceof ServerLevel serverlevel && !hasInfiniteMaterials()) {
					this.useItem.hurtAndBreak(i, serverlevel, this, item -> {
						this.onEquippedItemBroken(item, getSlotForHand(interactionhand));
//						net.neoforged.neoforge.event.EventHooks.onPlayerDestroyItem(this, this.useItem, interactionhand);
						stopUsingItem(); // Neo: Fix MC-168573 ("After breaking a shield, the player's off-hand can't finish using some items")
					});
				}
				if (this.useItem.isEmpty()) {
					if (interactionhand == InteractionHand.MAIN_HAND) {
						this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
					} else {
						this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
					}

					this.useItem = ItemStack.EMPTY;
					this.playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + this.level().random.nextFloat() * 0.4F);
				}
			}
		}
	}


	// Picking up items is handled by playerWrapper instead
	@Override
	protected void pickUpItem(ItemEntity itemEntity) {}


	@Override
	public InteractionResult interact(Player player, InteractionHand hand) {
		InteractionResult result = doInteract(player, hand);
		if (result.consumesAction()) {
			this.gameEvent(GameEvent.ENTITY_INTERACT, player);
		}
		return result;
	}

	public InteractionResult doInteract(Player player, InteractionHand hand) {
		ItemStack item = player.getItemInHand(hand);
		if (!item.isEmpty()) {
			if (item.is(Items.NAME_TAG)) {
				if (this.isAlive()) {
					boolean canRename = player.isCreative();
					boolean changeSkin = true;
					if (canRename) {
						Component name = item.get(DataComponents.CUSTOM_NAME);
						if (name != null) {
							this.setCustomName(name);
							if (changeSkin) {
								entityData.set(PowerUserMobEntity.DATA_PROFILE, Optional.of(
										new ResolvableProfile(Optional.of(name.getString()), Optional.empty(), new PropertyMap())));
							}
						}
						return InteractionResult.sidedSuccess(player.level().isClientSide());
					}
				}
				
				return InteractionResult.CONSUME_PARTIAL;
			}
		}
		
		if (!this.isAlive()) {
			return InteractionResult.PASS;
		}
		else {
			InteractionResult result = InteractionResult.PASS;

//			//if (isDebugDummy()) {
//			//	return DummyStuff.mobInteract(this, player, hand);
//			//}
			
			// this is empty, but who knows, maybe someone else injects smth into Mob#mobInteract for whatever reason
			result = this.mobInteract(player, hand);
			if (result.consumesAction()) {
				return result;
			}

			return InteractionResult.PASS;
		}
	}

	public InteractionResult leashableInteract(Player player, InteractionHand hand) {
		if (this.isAlive() && this instanceof Leashable leashable) {
			if (leashable.getLeashHolder() == player) {
				if (!this.level().isClientSide()) {
					leashable.dropLeash(true, !player.hasInfiniteMaterials());
					this.gameEvent(GameEvent.ENTITY_INTERACT, player);
				}

				return InteractionResult.sidedSuccess(this.level().isClientSide);
			}

			ItemStack itemstack = player.getItemInHand(hand);
			if (itemstack.is(Items.LEAD) && leashable.canHaveALeashAttachedToIt()) {
				if (!this.level().isClientSide()) {
					leashable.setLeashedTo(player, true);
				}

				itemstack.shrink(1);
				return InteractionResult.sidedSuccess(this.level().isClientSide);
			}
		}

		return InteractionResult.PASS;
	}

	@Override
	public boolean canBeLeashed() {
		return false;
	}
	

	@Override
	public void openCustomInventoryScreen(Player player) {
        if (!this.level().isClientSide()) {
        	player.openMenu(NpcInventoryExchangeContainer.createServerSide(this, false));
        }
	}


	@Override
	public boolean canUseSlot(EquipmentSlot slot) {
		return slot != EquipmentSlot.BODY;
	}
	
	@Override
	public boolean canAttackType(EntityType<?> type) {
		return true;
	}
	
	
	public int getFoodLevel() {
		if (!level().isClientSide()) {
			Player asPlayer = this.asPlayer();
			if (asPlayer != null) {
				return asPlayer.getFoodData().getFoodLevel();
			}
		}
		return entityData.get(SYNC_HUNGER);
	}
	
	public float getSaturationLevel() {
		if (!level().isClientSide()) {
			Player asPlayer = this.asPlayer();
			if (asPlayer != null) {
				return asPlayer.getFoodData().getSaturationLevel();
			}
		}
		return entityData.get(SYNC_SATURATION);
	}
	
	
	@Override
	public boolean isBaby() {
		return characterData.get().age < 0;
	}
	
	@Override
	public void setBaby(boolean baby) {
		CharacterPersonData characterData = this.characterData.get();
		if (characterData.age < 0 && !baby) {
			characterData.setAge(0);
		}
		else if (characterData.age >= 0 && baby) {
			characterData.setAge(CharacterPersonData.BABY_START_AGE);
		}
	}


	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
		@SuppressWarnings("deprecation")
		var ret = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
		boolean isDebugDummy = spawnType == MobSpawnType.COMMAND;
		if (isDebugDummy) {
			for (NpcFlags flag : NpcFlags.values()) {
				if (flag.trueForDebugDummy) {
					setFlag(flag, true);
				}
			}
		}
		return ret;
	}
	
	public void setFlag(NpcFlags flag, boolean value) {
		long stored = entityData.get(NPC_FLAGS);
		stored = BitwiseFlagUtil.set(stored, flag, value);
		entityData.set(NPC_FLAGS, stored);
	}
	
	public boolean getFlag(NpcFlags flag) {
		long stored = entityData.get(NPC_FLAGS);
		return BitwiseFlagUtil.get(stored, flag);
	}

	public static Pattern PLAYER_NAME_REGEX = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");
	public static boolean isLegitPlayerName(String playerName) {
		return PLAYER_NAME_REGEX.matcher(playerName).matches();
	}
	
	public boolean setSkinFromPlayerName(String playerName) {
		if (isLegitPlayerName(playerName)) {
			entityData.set(PowerUserMobEntity.DATA_PROFILE, Optional.of(
					new ResolvableProfile(Optional.of(playerName), Optional.empty(), new PropertyMap())));
			return true;
		}
		return false;
	}

}
