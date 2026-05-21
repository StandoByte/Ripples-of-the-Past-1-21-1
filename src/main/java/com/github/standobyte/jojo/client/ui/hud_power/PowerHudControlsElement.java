package com.github.standobyte.jojo.client.ui.hud_power;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.input.AbilityInputState;
import com.github.standobyte.jojo.client.input.HeldKeyTimer;
import com.github.standobyte.jojo.client.input.InputHandler;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme.AbilityControlsEntry;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme.Hotbar;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme.HotbarSlot;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme.InputsByKeyModifier;
import com.github.standobyte.jojo.client.input.controlscheme.ClientInputBind;
import com.github.standobyte.jojo.client.input.controlscheme.ClientKey;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.standskin.sprites.AbilityIconSprites;
import com.github.standobyte.jojo.client.textsymbols.IconSymbols;
import com.github.standobyte.jojo.client.ui.DrawHotbar;
import com.github.standobyte.jojo.client.ui.hud_power.PowerHud.AbilityHud;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.client.ui.utils.TextUtil;
import com.github.standobyte.jojo.client.ui.utils.tooltip.TooltipParams;
import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.client.util.functions.ShortenText;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities.AbilityConditionCheck;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.v1_21_4_stuff.missingmethods.ARGB;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.util.TriState;

public class PowerHudControlsElement extends HudElement {
	public PowerHudControlsElement(String name, int x0, int y0, int width, int height) { super(name, x0, y0, width, height); }
	public PowerHudControlsElement(String name, SnappingH snappingHorizontal, SnappingV snappingVertical, int xOffset, int yOffset, int width, int height) { super(name, snappingHorizontal, snappingVertical, xOffset, yOffset, width, height); }

	@Override
	public boolean shouldRender() {
		InputHandler input = InputHandler.getInstance();
		if (input == null) return false;

		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return false;

		ClientControlScheme controlScheme = input.getActiveControlScheme();
		if (controlScheme == null) return false;

		return !hud.forContainerMenu.isFalse();
	}

	@Override
	public void renderElement(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		clear();

		Minecraft mc = Minecraft.getInstance();
		InputHandler input = InputHandler.getInstance();
		Font font = mc.font;
		ClientControlScheme controlScheme = input.getActiveControlScheme();
		float partialTick = ClientUtil.partialTick(deltaTracker, false);
		StandSkin standSkin = StandSkinsLoader.getCurSkin();
		int textColor = controlScheme.powerClassCosmetic == PowerClass.STAND && standSkin != null ? standSkin.getColors().text() : 0xFFFFFFFF;

		this.prepare(hud, controlScheme, font, input.getCurModifier(), standSkin);
		// XXX (controls HUD) update size
		this.renderControls(this.getX(), this.getY(), mc, guiGraphics, deltaTracker, font, textColor, partialTick);
	}
	
	@Override
	protected void checkTooltip(double mouseX, double mouseY, DeltaTracker deltaTracker) {
		for (var abilitySlot : hoverableAbilities) {
			BindUI slot = abilitySlot.getFirst();
			ScreenRectangle rectangle = abilitySlot.getSecond();
			boolean isHovered = rectangle.containsPoint((int) mouseX, (int) mouseY);
			if (isHovered) {
				Screen screen = Minecraft.getInstance().screen;
				if (screen != null) {
					List<Component> tooltip = new ArrayList<>();
					for (var byInputMethod : slot.abilities.entrySet()) {
						InputMethod inputMethod = byInputMethod.getKey();
						AbilityBindUI bindUI = byInputMethod.getValue();
						Component keyName = switch (inputMethod) {
							case CLICK -> slot.fullKeybind;
							case HOLD -> Component.translatable("ripples_hud.hold_key", slot.fullKeybind);
						};
						Ability ability = bindUI.ability.ability;
						Power<?> power = ClientPowerCache.getPower(ability.abilityId.powerClass());
						Component abilityName = ability.getName(power);
						Component line = Component.translatable("ripples_hud.key_ability", keyName, abilityName)
								.withStyle(ChatFormatting.BLACK);
						tooltip.add(line);
					}
					screen.setTooltipForNextRenderPass(TextUtil.splitMultiLine(Minecraft.getInstance().font, 
							tooltip, TextUtil.TOOLTIP_MAX_WIDTH), new BelowOrAboveWidgetTooltipPositioner(rectangle), true);
					TooltipParams.set(TooltipParams.paperStyle());
				}
				break;
			}
		}
	}


	

	public static final int SLOT_WIDTH = 20;
	public static final int SLOT_HEIGHT = 20;
	
	public static class AbilityBindUI {
		public AbilityConditionCheck ability;
		public TextureAtlasSprite sprite;
		public ClientKey key;
		public KeyModifier modifier = KeyModifier.NONE;
		public InputMethod inputMethod;
		public Component keybindName;
		public Component keybindAbilityName;
	}
	
	public static class BindUI {
		public Component mainKey;
		@Nullable public Component modifierKey;
		public Component fullKeybind;
		@Nullable public KeyModifier modifier;
		public Map<InputMethod, AbilityBindUI> abilities = new EnumMap<>(InputMethod.class);
		
		public int y;
		public int keybindWidth;
		public int width;
	}

	public static class HotbarUILine {
		public List<HotbarSlotUI> slots = new ArrayList<>();
		public HotbarSlotUI selected;
		public Component keybind;
		public Component switchHint;

		public int y;
		public int keybindWidth;
		public int width;

		public boolean isSelectingAbility;
		public boolean highlight;
	}
	
	public static class HotbarSlotUI {
		public int slotIndex;
		public BindUI bind;
		public AbilityBindUI sprite;
		public Map<InputMethod, AbilityBindUI> abilities = new EnumMap<>(InputMethod.class);
	}


	public List<BindUI> binds = new ArrayList<>();
	public List<HotbarUILine> hotbars = new ArrayList<>();
	public List<Pair<BindUI, ScreenRectangle>> hoverableAbilities = new ArrayList<>();

	public void clear() {
		binds.clear();
		hotbars.clear();
		hoverableAbilities.clear();
	}

	public void prepare(AbilityHud hud, ClientControlScheme controlScheme, Font font, 
			@Nonnull KeyModifier modifier, @Nullable StandSkin standSkin) {
		if (controlScheme == null) return;
		ClientControlScheme.MoveGroup curGroup = controlScheme.getCurGroup();
		if (modifier == KeyModifier.ALT) modifier = KeyModifier.NONE;
		AbilityIconSprites abilityIconSprites = StandSkinsLoader.getInstance().abilityIcons;
		
		InputHandler modInput = InputHandler.getInstance();

		// separate keybinds
		Map<ClientKey, InputsByKeyModifier> binds = curGroup.getBinds();
		for (var bindEntry : binds.entrySet()) {
			ClientKey key = bindEntry.getKey();
			BindUI bindUI = new BindUI();
			InputsByKeyModifier bindsForInputMethod = bindEntry.getValue();
			
			boolean hasBind = false;
			for (InputMethod inputMethod : InputMethod.values()) {
				AbilityBindUI abilityBindUI = makeBindUI(inputMethod, 
						(KeyModifier mod) -> bindsForInputMethod.getAll(mod, inputMethod), 
						modifier, key, false, 
						abilityIconSprites, standSkin, 
						font, hud.forContainerMenu);
				if (abilityBindUI != null) {
					bindUI.modifier = null;
					bindUI.abilities.put(inputMethod, abilityBindUI);
					hasBind = true;
				}
			}
			
			if (!hasBind && modifier == KeyModifier.NONE) {
				// if you aren't pressing Ctrl or Shift, but there is no ability keybind to render, render the Ctrl and Shift ones instead
				for (InputMethod inputMethod : InputMethod.values()) {
					for (KeyModifier otherModifier : KeyModifier.values()) {
						if (otherModifier != modifier) {
							AbilityBindUI abilityBindUI = makeBindUI(inputMethod, 
									(KeyModifier mod) -> bindsForInputMethod.getAll(mod, inputMethod), 
									otherModifier, key, true, 
									abilityIconSprites, standSkin, 
									font, hud.forContainerMenu);
							
							if (abilityBindUI != null) {
								if (bindUI.modifier == null) {
									bindUI.modifier = otherModifier;
								}
								bindUI.abilities.put(inputMethod, abilityBindUI);
							}
						}
					}
				}
			}
			
			if (!bindUI.abilities.isEmpty()) {
				bindUI.mainKey = getKeyName(key);
				bindUI.modifierKey = getModifierName(bindUI.modifier);
				bindUI.fullKeybind = getKeyName(key, bindUI.mainKey, bindUI.modifier);
				
				bindUI.keybindWidth = font.width(bindUI.mainKey);
				if (bindUI.modifierKey != null) 
					bindUI.keybindWidth = Math.max(bindUI.keybindWidth, font.width(bindUI.modifierKey));
				bindUI.keybindWidth += 4;
				
				bindUI.width = bindUI.keybindWidth + bindUI.abilities.size() * SLOT_WIDTH + 4;
				this.binds.add(bindUI);
			}
		}

		// hotbars
		for (Hotbar hotbar : curGroup.hotbars) {
			if (!hotbar.slots.isEmpty()) {
				HotbarUILine hotbarUI = new HotbarUILine();

				ClientInputBind input = hotbar.useAbilityKey;
				HotbarSlot selectedSlot = hotbar.getSelected();
				
				hotbarUI.isSelectingAbility = modInput.isSelectingAbility(hotbar);
				hotbarUI.highlight = hotbarUI.isSelectingAbility && !hotbar.alwaysSwitchAbility();

				for (ClientControlScheme.HotbarSlot slot : hotbar.slots) {
					HotbarSlotUI slotUI = new HotbarSlotUI();
					ClientKey key = input.getKey();
					slotUI.bind = new BindUI();
					slotUI.bind.mainKey = getKeyName(key);
					slotUI.bind.modifierKey = getModifierName(input.getKeyModifier());
					slotUI.bind.fullKeybind = getKeyName(key, slotUI.bind.mainKey, input.getKeyModifier());
					slotUI.slotIndex = slot.index;
					
					for (InputMethod inputMethod : InputMethod.values()) {
						InputsByKeyModifier slotBinds = slot.getBinds();
						
						AbilityBindUI bind = makeBindUI(inputMethod, 
								(KeyModifier mod) -> slotBinds.getAll(mod, inputMethod), 
								modifier, key, false, 
								abilityIconSprites, standSkin, 
								font, hud.forContainerMenu);
						if (bind != null) {
							if (slotUI.sprite == null || inputMethod == InputMethod.HOLD && InputHandler.getInstance().isHeld(key, modifier)) {
								slotUI.sprite = bind;
							}
							slotUI.abilities.put(inputMethod, bind);
							slotUI.bind.abilities.put(inputMethod, bind);
						}
						
//						AbilityConditionCheck ability = ClientControlScheme.prioritizedAbility(modifier, 
//								(KeyModifier mod) -> slotBinds.getAll(mod, inputMethod), 
//								(AbilityInputState state) -> AbilityInputState.showAbilityInHUD(state, hud.forContainerMenu));
//						if (ability != null) {
//							AbilityBindUI bind = makeAbilityBindUI(key, null, 
//									inputMethod, ability, 
//									abilityIconSprites, standSkin, 
//									font, hud.forContainerMenu);
//							if (bind != null) {
//								if (slotUI.sprite == null || inputMethod == InputMethod.HOLD && InputHandler.getInstance().isHeld(key, modifier)) {
//									slotUI.sprite = bind;
//								}
//								slotUI.abilities.put(inputMethod, bind);
//								slotUI.bind.abilities.put(inputMethod, bind);
//							}
//						}
					}
					if (hotbarUI.isSelectingAbility || !slotUI.abilities.isEmpty()) {
						hotbarUI.slots.add(slotUI);
						if (slot == selectedSlot) {
							hotbarUI.selected = slotUI;
						}
					}
				}

				ClientKey hotbarKey = input.getKey();
				hotbarUI.keybind = getKeyName(hotbarKey, KeyModifier.NONE);
				hotbarUI.switchHint = null;
				if (hotbar.switchAbilityKey != null) {
					ClientKey boundKey = hotbar.switchAbilityKey.getKey();
					if (boundKey != null) {
						hotbarUI.switchHint = Component.translatable("ripples_hud.hotbar_switch", 
								getKeyName(boundKey, hotbar.switchAbilityKey.getKeyModifier()));
					}
				}
				
				hotbarUI.keybindWidth = font.width(hotbarUI.keybind) + 4;
				hotbarUI.width = hotbarUI.keybindWidth + hotbarUI.slots.size() * SLOT_WIDTH + 4;
				if (hotbarUI.switchHint != null) {
					hotbarUI.width = Math.max(font.width(hotbarUI.switchHint), hotbarUI.width);
				}

				this.hotbars.add(hotbarUI);
			}
		}


//		int maxKeybindWidth = this.binds.stream().mapToInt(bind -> bind.keybindWidth).max().orElse(0);
		int maxKeybindWidth = 17;
		for (BindUI bindUI : this.binds) {
			bindUI.keybindWidth = maxKeybindWidth;
		}

		int maxHotbarKeybindWidth = this.hotbars.stream().mapToInt(hotbar -> hotbar.keybindWidth).max().orElse(0);
		for (var hotbarUI : this.hotbars) {
			hotbarUI.keybindWidth = maxHotbarKeybindWidth;
		}

		int y = 0;
		for (BindUI bind : this.binds) {
			bind.y = y;
			hoverableAbilities.add(Pair.of(bind, new ScreenRectangle(
					this.getX() + bind.keybindWidth, 
					this.getY() + y, 
					bind.abilities.size() * SLOT_WIDTH + 2, SLOT_HEIGHT + 2)));
			y += SLOT_HEIGHT + 2;
		}
		for (HotbarUILine hotbar : this.hotbars) {
			y += 8;
			hotbar.y = y;
			int x = 12;
			for (HotbarSlotUI slot : hotbar.slots) {
				if (slot.bind != null) {
					hoverableAbilities.add(Pair.of(slot.bind, new ScreenRectangle(
							this.getX() + x, 
							this.getY() + y, 
							SLOT_WIDTH + 2, SLOT_HEIGHT + 2)));
				}
				x += SLOT_WIDTH;
			}
			y += SLOT_HEIGHT + 2;
			if (hotbar.isSelectingAbility || hotbar.switchHint != null) {
				y += font.lineHeight + 2;
			}
		}
	}


	@Nullable
	private static AbilityBindUI makeBindUI(InputMethod inputMethod, 
			Function<KeyModifier, List<AbilityControlsEntry>> getAbilities, 
			@Nonnull KeyModifier modifier, ClientKey key, boolean withModifierName, 
			AbilityIconSprites abilitySprites, @Nullable StandSkin standSkin, 
			Font font, TriState forContainerMenu) {
		AbilityConditionCheck ability = ClientControlScheme.prioritizedAbility(modifier, 
				getAbilities, 
				(AbilityInputState state) -> AbilityInputState.showAbilityInHUD(state, forContainerMenu));
		return ability == null ? null : makeAbilityBindUI(key, withModifierName ? modifier : KeyModifier.NONE, 
				inputMethod, ability, 
				abilitySprites, standSkin, 
				font, forContainerMenu);
	}

	@Nullable
	private static AbilityBindUI makeAbilityBindUI(ClientKey key, KeyModifier modifier, 
			InputMethod inputMethod, AbilityConditionCheck ability, 
			AbilityIconSprites abilitySprites, @Nullable StandSkin standSkin, 
			Font font, TriState forContainerMenu) {
		if (ability != null && ability.ability != null) {
			boolean showAbility = AbilityInputState.showAbilityInHUD(ability, forContainerMenu);

			if (showAbility) {
				Component keyName = getKeyName(key, modifier);
				Component bindName = inputMethod == InputMethod.HOLD ? Component.translatable("ripples_hud.hold_key", keyName) : keyName;
				Power<?> abilityCtx = ClientPowerCache.getPower(ability.ability.abilityId.powerClass());

				AbilityBindUI bindUI = new AbilityBindUI();

				bindUI.ability = ability;
				bindUI.sprite = abilitySprites.getAbilityIcon(ability.ability, abilityCtx, standSkin);
				bindUI.key = key;
				bindUI.modifier = modifier;
				bindUI.inputMethod = inputMethod;
				bindUI.keybindName = bindName;
				bindUI.keybindAbilityName = Component.translatable("ripples_hud.key_ability", bindName, ability.ability.getName(abilityCtx));

				return bindUI;
			}
		}
		return null;
	}


	public void renderControls(int x, int y, Minecraft mc, GuiGraphics guiGraphics, 
			DeltaTracker deltaTracker, Font font, int textColor, float partialTick) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		
		InputHandler modInput = InputHandler.getInstance();
		int alpha = InputHandler.inputsDisabled ? 0x40FFFFFF : BlitFloat.NO_TINT;
		textColor &= alpha;
		
		for (BindUI bind : this.binds) {
			int hotbarLength = bind.abilities.size();
			
			int x0 = x;
			int y0 = y;

			y += bind.y;
			if (bind.modifierKey == null) {
				int centered = (bind.keybindWidth - font.width(bind.fullKeybind)) / 2;
				guiGraphics.drawString(font, bind.fullKeybind, 
						x + centered, y + (SLOT_HEIGHT - font.lineHeight) / 2 + 2, 
						textColor);
			}
			else {
				int centered = (bind.keybindWidth - font.width(bind.modifierKey)) / 2;
				guiGraphics.drawString(font, bind.modifierKey, 
						x + centered, y + (SLOT_HEIGHT - font.lineHeight) / 2 - 3, 
						textColor);
				centered = (bind.keybindWidth - font.width(bind.mainKey)) / 2;
				guiGraphics.drawString(font, bind.mainKey, 
						x + centered, y + (SLOT_HEIGHT - font.lineHeight) / 2 + 7, 
						textColor);
			}
			x += bind.keybindWidth;

			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();

			int i = 0;
			int selectionX = -1;
			for (Map.Entry<InputMethod, AbilityBindUI> abilitySprite : bind.abilities.entrySet()) {
				AbilityBindUI abilityUI = abilitySprite.getValue();
				DrawHotbar.drawHotbarSlot(hotbarLength, i, false, guiGraphics.pose(), x, y, alpha);
				renderAbility(guiGraphics, x, y, abilityUI, mc, partialTick, alpha);

				boolean isClicked = switch (abilityUI.inputMethod) {
					case CLICK -> {
						yield modInput.wasKeyClickedRecently(abilityUI.key);
					}
					case HOLD -> {
						HeldKeyTimer heldKeyTimer = modInput.getHeldKeyTimer(abilityUI.key);
						yield heldKeyTimer != null && heldKeyTimer.isDefinitelyHold();
					}
				};
				if (isClicked) {
					selectionX = x;
				}
				
				x += SLOT_WIDTH;
				i++;
			}
			if (selectionX != -1) {
				DrawHotbar.drawHotbarSelection(false, guiGraphics.pose(), selectionX, y, alpha);
			}

			x = x0;
			y = y0;
		}

		for (HotbarUILine hotbar : this.hotbars) {
			int x0 = x;
			int y0 = y;
			int hotbarLength = hotbar.slots.size();
			
			y += hotbar.y;
			if (hotbar.keybind != null) {
				int centered = (hotbar.keybindWidth - font.width(hotbar.keybind)) / 2;
				guiGraphics.drawString(font, hotbar.keybind, 
						x + centered, y + (SLOT_HEIGHT - font.lineHeight) / 2, 
						textColor);
			}
			x += hotbar.keybindWidth;

			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();

			int i = 0;
			int selectionX = -1;
			for (HotbarSlotUI slot : hotbar.slots) {
				AbilityBindUI abilityUI = slot.sprite;
				DrawHotbar.drawHotbarSlot(hotbarLength, i, false, guiGraphics.pose(), x, y, alpha);
				if (abilityUI != null) {
					renderAbility(guiGraphics, x, y, abilityUI, mc, partialTick, alpha);
				}
				
				if (slot == hotbar.selected) {
					selectionX = x;
				}
				
				x += SLOT_WIDTH;
				i++;
			}
			if (selectionX != -1) {
				DrawHotbar.drawHotbarSelection(false, guiGraphics.pose(), selectionX, y, alpha);
				
				if (hotbar.highlight) {
					float time = modInput.getHotbarsSelectionTime();
					int highlightAlpha = (int) (ClientUtil.getHighlightAlpha(time + 20F, 40F, 40F, 0.25F, 0.5F) * 255F);
					guiGraphics.fill(selectionX - 1, y - 1, selectionX + 23, y + 23, ARGB.white(highlightAlpha));
					RenderSystem.enableBlend();
//					ClientUtil.fillSingleRect(selectionX + hotbarFold.getSlotWithIndex(selected).pos - 4, y - 4, 24, 23, 255, 255, 255, highlightAlpha);
				}
			}

			x = x0;
			y += SLOT_HEIGHT + 4;
			if (hotbar.isSelectingAbility) {
				x += 18;
				for (HotbarSlotUI slot : hotbar.slots) {
					if (!slot.abilities.isEmpty()) {
						int numberKey = HotbarSlot.numberKey(slot.slotIndex);
						if (numberKey != -1) {
							guiGraphics.drawString(font, String.valueOf(slot.slotIndex + 1), x, y, textColor);
						}
					}
					x += SLOT_WIDTH;
				}
				x = x0;
			}
			else if (hotbar.switchHint != null) {
				guiGraphics.drawString(font, hotbar.switchHint, x, y, textColor);
			}
			y += font.lineHeight + 4;

			x = x0;
			y = y0;
		}

		RenderSystem.disableBlend();
	}

	public static void renderAbility(GuiGraphics guiGraphics, float x, float y, AbilityBindUI abilityUi, Minecraft mc, float partialTick, int alpha) {
		AbilityConditionCheck abilityCheck = abilityUi.ability;
		Ability ability = abilityCheck.ability;
		Power<?> power = ClientPowerCache.getPower(ability.abilityId.powerClass());
		ability.renderAbilityIcon(power, guiGraphics, abilityUi.sprite, 
				x + 3, y + 3, abilityColor(alpha, abilityUi.ability));
		
		if (mc.player != null) {
			WindupIndicator windup = abilityUi.ability.ability.cl_windupIndicator(mc.player, windupIndicator, partialTick);
			if (windup != null) {
				renderWindupIndicator(guiGraphics, x + 13, y + 13, windup.value, windup.maxValue, mc, alpha);
				WindupAtCrosshair.setRender(windup);
			}
		}
	}
	
	static final WindupIndicator windupIndicator = new WindupIndicator();
	public static void renderWindupIndicator(GuiGraphics guiGraphics, float x, float y, float value, float maxValue, Minecraft mc, int color) {
		if (maxValue > 0) {
			float ratio;
			if (value < 0) {
				ratio = 0;
				color = FastColor.ARGB32.multiply(color, 0xC0FFFFFF);
			}
			else {
				ratio = Mth.clamp(value / maxValue, 0, 1);
			}
			
			BlitFloat.blitRadial(guiGraphics.pose(), mc, WINDUP_EMPTY.file, 
					x, y, WINDUP_EMPTY.width, WINDUP_EMPTY.height, 10, 
					ratio * (float) Math.PI * 2, 1 - ratio, color);
			
			BlitFloat.blitRadial(guiGraphics.pose(), mc, WINDUP_FULL.file, 
					x, y, WINDUP_FULL.width, WINDUP_FULL.height, 10, 
					0, ratio, color);
		}
	}
	
	public static final GuiIcon WINDUP_EMPTY = new GuiIcon(JojoMod.resLoc("textures/gui/windup_empty.png"), 13, 13);
	public static final GuiIcon WINDUP_FULL = new GuiIcon(JojoMod.resLoc("textures/gui/windup_full.png"), 13, 13);




	public static int abilityColor(int color, AbilityConditionCheck ability) {
		if (!ability.conditionCheck.isPositive()) {
			color = ARGB32.multiply(color, 0xFF606060);
		}
		return color;
	}

	public static final Component NOT_BOUND = Component.translatable("key.keyboard.unknown");
	
	public static Component getKeyName(@Nullable ClientKey key, Component keyName, KeyModifier modifier) {
		if (key == null) {
			return NOT_BOUND;
		}
		return modifier != null ? modifier.getCombinedName(key.getVanillaKey(), () -> keyName) : keyName;
	}
	
	public static Component getKeyName(@Nullable ClientKey key, KeyModifier modifier) { return getKeyName(key, getKeyName(key), modifier); }

	public static Component getKeyName(ClientKey key) {
		if (key == InputHandler.LMB) {
			return Component.literal(Character.toString(IconSymbols.LMB_CLICK_LARGE));
		}
		else if (key == InputHandler.RMB) {
			return Component.literal(Character.toString(IconSymbols.RMB_CLICK_LARGE));
		}
		else if (key == InputHandler.MMB) {
			return Component.literal(Character.toString(IconSymbols.MMB_CLICK_LARGE));
		}
		else {
			return ShortenText.shortenIfAble(key.keyName());
		}
	}
	
	public static final Component CONTROL = Component.translatable("neoforge.controlsgui.control.noplus");
	public static final Component CONTROL_MAC = Component.translatable("neoforge.controlsgui.control.mac.noplus");
	public static final Component SHIFT = Component.translatable("neoforge.controlsgui.shift.noplus");
	public static final Component ALT = Component.translatable("neoforge.controlsgui.alt.noplus");
	
	@Nullable
	public static Component getModifierName(KeyModifier modifier) {
		if (modifier == null) return null;
		return switch (modifier) {
			case CONTROL -> Minecraft.ON_OSX ? CONTROL_MAC : CONTROL;
			case SHIFT -> SHIFT;
			case ALT -> ALT;
			case NONE -> null;
		};
	}
	
	
	public static final String CONTROL_NOSPACE = "neoforge.controlsgui.control.nospace";
	public static final String CONTROL_MAC_NOSPACE = "neoforge.controlsgui.control.mac.nospace";
	public static final String SHIFT_NOSPACE = "neoforge.controlsgui.shift.nospace";
	public static final String ALT_NOSPACE = "neoforge.controlsgui.alt.nospace";
	
	public static Component getKeybindNoSpaceAtModifierPlus(KeyMapping keybind) {
		KeyModifier modifier = keybind.getKeyModifier();
		if (modifier == null || modifier == KeyModifier.NONE) {
			return Component.keybind(keybind.getName());
		}
		else {
			String key = switch (modifier) {
				case CONTROL -> Minecraft.ON_OSX ? CONTROL_MAC_NOSPACE : CONTROL_NOSPACE;
				case SHIFT -> SHIFT_NOSPACE;
				case ALT -> ALT_NOSPACE;
				default -> null;
			};
			return Component.translatable(key, keybind.getKey().getDisplayName());
		}
	}

}
