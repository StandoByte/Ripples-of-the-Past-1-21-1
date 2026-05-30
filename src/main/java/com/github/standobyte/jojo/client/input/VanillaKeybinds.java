package com.github.standobyte.jojo.client.input;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;

import org.apache.commons.lang3.mutable.MutableInt;
import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.ui.screen_jojomenu.IJojoMenuScreen;
import com.github.standobyte.jojo.client.ui.screen_jojomenu.JojoMenuTabs;
import com.github.standobyte.jojo.client.ui.screen_jojomenu.Tab;
import com.github.standobyte.jojo.config.core.types.ConfigBool;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.network.c2s.ClNoParamsPacket;
import com.github.standobyte.jojo.network.c2s.ClNoParamsPacket.PacketType;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPower;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.client_screens.StandTogglesScreen;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.network.PacketDistributor;

public class VanillaKeybinds {
	public static final String MAIN_CATEGORY = "key.categories." + JojoMod.MOD_ID;
	public KeyMapping summonStand;
	public KeyMapping standArmsOnlyHUD;
	public KeyMapping playerPowerHUD;
	public KeyMapping useAbility;
	public KeyMapping switchSpecial;
	public KeyMapping disableHUDControls;
	public KeyMapping jojoStuffMenu;

	public KeyMapping standToggle_breakBlocks;
	
	public static class KeyInGameCtx implements IKeyConflictContext {
		public BooleanSupplier extraCondition;

		public KeyInGameCtx(BooleanSupplier extraCondition) {
			this.extraCondition = extraCondition;
		}
		
		@Override
		public boolean isActive() {
			return KeyConflictContext.IN_GAME.isActive() && extraCondition.getAsBoolean();
		}

		@Override
		public boolean conflicts(IKeyConflictContext other) {
			return KeyConflictContext.IN_GAME.conflicts(other);
		}
	}
	
	public static VanillaKeybinds register(RegisterKeyMappingsEvent event) {
		VanillaKeybinds binds = new VanillaKeybinds();
		
		event.register(binds.summonStand = new Jokerge(
				JojoMod.MOD_ID + ".key.toggle_stand", 
				new KeyInGameCtx(() -> {
					StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
					return standPower != null && standPower.hasPower();
				}), 
				InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, MAIN_CATEGORY)
				.inInitOrder().withDescTooltip());
		
		event.register(binds.standArmsOnlyHUD = new Jokerge(
				JojoMod.MOD_ID + ".key.stand_mode", 
				new KeyInGameCtx(() -> {
					StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
					return standPower != null && standPower.hasPower() && !standPower.isSummoned();
				}), 
				KeyModifier.CONTROL, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, MAIN_CATEGORY)
				.inInitOrder().withDescTooltip());
		
		event.register(binds.playerPowerHUD = new Jokerge(
				JojoMod.MOD_ID + ".key.non_stand_mode", 
				new KeyInGameCtx(() -> {
					PlayerPower playerPower = ClientPowerCache.getPower(PowerClass.PLAYER_POWER);
					return playerPower != null && playerPower.hasPower();
				}), 
				InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, MAIN_CATEGORY)
				.inInitOrder().withDescTooltip());
		
		event.register(binds.useAbility = new Jokerge(
				JojoMod.MOD_ID + ".key.use_special_ability", 
				KeyConflictContext.IN_GAME, 
				InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_X, MAIN_CATEGORY)
				.inInitOrder().withDescTooltip());
		
		event.register(binds.switchSpecial = new Jokerge(
				JojoMod.MOD_ID + ".key.ability_hotbar", 
				KeyConflictContext.IN_GAME, 
				InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, MAIN_CATEGORY)
				.inInitOrder().withDescTooltip());
		
		event.register(binds.disableHUDControls = new Jokerge(
				JojoMod.MOD_ID + ".key.disable_hotbars", 
				KeyConflictContext.IN_GAME, 
				InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, MAIN_CATEGORY)
				.inInitOrder().withDescTooltip().canBeHoldOrToggle(JojoMod.config.getClient().toggleDisableHotbars));
		
		event.register(binds.jojoStuffMenu = new Jokerge(
				JojoMod.MOD_ID + ".key.jojo_menu", 
				KeyConflictContext.IN_GAME, 
				InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_BACKSLASH, MAIN_CATEGORY)
				.inInitOrder().withDescTooltip());
		
		// not registering it in the event to not add it to the settings menu
		binds.standToggle_breakBlocks = new Jokerge(
				JojoMod.MOD_ID + ".key.stand_toggle_break_blocks", 
				new KeyInGameCtx(() -> {
					StandTogglesScreen.lazyInitToggles();
					return StandTogglesScreen.breakBlocks.activeWhen.getAsBoolean();
				}), 
				InputConstants.UNKNOWN, MAIN_CATEGORY)
				.inInitOrder().withDescTooltip();
		
		return binds;
	}
	
	public void setFromConfig() {
		var config = JojoMod.config.getClient();
		config.toggleKeybind_standsBreakBlocks.resolveKeybind();
	}


	public void handleTick() {
		Minecraft mc = Minecraft.getInstance();
		InputHandler inputHandler = InputHandler.getInstance();
		
		if (standArmsOnlyHUD.consumeClick()) {
			inputHandler.curPowerClassToggle = inputHandler.curPowerClassToggle != PowerClass.STAND ? PowerClass.STAND : null;
		}
		
		if (playerPowerHUD.consumeClick()) {
			inputHandler.curPowerClassToggle = inputHandler.curPowerClassToggle != PowerClass.PLAYER_POWER ? PowerClass.PLAYER_POWER : null;
		}
		
		if (summonStand.consumeClick()) {
			StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
			StandType standType = standPower.getPowerType();
			if (standType != null) {
				if (standType.hasSummonMechanic) {
					PacketDistributor.sendToServer(ClNoParamsPacket.of(PacketType.SUMMON_STAND));
				}
				else {
					inputHandler.curPowerClassToggle = inputHandler.curPowerClassToggle != PowerClass.STAND ? PowerClass.STAND : null;
				}
			}
		}
		
		if (JojoMod.config.getClient().toggleDisableHotbars.getAsBoolean() && disableHUDControls.consumeClick()) {
			InputHandler.inputsDisabled = !InputHandler.inputsDisabled;
		}
		
		if (jojoStuffMenu.consumeClick()) {
			if (mc.screen instanceof IJojoMenuScreen) {
				mc.popGuiLayer();
			}
			else {
				Tab tab = JojoMenuTabs.getTabToOpenOnMenuKey();
				if (tab != null) {
					tab.onClick(mc, mc.screen);
				}
			}
		}
		
		
		if (mc.level != null) {
			StandTogglesScreen.ToggleEntry[] toggles = StandTogglesScreen.lazyInitToggles();
			for (StandTogglesScreen.ToggleEntry toggle : toggles) {
				if (toggle.keybind != null) {
					KeyMapping keybind = toggle.keybind.apply(this);
					if (keybind != null && keybind.consumeClick()) {
						if (toggle.clientCanToggle()) {
							StandTogglesScreen.breakBlocks.toggle();
						}
						else {
							mc.gui.setOverlayMessage(Component.translatable("jojo_ripples.toggle_overruled", 
									toggle.overrulingCommonSetting.get().optionStatus), false);
						}
					}
				}
			}
		}
	}
	
	
	
	public static final Set<String> ADD_DESC_TOOLTIP = new HashSet<>();
	public static final Map<String, ConfigBool> HOLD_OR_TOGGLE = new HashMap<>();
	
	public static class Jokerge extends KeyMapping {
		protected static Map<String, MutableInt> PER_CATEGORY = new HashMap<String, MutableInt>();
		protected int orderIndex = Integer.MAX_VALUE;

		public Jokerge(String name, int keyCode, String category) {	super(name, keyCode, category); }
		public Jokerge(String name, InputConstants.Type type, int keyCode, String category) { super(name, type, keyCode, category); }
		public Jokerge(String description, IKeyConflictContext keyConflictContext, InputConstants.Type inputType, int keyCode, String category) { super(description, keyConflictContext, inputType, keyCode, category); }
		public Jokerge(String description, IKeyConflictContext keyConflictContext, InputConstants.Key keyCode, String category) { super(description, keyConflictContext, keyCode, category); }
		public Jokerge(String description, IKeyConflictContext keyConflictContext, KeyModifier keyModifier, InputConstants.Type inputType, int keyCode, String category) { super(description, keyConflictContext, keyModifier, inputType, keyCode, category); }
		public Jokerge(String description, IKeyConflictContext keyConflictContext, KeyModifier keyModifier, InputConstants.Key keyCode, String category) { super(description, keyConflictContext, keyModifier, keyCode, category); }
		
		public Jokerge inInitOrder() {
			this.orderIndex = PER_CATEGORY.computeIfAbsent(this.getCategory(), __ -> new MutableInt()).getAndIncrement();
			return this;
		}
		
		public Jokerge withDescTooltip() {
			ADD_DESC_TOOLTIP.add(this.getName());
			return this;
		}
		
		public Jokerge canBeHoldOrToggle(ConfigBool clientSetting) {
			HOLD_OR_TOGGLE.put(this.getName(), clientSetting);
			return this;
		}

		@Override
		public int compareTo(KeyMapping other) {
			if (this.getCategory() == other.getCategory() && other instanceof Jokerge jokerge) {
				int compare = Integer.compare(this.orderIndex, jokerge.orderIndex);
				if (compare != 0) return compare;
			}
			return super.compareTo(other);
		}
	}
	
}
