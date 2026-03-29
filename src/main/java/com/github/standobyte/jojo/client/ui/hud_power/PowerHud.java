package com.github.standobyte.jojo.client.ui.hud_power;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.ClientTickHandler;
import com.github.standobyte.jojo.client.input.InputHandler;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.textsymbols.IconSymbols;
import com.github.standobyte.jojo.client.ui.hud_power.PowerHudControlsElement.AbilityBindUI;
import com.github.standobyte.jojo.client.ui.hud_power.PowerHudControlsElement.BindUI;
import com.github.standobyte.jojo.client.ui.hud_power.PowerHudControlsElement.HotbarUILine;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.client.ui.utils.TextUtil;
import com.github.standobyte.jojo.client.ui.utils.tooltip.MultiLineScreenTooltip;
import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.resolve.ResolveCounter;
import com.github.standobyte.jojo.mechanics.resolve.ResolveModeEffect;
import com.github.standobyte.jojo.mechanics.resolve.ResolveStageBuffs;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.PowerType;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.util.functions.MathUtil;
import com.github.standobyte.v1_21_4_stuff.missingmethods.ARGB;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.util.TriState;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class PowerHud {
	public static AbilityHud abilityHUDInstance;

	@SubscribeEvent
	public static void addHud(RegisterGuiLayersEvent event) {
		event.registerBelow(VanillaGuiLayers.BOSS_OVERLAY, 
				JojoMod.resLoc("ability_hud"), abilityHUDInstance = new AbilityHud());
	}
	
	
	public static boolean hasElementTooltips(Screen screen) {
		return screen instanceof ChatScreen
				|| /*F3+Esc*/ screen instanceof PauseScreen pause && !pause.showsPauseMenu();
	}
	
	public static boolean canDragElementsOn(Screen screen) {
		return screen instanceof ChatScreen
				|| screen instanceof PauseScreen pause && !pause.showsPauseMenu();
	}
	
	public static boolean isInContainerScreen() {
		return Minecraft.getInstance().screen instanceof AbstractContainerScreen;
	}
	
	@SubscribeEvent
	public static void onContainerMenuRender(ContainerScreenEvent.Render.Foreground event) {
		GuiGraphics graphics = event.getGuiGraphics();
		AbstractContainerScreen<?> screen = event.getContainerScreen();
		graphics.pose().pushPose();
		graphics.pose().translate(-screen.getGuiLeft(), -screen.getGuiTop(), 0.0F);
		abilityHUDInstance.setupRender(TriState.TRUE);
		abilityHUDInstance.renderAbilitiesHUD(graphics, Minecraft.getInstance().getTimer()/*getDeltaTracker()*/);
		graphics.pose().popPose();
	}
	
	@SubscribeEvent
	public static void addDraggableToScreen(ScreenEvent.Init.Post event) {	
		Screen screen = event.getScreen();
		if (canDragElementsOn(screen)) {
			for (HudElement element : abilityHUDInstance.elements.values()) {
				event.addListener(element);
			}
		}
	}
	
	public static boolean canHaveHudOpen() {
		Player player = Minecraft.getInstance().player;
		return player != null && !player.isSpectator();
	}
	
	
	public static class AbilityHud implements LayeredDraw.Layer {
		public Map<String, HudElement> elements = new HashMap<>();
		
		public <T extends HudElement> T addElement(T element) {
			element.hud = this;
			elements.put(element.name, element);
			return element;
		}
		
		
		public TriState forContainerMenu;
		private int mouseX;
		private int mouseY;
		
		public void setupRender(TriState forContainerMenu) {
			setupRender(forContainerMenu, -1, -1);
		}
		
		public void setupRender(TriState forContainerMenu, int mouseX, int mouseY) {
			this.forContainerMenu = forContainerMenu;
			this.mouseX = mouseX;
			this.mouseY = mouseY;
		}

	
		public PowerHudControlsElement controls = 	addElement(new PowerHudControlsElement("controls", 4, 44, -1, -1));
		public PowerIcon powerIcon = 			addElement(new PowerIcon("powerIcon", 11, 12, 16, 16));
		public Resolve resolveBar = 			addElement(new Resolve("resolve_bar", 31, 12, 32, 16));
		public Stamina staminaBar = 			addElement(new Stamina("stamina_bar", 81, 16, Bars.HORIZONTAL_LENGTH + 8, Bars.HORIZONTAL_WIDTH));
		public StandRange standRange = 			addElement(new StandRange("stand_range", 
				(int) staminaBar.xOffsetL + staminaBar.getWidth() + 10, (int) staminaBar.yOffsetU, -1, -1));
		public Finisher finisherBar = 			addElement(new Finisher("stand_finisher", 
				HudElement.SnappingH.CENTER, HudElement.SnappingV.CENTER, -16, -16, 32, 32));
		
		@Override
		public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
			Minecraft mc = Minecraft.getInstance();
			if (!canHaveHudOpen()) return;
			
			int mouseX = -1;
			int mouseY = -1;
			boolean isContainer = mc.screen instanceof AbstractContainerScreen;
			if (!isContainer) {
				if (mc.screen != null && hasElementTooltips(mc.screen)) {
					mouseX = (int)(mc.mouseHandler.xpos()
							* (double)mc.getWindow().getGuiScaledWidth()
							/ (double)mc.getWindow().getScreenWidth());
					mouseY = (int)(mc.mouseHandler.ypos()
							* (double)mc.getWindow().getGuiScaledHeight()
							/ (double)mc.getWindow().getScreenHeight());
				}
			}
			WindupAtCrosshair.setRender(null);
			setupRender(isContainer ? TriState.FALSE : TriState.DEFAULT, mouseX, mouseY);
			renderAbilitiesHUD(guiGraphics, deltaTracker);
			WindupAtCrosshair.renderCrosshair(guiGraphics, deltaTracker, mc);
		}

		public void renderAbilitiesHUD(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.options.hideGui) return;
			
			for (var element : elements.values()) {
				if (element.shouldRender()) {
					RenderSystem.enableBlend();
					RenderSystem.defaultBlendFunc();
					if (mouseX > -1 && mouseY > -1) {
						element.render(guiGraphics, deltaTracker, mouseX, mouseY);
					}
					else {
						element.render(guiGraphics, deltaTracker);
					}
				}
			}
			RenderSystem.disableBlend();
		}
		
		public boolean isAbilitySelected(String abilityName) {
			if (!canHaveHudOpen() || !controls.shouldRender()) return false;
			
			for (BindUI bind : controls.binds) {
				for (AbilityBindUI bindAbility : bind.abilities.values()) {
					if (abilityName.equals(bindAbility.ability.ability.name())) {
						return true;
					}
				}
			}
			for (HotbarUILine hotbar : controls.hotbars) {
				if (hotbar.selected != null) {
					for (AbilityBindUI bindAbility : hotbar.selected.abilities.values()) {
						if (abilityName.equals(bindAbility.ability.ability.name())) {
							return true;
						}
					}
				}
			}
			return false;
		}
		
	}
	
	
	public static class PowerIcon extends HudElement {
		protected PowerClass<?> powerClass;
		protected boolean standSummoned;

		public PowerIcon(String name, int x0, int y0, int width, int height) {
			super(name, x0, y0, width, height);
		}

		public PowerIcon(String name, SnappingH snappingHorizontal, SnappingV snappingVertical, 
				int xOffset, int yOffset, int width, int height) {
			super(name, snappingHorizontal, snappingVertical, xOffset, yOffset, width, height);
		}
		
		@Override
		protected void initText() {
			super.initText();
			tooltipText.body.clear();
		}

		@Override
		public boolean shouldRender() {
			standSummoned = ClientPowerCache.getPower(PowerClass.STAND).isSummoned();
			powerClass = null;
			
			if (!hud.forContainerMenu.isTrue()) {
				var controlScheme = InputHandler.getInstance().getActiveControlScheme();
				if (controlScheme != null) {
					powerClass = controlScheme.powerClassCosmetic;
					if (powerClass == PowerClass.STAND && !standSummoned) {
						powerClass = null;
					}
				}
			}
			
			if (standSummoned) {
				if (powerClass == null) {
					powerClass = PowerClass.STAND;
				}
				else if (powerClass != PowerClass.STAND) {
					standSummoned = false;
				}
			}
			
			return powerClass != null;
		}

		@Override
		public void renderElement(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
			if (powerClass != null) {
				if (powerClass == PowerClass.STAND) {
					renderClientStandIcon(guiGraphics.pose(), getX(), getY());
				}
				else {
					Power<?> power = ClientPowerCache.getPower(powerClass);
					if (power != null && power.hasPower()) {
						GuiIcon icon = getPowerIcon(power.getPowerType());
						icon.render(guiGraphics.pose(), getX(), getY());
					}
				}
			}
		}
		
		@Override
		protected void checkTooltip(double mouseX, double mouseY, DeltaTracker deltaTracker) {
			if (standSummoned) {
				Power<?> power = ClientPowerCache.getPower(PowerClass.STAND);
				if (power != null && power.hasPower()) {
					Component powerName = power.getName();
					tooltipText.setTitle(Component.translatable("ripples_hud.stand_summoned", powerName.copy())
							.withStyle(ChatFormatting.BLACK));
				}
			}
			else {
				Power<?> power = ClientPowerCache.getPower(powerClass);
				if (power != null && power.hasPower()) {
					Component powerName = power.getName();
					tooltipText.setTitle(powerName.copy()
							.withStyle(ChatFormatting.BLACK));
				}
			}
			super.checkTooltip(mouseX, mouseY, deltaTracker);
		}
	}
	
	public static void renderClientStandIcon(PoseStack pose, int x, int y) {
		renderStandIcon(ClientPowerCache.getPower(PowerClass.STAND), pose, x, y);
	}
	
	public static void renderStandIcon(StandPower standPower, PoseStack pose, int x, int y) {
		if (standPower != null) {
			StandSkin skin = StandSkinsLoader.getInstance().getSkin(standPower);
			if (skin != null) {
				GuiIcon icon = skin.getStandIcon();
				if (icon != null) {
					RenderSystem.enableBlend();
					RenderSystem.defaultBlendFunc();
					icon.render(pose, x, y);
					RenderSystem.disableBlend();
				}
			}
		}
	}
	
	protected static final Map<ResourceLocation, GuiIcon> POWER_ICONS = new HashMap<>();
	public static GuiIcon getPowerIcon(PowerType powerType) {
		return POWER_ICONS.computeIfAbsent(powerType.getId(), 
				id -> new GuiIcon(id.withPath(path -> "textures/power/" + path + ".png"), 16, 16));
	}
	public static GuiIcon getPowerIcon(Supplier<? extends PowerType> powerType) { return getPowerIcon(powerType.get()); }
		
		
	public static class Resolve extends HudElement {
		public static final GuiIcon RESOLVE_MODE = new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_mode_bar.png"), 40, 40);
		public static final GuiIcon HORIZONTAL_EMPTY = new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_horizontal_empty.png"), 32, 16);
		public static final GuiIcon HORIZONTAL_FULL = new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_horizontal_full.png"), 32, 16);
		public static final GuiIcon VERTICAL_EMPTY = new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_vertical_empty.png"), 16, 32);
		public static final GuiIcon VERTICAL_FULL = new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_vertical_full.png"), 16, 32);
		
		public static final GuiIcon STAGE_BAR_HORIZONTAL = new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_stages_h.png"), 32, 16);
		public static final GuiIcon[] STAGE_HORIZONTAL = new GuiIcon[] {
				new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_stage_1_h.png"), 32, 16),
				new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_stage_2_h.png"), 32, 16),
				new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_stage_3_h.png"), 32, 16),
				new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_stage_4_h.png"), 32, 16)
		};
		public static final GuiIcon[] STAGE_UNLOCKED_HORIZONTAL = new GuiIcon[] {
				new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_stage_unlocked1_h.png"), 32, 16),
				new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_stage_unlocked2_h.png"), 32, 16),
				new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_stage_unlocked3_h.png"), 32, 16),
				new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_stage_unlocked4_h.png"), 32, 16)
		};
		public static final GuiIcon STAGE_BAR_VERTICAL = new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_stages_v.png"), 16, 32);
		public static final GuiIcon[] STAGE_VERTICAL = new GuiIcon[] {
				new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_stage_1_v.png"), 16, 32),
				new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_stage_2_v.png"), 16, 32),
				new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_stage_3_v.png"), 16, 32),
				new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_stage_4_v.png"), 16, 32)
		};
		public static final GuiIcon[] STAGE_UNLOCKED_VERTICAL = new GuiIcon[] {
				new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_stage_unlocked1_v.png"), 16, 32),
				new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_stage_unlocked2_v.png"), 16, 32),
				new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_stage_unlocked3_v.png"), 16, 32),
				new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_stage_unlocked4_v.png"), 16, 32)
		};
		
		public static final GuiIcon[] LVL_HORIZONTAL = new GuiIcon[] {
				new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_lvl_1_h.png"), 32, 16),
				new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_lvl_2_h.png"), 32, 16),
				new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_lvl_3_h.png"), 32, 16),
				new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_lvl_4_h.png"), 32, 16)
		};
		public static final GuiIcon[] LVL_VERTICAL = new GuiIcon[] {
				new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_lvl_1_v.png"), 16, 32),
				new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_lvl_2_v.png"), 16, 32),
				new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_lvl_3_v.png"), 16, 32),
				new GuiIcon(JojoMod.resLoc("textures/hud/stand_resolve/stand_resolve_lvl_4_v.png"), 16, 32)
		};

		public Resolve(String name, int x0, int y0, int width, int height) {
			super(name, x0, y0, width, height);
		}

		public Resolve(String name, SnappingH snappingHorizontal, SnappingV snappingVertical, 
				int xOffset, int yOffset, int width, int height) {
			super(name, snappingHorizontal, snappingVertical, xOffset, yOffset, width, height);
		}
		
		@Override
		protected void initText() {
		}

		@Override
		public boolean shouldRender() {
			if (hud.forContainerMenu == TriState.TRUE) return false;
			StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
			if (standPower != null && standPower.usesResolve()) {
				ClientControlScheme controlScheme = InputHandler.getInstance().getActiveControlScheme();
				return controlScheme != null && controlScheme.hasAbility(ability -> ability.powerClass() == PowerClass.STAND);
			}
			
			return false;
		}
		
		@Override
		public void renderElement(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
			StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
			float partialTick = ClientUtil.partialTick(deltaTracker, false);
			Minecraft mc = Minecraft.getInstance();
			
			int width = getWidth();
			int height = getHeight();
			
			GuiIcon emptySprite = HORIZONTAL_EMPTY;
			GuiIcon fullSprite = HORIZONTAL_FULL;
			int spriteWidth = 32;
			int spriteHeight = 16;
			int x = getX() + (width - spriteWidth) / 2;
			int y = getY() + (height - spriteHeight) / 2;

			LivingEntity user = standPower.getUser();
			ResolveCounter resolve = standPower.resolveCounter;
			MobEffectInstance resolveEffect = ResolveModeEffect.maxDurationResolveEffect(user);
			if (resolveEffect != null) {
				// resolve mode timer circle
				int duration = resolveEffect.getDuration();
				int timer = resolve.resolveModeTimer;
				int timerInitial = resolve.resolveModeInitial;
				
				if (timerInitial > -1 && timer > -1) {
					duration = Math.min(timer, duration);
				}
				float value = duration + 1 - partialTick;
				if (value > 0) {
					float resolveModeDurationRatio = timerInitial > 0 ? value / timerInitial : 1;
					BlitFloat.blitRadial(guiGraphics.pose(), mc, RESOLVE_MODE.file, 
							x + (spriteWidth - RESOLVE_MODE.width) / 2, y + (spriteHeight - RESOLVE_MODE.height) / 2, RESOLVE_MODE.width, RESOLVE_MODE.height, 0, 
							0, resolveModeDurationRatio, BlitFloat.NO_TINT);
				}
			}
			
			float lmbRmbFadeIn = 0;
			if (resolveEffect == null && resolve.getCurStage() >= 0) {
				float tick = ClientTickHandler.tickCount + partialTick;
				lmbRmbFadeIn = 2 * Mth.sin(tick * 0.05f) - 0.75f;
				lmbRmbFadeIn = Mth.clamp(lmbRmbFadeIn, 0, 1);
			}
			if (lmbRmbFadeIn < 1) {
				// resolve kanji fill
				float alpha = 1 - lmbRmbFadeIn;
				BlitFloat.blit(guiGraphics.pose(), mc, emptySprite.file, 
						x, y, spriteWidth, spriteHeight, 0, 
						ARGB.white(alpha));
				float resolveRatio = resolve.getResolveBarFill();
				float fillWidth = resolveRatio >= 1 ? spriteWidth : 2 + (spriteWidth - 6) * resolveRatio;
				BlitFloat.blit(guiGraphics.pose(), mc, fullSprite.file, 
						x, y, fillWidth, spriteHeight, 0, 
						0, 0, fillWidth, spriteHeight, spriteWidth, spriteHeight, 
						ARGB.white(alpha));
			}
			if (lmbRmbFadeIn > 0) {
				// LMB + RMB prompt fading in and out
				String resolveActivationPrompt = IconSymbols.LMB_CLICK_LARGE + "+" + IconSymbols.RMB_CLICK_LARGE;
				guiGraphics.drawCenteredString(mc.font, Component.literal(resolveActivationPrompt), 
						x + spriteWidth / 2, y + 4, ARGB.white(TextUtil.fixAlpha(lmbRmbFadeIn)));
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
			}
			
			int lvlX = x;
			int lvlY = y + 16;
			if (resolveEffect != null) {
				// resolve mode level
				GuiIcon[] lvlSprites = LVL_HORIZONTAL;
				GuiIcon sprite = lvlSprites[Mth.clamp(resolveEffect.getAmplifier(), 0, lvlSprites.length - 1)];
				sprite.render(guiGraphics.pose(), lvlX, lvlY);
			}
			else {
				GuiIcon sprite;
				GuiIcon[] stageSprites;
				
				// empty bar
				sprite = STAGE_BAR_HORIZONTAL;
				sprite.render(guiGraphics.pose(), lvlX, lvlY);
				
				// unlocked resolve stages
				stageSprites = STAGE_UNLOCKED_HORIZONTAL;
				sprite = stageSprites[Mth.clamp(resolve.getUnlockedStage(), 0, stageSprites.length - 1)];
				sprite.render(guiGraphics.pose(), lvlX, lvlY);
				
				// current resolve stage
				int curStage = resolve.getCurStage();
				if (curStage >= 0) {
					stageSprites = STAGE_HORIZONTAL;
					sprite = stageSprites[Mth.clamp(curStage, 0, stageSprites.length - 1)];
					sprite.render(guiGraphics.pose(), lvlX, lvlY);
				}
			}
			
			if (resolveEffect == null) {
				float multiplier = resolve.totalMultiplier(standPower.getUser());
				if (multiplier > 1) {
					Component multiplierText = Component.literal("x" + String.format("%.2f", multiplier));
					StandSkin skin = StandSkinsLoader.getCurSkin();
					guiGraphics.drawCenteredString(mc.font, multiplierText, x + width / 2, y - 8, skin != null ? skin.getColor() : 0xFFFFFFFF);
				}
			}
		}
		
		@Override
		protected void checkTooltip(double mouseX, double mouseY, DeltaTracker deltaTracker) {
			StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
			ResolveCounter resolve = standPower.resolveCounter;
			Minecraft mc = Minecraft.getInstance();
			
			this.yOffsetU = 4;
			updateRectangle(32, 32);
			
			MultiLineScreenTooltip tooltip;
			double y = mouseY - this.getY();
			if (y <= 8) {
				// Resolve multiplier
				tooltip = new MultiLineScreenTooltip(
						Component.translatable("ripples_hud.resolve_multiplier")
										.withStyle(ChatFormatting.BLACK), 
						Component.translatable("ripples_hud.resolve_multiplier.hp",
								String.format("%.2f", resolve.missingHpMultiplier(standPower.getUser(), 0)))
										.withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY));
			}
			else if (y > 24) {
				// Resolve stage
				boolean vampire = !ResolveStageBuffs.getsDamageResFromResolve(mc.player);
				tooltip = new MultiLineScreenTooltip(
						Component.translatable("ripples_hud.resolve_stage",
								resolve.getCurStage() + 1,
								resolve.getUnlockedStage() + 1)
										.withStyle(ChatFormatting.BLACK), 
						Component.translatable("ripples_hud.resolve_stage.desc")
										.withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY),
						Component.translatable("ripples_hud.resolve_stage.desc1" + (vampire ? ".vamp" : ""))
										.withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY));
			}
			else {
				int resolveModeTimer = resolve.resolveModeTimer;
				if (resolveModeTimer > 0) {
					// Resolve mode timer
					boolean passedAllStages = ResolveStageBuffs.keepResolveModeAtHalfPassively(standPower, resolve);
					tooltip = new MultiLineScreenTooltip(
							Component.translatable("ripples_hud.resolve_mode",
									Component.literal(StringUtil.formatTickDuration(resolveModeTimer, mc.level.tickRateManager().tickrate())))
											.withStyle(ChatFormatting.BOLD).withStyle(style -> style.withColor(0xFFC6151F)), 
							Component.translatable("ripples_hud.resolve_mode.desc" + (passedAllStages ? ".free" : ""))
											.withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY));
				}
				else {
					// Resolve bar fill
					tooltip = new MultiLineScreenTooltip(
							Component.translatable("ripples_hud.resolve_bar",
									Component.literal(String.valueOf((int) (resolve.getResolveBarFill() * 100))))
											.withStyle(ChatFormatting.BLACK), 
							Component.translatable("ripples_hud.resolve_bar.desc")
											.withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY),
							Component.translatable("ripples_hud.resolve_bar.desc1")
											.withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY));
				}
			}
			this.tooltip.set(tooltip);
			super.checkTooltip(mouseX, mouseY, deltaTracker);
		}
	}
	
	
	public static class Stamina extends HudElement {
		public static final GuiIcon ICON = new GuiIcon(JojoMod.resLoc("textures/hud/stand_stamina.png"), 20, 20);
		public static final ResourceLocation BAR_HORIZONTAL_FILL = JojoMod.resLoc("textures/hud/bars/bar_horizontal_stamina.png");
		public static final ResourceLocation BAR_HORIZONTAL_MINI_FILL = JojoMod.resLoc("textures/hud/bars/bar_horizontal_mini_stamina.png");
		public static final ResourceLocation BAR_VERTICAL_FILL = JojoMod.resLoc("textures/hud/bars/bar_vertical_stamina.png");
		public static final ResourceLocation BAR_VERTICAL_MINI_FILL = JojoMod.resLoc("textures/hud/bars/bar_vertical_mini_stamina.png");

		public Stamina(String name, int x0, int y0, int width, int height) {
			super(name, x0, y0, width, height);
		}

		public Stamina(String name, SnappingH snappingHorizontal, SnappingV snappingVertical, 
				int xOffset, int yOffset, int width, int height) {
			super(name, snappingHorizontal, snappingVertical, xOffset, yOffset, width, height);
		}
		
		public MultiLineScreenTooltip tooltipResolve;
		
		@Override
		protected void initText() {
			this.tooltipText = new MultiLineScreenTooltip(
					Component.translatable("ripples_hud." + name).withStyle(ChatFormatting.BLACK), 
					Component.translatable("ripples_hud." + name + ".desc", 
							Component.translatable("ripples_hud." + name + ".desc1.regular"))
					.withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY));
			this.tooltipResolve = new MultiLineScreenTooltip(
					Component.translatable("ripples_hud." + name).withStyle(ChatFormatting.BLACK), 
					Component.translatable("ripples_hud." + name + ".desc", 
							Component.translatable("ripples_hud." + name + ".desc1.resolve"))
					.withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY));
			this.tooltip.set(this.tooltipText);
		}

		@Override
		public boolean shouldRender() {
			if (hud.forContainerMenu.isTrue()) return false;
			StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
			if (standPower != null && !standPower.isUserCreative() && standPower.usesStamina()) {
				ClientControlScheme controlScheme = InputHandler.getInstance().getActiveControlScheme();
				return controlScheme != null && controlScheme.hasAbility(ability -> ability.powerClass() == PowerClass.STAND);
			}
			
			return false;
		}
		
		@Override
		public void renderElement(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
			StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
			float staminaRatio = standPower.getStaminaRatio(ClientUtil.partialTick(deltaTracker, false));
			int x = getX() + 8;
			int y = getY();
			float alpha = ResolveStageBuffs.ignoreStaminaDebuff(Minecraft.getInstance().player) ? 0.5f : 1;
			Bars.renderHorizontalBar(guiGraphics.pose(), x, y, staminaRatio, BAR_HORIZONTAL_FILL, BlitFloat.NO_TINT, alpha);
			ICON.render(guiGraphics.pose(), x - 12, y - 6, ARGB.white(alpha));
		}
		
		@Override
		protected void checkTooltip(double mouseX, double mouseY, DeltaTracker deltaTracker) {
			StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
			
			if (ResolveStageBuffs.ignoreStaminaDebuff(Minecraft.getInstance().player)) {
				this.tooltip.set(this.tooltipResolve);
			}
			else {
				this.tooltip.set(this.tooltipText);
			}
			
			MultiLineScreenTooltip tooltipText = (MultiLineScreenTooltip) this.tooltip.get();
			float value = standPower.getStamina();
			float maxValue = standPower.getMaxStamina();
			float ratio = standPower.getStaminaRatio();
			tooltipText.setTitle(Component.translatable("ripples_hud.stamina_bar",
					Component.literal(String.valueOf((int) value)).withStyle(style -> style.withColor(color(ratio))),
					Component.literal(String.valueOf((int) maxValue))
					).withStyle(ChatFormatting.BLACK));
			super.checkTooltip(mouseX, mouseY, deltaTracker);
		}
		
		public static int color(float ratio) {
			return FastColor.ARGB32.colorFromFloat(1, (1 - ratio) * 0.6f, ratio * 0.6f, 0f);
		}
	}
	
	
	public static class Finisher extends HudElement {
		public static final ResourceLocation[] BARS = {
				JojoMod.resLoc("textures/hud/stand_finisher_1.png"),
				JojoMod.resLoc("textures/hud/stand_finisher_2.png"),
				JojoMod.resLoc("textures/hud/stand_finisher_3.png")
		};
		public static final ResourceLocation[] BARS_FULL = {
				JojoMod.resLoc("textures/hud/stand_finisher_1_full.png"),
				JojoMod.resLoc("textures/hud/stand_finisher_2_full.png")
		};

		public Finisher(String name, int x0, int y0, int width, int height) {
			super(name, x0, y0, width, height);
		}

		public Finisher(String name, SnappingH snappingHorizontal, SnappingV snappingVertical, 
				int xOffset, int yOffset, int width, int height) {
			super(name, snappingHorizontal, snappingVertical, xOffset, yOffset, width, height);
		}

		@Override
		public boolean shouldRender() {
			if (hud.forContainerMenu.isTrue()) return false;
			StandEntity stand = ClientGlobals.playerStandEntity;
			return stand != null;
		}
		
		@Override
		public void renderElement(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
			Minecraft mc = Minecraft.getInstance();
			StandEntity stand = ClientGlobals.playerStandEntity;
			float partialTick = ClientUtil.partialTick(deltaTracker, false);
			float finisher = stand.getFinisherMeter(partialTick);
			
			int crosshairX = (guiGraphics.guiWidth() - 15) / 2;
			int crosshairY = (guiGraphics.guiHeight() - 15) / 2;
			
			float width = getWidth();
			float height = getHeight();
			float x = crosshairX - width / 4;
			float y = crosshairY - height / 4;
			int color = ARGB.white(0.5f);

			int fullFinishers = Mth.floor(finisher);
			if (fullFinishers > 0) {
				BlitFloat.blit(guiGraphics.pose(), mc, BARS_FULL[Math.min(fullFinishers, BARS_FULL.length) - 1], 
						x, y, width, height, 0, 
						color);
			}
			
			float finisherFill = Mth.frac(finisher);
			BlitFloat.blitRadial(guiGraphics.pose(), mc, BARS[Math.min(fullFinishers, BARS.length - 1)], 
					x, y, width, height, 0, 
					0, finisherFill, color);
		}
	}
	
	
	public static class StandRange extends HudElement {

		public StandRange(String name, int x0, int y0, int width, int height) {
			super(name, x0, y0, width, height);
		}

		public StandRange(String name, SnappingH snappingHorizontal, SnappingV snappingVertical, 
				int xOffset, int yOffset, int width, int height) {
			super(name, snappingHorizontal, snappingVertical, xOffset, yOffset, width, height);
		}

		@Override
		public boolean shouldRender() {
			if (hud.forContainerMenu.isTrue()) return false;
			StandEntity stand = ClientGlobals.playerStandEntity;
			return stand != null && stand.isManuallyControlled();
		}
		
		@Override
		public void renderElement(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
			StandEntity stand = ClientGlobals.playerStandEntity;
			double distance = MathUtil.getAABBDistance(stand.getBoundingBox(), stand.getUser().getBoundingBox());
			double damageFactor = stand.rangeEfficiency;
			Font font = Minecraft.getInstance().font;

			int x = this.getX();
			int y = this.getY();
			int width;
			int height;
			Component distanceString = Component.literal(String.format("%.2f m", distance));
			guiGraphics.drawString(font, distanceString, x, y, 0xFFFFFFFF);
			width = font.width(distanceString);
			height = font.lineHeight;
			if (damageFactor < 1) {
				y += 12;
				Component strength = Component.translatable("jojo_ripples.overlay.stand_strength", String.format("%.2f%%", damageFactor * 100F));
				guiGraphics.drawString(font, strength, x, y, 0xFF4040);
				width = Math.max(width, font.width(strength));
				height += 12;
			}
			updateRectangle(width, height);
		}
	}
	
}
