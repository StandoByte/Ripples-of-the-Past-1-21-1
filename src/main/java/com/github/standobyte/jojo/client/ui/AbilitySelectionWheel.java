package com.github.standobyte.jojo.client.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

import javax.annotation.Nullable;

import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.input.InputHandler;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme.AbilityControlsEntry;
import com.github.standobyte.jojo.client.input.controlscheme.ClientControlScheme.HotbarSlot;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.standskin.sprites.AbilityIconSprites;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.client.ui.utils.tooltip.TooltipParams;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.Moveset;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.github.standobyte.jojo.powersystem.ability.condition.AvailableAbilities.AbilityConditionCheck;
import com.github.standobyte.jojo.powersystem.ability.controls.InputMethod;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.v1_21_4_stuff.missingmethods.ARGB;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.settings.KeyModifier;

public class AbilitySelectionWheel extends Screen implements ScreenLetsUseWASD {
	protected static final ResourceLocation DEFAULT_TEXTURE = JojoMod.resLoc("textures/ability_wheel.png");
	protected ResourceLocation texture;
	public ClientControlScheme.Hotbar abilities;
	protected StandSkin standSkin;

	public AbilitySelectionWheel(ClientControlScheme.Hotbar abilities) {
		super(Component.translatable("jojo_ripples.screen.ability_selection_wheel"));
		this.abilities = abilities;
	}
	
	public void init() {
		StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
		if (standPower != null) {
			StandSkinsLoader skinLoader = StandSkinsLoader.getInstance();
			standSkin = skinLoader.getSkin(standPower);
			if (standSkin != null) {
				texture = standSkin.getTexture(DEFAULT_TEXTURE);
			}
		}
		if (texture == null) {
			texture = DEFAULT_TEXTURE;
		}
	}
	
	@Nullable protected int[] mouseIgnorePos = null;
	public void setIgnoreMouseUntilMove(OptionalInt newSelectedSlot) {
		Window window = minecraft.getWindow();
		long windowHandle = window.getWindow();
//		if (mouseIgnorePos == null) {
//			GLFW.glfwSetInputMode(windowHandle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
//		}
		if (newSelectedSlot.isPresent()) {
			double width = (double)window.getScreenWidth();
			double height = (double)window.getScreenHeight();
			int[] pos = posAtSector(newSelectedSlot.getAsInt(), abilities.slots.size(), 50);
			double xpos = pos[0] / (double)window.getGuiScaledWidth() * width;
			double ypos = pos[1] / (double)window.getGuiScaledHeight() * height;
			GLFW.glfwSetCursorPos(windowHandle, xpos, ypos);
		}
		mouseIgnorePos = new int[] { ClientUtil.getScreenMouseX(), ClientUtil.getScreenMouseY() };
	}
	
	protected int[] posAtSector(int index, int count, double distFromCenter) {
		Window window = minecraft.getWindow();
		double angle = (index + 0.5) * 2 * Math.PI / count;
		if (COUNTER_CLOCKWISE) {
			angle = 2 * Math.PI - angle;
		}
		double width = (double)window.getGuiScaledWidth();
		double height = (double)window.getGuiScaledHeight();
		double xpos = width / 2 + Math.sin(angle) * distFromCenter;
		double ypos = height / 2 - Math.cos(angle) * distFromCenter;
		return new int[] { (int) xpos, (int) ypos };
	}
	
	public boolean checkIsIgnoringMouse(int mouseX, int mouseY) {
//		long window = minecraft.getWindow().getWindow();
		if (mouseIgnorePos != null && (mouseIgnorePos[0] != mouseX || mouseIgnorePos[1] != mouseY)) {
//			GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
			mouseIgnorePos = null;
		}
		return mouseIgnorePos != null;
	}
	
	protected static final boolean COUNTER_CLOCKWISE = true;

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		if (abilities == null || !InputHandler.getInstance().isSelectingAbility(abilities)) {
			onClose();
			return;
		}
		super.render(guiGraphics, mouseX, mouseY, partialTick);

		float width = 256;
		float height = 256;
		float x = (this.width - width) / 2f;
		float y = (this.height - height) / 2f;
		PoseStack pose = guiGraphics.pose();
		ResourceLocation texture = DEFAULT_TEXTURE;
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		int textColor = standSkin != null ? standSkin.getColor() : 0xFFFFFFFF;
		
		boolean ignoreMouse = checkIsIgnoringMouse(mouseX, mouseY);
		hoveredSlotIndex = ignoreMouse ? abilities.slotIndex : getSlotIndexAt(mouseX, mouseY);
		
		int n = abilities.slots.size();
		float angle0 = 0;
		float fill = 1f / n;
		float angleStep = 2 * (float) Math.PI * fill;
		if (COUNTER_CLOCKWISE) {
			angleStep = -angleStep;
			fill = -fill;
		}
		
		AbilityIconSprites abilityIconSprites = StandSkinsLoader.getInstance().abilityIcons;
		for (int i = 0; i < n; i++) {
			HotbarSlot slot = abilities.slots.get(i);
			KeyModifier curModifier = InputHandler.getInstance().getCurModifier();
			AbilityConditionCheck ability = slot.showAbility(curModifier);
			boolean showAbility = ability != null;
			TextureAtlasSprite abilitySprite = ability != null ? abilityIconSprites.getAbilityIcon(ability.ability.name(), standSkin) : null;
			
			if (i == hoveredSlotIndex && !showAbility) { 
				hoveredSlotIndex = -1;
			}
			boolean highlight = i == hoveredSlotIndex;
			float alpha = highlight ? 0.5f : 0.25f;
			if (highlight) {
				pose.pushPose();
				pose.translate(this.width / 2, this.height / 2, 0);
				pose.scale(1.1f, 1.1f, 1);
				pose.translate(-this.width / 2, -this.height / 2, 10);
			}
			BlitFloat.blitRadial(pose, Minecraft.getInstance(), texture, 
					x, y, width, height, 0, 
					angle0, fill, ARGB.white(alpha));
			angle0 += angleStep;
			if (highlight) {
				pose.popPose();
			}
			
			if (abilitySprite != null) {
				int[] iconPos = posAtSector(i, n, 75);
				float iconWidth = 16;
				float iconHeight = 16;
				BlitFloat.blit(pose, minecraft, abilitySprite, 
						iconPos[0] - iconWidth / 2, iconPos[1] - iconHeight / 2, iconWidth, iconHeight, 0, BlitFloat.NO_TINT);
			}
			

			if (showAbility) {
				int numberKey = HotbarSlot.numberKey(slot.index);
				if (numberKey != -1) {
					int[] digitPos = posAtSector(i, n, 90);
					guiGraphics.drawCenteredString(minecraft.font, String.valueOf(numberKey), 
							digitPos[0], digitPos[1] - minecraft.font.lineHeight / 2, textColor);
					RenderSystem.enableBlend();
					RenderSystem.defaultBlendFunc();
				}
			}
		}
		RenderSystem.disableBlend();
		
		abilityNames.clear();
		if (hoveredSlotIndex != -1 && !abilities.slots.isEmpty()) {
			if (!ignoreMouse) {
				abilities.slotIndex = hoveredSlotIndex;
			}
			
			hoveredSlot = abilities.slots.get(hoveredSlotIndex);
			KeyModifier curModifier = InputHandler.getInstance().getCurModifier();
			for (InputMethod inputMethod : InputMethod.values()) {
				AbilityControlsEntry abilityPath = hoveredSlot.getBinds()
						.getFirst(curModifier, inputMethod);							if (abilityPath == null) continue;
				Power<?> power = ClientPowerCache.getPower(abilityPath.powerClass());	if (power == null) continue;
				Moveset moveset = power.getMoveset();									if (moveset == null) continue;
				Ability ability = moveset.getAbility(abilityPath.abilityName());		if (ability == null) continue;
				
				abilityNames.add(ability.getName(power).copy().withStyle(ChatFormatting.BLACK));
			}
		}
		else {
			hoveredSlot = null;
		}
		if (!abilityNames.isEmpty()) {
			TooltipParams.set(TooltipParams.paperStyle());
			guiGraphics.renderComponentTooltip(font, abilityNames, mouseX, mouseY);
		}
	}

	protected int hoveredSlotIndex;
	protected HotbarSlot hoveredSlot;
	protected List<Component> abilityNames = new ArrayList<>(2);

	public int getSlotIndexAt(int mouseX, int mouseY) {
		if (abilities == null) return -1;
		int xCenter = this.width / 2;
		int yCenter = this.height / 2;
		if (xCenter == mouseX && yCenter == mouseY) {
			return -1;
//			return abilities.slots.size() > abilities.slotIndex ? abilities.slotIndex : -1;
		}
		int xOffs = mouseX - xCenter;
		int yOffs = mouseY - yCenter;
		double angle = Mth.atan2(xOffs, -yOffs); // [0; 2*PI)
		if (COUNTER_CLOCKWISE) {
			angle = 2 * Math.PI - angle;
		}
		if (angle < 0) angle += 2 * Math.PI;
		if (angle >= 2 * Math.PI) angle -= 2 * Math.PI;
		int index = Mth.floor(abilities.slots.size() * angle / (2 * Math.PI));
		return index;
	}
	
	protected boolean pickAbilityAt(int mouseX, int mouseY) {
		if (abilities != null) {
			int clickedIndex = getSlotIndexAt(mouseX, mouseY);
			if (clickedIndex >= 0 && clickedIndex < abilities.slots.size()) {
				abilities.slotIndex = clickedIndex;
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (super.mouseClicked(mouseX, mouseY, button)) return true;
		
		if (pickAbilityAt((int) mouseX, (int) mouseY)) {
			onClose();
			return true;
		}
		
		return false;
	}
	
	@Override
	public void onClose() {
		InputHandler.getInstance().setSelectingAbility(abilities, null, false);
		super.onClose();
	}




	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {}

}
