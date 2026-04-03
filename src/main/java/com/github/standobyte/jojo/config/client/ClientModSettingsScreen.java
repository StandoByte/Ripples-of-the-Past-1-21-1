package com.github.standobyte.jojo.config.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.itemrender.ItemIconModels;
import com.github.standobyte.jojo.client.textsymbols.IconSymbols;
import com.github.standobyte.jojo.client.textsymbols.sprite.IconGlyphInfo;
import com.github.standobyte.jojo.client.textsymbols.sprite.IconGlyphsCache;
import com.github.standobyte.jojo.client.ui.screen_widgets.ButtonInLayout;
import com.github.standobyte.jojo.client.ui.screen_widgets.ItemButton;
import com.github.standobyte.jojo.client.ui.utils.Alignment;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.config.SettingsField;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.util.reflection.ClientReflection;
import com.github.standobyte.v1_21_4_stuff.GuiScissor;

import it.unimi.dsi.fastutil.objects.Object2CharArrayMap;
import it.unimi.dsi.fastutil.objects.Object2CharMap;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

// just noticed that, when the screen is opened, the main menu panorama rotates twice as fast
// obviously that's not worth anyone's brain cells to go and figure out why, but that's so random
@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ClientModSettingsScreen extends Screen {
    protected final Screen lastScreen;
	protected final ClientModSettings settings;
	protected final ClientModSettings.Settings settingsValues;
	
	protected final List<Title> categories = new ArrayList<>();
	protected static record Title(Component title, int y) {}

	public ClientModSettingsScreen(Screen lastScreen, ClientModSettings settings) {
		this(lastScreen, settings, Component.translatable("jojo_ripples.options.client.title"));
	}

	public ClientModSettingsScreen(Screen lastScreen, ClientModSettings settings, Component title) {
		super(title);
		this.lastScreen = lastScreen;
		this.settings = settings;
		this.settingsValues = ClientModSettings.getSettingsReadOnly();
		categoryOpenedTimestampSoThatScrollingDoesntSuck = Util.getMillis();
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.lastScreen);
	}

	@Override
	protected void init() {
		addRenderableWidgets();
	}

	protected void addRenderableWidgets() {
		categories.clear();
		int i = 0;
		int yOffset = 0;
		
		// TODO make client config screen scrollable for when there'll be more config options

//		BooleanSetting characterVoiceLines = new BooleanSetting(settings, 
//				Component.translatable("jojo_ripples.config.client.characterVoiceLines"), 
//				Component.translatable("jojo_ripples.config.client.characterVoiceLines.tooltip")
//				) {
//			@Override public Boolean get() { return settingsValues.characterVoiceLines; }
//			@Override public void set(Boolean value) { settingsValues.characterVoiceLines = value; }
//		};
//		addRenderableWidget(characterVoiceLines.createButton(calcButtonX(i), calcButtonY(i++) + yOffset, 150, 20, this, i));
//
//		BooleanSetting menacingParticles = new BooleanSetting(settings, 
//				Component.translatable("jojo_ripples.config.client.menacingParticles"), 
//				Component.translatable("jojo_ripples.config.client.menacingParticles.tooltip")
//				) {
//			@Override public Boolean get() { return settingsValues.menacingParticles; }
//			@Override public void set(Boolean value) { settingsValues.menacingParticles = value; }
//		};
//		addRenderableWidget(menacingParticles.createButton(calcButtonX(i), calcButtonY(i++) + yOffset, 150, 20, this, i));

		
		// HUD settings
		
		yOffset += 10;
		if (i % 2 == 1) ++i;
		categories.add(new Title(Component.translatable("jojo_ripples.options.client.hud"), calcButtonY(i) + yOffset));
		yOffset += 15;

//		EnumSetting<PositionConfig> barsPosition = new EnumSetting<PositionConfig>(settings, 
//				Component.translatable("jojo_ripples.config.client.barsPosition"), 
//				Component.translatable("jojo_ripples.config.client.barsPosition.tooltip"), 
//				PositionConfig.class) {
//			@Override public PositionConfig get() { return settingsValues.barsPosition; }
//			@Override public void set(PositionConfig value) { settingsValues.barsPosition = value; }
//		};
//		addRenderableWidget(barsPosition.createButton(calcButtonX(i), calcButtonY(i++) + yOffset, 150, 20, this, i));
//
//		EnumSetting<PositionConfig> hotbarsPosition = new EnumSetting<PositionConfig>(settings, 
//				Component.translatable("jojo_ripples.config.client.hotbarsPosition"), 
//				Component.translatable("jojo_ripples.config.client.hotbarsPosition.tooltip"), 
//				PositionConfig.class) {
//			@Override public PositionConfig get() { return settingsValues.hotbarsPosition; }
//			@Override public void set(PositionConfig value) { settingsValues.hotbarsPosition = value; }
//		};
//		addRenderableWidget(hotbarsPosition.createButton(calcButtonX(i), calcButtonY(i++) + yOffset, 150, 20, this, i));
//
//		EnumSetting<HudTextRender> hudNamesRender = new EnumSetting<HudTextRender>(settings, 
//				Component.translatable("jojo_ripples.config.client.hudNamesRender"), 
//				Component.translatable("jojo_ripples.config.client.hudNamesRender.tooltip"), 
//				HudTextRender.class) {
//			@Override public HudTextRender get() { return settingsValues.hudTextRender; }
//			@Override public void set(HudTextRender value) { settingsValues.hudTextRender = value; }
//		};
//		addRenderableWidget(hudNamesRender.createButton(calcButtonX(i), calcButtonY(i++) + yOffset, 150, 20, this, i));
//
//		BooleanSetting hudHotbarsFold = new BooleanSetting(settings, 
//				Component.translatable("jojo_ripples.config.client.hudHotbarsFold"), 
//				Component.translatable("jojo_ripples.config.client.hudHotbarsFold.tooltip")
//				) {
//			@Override public Boolean get() { return settingsValues.hudHotbarFold; }
//			@Override public void set(Boolean value) { 
//				settingsValues.hudHotbarFold = value;
//				if (minecraft.player != null) {
//					for (PowerClass<?> power : PowerClass.values()) {
//						power.getOptional(minecraft.player).ifPresent(Power::clUpdateHud);
//					}
//				}
//			}
//		};
//		addRenderableWidget(hudHotbarsFold.createButton(calcButtonX(i), calcButtonY(i++) + yOffset, 150, 20, this, i));
//
//		BooleanSetting showLockedSlots = new BooleanSetting(settings, 
//				Component.translatable("jojo_ripples.config.client.showLockedSlots"), 
//				Component.translatable("jojo_ripples.config.client.showLockedSlots.tooltip")
//				) {
//			@Override public Boolean get() { return settingsValues.showLockedSlots; }
//			@Override public void set(Boolean value) {
//				settingsValues.showLockedSlots = value;
//				if (minecraft.player != null) {
//					for (PowerClass<?> power : PowerClass.values()) {
//						power.getOptional(minecraft.player).ifPresent(Power::clUpdateHud);
//					}
//				}
//			}
//		};
//		addRenderableWidget(showLockedSlots.createButton(calcButtonX(i), calcButtonY(i++) + yOffset, 150, 20, this, i));

		Setting<Boolean> abilitySelectionWheel = new BooleanSetting(settings, 
				Component.translatable("jojo_ripples.config.client.abilitySelectionWheel"), 
				Component.translatable("jojo_ripples.config.client.abilitySelectionWheel.tooltip")
				) {
			@Override public Boolean get() { return settingsValues.abilitySelectionWheel; }
			@Override public void set(Boolean value) {  settingsValues.abilitySelectionWheel = value; }
		}.withIcon(toIconPath("ability_selection_wheel"));
		addRenderableWidget(abilitySelectionWheel.createButton(calcButtonX(i), calcButtonY(i++) + yOffset, 150, 20, this, i));

		
		// Stand settings

		yOffset += 10;
		if (i % 2 == 1) ++i;
		categories.add(new Title(Component.translatable("jojo_ripples.options.client.stand"), calcButtonY(i) + yOffset));
		yOffset += 15;

		Setting<Boolean> standAimMarker = new BooleanSetting(settings, 
				Component.translatable("jojo_ripples.config.client.standAimMarker"), 
				Component.translatable("jojo_ripples.config.client.standAimMarker.tooltip")
				) {
			@Override public Boolean get() { return settingsValues.standAimMarker; }
			@Override public void set(Boolean value) {  settingsValues.standAimMarker = value; }
		}.withIcon(toIconPath("stand_aim_marker"), iconPath -> new IconGlyphInfo(new GuiIcon(iconPath, 17, 17), 17, 17, 0, -5, 5));
		addRenderableWidget(standAimMarker.createButton(calcButtonX(i), calcButtonY(i++) + yOffset, 150, 20, this, i));
		
		Setting<Boolean> standMotionTilt = new BooleanSetting(settings, 
				Component.translatable("jojo_ripples.config.client.standMotionTilt"), 
				Component.translatable("jojo_ripples.config.client.standMotionTilt.tooltip")
				) {
			@Override public Boolean get() { return settingsValues.standMotionTilt; }
			@Override public void set(Boolean value) { settingsValues.standMotionTilt = value; }
		};
		addRenderableWidget(standMotionTilt.createButton(calcButtonX(i), calcButtonY(i++) + yOffset, 150, 20, this, i));

//		BooleanSetting resolveShaders = new BooleanSetting(settings, 
//				Component.translatable("jojo_ripples.config.client.resolveShaders"), 
//				Component.translatable("jojo_ripples.config.client.resolveShaders.tooltip")
//				) {
//			@Override public Boolean get() { return settingsValues.resolveShaders; }
//			@Override public void set(Boolean value) { 
//				settingsValues.resolveShaders = value;
//				if (!value) {
//					ShaderEffectApplier.getInstance().stopResolveShader();
//				}
//			}
//		};
//		addRenderableWidget(resolveShaders.createButton(calcButtonX(i), calcButtonY(i++) + yOffset, 150, 20, this, i));
//
//		BooleanSetting timeStopAnimation = new BooleanSetting(settings, 
//				Component.translatable("jojo_ripples.config.client.timeStopAnimation"), 
//				Component.translatable("jojo_ripples.config.client.timeStopAnimation.tooltip")
//				) {
//			@Override public Boolean get() { return settingsValues.timeStopAnimation; }
//			@Override public void set(Boolean value) { settingsValues.timeStopAnimation = value; }
//		};
//		addRenderableWidget(timeStopAnimation.createButton(calcButtonX(i), calcButtonY(i++) + yOffset, 150, 20, this, i));
//
//		Setting<HumanoidArm> standSide = new EnumSetting<HumanoidArm>(settings, 
//				Component.translatable("jojo_ripples.config.client.standSide"), 
//				Component.translatable("jojo_ripples.config.client.standSide.tooltip"), 
//				HumanoidArm.class) {
//			@Override public HumanoidArm get() { return settingsValues.broadcasted.standSide; }
//			@Override public void set(HumanoidArm value) { settingsValues.broadcasted.standSide = value; }
//		}
//		.prefix("stand_")
//		.setBroadcasted();
//		addRenderableWidget(standSide.createButton(calcButtonX(i), calcButtonY(i++) + yOffset, 150, 20, this, i));
//
//
//		BooleanSetting standOutline = new BooleanSetting(settings, 
//				Component.translatable("jojo_ripples.config.client.standOutline"), 
//				Component.translatable("jojo_ripples.config.client.standOutline.tooltip")
//				) {
//			@Override public Boolean get() { return settingsValues.standOutline; }
//			@Override public void set(Boolean value) { settingsValues.standOutline = value; }
//		};
//		addRenderableWidget(standOutline.createButton(calcButtonX(i), calcButtonY(i++) + yOffset, 150, 20, this, i));
		
		
		// Hamon settings

		yOffset += 10;
		if (i % 2 == 1) ++i;
		categories.add(new Title(Component.translatable("jojo_ripples.options.client.hamon"), calcButtonY(i) + yOffset));
		yOffset += 15;

//		BooleanSetting thirdPersonHamonAura = new BooleanSetting(settings, 
//				Component.translatable("jojo_ripples.config.client.thirdPersonHamonAura"), 
//				Component.translatable("jojo_ripples.config.client.thirdPersonHamonAura.tooltip"), 
//				null) {
//			@Override public Boolean get() { return settingsValues.thirdPersonHamonAura; }
//			@Override public void set(Boolean value) { 
//				settingsValues.thirdPersonHamonAura = value;
//			}
//		};
//		addRenderableWidget(thirdPersonHamonAura.createButton(calcButtonX(i), calcButtonY(i++) + yOffset, 150, 20, this, i));
//
//		BooleanSetting firstPersonHamonAura = new BooleanSetting(settings, 
//				Component.translatable("jojo_ripples.config.client.firstPersonHamonAura"), 
//				Component.translatable("jojo_ripples.config.client.firstPersonHamonAura.tooltip"), 
//				null) {
//			@Override public Boolean get() { return settingsValues.firstPersonHamonAura; }
//			@Override public void set(Boolean value) { 
//				settingsValues.firstPersonHamonAura = value;
//			}
//		};
//		addRenderableWidget(firstPersonHamonAura.createButton(calcButtonX(i), calcButtonY(i++) + yOffset, 150, 20, this, i));
//
//		BooleanSetting hamonAuraBlur = new BooleanSetting(settings, 
//				Component.translatable("jojo_ripples.config.client.hamonAuraBlur"), 
//				Component.translatable("jojo_ripples.config.client.hamonAuraBlur.tooltip"), 
//				null) {
//			@Override public Boolean get() { return settingsValues.hamonAuraBlur; }
//			@Override public void set(Boolean value) { 
//				settingsValues.hamonAuraBlur = value;
//			}
//		};
//		addRenderableWidget(hamonAuraBlur.createButton(calcButtonX(i), calcButtonY(i++) + yOffset, 150, 20, this, i));
		
		
		// Vampirism settings

		yOffset += 10;
		if (i % 2 == 1) ++i;
		categories.add(new Title(Component.translatable("jojo_ripples.options.client.vampirism"), calcButtonY(i) + yOffset));
		yOffset += 15;

//		BooleanSetting glowingEyes = new BooleanSetting(settings, 
//				Component.translatable("jojo_ripples.config.client.vampireGlowingEyes"), 
//				Component.translatable("jojo_ripples.config.client.vampireGlowingEyes.tooltip")
//				) {
//			@Override public Boolean get() { return settingsValues.broadcasted.vampireGlowingEyes; }
//			@Override public void set(Boolean value) { 
//				settingsValues.broadcasted.vampireGlowingEyes = value;
//			}
//		};
//		addRenderableWidget(glowingEyes.createButton(calcButtonX(i), calcButtonY(i++) + yOffset, 150, 20, this, i));
		
		


//		addRenderableWidget(new Button.Builder(
//				Component.translatable("jojo_ripples.options.client.hud"), 
//				button -> minecraft.setScreen(new HudSettings(this, settings, button.getMessage())))
//				.bounds(calcButtonX(i), calcButtonY(i++) + yOffset + 6, 150, 20).build(/*Button::new*/));
//
//		addRenderableWidget(new Button.Builder(
//				Component.translatable("jojo_ripples.options.client.stand"), 
//				button -> minecraft.setScreen(new StandSettings(this, settings, button.getMessage())))
//				.bounds(calcButtonX(i), calcButtonY(i++) + yOffset + 6, 150, 20).build(/*Button::new*/));
//
//		addRenderableWidget(new Button.Builder(
//				Component.translatable("jojo_ripples.options.client.hamon"), 
//				button -> minecraft.setScreen(new HamonSettings(this, settings, button.getMessage())))
//				.bounds(calcButtonX(i), calcButtonY(i++) + yOffset + 6, 150, 20).build(/*Button::new*/));
//
//		addRenderableWidget(new Button.Builder(
//				Component.translatable("jojo_ripples.options.client.vampirism"), 
//				button -> minecraft.setScreen(new VampirismSettings(this, settings, button.getMessage())))
//				.bounds(calcButtonX(i), calcButtonY(i++) + yOffset + 6, 150, 20).build(/*Button::new*/));
		
		addBackButton(CommonComponents.GUI_DONE, i);
	}

	protected void addBackButton(Component text, int buttonsAdded) {
		buttonsAdded += 2;
		if (buttonsAdded % 2 == 1) {
			++buttonsAdded;
		}

		addRenderableWidget(new Button.Builder(
				text, button -> minecraft.setScreen(lastScreen))
				.bounds(this.width / 2 - 100, 
						this.height - 26, 
						200, 20)
				.build(/*Button::new*/));
	}



//	@Deprecated
//	public static class HudSettings extends ClientModSettingsScreen {
//
//		public HudSettings(Screen lastScreen, ClientModSettings settings, Component title) {
//			super(lastScreen, settings, title);
//		}
//
//		@Override
//		protected void addRenderableWidgets() {
//			int i = 0;
//
////			EnumSetting<PositionConfig> barsPosition = new EnumSetting<PositionConfig>(settings, 
////					Component.translatable("jojo_ripples.config.client.barsPosition"), 
////					Component.translatable("jojo_ripples.config.client.barsPosition.tooltip"), 
////					PositionConfig.class) {
////				@Override public PositionConfig get() { return settingsValues.barsPosition; }
////				@Override public void set(PositionConfig value) { settingsValues.barsPosition = value; }
////			};
////			addRenderableWidget(barsPosition.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
////
////
////			EnumSetting<PositionConfig> hotbarsPosition = new EnumSetting<PositionConfig>(settings, 
////					Component.translatable("jojo_ripples.config.client.hotbarsPosition"), 
////					Component.translatable("jojo_ripples.config.client.hotbarsPosition.tooltip"), 
////					PositionConfig.class) {
////				@Override public PositionConfig get() { return settingsValues.hotbarsPosition; }
////				@Override public void set(PositionConfig value) { settingsValues.hotbarsPosition = value; }
////			};
////			addRenderableWidget(hotbarsPosition.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
////
////
////			EnumSetting<HudTextRender> hudNamesRender = new EnumSetting<HudTextRender>(settings, 
////					Component.translatable("jojo_ripples.config.client.hudNamesRender"), 
////					Component.translatable("jojo_ripples.config.client.hudNamesRender.tooltip"), 
////					HudTextRender.class) {
////				@Override public HudTextRender get() { return settingsValues.hudTextRender; }
////				@Override public void set(HudTextRender value) { settingsValues.hudTextRender = value; }
////			};
////			addRenderableWidget(hudNamesRender.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
////
////
////			BooleanSetting hudHotbarsFold = new BooleanSetting(settings, 
////					Component.translatable("jojo_ripples.config.client.hudHotbarsFold"), 
////					Component.translatable("jojo_ripples.config.client.hudHotbarsFold.tooltip")
////					) {
////				@Override public Boolean get() { return settingsValues.hudHotbarFold; }
////				@Override public void set(Boolean value) { 
////					settingsValues.hudHotbarFold = value;
////					if (minecraft.player != null) {
////						for (PowerClass<?> power : PowerClass.values()) {
////							power.getOptional(minecraft.player).ifPresent(Power::clUpdateHud);
////						}
////					}
////				}
////			};
////			addRenderableWidget(hudHotbarsFold.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
////
////
////			BooleanSetting showLockedSlots = new BooleanSetting(settings, 
////					Component.translatable("jojo_ripples.config.client.showLockedSlots"), 
////					Component.translatable("jojo_ripples.config.client.showLockedSlots.tooltip")
////					) {
////				@Override public Boolean get() { return settingsValues.showLockedSlots; }
////				@Override public void set(Boolean value) {
////					settingsValues.showLockedSlots = value;
////					if (minecraft.player != null) {
////						for (PowerClass<?> power : PowerClass.values()) {
////							power.getOptional(minecraft.player).ifPresent(Power::clUpdateHud);
////						}
////					}
////				}
////			};
////			addRenderableWidget(showLockedSlots.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
//
//
//			BooleanSetting abilitySelectionWheel = new BooleanSetting(settings, 
//					Component.translatable("jojo_ripples.config.client.abilitySelectionWheel"), 
//					Component.translatable("jojo_ripples.config.client.abilitySelectionWheel.tooltip")
//					) {
//				@Override public Boolean get() { return settingsValues.abilitySelectionWheel; }
//				@Override public void set(Boolean value) { 
//					settingsValues.abilitySelectionWheel = value;
//				}
//			};
//			addRenderableWidget(abilitySelectionWheel.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
//
//			addBackButton(CommonComponents.GUI_BACK, i);
//		}
//
//	}
//
//	@Deprecated
//	public static class StandSettings extends ClientModSettingsScreen {
//
//		public StandSettings(Screen lastScreen, ClientModSettings settings, Component title) {
//			super(lastScreen, settings, title);
//		}
//
//		@Override
//		protected void addRenderableWidgets() {
//			int i = 0;
//
////			BooleanSetting resolveShaders = new BooleanSetting(settings, 
////					Component.translatable("jojo_ripples.config.client.resolveShaders"), 
////					Component.translatable("jojo_ripples.config.client.resolveShaders.tooltip")
////					) {
////				@Override public Boolean get() { return settingsValues.resolveShaders; }
////				@Override public void set(Boolean value) { 
////					settingsValues.resolveShaders = value;
////					if (!value) {
////						ShaderEffectApplier.getInstance().stopResolveShader();
////					}
////				}
////			};
////			addRenderableWidget(resolveShaders.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
////
////
////			BooleanSetting timeStopAnimation = new BooleanSetting(settings, 
////					Component.translatable("jojo_ripples.config.client.timeStopAnimation"), 
////					Component.translatable("jojo_ripples.config.client.timeStopAnimation.tooltip")
////					) {
////				@Override public Boolean get() { return settingsValues.timeStopAnimation; }
////				@Override public void set(Boolean value) { settingsValues.timeStopAnimation = value; }
////			};
////			addRenderableWidget(timeStopAnimation.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
////
////
////			Setting<HumanoidArm> standSide = new EnumSetting<HumanoidArm>(settings, 
////					Component.translatable("jojo_ripples.config.client.standSide"), 
////					Component.translatable("jojo_ripples.config.client.standSide.tooltip"), 
////					HumanoidArm.class) {
////				@Override public HumanoidArm get() { return settingsValues.broadcasted.standSide; }
////				@Override public void set(HumanoidArm value) { settingsValues.broadcasted.standSide = value; }
////			}
////			.prefix("stand_")
////			.setBroadcasted();
////			addRenderableWidget(standSide.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
////
////
////			BooleanSetting standMotionTilt = new BooleanSetting(settings, 
////					Component.translatable("jojo_ripples.config.client.standMotionTilt"), 
////					Component.translatable("jojo_ripples.config.client.standMotionTilt.tooltip")
////					) {
////				@Override public Boolean get() { return settingsValues.standMotionTilt; }
////				@Override public void set(Boolean value) { settingsValues.standMotionTilt = value; }
////			};
////			addRenderableWidget(standMotionTilt.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
////
////			BooleanSetting standOutline = new BooleanSetting(settings, 
////					Component.translatable("jojo_ripples.config.client.standOutline"), 
////					Component.translatable("jojo_ripples.config.client.standOutline.tooltip")
////					) {
////				@Override public Boolean get() { return settingsValues.standOutline; }
////				@Override public void set(Boolean value) { settingsValues.standOutline = value; }
////			};
////			addRenderableWidget(standOutline.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
//
//			addBackButton(CommonComponents.GUI_BACK, i);
//		}
//
//	}
//
//	@Deprecated
//	public static class HamonSettings extends ClientModSettingsScreen {
//
//		public HamonSettings(Screen lastScreen, ClientModSettings settings, Component title) {
//			super(lastScreen, settings, title);
//		}
//
//		@Override
//		protected void addRenderableWidgets() {
//			int i = 0;
//
//			BooleanSetting thirdPersonHamonAura = new BooleanSetting(settings, 
//					Component.translatable("jojo_ripples.config.client.thirdPersonHamonAura"), 
//					Component.translatable("jojo_ripples.config.client.thirdPersonHamonAura.tooltip")
//					) {
//				@Override public Boolean get() { return settingsValues.thirdPersonHamonAura; }
//				@Override public void set(Boolean value) { 
//					settingsValues.thirdPersonHamonAura = value;
//				}
//			};
//			addRenderableWidget(thirdPersonHamonAura.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
//
//			BooleanSetting firstPersonHamonAura = new BooleanSetting(settings, 
//					Component.translatable("jojo_ripples.config.client.firstPersonHamonAura"), 
//					Component.translatable("jojo_ripples.config.client.firstPersonHamonAura.tooltip")
//					) {
//				@Override public Boolean get() { return settingsValues.firstPersonHamonAura; }
//				@Override public void set(Boolean value) { 
//					settingsValues.firstPersonHamonAura = value;
//				}
//			};
//			addRenderableWidget(firstPersonHamonAura.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
//
//			BooleanSetting hamonAuraBlur = new BooleanSetting(settings, 
//					Component.translatable("jojo_ripples.config.client.hamonAuraBlur"), 
//					Component.translatable("jojo_ripples.config.client.hamonAuraBlur.tooltip")
//					) {
//				@Override public Boolean get() { return settingsValues.hamonAuraBlur; }
//				@Override public void set(Boolean value) { 
//					settingsValues.hamonAuraBlur = value;
//				}
//			};
//			addRenderableWidget(hamonAuraBlur.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
//
//			addBackButton(CommonComponents.GUI_BACK, i);
//		}
//
//	}
//
//	@Deprecated
//	public static class VampirismSettings extends ClientModSettingsScreen {
//
//		public VampirismSettings(Screen lastScreen, ClientModSettings settings, Component title) {
//			super(lastScreen, settings, title);
//		}
//
//		@Override
//		protected void addRenderableWidgets() {
//			int i = 0;
//
////			BooleanSetting glowingEyes = new BooleanSetting(settings, 
////					Component.translatable("jojo_ripples.config.client.vampireGlowingEyes"), 
////					Component.translatable("jojo_ripples.config.client.vampireGlowingEyes.tooltip")
////					) {
////				@Override public Boolean get() { return settingsValues.broadcasted.vampireGlowingEyes; }
////				@Override public void set(Boolean value) { 
////					settingsValues.broadcasted.vampireGlowingEyes = value;
////				}
////			};
////			addRenderableWidget(glowingEyes.createButton(calcButtonX(i), calcButtonY(i++), 150, 20, this, i));
//
//			addBackButton(CommonComponents.GUI_BACK, i);
//		}
//
//	}



	protected int calcButtonX(int i) {
		return this.width / 2 - 155 + i % 2 * 160;
	}

	protected int calcButtonY(int i) {
		return this.height / 6 - 12 + 24 * (i >> 1);
	}

	@Override
	public void removed() {
		settings.save();
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
		guiGraphics.drawCenteredString(font, title, width / 2, 15, 0xFFFFFF);
		for (var categoryTitle : categories) {
			guiGraphics.drawCenteredString(font, categoryTitle.title, width / 2, categoryTitle.y, 0xC0C0C0);
		}
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
	}



	protected static Object2CharMap<ResourceLocation> iconSymbols = new Object2CharArrayMap<>();
	
	public static ResourceLocation toIconPath(String fileName) {
		return JojoMod.resLoc("textures/gui/sprites/settings/" + fileName + ".png");
	}
	
	public static abstract class Setting<T> implements SettingsField<T>{
		protected ClientModSettings settings;
		protected Component name;
		protected Component nameWithSprite;
		protected Component tooltip;
		@Nullable protected ResourceLocation sprite;
		protected char spriteCode;
		
		protected boolean broadcast = false;
		
		public Setting(ClientModSettings settings, Component name, @Nullable Component tooltip) {
			this.settings = settings;
			this.name = name;
			this.tooltip = tooltip;
			this.nameWithSprite = name;
		}
		
		public Setting<T> withIcon(ResourceLocation sprite) {
			return withIcon(sprite, iconPath -> new IconGlyphInfo(new GuiIcon(iconPath, 16, 16), 16, 16, 0, -4, 4));
		}
		
		public Setting<T> withIcon(ResourceLocation sprite, Function<ResourceLocation, IconGlyphInfo> createGlyph) {
			this.sprite = sprite;
			if (sprite != null) {
				spriteCode = iconSymbols.computeIfAbsent(sprite, (ResourceLocation iconPath) -> 
						IconGlyphsCache.makeCharCodeFor(createGlyph.apply(iconPath)));
				this.nameWithSprite = Component.literal(String.valueOf(spriteCode)).append(name);
			}
			return this;
		}

		public Setting<T> setBroadcasted() {
			this.broadcast = true;
			return this;
		}
		
		protected Component nameWithSprite() {
			return sprite != null && IconSymbols.spriteExists(spriteCode) ? nameWithSprite : name;
		}

		public abstract Button createButton(int x, int y, int width, int height, Screen screen, int buttonI);
	}

	public static abstract class BooleanSetting extends Setting<Boolean> {

		public BooleanSetting(ClientModSettings settings, Component name, @Nullable Component tooltip) {
			super(settings, name, tooltip);
		}

		@Override
		public Button createButton(int x, int y, int width, int height, Screen screen, int buttonI) {
			return new ScrollingStringButton(
					x, y, width, height,
					CommonComponents.optionStatus(nameWithSprite(), get()), 
					button -> {
						settings.editSettings(s -> {
							set(!get());
							button.setMessage(CommonComponents.optionStatus(nameWithSprite(), get()));
						}, broadcast);
					},
					Tooltip.create(tooltip))
					.setAlignment(buttonI % 2 == 0 ? Alignment.LEFT : Alignment.RIGHT);
		}
	}

	public static abstract class EnumSetting<T extends Enum<T>> extends Setting<T> {
		protected Class<T> enumClass;
		protected String prefix = "jojo_ripples.config.client.option.";

		public EnumSetting(ClientModSettings settings, Component name, @Nullable Component tooltip, Class<T> enumClass) {
			super(settings, name, tooltip);
			this.enumClass = enumClass;
		}

		public EnumSetting<T> prefix(String prefix) {
			this.prefix += prefix;
			return this;
		}

		@Override
		public Button createButton(int x, int y, int width, int height, Screen screen, int buttonI) {
			return new ScrollingStringButton(
					x, y, width, height,
					Component.translatable("options.generic_value", nameWithSprite(), getValueMessage(get())), 
					button -> {
						settings.editSettings(s -> {
							T[] values = enumClass.getEnumConstants();
							T val = get();
							T nextVal = values[(val.ordinal() + 1) % values.length];
							set(nextVal);
							button.setMessage(Component.translatable("options.generic_value", nameWithSprite(), getValueMessage(nextVal)));
						}, broadcast);
					},
					Tooltip.create(tooltip))
					.setAlignment(buttonI % 2 == 0 ? Alignment.LEFT : Alignment.RIGHT);
		}

		private Component getValueMessage(T value) {
			return Component.translatable(prefix + value.name().toLowerCase());
		}
    }
    
    
    
	// yes, the modern versions have scrolling text too, but this one renders full text when the button is hovered
    private static class ScrollingStringButton extends Button {
        private Alignment alignment = Alignment.LEFT;
        
        public ScrollingStringButton(int pX, int pY, int pWidth, int pHeight, Component pMessage,
        		Button.OnPress pOnPress) {
            super(new Button.Builder(pMessage, pOnPress).bounds(pX, pY, pWidth, pHeight));
        }
        
        public ScrollingStringButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, 
        		Button.OnPress pOnPress, Tooltip pOnTooltip) {
            super(new Button.Builder(pMessage, pOnPress).bounds(pX, pY, pWidth, pHeight).tooltip(pOnTooltip));
        }
        
        public ScrollingStringButton setAlignment(Alignment alignment) {
            this.alignment = alignment;
            return this;
        }

        @Override
        protected void renderScrollingString(GuiGraphics guiGraphics, Font font, int width, int color) {
        	int x0 = this.getX() + width;
        	int x1 = this.getX() + this.getWidth() - width;
        	int y0 = this.getY();
        	int y1 = this.getY() + this.getHeight();

        	Component text = getMessage();
            int textWidth = font.width(text);
            int y = (y0 + y1 - 9) / 2 + 1;
            int buttonWidth = x1 - x0;
            if (textWidth > buttonWidth && isHovered()) {
                switch (alignment) {
                case LEFT:
                    guiGraphics.drawString(font, text, x0, y, color);
                    break;
                case RIGHT:
                    guiGraphics.drawString(font, text, x1 - textWidth, y, color);
                    break;
                }
            }
            else {
            	_renderScrollingString(guiGraphics, font, text, (x0 + x1) / 2, x0, y0, x1, y1, color, categoryOpenedTimestampSoThatScrollingDoesntSuck);
            }
        }

        public static void _renderScrollingString(GuiGraphics guiGraphics, Font font, 
        		Component text, int centerX, int minX, int minY, int maxX, int maxY, int color, 
        		long startingTime) {
        	int i = font.width(text);
        	int j = (minY + maxY - 9) / 2 + 1;
        	int k = maxX - minX;
        	if (i > k) {
        		int l = i - k;
        		double d0 = (double)(Util.getMillis() - startingTime) / 1000.0;
        		double d1 = Math.max((double)l * 0.5, 3.0);
        		double d2 = 1 - (Math.sin((Math.PI / 2) * Math.cos((Math.PI * 2) * d0 / d1)) / 2.0 + 0.5);
        		double d3 = Mth.lerp(d2, 0.0, (double)l);
        		GuiScissor.enableScissor(guiGraphics, minX, minY, maxX, maxY);
        		guiGraphics.drawString(font, text, minX - (int)d3, j, color);
        		guiGraphics.disableScissor();
        	} else {
        		int i1 = Mth.clamp(centerX, minX + i / 2, maxX - i / 2);
        		guiGraphics.drawCenteredString(font, text, i1, j, color);
        	}
        }
    }
    static long categoryOpenedTimestampSoThatScrollingDoesntSuck;
    
    
    


	@SubscribeEvent(priority = EventPriority.LOW)
	public static void addToScreen(ScreenEvent.Init.Post event) {
		Screen screen = event.getScreen();
		if (screen instanceof OptionsScreen optionsScreen) {
			List<AbstractWidget> otherModdedButtons = event.getListenersList().stream()
					.filter(b -> b instanceof AbstractWidget)
					.map(b -> (AbstractWidget) b)
					.toList();
			
			Component tooltip = Component.translatable("jojo_ripples.options.client.title");
			ItemButton settingsButton = new ItemButton(-1, -1, 20, 20, 
					ItemIconModels.makeIconItem(ItemIconModels.MOD_LOGO),
					__ -> optionsScreen.getMinecraft().setScreen(new ClientModSettingsScreen(optionsScreen, ClientModSettings.getInstance())),
					Tooltip.create(tooltip),
					tooltip);
			Layout fuckTheseAbstractions = new ButtonInLayout(settingsButton, button -> {
				int[] buttonPos = findPosForButton(optionsScreen, otherModdedButtons, button);
				button.setX(buttonPos[0]);
				button.setY(buttonPos[1]);
			});
			ClientReflection.getLayout(optionsScreen).addToContents(fuckTheseAbstractions);
			fuckTheseAbstractions.arrangeElements();
			event.addListener(settingsButton);
		}
	}
    
    private static int[] findPosForButton(Screen optionsScreen, List<AbstractWidget> otherModdedButtons, AbstractWidget except) {
		final int minY = 87;
		final int maxY = minY + 100;

		final int minX1 = 0;
		final int maxX1 = optionsScreen.width / 2 - 155 - 20 - 5;
		final int minX2 = optionsScreen.width / 2 + 160;
		final int maxX2 = optionsScreen.width - 20;

		final int minX3 = optionsScreen.width / 2 - 155;
		final int maxX3 = minX3 + 290;
		final int y3 = maxY + 24;

		int[] buttonPos = null;

		// try placing the button to the right side
		for (int x = minX2; x <= maxX2 && buttonPos == null; x += 25) {
			int y = maxY;
			if (ModList.get().isLoaded("essential")) y -= 24; // for fuck's sake
			for (; y >= minY && buttonPos == null; y -= 24) {
				buttonPos = noOverlapPos(otherModdedButtons, except, x, y);
			}
		}
		// ...or to the left side
		if (buttonPos == null) {
			for (int x = maxX1; x >= minX1 && buttonPos == null; x -= 25) {
				for (int y = maxY; y >= minY && buttonPos == null; y -= 24) {
					buttonPos = noOverlapPos(otherModdedButtons, except, x, y);
				}
			}
		}
		// ...or below the vanilla options
		if (buttonPos == null) {
			for (int x = minX3; x <= maxX3 && buttonPos == null; x += 29) {
				buttonPos = noOverlapPos(otherModdedButtons, except, x, y3);
			}
		}
		// ...how many new buttons are there?? fuck it, just put it at the "Done" button
		if (buttonPos == null) {
			buttonPos = new int[] { optionsScreen.width / 2 + 110, optionsScreen.height - 26 };
		}
		
		return buttonPos;
    }

	@Nullable
	private static int[] noOverlapPos(List<AbstractWidget> buttonsList, AbstractWidget except, int x, int y) {
		int x2 = x + 20;
		int y2 = y + 20;
		return buttonsList.stream().filter(button -> button != except).anyMatch(button -> {
			return button.getX() < x2 && button.getX() + button.getWidth() > x && button.getY() < y2 && button.getY() + button.getHeight() > y;
		}) ? null : new int[] { x, y };
	}

}
