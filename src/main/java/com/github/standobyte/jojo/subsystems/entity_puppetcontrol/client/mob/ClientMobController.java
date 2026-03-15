package com.github.standobyte.jojo.subsystems.entity_puppetcontrol.client.mob;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ui.hud_misc.VanillaGuiHelper;
import com.github.standobyte.jojo.client.ui.hud_misc.VanillaHudSprites;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mixin.entity_like_player.puppetcontrol.client.GuiAccessor;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.client.ClientEntityController;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.client.ItemNameAboveHotbarTimer;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.client.stand.StandHudElements.HealthHudTracker;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.mob.HardcodedMobControlCommands;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.mob.MobControlUtil;
import com.github.standobyte.jojo.subsystems.entity_puppetcontrol.mob.HardcodedMobControlCommands.WitchPotionMode;
import com.github.standobyte.jojo.util.functions.UtilFunctions;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.Input;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

public class ClientMobController extends ClientEntityController {

	public ClientMobController(Entity entity) {
		super(entity);
	}


	@Override
	public void onSet() {
		NeoForge.EVENT_BUS.register(this);
		if (entityAsLiving instanceof Witch) {
			witchHotbar = new WitchStuff();
			witchHotbar.setInitialHotbarSlot(entityAsLiving);
		}
	}

	@Override
	public void onUnset() {
		NeoForge.EVENT_BUS.unregister(this);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onInputUpdate(MovementInputUpdateEvent event) {
		Input clientInput = event.getInput();
		Mob controlledMob = MobControlUtil.getMobOrMobVehicle(entityAsLiving);
		
		// LookControl tick is being cancelled in com.github.standobyte.jojo.mixin.entitycontrol.mob.MobAILookMixin
		
		Vec3 prev = entityAsLiving.position();
		MoveControl moveControl = controlledMob.getMoveControl();
		moveControl.strafe(clientInput.forwardImpulse, clientInput.leftImpulse);
		prev = entityAsLiving.position().subtract(prev);
		
		if (clientInput.jumping) {
			JumpControl jumpControl = controlledMob.getJumpControl();
			jumpControl.jump();
		}
        
		UtilFunctions.wrapYRotationAngles(entityAsLiving);
	}
	
	protected boolean lmbHeld;
	protected boolean rmbHeld;
	@Override
	public void tickPre() {
		if (mc.getConnection() == null) {
			setInstance(null);
			return;
		}
		if (mc.screen == null) {
			if (witchHotbar != null) {
				witchHotbar.handleVanillaKeybinds(mc);
			}

			boolean holdingLMB = mc.options.keyAttack.isDown();
			boolean holdingRMB = mc.options.keyUse.isDown();
			
			if (lmbHeld != holdingLMB) {
				if (holdingLMB) {
					PacketDistributor.sendToServer(new ClControlledMobCommandPacket(HardcodedMobControlCommands.CommandType.PRESS_LMB, mc.hitResult));
				}
				else {
					PacketDistributor.sendToServer(new ClControlledMobCommandPacket(HardcodedMobControlCommands.CommandType.RELEASE_LMB));
				}
			}
			if (rmbHeld != holdingRMB) {
				if (holdingRMB) {
					PacketDistributor.sendToServer(new ClControlledMobCommandPacket(HardcodedMobControlCommands.CommandType.PRESS_RMB, mc.hitResult));
				}
				else {
					PacketDistributor.sendToServer(new ClControlledMobCommandPacket(HardcodedMobControlCommands.CommandType.RELEASE_RMB));
				}
			}
			
			while (mc.options.keyAttack.consumeClick()) {}
			while (mc.options.keyUse.consumeClick()) {}
			while (mc.options.keyPickItem.consumeClick()) {}
			
			this.lmbHeld = holdingLMB;
			this.rmbHeld = holdingRMB;
		}
	}
	
	@SubscribeEvent
	public void cancelPlayerClickInput(InteractionKeyMappingTriggered event) {
		event.setCanceled(true);
		event.setSwingHand(false);
	}

	@Override
	public void tick() {
		if (witchHotbar != null) {
			witchHotbar.itemName.tick(entityAsLiving.getMainHandItem(), mc);
		}
		PacketDistributor.sendToServer(new ClMobControlMovementPacket(entity.getId(), 
				entity.getX(), entity.getY(), entity.getZ(), 
				entity.getXRot(), entity.getYRot(), entity.onGround()));
	}
	
	@Override
	public boolean turn(double yRot, double xRot) {
		super.turn(yRot, xRot);
		Mob vehicle = MobControlUtil.getMobOrMobVehicle(entityAsLiving);
		if (vehicle != this.entity) {
			vehicle.setYRot(this.entity.getYRot());
		}
		return true;
	}
	
	@SubscribeEvent
	public void onMouseScroll(InputEvent.MouseScrollingEvent event) {
		double scrollX = event.getScrollDeltaX();
		double scrollY = event.getScrollDeltaY();
		if (scrollX != 0 || scrollY != 0) {
			double scroll = scrollY == 0 ? -scrollX : scrollY;
			if (witchHotbar != null) {
				witchHotbar.onMouseScroll(scroll < 0);
			}
		}
		event.setCanceled(true);
	}
	

	@Override
	public boolean isBeingControlled(Entity entity) {
		return super.isBeingControlled(entity) || MobControlUtil.getMobOrMobVehicle(this.entity) == entity;
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void clearInput(MovementInputUpdateEvent event) {
		Input clientInput = event.getInput();
		clearInput(clientInput);
	}


	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void removeHudElements(RenderGuiLayerEvent.Pre event) {
		ResourceLocation layerName = event.getName();
		if (layerName.equals(VanillaGuiLayers.EXPERIENCE_BAR) || layerName.equals(VanillaGuiLayers.EXPERIENCE_LEVEL)) {
			event.setCanceled(true);
		}
	}
	
	protected HealthHudTracker healthHudTracker = new HealthHudTracker();
	protected WitchStuff witchHotbar;
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public void renderHudElements(RenderGuiLayerEvent.Pre event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.options.hideGui) return;
		
		ResourceLocation layerName = event.getName();
		GuiGraphics guiGraphics = event.getGuiGraphics();
		DeltaTracker deltaTracker = event.getPartialTick();
		GuiAccessor gui = (GuiAccessor) mc.gui;
		VanillaHudSprites.cacheSpritePaths(gui);
		if (layerName.equals(VanillaGuiLayers.PLAYER_HEALTH)) {
			VanillaGuiHelper.renderHealth(entityAsLiving, entityAsLiving, guiGraphics, gui, mc, healthHudTracker);
		}
		if (layerName.equals(VanillaGuiLayers.VEHICLE_HEALTH)) {
			VanillaGuiHelper.renderVehicleHealth(entityAsLiving, guiGraphics, gui, mc);
		}
		else if (layerName.equals(VanillaGuiLayers.ARMOR_LEVEL)) {
			VanillaGuiHelper.renderArmor(entityAsLiving, guiGraphics, gui, mc);
		}
		else if (layerName.equals(VanillaGuiLayers.AIR_LEVEL)) {
			VanillaGuiHelper.renderAir(entityAsLiving, guiGraphics, gui, mc);
		}
		else if (layerName.equals(VanillaGuiLayers.EFFECTS)) {
			VanillaGuiHelper.renderStatusEffects(entityAsLiving, guiGraphics, gui, mc, 0xFFFFFFFF);
		}
		else if (layerName.equals(VanillaGuiLayers.HOTBAR)) {
			int center = guiGraphics.guiWidth() / 2;
			if (witchHotbar != null) {
				witchHotbar.renderHotbar(entityAsLiving, guiGraphics, deltaTracker, gui, mc, center);
			}
			else {
				int xLeft = center;
				int xRight = center;
				VanillaGuiHelper.renderLivingHeldItems(entityAsLiving, guiGraphics, gui, deltaTracker, mc, xLeft, xRight, true);
			}
		}
		else if (layerName.equals(VanillaGuiLayers.SELECTED_ITEM_NAME)) {
			if (witchHotbar != null) {
				witchHotbar.renderHotbarText(entityAsLiving, guiGraphics, deltaTracker, gui, mc);
			}
			event.setCanceled(true);
		}
//		else if (layerName.equals(VanillaGuiLayers.CROSSHAIR)) {
//			
//		}
	}
	
	
	public static class WitchStuff {
		@Nullable public Integer hotbarSlot;
		public WitchPotionMode witchPotionMode;
		public ItemNameAboveHotbarTimer itemName = new ItemNameAboveHotbarTimer();
		

		public void handleVanillaKeybinds(Minecraft mc) {
			for (int i = 0; i < 9; i++) {
				if (mc.options.keyHotbarSlots[i].consumeClick()) {
					onNumberKeyPressed(i);
				}
			}

			while (mc.options.keySwapOffhand.consumeClick()) {
				onFKeyPressed();
			}
		}
		
		
		public void onMouseScroll(boolean forward) {
			ItemStack[] potions = HardcodedMobControlCommands.getWitchPotions(witchPotionMode);
			if (potions != null) {
				if (hotbarSlot == null) {
					setSelectedSlot(forward ? 0 : potions.length - 1);
				}
				else {
					setSelectedSlot(((forward ? hotbarSlot + 1 : hotbarSlot - 1) + potions.length) % potions.length);
				}
			}
		}

		public void onNumberKeyPressed(int slot) {
			ItemStack[] potions = HardcodedMobControlCommands.getWitchPotions(witchPotionMode);
			if (potions != null) {
				if (slot >= 0 && slot < potions.length) {
					setSelectedSlot(slot);
				}
				else {
					setSelectedSlot(null);
				}
			}
		}
		
		public void onFKeyPressed() {
			if (witchPotionMode == WitchPotionMode.DRINK) {
				setPotionMode(WitchPotionMode.SPLASH);
			}
			else {
				setPotionMode(WitchPotionMode.DRINK);
			}
		}
		
		// XXX set them on the client side too, to account for server lag
		// make sure that the right click will use the correct potion, similarly to net.minecraft.client.multiplayer.MultiPlayerGameMode#ensureHasSentCarriedItem()
		public void setSelectedSlot(Integer slot) {
			this.hotbarSlot = slot;
			int networkSlot = slot != null ? HardcodedMobControlCommands
					.getWitchPotionSlotNumber(witchPotionMode, hotbarSlot) : 127;
			PacketDistributor.sendToServer(new ClControlledMobCommandPacket(
					HardcodedMobControlCommands.CommandType.PICK_SLOT, networkSlot));
		}
		
		public void setPotionMode(WitchPotionMode mode) {
			this.witchPotionMode = mode;
			setSelectedSlot(null);
		}
		
		
		public static final GuiIcon MODE_DRINK = new GuiIcon(JojoMod.resLoc("textures/gui/sprites/witch_mode_drink.png"), 16, 16);
		public static final GuiIcon MODE_SPLASH = new GuiIcon(JojoMod.resLoc("textures/gui/sprites/witch_mode_splash.png"), 16, 16);
		public void renderHotbar(LivingEntity witch, GuiGraphics guiGraphics, DeltaTracker deltaTracker, GuiAccessor gui, Minecraft mc, int center) {
			int width = 182;
			int halfWidth = width / 2;
			RenderSystem.enableBlend();
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(0.0F, 0.0F, -90.0F);
			guiGraphics.blitSprite(VanillaHudSprites.HOTBAR_SPRITE, center - 91, guiGraphics.guiHeight() - 22, width, 22);
			if (hotbarSlot != null) {
				guiGraphics.blitSprite(VanillaHudSprites.HOTBAR_SELECTION_SPRITE, center - 91 - 1 + hotbarSlot * 20, guiGraphics.guiHeight() - 22 - 1, 24, 23);
			}
			
			guiGraphics.blitSprite(VanillaHudSprites.HOTBAR_OFFHAND_LEFT_SPRITE, center - halfWidth - 29, guiGraphics.guiHeight() - 23, 29, 24);

			guiGraphics.pose().popPose();
			RenderSystem.disableBlend();
			int seed = 1;

			if (witchPotionMode == null) setPotionMode(WitchPotionMode.SPLASH);
			ItemStack[] potions = HardcodedMobControlCommands.getWitchPotions(witchPotionMode);
			int y = guiGraphics.guiHeight() - 16 - 3;
			for (int i = 0; i < potions.length; i++) {
				int x = center - halfWidth + 1 + i * 20 + 2;
				ItemStack item = potions[i];
				VanillaGuiHelper.renderSlot(guiGraphics, x, y, deltaTracker, witch, item, mc, seed++);
			}

			GuiIcon modeSwitcherSprite = switch (witchPotionMode) {
				case SPLASH -> MODE_DRINK;
				case DRINK -> MODE_SPLASH;
			};
			RenderSystem.enableBlend();
			modeSwitcherSprite.render(guiGraphics.pose(), center - halfWidth - 26, y);
		}

		public void renderHotbarText(LivingEntity witch, GuiGraphics guiGraphics, DeltaTracker deltaTracker, GuiAccessor gui, Minecraft mc) {
			itemName.renderSelectedItemName(guiGraphics, mc);
			
			int modeSwitcherX = guiGraphics.guiWidth() / 2 - 109;
			int modeSwitcherY = guiGraphics.guiHeight() - 33;
			InputConstants.Key key = mc.options.keySwapOffhand.getKey();
			if (key != null && !key.equals(InputConstants.UNKNOWN)) {
				Component keyName = key.getDisplayName();
				guiGraphics.drawCenteredString(mc.font, keyName, modeSwitcherX, modeSwitcherY, BlitFloat.NO_TINT);
			}
		}

		public void setInitialHotbarSlot(LivingEntity witch) {
			ItemStack heldItem = witch.getMainHandItem();
			if (!heldItem.isEmpty()) {
				Item item = heldItem.getItem();
				if (item == Items.POTION) {
					witchPotionMode = WitchPotionMode.DRINK;
				}
				else if (item == Items.SPLASH_POTION) {
					witchPotionMode = WitchPotionMode.SPLASH;
				}
				else {
					witchPotionMode = null;
				}
				ItemStack[] potions = HardcodedMobControlCommands.getWitchPotions(witchPotionMode);
				if (potions != null) {
					for (int i = 0; i < potions.length; i++) {
						if (ItemStack.matches(potions[i], heldItem)) {
							hotbarSlot = i;
							break;
						}
					}
				}
			}
		}
		
	}

}
