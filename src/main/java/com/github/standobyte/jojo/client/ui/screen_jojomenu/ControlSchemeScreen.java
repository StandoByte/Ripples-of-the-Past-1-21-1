package com.github.standobyte.jojo.client.ui.screen_jojomenu;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.standskin.sprites.AbilityIconSprites;
import com.github.standobyte.jojo.client.ui.hud_power.PowerHud;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.client.ui.utils.tooltip.TooltipParams;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.Power;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.ability.Ability;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

// TODO don't render the right-side tab
public class ControlSchemeScreen extends Screen implements IJojoMenuScreen {
	protected ResourceLocation texture;
	protected ResourceLocation textureSidePanel;
	protected GuiIcon abilitySlot;
	protected TabCategory category;
	protected Tab tab;
	
	protected List<PowerClassName> powerClasses = new ArrayList<>();
	protected List<AbilityEntry> mainAbilities = new ArrayList<>();

	public ControlSchemeScreen(Component title, TabCategory category, Tab tab) {
		super(title);
		this.category = category;
		this.tab = tab;
		this.texture = JojoMod.resLoc("textures/gui/paper_style/control_scheme.png");
		this.textureSidePanel = JojoMod.resLoc("textures/gui/paper_style/control_scheme2.png");
		this.abilitySlot = new GuiIcon(texture, 0, 227, 18, 18, 512, 512);
	}
	
	@Override
	public void init() {
		super.init();
		
		// Create the list off all abilities the player can add a keybind to
		this.powerClasses.clear();
		this.mainAbilities.clear();
		int powerClassI = 1;
		int row = 0;
		for (PowerClass<?> powerClass : PowerClass.values()) {
			Power<?> power = ClientPowerCache.getPower(powerClass);
			if (power != null && power.hasPower()) {
				powerClasses.add(new PowerClassName(powerClass, powerClassI * POWER_NAME_HEIGHT + (row - 1) * SLOT_HEIGHT));
				var abilities = power.getMoveset().abilities.values();
				int column = 0;
				for (Ability ability : abilities) {
					if (ability.addToControlSchemeEditing()) {
						int x = SLOTS_X_OFFSET + column * SLOT_WIDTH;
						int y = SLOTS_Y_OFFSET + row * SLOT_HEIGHT + powerClassI * POWER_NAME_HEIGHT;
						mainAbilities.add(new AbilityEntry(ability, x, y));
						
						column++;
						if (column == MAX_ICONS_IN_ROW) {
							row++;
							column = 0;
						}
					}
				}
				row++;
			}
			powerClassI++;
		}
	}
	
	protected int SLOT_WIDTH = 18;
	protected int SLOT_HEIGHT = 18;
	protected int ICON_WIDTH = 16;
	protected int ICON_HEIGHT = 16;
	protected int SLOTS_X_OFFSET = 235;
	protected int SLOTS_Y_OFFSET = 4;
	protected int POWER_NAME_HEIGHT = 20;
	protected int MAX_ICONS_IN_ROW = 4;

	protected static record PowerClassName(PowerClass<?> powerClass, int y) {}
	protected static record AbilityEntry(Ability ability, int x, int y) {}

	@Override
	public TabCategory getTabCategory() {
		return category;
	}

	@Override
	public Tab getTab() {
		return tab;
	}

	@Override public int getWindowWidth() { return 319; }
	@Override public boolean rightSideTabsEnabled() { return false; }

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float p_283123_) {
		super.render(guiGraphics, mouseX, mouseY, p_283123_);

		int x = getWindowX(this);
		int y = getWindowY(this);
		int width = getWindowWidth();
		int height = getWindowHeight();
		BlitFloat.blit(guiGraphics.pose(), Minecraft.getInstance(), texture, 
				x, y, width, height, 0, 
				0, 0, width, height, 512, 512, 
				BlitFloat.NO_TINT);
		
		renderTabs(guiGraphics, this);
		
		RenderSystem.enableBlend();
		// Sidebar with all the abilities
		
			// Power names
		for (var powerClassLine : powerClasses) {
			int powerIconX = SLOTS_X_OFFSET + x;
			int powerIconY = SLOTS_Y_OFFSET + y + powerClassLine.y;
			Power<?> power = powerClassLine.powerClass.get(minecraft.player);
			
			if (powerClassLine.powerClass == PowerClass.STAND) {
				PowerHud.renderClientStandIcon(guiGraphics.pose(), powerIconX, powerIconY);
			}
			else {
				ResourceLocation powerTypeId = power.getPowerType().getId();
				ResourceLocation powerIcon = ResourceLocation.fromNamespaceAndPath(powerTypeId.getNamespace(), "textures/power/" + powerTypeId.getPath() + ".png");
				BlitFloat.blit(guiGraphics.pose(), minecraft, powerIcon, powerIconX, powerIconY, 16, 16, 0, BlitFloat.NO_TINT);
			}
		}

			// Ability icons
		AbilityIconSprites abilityIconSprites = StandSkinsLoader.getInstance().abilityIcons;
		PoseStack pose = guiGraphics.pose();
		StandSkin standSkin = StandSkinsLoader.getCurSkin();
		
		for (AbilityEntry ability : mainAbilities) {
			TextureAtlasSprite abilitySprite = abilityIconSprites.getAbilityIcon(ability.ability.name(), standSkin);
			int abilityX = ability.x + x;
			int abilityY = ability.y + y;
			
			abilitySlot.render(pose, abilityX, abilityY);
			BlitFloat.blit(pose, minecraft, abilitySprite, 
					abilityX + (SLOT_WIDTH - ICON_WIDTH) / 2, 
					abilityY + (SLOT_HEIGHT - ICON_HEIGHT) / 2, ICON_WIDTH, ICON_HEIGHT, 0, BlitFloat.NO_TINT);
		}
		
		
		AbilityEntry hoveredMovesetAbility = getMovesetAbilitySlotAt(mouseX, mouseY);
		if (hoveredMovesetAbility != null) {
			Ability ability = hoveredMovesetAbility.ability;
			TooltipParams.set(TooltipParams.paperStyle());
			setTooltipForNextRenderPass(ability.getName(ability.getUserPower(minecraft.player)).copy().withStyle(ChatFormatting.BLACK));
		}
		else {
			renderTabTooltip(guiGraphics, this, mouseX, mouseY);
		}
	}
	
	@Nullable
	protected AbilityEntry getMovesetAbilitySlotAt(int mouseX, int mouseY) {
		mouseX -= getWindowX(this);
		mouseY -= getWindowY(this);
		for (AbilityEntry abilitySlot : mainAbilities) {
			if (abilitySlot.x <= mouseX && abilitySlot.x + SLOT_WIDTH > mouseX
					&& abilitySlot.y <= mouseY && abilitySlot.y + SLOT_HEIGHT > mouseY) {
				return abilitySlot;
			}
		}
		
		return null;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (clickTab(mouseX, mouseY, button, this)) return true;
		return super.mouseClicked(mouseX, mouseY, button);
	}

}
