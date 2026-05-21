package com.github.standobyte.jojo.client.input;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Queue;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableInt;
import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.ClientTickHandler;
import com.github.standobyte.jojo.client.input.clickhold.AmbiguousKeyPress;
import com.github.standobyte.jojo.client.input.controlscheme.AllControlSchemes;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme.Hotbar;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme.HotbarSlot;
import com.github.standobyte.jojo.client.input.controlscheme.ClientKey;
import com.github.standobyte.jojo.client.ui.AbilitySelectionWheel;
import com.github.standobyte.jojo.client.ui.hud_power.PowerHud;
import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.event.client.PreKeyInputEvent;
import com.github.standobyte.jojo.mechanics.resolve.ClActivateResolvePacket;
import com.github.standobyte.jojo.network.c2s.ClAbilityInputPacket;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities.AbilityConditionCheck;
import com.github.standobyte.jojo.powersystem.ability.condition.ConditionCheck;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.ability.input.AbilityInput;
import com.github.standobyte.jojo.powersystem.ability.input.AbilityInput.InputEventType;
import com.github.standobyte.jojo.powersystem.ability.input.ActionInputBuffer.BufferingState;
import com.github.standobyte.jojo.powersystem.entityaction.EntityActionInstance;
import com.github.standobyte.jojo.powersystem.entityaction.LivingComponentAction;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.github.standobyte.jojo.util.enums.Direction2D;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.InputEvent.InteractionKeyMappingTriggered;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

public class InputHandler {
	private static InputHandler instance;
	private final Minecraft mc = Minecraft.getInstance();
	
	public static final ClientKey LMB = ClientKey.make(InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_LEFT);
	public static final ClientKey RMB = ClientKey.make(InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_RIGHT);
	public static final ClientKey MMB = ClientKey.make(InputConstants.Type.MOUSE, InputConstants.MOUSE_BUTTON_MIDDLE);
	
	public static void init(RegisterKeyMappingsEvent event) {
		if (instance == null) {
			instance = new InputHandler();
			instance.registerBindings(event);
			NeoForge.EVENT_BUS.register(instance);
		}
	}
	
	public static InputHandler getInstance() {
		return instance;
	}
	
	public VanillaKeybinds vanillaKeybinds;
	
	private void registerBindings(RegisterKeyMappingsEvent event) {
		this.vanillaKeybinds = VanillaKeybinds.register(event);
	}
	
	public static boolean inputsDisabled;
	
	
	@SubscribeEvent
	public void handleKeyBindingsPost(ClientTickEvent.Post event) {
		vanillaKeybinds.handleTick();
		tickReleaseEventQueue();
		tickKeyPressIndication();
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onFrameUpdate(RenderFrameEvent.Pre event) {
		if (!JojoMod.config.getClient().toggleDisableHotbars.getAsBoolean()) {
			inputsDisabled = _heldKeys.containsKey(ClientKey.fromVanillaKeybind(vanillaKeybinds.disableHUDControls));
		}
		
		float tickDelta = mc.getTimer()/*getDeltaTracker()*/.getRealtimeDeltaTicks();
		frameUpdateHeldKeys(tickDelta);
	}



	@SubscribeEvent(priority = EventPriority.LOW)
	public void handleKeyInput(PreKeyInputEvent event) {
		if (mc.getConnection() == null) return;

		InputConstants.Type keyType;
		int keyCode;
		if (event.getKey() == -1) {
			keyType = InputConstants.Type.SCANCODE;
			keyCode = event.getScanCode();
		}
		else {
			keyType = InputConstants.Type.KEYSYM;
			keyCode = event.getKey();
		}
		handleInputEvent(ClientKey.make(keyType, keyCode), event.getAction(), event.getModifiers(), event);
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public void handleMouseInput(InputEvent.MouseButton.Pre event) {
		if (mc.getConnection() == null) return;
		
		handleInputEvent(ClientKey.make(InputConstants.Type.MOUSE, event.getButton()), event.getAction(), event.getModifiers(), event);
	}
	
	public void handleInputEvent(ClientKey key, int action, int modifiers, ICancellableEvent event) {
		if (input(key, action, modifiers)) {
			event.setCanceled(true);
		}
		
		Key vanillaKey = key.getVanillaKey();
		if (vanillaKey != null) {
			switch (action) {
				case InputConstants.PRESS -> addKeyModifier(vanillaKey);
				case InputConstants.RELEASE -> removeKeyModifier(vanillaKey);
			}
		}
	}
	
	
	protected boolean shouldQueueInput() {
		return !(mc.screen == null || PowerHud.isInContainerScreen() || mc.screen instanceof AbilitySelectionWheel);
	}
	
	protected void tickReleaseEventQueue() {
		if (!keyReleaseEventQueue.isEmpty() && mc.getConnection() != null && !shouldQueueInput()) {
			for (DelayedInput keyRelease : keyReleaseEventQueue) {
				input(keyRelease.key, keyRelease.action, keyRelease.modifiers);
			}
			keyReleaseEventQueue.clear();
		}
	}
	
	public static record DelayedInput(ClientKey key, int action, int modifiers) {}
	
	private Queue<DelayedInput> keyReleaseEventQueue = new ArrayDeque<>();

	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void handleMouseScroll1(InputEvent.MouseScrollingEvent event) {
		if (mc.getConnection() == null) return;
		if (hotbarScroll(event.getScrollDeltaY())) {
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void handleMouseScrollWithWheelOpened(ScreenEvent.MouseScrolled.Pre event) {
		if (mc.getConnection() == null) return;
		if (hotbarScroll(event.getScrollDeltaY())) {
			event.setCanceled(true);
		}
	}
	
	
	public PowerClass<?> curPowerClassToggle = null;
	
	public ClientControlScheme getActiveControlScheme() {
		Power<?> curPower = null;
		
		if (mc.player != null) {
			StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
			if (standPower != null && standPower.hasPower() && standPower.isSummoned()) {
				curPower = standPower;
			}
			else if (curPowerClassToggle != null) {
				Power<?> power = ClientPowerCache.getPower(curPowerClassToggle);
				if (power != null && power.hasPower()) {
					curPower = power;
				}
			}
		}
		if (curPower != null) {
			return AllControlSchemes.getForPowerType(curPower.getPowerType());
		}
		return null;
	}
	
	/**
	 * Handles the direct events of keyboard/mouse inputs to trigger abilities from the player's moveset.
	 * @return true if the vanilla input should be cancelled.
	 */
	public boolean input(ClientKey key, int inputType, int modifiers) {
		boolean cancelVanilla = false;
		short keyId = key.keyId();
		
		if (shouldQueueInput()) {
			if (inputType == InputConstants.RELEASE) {
				keyReleaseEventQueue.add(new DelayedInput(key, inputType, modifiers));
			}
			return false;
		}
		
		switch (inputType) {
			case InputConstants.PRESS -> {
				ClientControlScheme controlScheme = getActiveControlScheme();
				if (controlScheme == null) return false;
				
				boolean secondKeyInDualPress = checkDualPress(key);
				if (secondKeyInDualPress) {
					cancelVanilla = true;
					return true;
				}
				
				StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
				boolean canEnterResolveMode = (key == LMB || key == RMB) 
						&& standPower != null 
						&& PowerHud.abilityHUDInstance.resolveBar.shouldRender() 
						&& standPower.resolveCounter.getCurStage() >= 0;
				
				cancelVanilla |= hotbarPickSlot(key);
				
				KeyModifier keyModifier = getCurModifier();
				
				CurInput input = getInputAbilitiesOnClick(controlScheme, key, keyModifier);
				@Nullable BaseAndActiveAbility heldAbility = input.heldAbility.curActiveAbility != null ? input.heldAbility : null;
				@Nullable BaseAndActiveAbility clickAbility = input.clickAbility.curActiveAbility != null ? input.clickAbility : null;
				
				cancelVanilla |= heldAbility != null || clickAbility != null;
				HeldKeyTimer heldKeyTimer = new HeldKeyTimer(key, cancelVanilla, keyModifier);
				
				int ambiguity = 0;
				if (canEnterResolveMode) ambiguity++;
				if (heldAbility != null) ambiguity++;
				if (clickAbility != null) ambiguity++;
				
				AmbiguousKeyPress ambiguousKeyPress = null;
				if (ambiguity >= 2) {
					ambiguousKeyPress = new AmbiguousKeyPress();
					
					if (canEnterResolveMode) {
						ambiguousKeyPress.onDualKeyClick = (ClientKey secondKeyPressed, float timeTook) -> {
							if (key == LMB && secondKeyPressed == RMB || key == RMB && secondKeyPressed == LMB) {
								PacketDistributor.sendToServer(new ClActivateResolvePacket(true));
								return true;
							}
							return false;
						};
					}

					if (heldAbility != null) {
						Ability heldBaseAbility = heldAbility.baseAbility;
						ambiguousKeyPress.onHold = (float ticksToResolveHeld) -> {
							AvailableAbilities curAbilities = ClientPowerCache.getAvailableAbilities(heldBaseAbility.abilityId.powerClass());
							AbilityConditionCheck abilityResolved = curAbilities.getContextVariationContainer(heldBaseAbility);
							doClickInput(InputEventType.PRESS_HOLD, keyId, heldBaseAbility, abilityResolved, ticksToResolveHeld);
						};
					}
					
					if (clickAbility != null) {
						Ability clickBaseAbility = clickAbility.baseAbility;
						ambiguousKeyPress.onClick = (float ticksToResolveClick) -> {
							AvailableAbilities curAbilities = ClientPowerCache.getAvailableAbilities(clickBaseAbility.abilityId.powerClass());
							AbilityConditionCheck abilityResolved = curAbilities.getContextVariationContainer(clickBaseAbility);
							doClickInput(InputEventType.PRESS_CLICK, keyId, clickBaseAbility, abilityResolved, ticksToResolveClick);
						};
					}
				}
				
				if (ambiguousKeyPress != null) {
					heldKeyTimer.setAmbiguousInputMethod(ambiguousKeyPress);
				}
				else {
					InputMethod inputMethod = 
							heldAbility != null ? InputMethod.HOLD : 
							clickAbility != null ? InputMethod.CLICK : 
							null;
					if (inputMethod != null) {
						switch (inputMethod) {
							case HOLD -> doClickInput(InputEventType.PRESS_HOLD, keyId, heldAbility.baseAbility, heldAbility.curActiveAbility, 0);
							case CLICK -> doClickInput(InputEventType.PRESS_CLICK, keyId, clickAbility.baseAbility, input.clickAbility.curActiveAbility, 0);
						}
					}
				}
				
				putHeldKeyTimer(key, heldKeyTimer);
				
				if (heldAbility == null && clickAbility == null && mc.screen == null) {
					checkStartHotbarSelection(key);
				}
			}
			case InputConstants.RELEASE -> {
				HeldKeyTimer heldTicks = getHeldKeyTimer(key);
				if (heldTicks != null) {
					clickHeldOnRelease(heldTicks, keyId);
					doReleaseInput(keyId);
					removeHeldKeyTimer(key);
				}

				checkStopHotbarSelection(key);
			}
			case InputConstants.REPEAT -> {
				HeldKeyTimer heldKey = getHeldKeyTimer(key);
				cancelVanilla |= heldKey != null && heldKey.cancelVanilla;
			}
		}
		return cancelVanilla;
	}
	
	private FriendlyByteBuf extraInputBuf = new FriendlyByteBuf(Unpooled.buffer());
	private void doClickInput(InputEventType type, short keyId, 
			Ability baseAbility, AbilityConditionCheck abilityResolved, 
			float clickHoldResolveTime) {
		Player player = mc.player;
		if (abilityResolved == null || player == null) return;

		ConditionCheck conditionCheck = abilityResolved.conditionCheck;
		if (conditionCheck.positive()) {
			BufferingState bufferingState = BufferingState.clickCanBuffer();
			InputMethod inputMethod = type.inputMethod;
			Ability ability = abilityResolved.ability;
			ability.writeExtraInput(extraInputBuf, player, true);
			AbilityInput.keyPress(keyId, ability, player, extraInputBuf, 
					inputMethod, clickHoldResolveTime, bufferingState, baseAbility.abilityId);
			extraInputBuf.clear();
		}
		PacketDistributor.sendToServer(ClAbilityInputPacket.keyPress(keyId, player, baseAbility, type, clickHoldResolveTime));
	}
	
	private void doReleaseInput(short keyId) {
		Player player = mc.player;
		AbilityInput.keyRelease(keyId, player);
		PacketDistributor.sendToServer(ClAbilityInputPacket.releaseHold(keyId));
	}
	
	
	// Held keys stuff
	
	public Map<ClientKey, HeldKeyTimer> _heldKeys = new HashMap<>();
	public Map<ClientKey, MutableInt> _recentlyPressed = new HashMap<>();
	
	public HeldKeyTimer getHeldKeyTimer(ClientKey key) {
		return _heldKeys.get(key);
	}
	
	public void putHeldKeyTimer(ClientKey key, HeldKeyTimer timer) {
		_heldKeys.put(key, timer);
	}
	
	public HeldKeyTimer removeHeldKeyTimer(ClientKey key) {
		HeldKeyTimer timer = _heldKeys.remove(key);
		return timer;
	}
	
	
	public void onResolvedKeyAsClick(ClientKey key) {
		_recentlyPressed.computeIfAbsent(key, __ -> new MutableInt(0)).setValue(3);
	}
	
	public boolean wasKeyClickedRecently(ClientKey key) {
		MutableInt timer = _recentlyPressed.get(key);
		return timer != null && timer.intValue() >= 0;
	}
	
	protected void tickKeyPressIndication() {
		for (MutableInt timer : _recentlyPressed.values()) {
			if (timer.intValue() >= 0) {
				timer.decrement();
			}
		}
	}
	
	
	public boolean isHeld(ClientKey key, @Nullable KeyModifier modifier) {
		HeldKeyTimer timer = getHeldKeyTimer(key);
		if (timer != null) {
			return modifier == null || timer.modifier == modifier;
		}
		return false;
	}
	
	public boolean isKeyHeld(int keyCode) {
		return isHeld(ClientKey.make(InputConstants.Type.KEYSYM, keyCode), null);
	}
	
	private void clickHeldOnRelease(HeldKeyTimer heldKeyTimer, short keyId) {
		AmbiguousKeyPress inputResolution = heldKeyTimer.getAmbiguousInputMethod();
		if (inputResolution != null) {
			AmbiguousKeyPress.Result wasItClick = inputResolution.keyReleased();
			if (wasItClick != null && wasItClick.input() == AmbiguousKeyPress.InputState.CLICK) {
				if (inputResolution.onClick != null) {
					inputResolution.onClick.handleInput(wasItClick.timeTook());
				}
				heldKeyTimer.setAmbiguousInputMethod(null);
				onResolvedKeyAsClick(heldKeyTimer.key);
			}
		}
	}
	
	private void frameUpdateHeldKeys(float tickDelta) {
		for (HeldKeyTimer timer : _heldKeys.values()) {
			AmbiguousKeyPress.Result changedState = timer.frameUpdate(tickDelta);
			if (changedState != null) {
				AmbiguousKeyPress inputResolution = timer.getAmbiguousInputMethod();
				switch (changedState.input()) {
					case ASSUME_HOLD -> {}
					case HOLD -> {
						if (inputResolution.onHold != null) {
							inputResolution.onHold.handleInput(changedState.timeTook());
						}
						timer.setAmbiguousInputMethod(null);
					}
					default -> {}
				}
			}
		}
	}
	
	private boolean checkDualPress(ClientKey pressedKey) {
		boolean result = false;
		var iter = _heldKeys.entrySet().iterator();
		while (iter.hasNext()) {
			var heldKeyEntry = iter.next();
			HeldKeyTimer timer = heldKeyEntry.getValue();
			if (!timer.isDefinitelyHold() && timer.ambiguousInputMethod.onDualKeyClick != null
					&& timer.ambiguousInputMethod.onDualKeyClick.checkHandleInput(pressedKey, timer.timeHeld)) {
				result = true;
				iter.remove();
			}
		}
		return result;
	}
	
	
	// Key modifiers (Ctrl/Shift/Alt)
	
	private List<KeyModifier> modifiersQueue = new ArrayList<>();
	
	private void addKeyModifier(Key key) {
		KeyModifier modifier = KeyModifier.getKeyModifier(key);
		if (modifier != KeyModifier.NONE && !modifiersQueue.contains(modifier)) {
			modifiersQueue.add(modifier);
		}
	}
	
	private void removeKeyModifier(Key key) {
		KeyModifier modifier = KeyModifier.getKeyModifier(key);
		if (modifier != KeyModifier.NONE) {
			modifiersQueue.remove(modifier);
		}
	}
	
	@Nonnull
	public KeyModifier getCurModifier() {
		return !modifiersQueue.isEmpty() ? modifiersQueue.get(modifiersQueue.size() - 1) : KeyModifier.NONE;
	}
	
	
	// The function that figures out what ability has the player inputed.

	static Predicate<AbilityInputState> filter = (AbilityInputState inputState)
			-> AbilityInputState.isInputActive(inputState, PowerHud.isInContainerScreen());
	private CurInput getInputAbilitiesOnClick(ClientControlScheme controlScheme, ClientKey key, KeyModifier keyModifier) {
		CurInput input = CurInput.instance;
		input.reset();
		
		if (controlScheme != null) {
			ClientControlScheme.setPrioritizedAbility(input.heldAbility, keyModifier, 
					(KeyModifier mod) -> controlScheme.getBindsWithModifier(InputMethod.HOLD, key, mod), 
					filter);
			ClientControlScheme.setPrioritizedAbility(input.clickAbility, keyModifier, 
					(KeyModifier mod) -> controlScheme.getBindsWithModifier(InputMethod.CLICK, key, mod), 
					filter);
		}
		
		return input;
	}
	
	@Nullable
	protected ClientControlScheme getCurControlScheme(Power<?> power) {
		if (power != null && power.hasPower()) {
			return AllControlSchemes.getForPowerType(power.getPowerType());
		}
		return null;
	}
	
	static class CurInput {
		private static CurInput instance = new CurInput();
		
		public final BaseAndActiveAbility heldAbility = new BaseAndActiveAbility();
		public final BaseAndActiveAbility clickAbility = new BaseAndActiveAbility();
		
		public void reset() {
			heldAbility.reset();
			clickAbility.reset();
		}
	}
	
	public static class BaseAndActiveAbility {
		public Ability baseAbility;
		public AbilityConditionCheck curActiveAbility;
		
		public void set(Ability baseAbility, AbilityConditionCheck curActiveAbility) {
			this.baseAbility = baseAbility;
			this.curActiveAbility = curActiveAbility;
		}
		
		public void reset() {
			this.baseAbility = null;
			this.curActiveAbility = null;
		}
	}
	
	
	// Hotbar stuff
	
	public Map<Hotbar, ClientKey> hotbarsSelection = new IdentityHashMap<>();
	protected float hotbarsSelectionTimestamp;
	
	public void checkStartHotbarSelection(ClientKey pressedKey) {
		ClientControlScheme controlScheme = getActiveControlScheme();
		if (controlScheme != null) {
			Hotbar wheelHotbar = null;
			ClientControlScheme.MoveGroup curControls = controlScheme.getCurGroup();
			for (Hotbar abilityHotbar : curControls.hotbars) {
				if (abilityHotbar.switchAbilityKey != null && abilityHotbar.switchAbilityKey.keyMatches(pressedKey, getCurModifier())) {
					if (wheelHotbar == null) wheelHotbar = abilityHotbar;
					setSelectingAbility(abilityHotbar, pressedKey, true);
				}
			}
			if (JojoMod.config.getClient().abilitySelectionWheel.getAsBoolean() && wheelHotbar != null) {
				mc.setScreen(new AbilitySelectionWheel(wheelHotbar));
			}
		}
	}
	
	public void checkStopHotbarSelection(ClientKey releasedKey) {
		if (!hotbarsSelection.isEmpty()) {
			var iter = hotbarsSelection.entrySet().iterator();
			while (iter.hasNext()) {
				var entry = iter.next();
				ClientKey hotbarKey = entry.getValue();
				if (hotbarKey == releasedKey) {
					iter.remove();
				}
			}
		}
	}
	
	public boolean hotbarScroll(double scrollDelta) {
		@Nullable AbilitySelectionWheel curWheel = mc.screen instanceof AbilitySelectionWheel w ? w : null;
		if (mc.screen != null && curWheel == null) return false;
		
		boolean scrolledAHotbar = false;
		ClientControlScheme controlScheme = getActiveControlScheme();
		if (controlScheme != null) {
			ClientControlScheme.MoveGroup curControls = controlScheme.getCurGroup();
			for (Hotbar hotbar : curControls.hotbars) {
				if (isSelectingAbility(hotbar)) {
					int newIndex = hotbar.slotIndex;
					do {
						int n = hotbar.slots.size();
						newIndex = (newIndex - (int) scrollDelta);
						if (newIndex < 0) newIndex += (-newIndex / n + 1) * n;
						newIndex %= n;
						
						HotbarSlot slot = hotbar.slots.get(newIndex);
						if (slot.showAbility(getCurModifier()) != null) {
							break;
						}
					}
					while (newIndex != hotbar.slotIndex);
					
					if (newIndex != hotbar.slotIndex) {
						hotbar.slotIndex = newIndex;
						if (curWheel != null && curWheel.abilities == hotbar) {
							curWheel.setIgnoreMouseUntilMove(OptionalInt.of(newIndex));
						}
					}
					
					scrolledAHotbar |= true;
				}
			}
		}
		return scrolledAHotbar;
	}
	
	public boolean hotbarPickSlot(ClientKey digitKey) {
		Key vanillaKey = digitKey.getVanillaKey();
		int newIndex = -1;
		for (int i = 0; i < mc.options.keyHotbarSlots.length; i++) {
			if (vanillaKey == mc.options.keyHotbarSlots[i].getKey()) {
				newIndex = i;
				break;
			}
		}
		if (newIndex < 0) return false;

		boolean pickedAHotbarSlot = false;
		ClientControlScheme controlScheme = getActiveControlScheme();
		if (controlScheme != null) {
			ClientControlScheme.MoveGroup curControls = controlScheme.getCurGroup();
			@Nullable AbilitySelectionWheel curWheel = mc.screen instanceof AbilitySelectionWheel w ? w : null;
			for (Hotbar hotbar : curControls.hotbars) {
				if (isSelectingAbility(hotbar) && newIndex < hotbar.slots.size()) {
					HotbarSlot slot = hotbar.slots.get(newIndex);
					if (slot.showAbility(getCurModifier()) != null) {
						hotbar.slotIndex = newIndex;
						if (curWheel != null && curWheel.abilities == hotbar) {
							curWheel.setIgnoreMouseUntilMove(OptionalInt.of(newIndex));
						}
					}
					
					pickedAHotbarSlot |= true;
				}
			}
		}
		return pickedAHotbarSlot;
	}
	
	public boolean isSelectingAbility(Hotbar hotbar) {
		return !inputsDisabled && hotbar.alwaysSwitchAbility()
				|| hotbarsSelection.containsKey(hotbar);
	}
	
	public void setSelectingAbility(Hotbar hotbar, ClientKey key, boolean selecting) {
		if (selecting) {
			if (hotbarsSelection.isEmpty()) {
				hotbarsSelectionTimestamp = ClientTickHandler.tickCount + ClientUtil.partialTick();
			}
			hotbarsSelection.put(hotbar, key);
		}
		else {
			hotbarsSelection.remove(hotbar);
		}
	}
	
	public float getHotbarsSelectionTime() {
		float time = ClientTickHandler.tickCount + ClientUtil.partialTick();
		return time - hotbarsSelectionTimestamp;
	}
	
	
	public void onUpdatedControls(ClientControlScheme.MoveGroup newControls) {
		hotbarsSelection.clear();
	}
	
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void playerMovementInput(MovementInputUpdateEvent event) {
		Player player = event.getEntity();
		Input input = event.getInput();
		float movementMultiplier = 1;
		
		EntityActionInstance playerAction = LivingComponentAction.getCurEntityAction(player);
		if (playerAction != null) {
			movementMultiplier *= playerAction.userWalkSpeed;
		}
		
		StandEntity stand = ClientGlobals.playerStandEntity;
		if (stand != null) {
			EntityActionInstance standAction = LivingComponentAction.getCurEntityAction(stand);
			if (standAction != null) {
				movementMultiplier *= standAction.userWalkSpeed;
			}
		}
		
		if (movementMultiplier != 1) {
			input.forwardImpulse *= movementMultiplier;
			input.leftImpulse *= movementMultiplier;
			if (movementMultiplier < 1) {
				player.setSprinting(false);
			}
		}
	}
	
	
	public static final Int2ObjectMap<Direction2D> ARROW_KEYS = Util.make(new Int2ObjectOpenHashMap<>(), map -> {
		map.put(GLFW.GLFW_KEY_LEFT,  Direction2D.LEFT);
		map.put(GLFW.GLFW_KEY_UP,	 Direction2D.UP);
		map.put(GLFW.GLFW_KEY_RIGHT, Direction2D.RIGHT);
		map.put(GLFW.GLFW_KEY_DOWN,  Direction2D.DOWN);
	});
	
	@Nullable
	public static Direction2D getArrowKey(int keyCode) {
		return ARROW_KEYS.get(keyCode);
	}


	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void fixArrowPunchKick(InteractionKeyMappingTriggered event) {
		if (event.isAttack() && !isValidPlayerAttackTarget(mc.hitResult)) {
			event.setCanceled(true); // prevents kick for "Attempting to attack an invalid entity"
			event.setSwingHand(false);
		}
	}

	public static boolean isValidPlayerAttackTarget(HitResult hitResult) {
		if (hitResult.getType() == HitResult.Type.ENTITY) {
			Entity entity = ((EntityHitResult) hitResult).getEntity();
			if (entity == Minecraft.getInstance().player || entity instanceof AbstractArrow
					/* || entity instanceof ItemEntity || entity instanceof ExperienceOrb */) {
				return false;
			}
		}
		return true;
	}
}
