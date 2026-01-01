package com.github.standobyte.jojo.client.input;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.mutable.MutableInt;
import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.client.ClientGlobals;
import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.ClientTickHandler;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.config.ClientModSettings;
import com.github.standobyte.jojo.client.input.controlscheme.AllControlSchemes;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme.AbilityControlsEntry;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme.Hotbar;
import com.github.standobyte.jojo.client.input.controlscheme.ClientKey;
import com.github.standobyte.jojo.client.ui.AbilitySelectionWheel;
import com.github.standobyte.jojo.client.ui.powerhud.PowerHud;
import com.github.standobyte.jojo.core.event.client.PreKeyInputEvent;
import com.github.standobyte.jojo.core.packet.fromclient.ClAbilityInputPacket;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
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
import com.github.standobyte.jojo.util.CommonEnums.Direction2D;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.datafixers.util.Pair;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
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
		tickHeldKeyTimers();
		tickReleaseEventQueue();
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onFrameUpdate(RenderFrameEvent.Pre event) {
		if (!ClientModSettings.getSettingsReadOnly().toggleDisableHotbars) {
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
		if (action == InputConstants.RELEASE && mc.screen instanceof ChatScreen) {
			keyReleaseEventQueue.add(new DelayedInput(key, action, modifiers));
		}
		else if (input(key, action, modifiers)) {
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
	
	protected void tickReleaseEventQueue() {
		if (!keyReleaseEventQueue.isEmpty() && mc.getConnection() != null && mc.screen == null) {
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
	
	
	private FriendlyByteBuf inputBuf = new FriendlyByteBuf(Unpooled.buffer());
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
		
		switch (inputType) {
			case InputConstants.PRESS -> {
				ClientControlScheme controlScheme = getActiveControlScheme();
				if (controlScheme == null) return false;
				
				cancelVanilla |= hotbarPickSlot(key);
				
				KeyModifier keyModifier = getCurModifier();
				
				CurInput input = getInputAbilitiesOnClick(controlScheme, key, keyModifier);
				@Nullable Ability heldAbility = input.heldAbility != null ? input.heldAbility.ability : null;
				@Nullable Ability clickAbility = input.clickAbility != null ? input.clickAbility.ability : null;
				
				boolean ambiguousClickOrHold = heldAbility != null && clickAbility != null;
				InputMethod inputMethod = 
						ambiguousClickOrHold ? null : 
						heldAbility != null ? InputMethod.HOLD : 
						clickAbility != null ? InputMethod.CLICK : 
						null;
				cancelVanilla |= heldAbility != null || clickAbility != null;

				HeldKeyTimer heldKeyTimer = new HeldKeyTimer(key, cancelVanilla, keyModifier);
				if (ambiguousClickOrHold) {
					// TODO (!!!!) only do this if both abilities have a windup (if not, then idfk, it's 2AM rn)
					// also consider that the windup might be shorted than 4 ticks
					heldKeyTimer.setResolveInputMethod(new ClickHoldResolve(heldAbility, clickAbility));
				}
				else if (inputMethod != null) {
					heldKeyTimer.setInputMethod(inputMethod);
					switch (inputMethod) {
						case HOLD -> doInput(InputEventType.PRESS_HOLD, keyId, heldAbility, input.heldAbility.conditionCheck, 0);
						case CLICK -> doInput(InputEventType.PRESS_CLICK, keyId, clickAbility, input.clickAbility.conditionCheck, 0);
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
					doInput(InputEventType.RELEASE, keyId, null, ConditionCheck.POSITIVE, 0);
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
	
	private void doInput(InputEventType type, short keyId, Ability ability, ConditionCheck conditionCheck, float timeTookToResolve) {
		Player player = mc.player;
		switch (type) {
			case PRESS_CLICK, PRESS_HOLD -> {
				if (ability == null || player == null) return;

				if (conditionCheck.isPositive()) {
					ability.writeExtraInput(inputBuf, player, true);
					AbilityInput.keyPress(keyId, ability, player, inputBuf, 
							type.inputMethod, timeTookToResolve, BufferingState.clickCanBuffer());
				}
				PacketDistributor.sendToServer(ClAbilityInputPacket.keyPress(keyId, player, ability, type, timeTookToResolve));
			}
			case RELEASE -> {
				AbilityInput.keyRelease(keyId, player);
				PacketDistributor.sendToServer(ClAbilityInputPacket.releaseHold(keyId));
			}
		}
	}
	
	
	// Held keys stuff
	
	public Map<ClientKey, HeldKeyTimer> _heldKeys = new HashMap<>();
	public Map<ClientKey, MutableInt> _recentlyClicked = new HashMap<>();
	
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
	
	protected void tickHeldKeyTimers() {
		for (var heldKey : _heldKeys.values()) {
			heldKey.incTicks();
		}
		
		for (MutableInt timer : _recentlyClicked.values()) {
			if (timer.intValue() >= 0) {
				timer.decrement();
			}
		}
	}
	
	public void onResolvedKeyAsClick(ClientKey key) {
		_recentlyClicked.computeIfAbsent(key, __ -> new MutableInt(0)).setValue(3);
	}
	
	public boolean wasKeyClickedRecently(ClientKey key) {
		MutableInt timer = _recentlyClicked.get(key);
		return timer != null && timer.intValue() >= 0;
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
		ClickHoldResolve keyResolution = heldKeyTimer.getResolvingInputMethod();
		if (keyResolution != null) {
			ClickHoldResolve.Result wasItClick = keyResolution.keyReleased();
			if (wasItClick != null && wasItClick.input() == ClickHoldResolve.InputState.CLICK) {
				Ability ability = keyResolution.clickAbility;
				ConditionCheck conditionCheck = ClientPowerCache.getAvailableAbilities(ability.abilityId.powerClass()).getConditionCheck(ability);
				float ticksToResolveClick = wasItClick.timeTook();
				doInput(InputEventType.PRESS_CLICK, keyId, ability, conditionCheck, ticksToResolveClick);
				heldKeyTimer.setInputMethod(InputMethod.CLICK);
			}
		}
	}
	
	private void frameUpdateHeldKeys(float tickDelta) {
		for (HeldKeyTimer timer : _heldKeys.values()) {
			ClickHoldResolve keyResolution = timer.getResolvingInputMethod();
			if (keyResolution != null) {
				var changedState = keyResolution.frameUpdate(tickDelta);
				if (changedState != null) {
					switch (changedState.input()) {
						case ASSUME_HOLD -> {}
						case HOLD -> {
							Ability ability = keyResolution.heldAbility;
							ConditionCheck conditionCheck = ClientPowerCache.getAvailableAbilities(ability.getAbilityId().powerClass()).getConditionCheck(ability);
							float ticksToResolveHeld = changedState.timeTook();
							doInput(InputEventType.PRESS_HOLD, timer.key.keyId(), ability, conditionCheck, ticksToResolveHeld);
							timer.setInputMethod(InputMethod.HOLD);
						}
						default -> {}
					}
				}
			}
		}
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
	
	private CurInput getInputAbilitiesOnClick(ClientControlScheme controlScheme, ClientKey key, KeyModifier keyModifier) {
		CurInput input = CurInput.instance;
		input.heldAbility = null;
		input.clickAbility = null;
		
		if (controlScheme != null) {
			List<AbilityControlsEntry> heldBound = controlScheme.getBindsWithModifier(InputMethod.HOLD, key, keyModifier);
			List<AbilityControlsEntry> clickBound = controlScheme.getBindsWithModifier(InputMethod.CLICK, key, keyModifier);
			
			if (!(heldBound.isEmpty() && clickBound.isEmpty())) {
				Predicate<AbilityInputState> filter = inputState -> AbilityInputState.isInputActive(inputState, PowerHud.isInContainerScreen());
				input.heldAbility = ClientControlScheme.prioritizedAbility(heldBound, filter);
				input.clickAbility = ClientControlScheme.prioritizedAbility(clickBound, filter);
			}
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
		
		public AbilityConditionCheck heldAbility;
		public AbilityConditionCheck clickAbility;
	}
	
	
	// Hotbar stuff
	
	public Set<Pair<Hotbar, ClientKey>> hotbarsSelection = Sets.newIdentityHashSet();
	protected float hotbarsSelectionTimestamp;
	
	public void checkStartHotbarSelection(ClientKey pressedKey) {
		ClientControlScheme controlScheme = getActiveControlScheme();
		if (controlScheme != null) {
			Hotbar wheelHotbar = null;
			var curControls = controlScheme.getCurGroup().getValue();
			for (Hotbar abilityHotbar : curControls.hotbars) {
				if (abilityHotbar.switchAbilityKey.keyMatches(pressedKey, getCurModifier())) {
					if (wheelHotbar == null) wheelHotbar = abilityHotbar;
					setSelectingAbility(abilityHotbar, pressedKey, true);
				}
			}
			if (ClientModSettings.getSettingsReadOnly().abilitySelectionWheel && wheelHotbar != null) {
				mc.setScreen(new AbilitySelectionWheel(wheelHotbar));
			}
		}
	}
	
	public void checkStopHotbarSelection(ClientKey releasedKey) {
		if (!hotbarsSelection.isEmpty()) {
			var iter = hotbarsSelection.iterator();
			while (iter.hasNext()) {
				var entry = iter.next();
				ClientKey hotbarKey = entry.getSecond();
				if (hotbarKey == releasedKey) {
					iter.remove();
				}
			}
		}
	}
	
	public boolean hotbarScroll(double scrollDelta) {
		if (hotbarsSelection.isEmpty()) return false;
		@Nullable AbilitySelectionWheel curWheel = mc.screen instanceof AbilitySelectionWheel w ? w : null;
		for (var entry : hotbarsSelection) {
			Hotbar hotbar = entry.getFirst();
			int n = hotbar.slots.size();
			int newIndex = (hotbar.slotIndex - (int) scrollDelta);
			if (newIndex < 0) newIndex += (-newIndex / n + 1) * n;
			newIndex %= n;
			
			hotbar.slotIndex = newIndex;
			if (curWheel != null && curWheel.abilities == hotbar) {
				curWheel.setIgnoreMouseUntilMove(OptionalInt.of(newIndex));
			}
		}
		return true;
	}
	
	public boolean hotbarPickSlot(ClientKey digitKey) {
		if (hotbarsSelection.isEmpty()) return false;
		
		Key vanillaKey = digitKey.getVanillaKey();
		int newIndex = -1;
		for (int i = 0; i < mc.options.keyHotbarSlots.length; i++) {
			if (vanillaKey == mc.options.keyHotbarSlots[i].getKey()) {
				newIndex = i;
				break;
			}
		}
		if (newIndex < 0) return false;
		
		@Nullable AbilitySelectionWheel curWheel = mc.screen instanceof AbilitySelectionWheel w ? w : null;
		for (var entry : hotbarsSelection) {
			Hotbar hotbar = entry.getFirst();
			if (newIndex < hotbar.slots.size()) {
				hotbar.slotIndex = newIndex;
				if (curWheel != null && curWheel.abilities == hotbar) {
					curWheel.setIgnoreMouseUntilMove(OptionalInt.of(newIndex));
				}
			}
		}
		return true;
	}
	
	public boolean isSelectingAbility(Hotbar hotbar) {
		return hotbarsSelection.stream().anyMatch(entry -> entry.getFirst() == hotbar);
	}
	
	public void setSelectingAbility(Hotbar hotbar, ClientKey key, boolean selecting) {
		if (selecting) {
			if (hotbarsSelection.isEmpty()) {
				hotbarsSelectionTimestamp = ClientTickHandler.tickCount + ClientUtil.partialTick();
			}
			hotbarsSelection.add(Pair.of(hotbar, key));
		}
		else {
			hotbarsSelection.removeIf(entry -> entry.getFirst() == hotbar);
		}
	}
	
	public float getHotbarsSelectionTime() {
		float time = ClientTickHandler.tickCount + ClientUtil.partialTick();
		return time - hotbarsSelectionTimestamp;
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
