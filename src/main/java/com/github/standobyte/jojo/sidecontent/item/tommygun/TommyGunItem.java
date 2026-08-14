package com.github.standobyte.jojo.sidecontent.item.tommygun;

import java.util.ArrayList;
import java.util.List;

import com.github.standobyte.jojo.client.sound.ClientsideSoundsHelper;
import com.github.standobyte.jojo.client.sound.sounds.TommyGunLoopSound;
import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.init.ModSoundEvents;
import com.github.standobyte.jojo.mechanics.voiceline.VoiceLineServerSide;
import com.github.standobyte.jojo.util.functions.ItemUtil;
import com.github.standobyte.jojo.util.functions.MathUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TommyGunItem extends Item {

	public TommyGunItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (player.isShiftKeyDown()) {
			return reload(stack, player, level) ? InteractionResultHolder.consume(stack) : InteractionResultHolder.fail(stack);
		}
		else {
			player.startUsingItem(hand);
			return InteractionResultHolder.consume(stack);
		}
	}

	@Override
	public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingTicks) {
		int ammo = getAmmo(stack);
		int tick = getUseDuration(stack, entity) - remainingTicks;
		boolean shotTick = tick % 2 == 0;
		if (remainingTicks <= 1) {
			entity.releaseUsingItem();
			return;
		}
		if (!level.isClientSide()) {
			if (remainingTicks == getUseDuration(stack, entity) - 14 && ammo == MAX_AMMO - 7) {
				VoiceLineServerSide.play(entity, ModSoundEvents.VOICELINE_TOMMY_GUN_SCREAM);
			}
			if (ammo > 0) {
				//shotTick = true;
				//int bulletsPerTickForLulz = 20;
				if (shotTick) {
					//for (int i = 0; i < bulletsPerTickForLulz; i++) {
					BulletEntity bullet = new BulletEntity(entity, level);
					Vec3 pos = entity.getEyePosition(1).subtract(0, bullet.getBbHeight() / 2, 0).add(entity.getLookAngle());
					bullet.shootFromRotation(entity, 2f, 0);
					//pos = pos.add(bullet.getDeltaMovement().scale((double) i / bulletsPerTickForLulz));
					bullet.setPos(pos.x, pos.y, pos.z);
					level.addFreshEntity(bullet);
					if (!(entity instanceof Player player && player.getAbilities().instabuild)) {
						consumeAmmo(stack, 1);
					}
					//if (getAmmo(stack) <= 0) break;
					//}
				}
			}
			else {
				entity.releaseUsingItem();
			}
		}
		if (ammo > 0) {
			if (shotTick) {
				RandomSource random = entity.getRandom();
				if (entity.getType() == EntityType.PLAYER ? level.isClientSide() : !level.isClientSide()) {
					float recoil = 1F + Math.min((1F - (float) remainingTicks / (float) getUseDuration(stack, entity)) * 6F, 3F);
					entity.setYRot(entity.getYRot() + (random.nextFloat() - 0.5F) * 0.3F * recoil);
					entity.setXRot(Mth.clamp(entity.getXRot() -random.nextFloat() * 0.75F * recoil, -90, 90));
				}
				if (!level.isClientSide()) {
					stack.set(ModItemDataComponents.ACTIVATION_TICKS, 3);
				}
			}
		}
		else {
			entity.playSound(ModSoundEvents.TOMMY_GUN_NO_AMMO.get(), 1.0F, 1.0F);
		}
		if (level.isClientSide() && remainingTicks == getUseDuration(stack, entity)) {
			ClientsideSoundsHelper.playNonVanillaClassSound(new TommyGunLoopSound(
					ModSoundEvents.TOMMY_GUN_LOOP.get(), entity.getSoundSource(), 1.0F, entity, stack));
		}
	}

	public static final int MAX_AMMO = 50;
	public static int getAmmo(ItemStack gun) {
		if (gun.has(ModItemDataComponents.GUN_AMMO)) {
			return gun.get(ModItemDataComponents.GUN_AMMO);
		}
		return MAX_AMMO;
	}
	
	public static void setAmmo(ItemStack gun, int ammo) {
		if (gun.has(ModItemDataComponents.GUN_AMMO)) {
			gun.set(ModItemDataComponents.GUN_AMMO, ammo);
		}
	}

	public static boolean consumeAmmo(ItemStack gun, int amount) {
		int ammo = getAmmo(gun);
		if (ammo < 0) {
			setAmmo(gun, 0);
			return false;
		}
		if (ammo > 0) {
			setAmmo(gun, Math.max(ammo - amount, 0));
			return true;
		}
		return false;
	}

	private boolean reload(ItemStack stack, LivingEntity entity, Level level) {
		int ammoToLoad = MAX_AMMO - getAmmo(stack);
		if (ammoToLoad > 0) {
			if (entity instanceof Player player) {
				ammoToLoad = consumeAmmo(player, ammoToLoad);
				if (!level.isClientSide()) {
					player.getCooldowns().addCooldown(this, ammoToLoad * 2);
				}
			}
			if (ammoToLoad > 0) {
				if (!level.isClientSide()) {
					setAmmo(stack, getAmmo(stack) + ammoToLoad);
				}
				return true;
			}
		}
		return false;
	}

	private static final int BULLETS_PER_GUNPOWDER = 8;
	private int consumeAmmo(Player player, int ammoToLoad) {
		if (!player.getAbilities().instabuild) {
			List<ItemStack> ironNuggets = new ArrayList<>();
			int ironNuggetsCount = 0;
			List<ItemStack> gunpowder = new ArrayList<>();
			int gunpowderCount = 0;

			Inventory inventory = player.getInventory();
			for (int i = 0; i < inventory.getContainerSize(); ++i) {
				ItemStack inventoryStack = inventory.getItem(i);
				if (inventoryStack.getItem() == Items.IRON_NUGGET) {
					ironNuggets.add(inventoryStack);
					ironNuggetsCount += inventoryStack.getCount();
				}
				else if (inventoryStack.getItem() == Items.GUNPOWDER) {
					gunpowder.add(inventoryStack);
					gunpowderCount += inventoryStack.getCount();
				}
			}

			ammoToLoad = MathUtil.min(ironNuggetsCount, gunpowderCount * BULLETS_PER_GUNPOWDER, ammoToLoad);
			ironNuggetsCount = ammoToLoad;
			gunpowderCount = Mth.ceil((float) ammoToLoad / BULLETS_PER_GUNPOWDER);

			boolean clientSide = player.level().isClientSide();
			for (ItemStack ironNuggetsStack : ironNuggets) {
				int consumed = Math.min(ironNuggetsStack.getCount(), ironNuggetsCount);
				if (!clientSide) {
					ironNuggetsStack.shrink(consumed);
				}
				ironNuggetsCount -= consumed;
				if (ironNuggetsCount == 0) break;
			}
			for (ItemStack gunpowderStack : gunpowder) {
				int consumed = Math.min(gunpowderStack.getCount(), gunpowderCount);
				if (!clientSide) {
					gunpowderStack.shrink(consumed);
				}
				gunpowderCount -= consumed;
				if (gunpowderCount == 0) break;
			}
		}
		return ammoToLoad;
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return getAmmo(stack) < MAX_AMMO;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		return Math.round((float) getAmmo(stack) * (float) MAX_BAR_WIDTH / (float) MAX_AMMO);
	}

	@Override
	public int getBarColor(ItemStack stack) {
		float stackMaxDamage = MAX_AMMO;
		float f = Mth.clamp((stackMaxDamage - (float) getAmmo(stack)) / stackMaxDamage, 0, 1);
		return Mth.hsvToRgb((1 - f) / 3.0F, 1.0F, 1.0F);
	}

	@Override
	public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingTicks) {
		if (remainingTicks <= 1) {
			VoiceLineServerSide.play(entity, ModSoundEvents.VOICELINE_TOMMY_GUN_SHOT_MAGAZINE);
		}
	}

	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
		if (!level.isClientSide()) {
			ItemUtil.tickTimerDown(stack, ModItemDataComponents.ACTIVATION_TICKS);
		}
	}

	public static int getGunshotTick(ItemStack stack) {
		return stack.getOrDefault(ModItemDataComponents.ACTIVATION_TICKS, 0);
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.NONE;
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return 100;
	}

//	@Override
//	public boolean hurtEnemy(ItemStack itemStack, LivingEntity target, LivingEntity user) {
//		return INonStandPower.getNonStandPowerOptional(user).map(power -> 
//		power.getTypeSpecificData(ModPowers.HAMON.get()).map(hamon -> {
//			if (hamon.characterIs(ModHamonSkills.CHARACTER_JOSEPH.get())) {
//				if (!user.level.isClientSide()) {
//					if (power.consumeEnergy(200) && DamageUtil.dealHamonDamage(target, 0.15F, user, null)) {
//						target.invulnerableTime = 0;
//						hamon.hamonPointsFromAction(HamonStat.STRENGTH, 200);
//						VoiceLineServerSide.play(entity, ModSoundEvents.VOICELINE_TOMMY_GUN_PUNCH);
//						return true;
//					}
//					return false;
//				}
//				return true;
//			}
//			return false;
//		}).orElse(false)).orElse(false);
//	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return oldStack.getItem() != newStack.getItem();
	}

	@Override
	public void appendHoverText(ItemStack item, Item.TooltipContext ctx, List<Component> tooltip, TooltipFlag flags) {
		tooltip.add(Component.translatable("item.jojo_ripples.tommy_gun.hint", 
				Component.keybind("key.sneak"), Component.keybind("key.use")).withStyle(ChatFormatting.GRAY));

		ClientUtil.addItemReferenceQuote(tooltip, this);
		tooltip.add(ClientUtil.donoItemTooltip("KingKKrill"));
	}
}
