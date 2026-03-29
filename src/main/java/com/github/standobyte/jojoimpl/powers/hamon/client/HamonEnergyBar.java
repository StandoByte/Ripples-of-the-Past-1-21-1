package com.github.standobyte.jojoimpl.powers.hamon.client;

import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.ui.hud_power.Bars;
import com.github.standobyte.jojo.client.ui.hud_power.HudElement;
import com.github.standobyte.jojo.client.ui.hud_power.PowerHud;
import com.github.standobyte.jojo.client.ui.hud_power.PowerHud.AbilityHud;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.client.ui.utils.tooltip.MultiLineScreenTooltip;
import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.power.ModPlayerPowers;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.util.functions.MathUtil;
import com.github.standobyte.jojoimpl.powers.hamon.HamonData;
import com.github.standobyte.jojoimpl.powers.hamon.HamonPowerType;
import com.github.standobyte.v1_21_4_stuff.missingmethods.ARGB;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class HamonEnergyBar extends HudElement {
	public static HamonEnergyBar hamonEnergyBar;

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void addHud(RegisterGuiLayersEvent event) {
		AbilityHud hud = PowerHud.abilityHUDInstance;
		if (hud != null) {
			hud.addElement(hamonEnergyBar = new HamonEnergyBar("energy_hamon", 81, 16, Bars.HORIZONTAL_LENGTH + 8, Bars.HORIZONTAL_WIDTH));
		}
	}
	

	public static final GuiIcon ICON = new GuiIcon(JojoMod.resLoc("textures/hud/energy_hamon.png"), 20, 20);

	public HamonEnergyBar(String name, int x0, int y0, int width, int height) {
		super(name, x0, y0, width, height);
	}

	public HamonEnergyBar(String name, SnappingH snappingHorizontal, SnappingV snappingVertical, 
			int xOffset, int yOffset, int width, int height) {
		super(name, snappingHorizontal, snappingVertical, xOffset, yOffset, width, height);
	}

	@Override
	protected void initText() {
		this.tooltipText = new MultiLineScreenTooltip(
				Component.translatable("ripples_hud." + name).withStyle(ChatFormatting.BLACK), 
				Component.translatable("ripples_hud." + name + ".desc", 
						Component.translatable("ripples_hud." + name + ".desc1"))
				.withStyle(ChatFormatting.ITALIC, ChatFormatting.DARK_GRAY));
		this.tooltip.set(this.tooltipText);
	}

	@Override
	public boolean shouldRender() {
		if (hud.forContainerMenu.isTrue()) return false;
		HamonPowerType hamon = ModPlayerPowers.HAMON.get();
		return hamon != null && controlsHaveTypeAndAbility(PowerClass.PLAYER_POWER, hamon);
	}

	@Override
	public void renderElement(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		HamonData hamon = ClientPowerCache.getPower(PowerClass.PLAYER_POWER)
				.getCurTypeData(ModPlayerPowers.HAMON).orElse(null);
		if (hamon != null) {
			float partialTick = ClientUtil.partialTick(deltaTracker, false);
			float value = hamon.energy.energyAmount.lerp(partialTick);
			float maxValue = hamon.energy._maxEnergy.lerp(partialTick);
			if (maxValue <= 0) return;
			
			float valueInRegularBar = hamon.energy.getMaxEnergyPossible();
			float barLength = maxValue / valueInRegularBar;
			
			float ratio = MathUtil.ratioSafe(value, maxValue);
			int x = getX() + 8;
			int y = getY();
			renderShrinkingHorizontalBar(guiGraphics.pose(), x, y, 
					ratio, barLength, 
					Bars.BAR_HORIZONTAL_FILL, HamonPowerType.UI_COLOR, 1);
			ICON.render(guiGraphics.pose(), x - 12, y - 6);
		}
	}
	
	public static void renderShrinkingHorizontalBar(PoseStack poseStack, float x, float y, 
			float barFill, float barLengthFactor, 
			ResourceLocation barFillSprite, int fillTint, float alpha) {
		float length = Bars.HORIZONTAL_LENGTH;
		float width = Bars.HORIZONTAL_WIDTH;
		ResourceLocation barEmpty = Bars.BAR_HORIZONTAL_EMPTY;
		ResourceLocation barScale = Bars.BAR_HORIZONTAL_SCALE;
		int colorMain = ARGB.white(alpha);
		int colorFill = ARGB.color(alpha, fillTint);
		Minecraft mc = Minecraft.getInstance();
		
		float fullLength = length * barLengthFactor;
		
		// outside part
		float lengthLeft = Math.min(fullLength / 2, 6);
		float lengthRight = Math.min(length - lengthLeft, 6);
		float lengthMiddle = fullLength - lengthLeft - lengthRight;
		float x1 = 0;
		float x2 = fullLength - lengthRight;
		// left segment of the bar
		BlitFloat.blit(poseStack, mc, barEmpty, 
				x, y, lengthLeft, width, 0, 
				0, 0, lengthLeft, width, length, width, 
				colorMain);
		// middle segment
		if (lengthMiddle > 0) {
			x1 += lengthLeft;
			BlitFloat.blit(poseStack, mc, barEmpty, 
					x + x1, y, lengthMiddle, width, 0, 
					lengthLeft, 0, lengthMiddle, width, length, width, 
					colorMain);
		}
		// right segment
		x1 = fullLength - lengthRight;
		BlitFloat.blit(poseStack, mc, barEmpty, 
				x + x1, y, lengthRight, width, 0, 
				length - lengthRight, 0, lengthRight, width, length, width, 
				colorMain);

		// colored fill
		x1 = 0;
		float fillULength = (fullLength - 2) * barFill + 1;
		BlitFloat.blit(poseStack, mc, barFillSprite, 
				x, y, fillULength, width, 0, 
				0, 0, fillULength, width, length, width, 
				colorFill);

		// translucent scale on top of the bar
		x1 = 0;
		BlitFloat.blit(poseStack, mc, barScale, 
				x, y, fullLength, width, 0, 
				0, 0, fullLength, width, length, width, 
				colorMain);
	}

	@Override
	protected void checkTooltip(double mouseX, double mouseY, DeltaTracker deltaTracker) {
		HamonData hamon = ClientPowerCache.getPower(PowerClass.PLAYER_POWER)
				.getCurTypeData(ModPlayerPowers.HAMON).orElse(null);
		if (hamon != null) {
			MultiLineScreenTooltip tooltipText = (MultiLineScreenTooltip) this.tooltip.get();
			float value = hamon.energy.getEnergy();
			float maxValue = hamon.energy.getCurMaxEnergy();
			float ratio = MathUtil.ratioSafe(value, maxValue);
			tooltipText.setTitle(Component.translatable("ripples_hud." + name,
					Component.literal(String.valueOf((int) value)).withStyle(style -> style.withColor(PowerHud.Stamina.color(ratio))),
					Component.literal(String.valueOf((int) maxValue))
					).withStyle(ChatFormatting.BLACK));
			super.checkTooltip(mouseX, mouseY, deltaTracker);
		}

	}

}
