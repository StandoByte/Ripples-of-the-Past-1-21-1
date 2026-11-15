package com.github.standobyte.jojo.client.ui.skill_tree;

import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ClientPowerCache;
import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.standskin.sprites.AbilityIconSprites;
import com.github.standobyte.jojo.client.ui.skill_tree.StandSkillsStructured.Sprites32x32;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.unlockableskill.UnlockableSkill;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

// net.minecraft.client.gui.screens.advancements.AdvancementWidget copypaste
public class SkillNodeUI<T> {
	private static final ResourceLocation TITLE_BOX_SPRITE = ResourceLocation.withDefaultNamespace("advancements/title_box");
	private static final int HEIGHT = 26;
	private static final int BOX_X = 0;
	private static final int BOX_WIDTH = 200;
	private static final int FRAME_WIDTH = 26;
	private static final int ICON_X = 8;
	private static final int ICON_Y = 5;
	private static final int ICON_WIDTH = 26;
	private static final int TITLE_PADDING_LEFT = 3;
	private static final int TITLE_PADDING_RIGHT = 5;
	private static final int TITLE_X = 32;
	private static final int TITLE_Y = 9;
	private static final int TITLE_MAX_WIDTH = 163;
	private static final int[] TEST_SPLIT_OFFSETS = new int[]{0, 10, -10, 25, -25};
	private final SkillTreeUI<T> tab;
	private final SkillNode<T> node;
	private final SkillDisplayInfo display;
	private final int width;
	private final Minecraft minecraft;
	@Nullable
	private SkillNodeUI<T> parent;
	public final List<SkillNodeUI<T>> children = Lists.newArrayList();
	public int x;
	public int y;

	public SkillNodeUI(SkillTreeUI<T> tab, Minecraft minecraft, SkillNode<T> node, SkillDisplayInfo display) {
		this.tab = tab;
		this.node = node;
		this.display = display;
		this.minecraft = minecraft;
//		this.title = Language.getInstance().getVisualOrder(minecraft.font.substrByWidth(display.getTitle(), 163));
		updatePosition(display);
//		int i = this.getMaxProgressWidth();
		int j = 150;//29 + minecraft.font.width(this.title) + i;
//		this.description = Language.getInstance()
//				.getVisualOrder(
//						this.findOptimalLines(ComponentUtils.mergeStyles(display.getDescription().copy(), Style.EMPTY.withColor(display.getType().getChatColor())), j)
//						);
//
//		for (FormattedCharSequence formattedcharsequence : this.description) {
//			j = Math.max(j, minecraft.font.width(formattedcharsequence));
//		}
//
		this.width = j + 3 + 5;
	}
	
	public void updatePosition(SkillDisplayInfo display) {
		this.x = Mth.floor(display.getX() * 28.0F);
		this.y = Mth.floor(display.getY() * 27.0F);
	}
	
	public T getValue() {
		return node.value();
	}

	private static float getMaxWidth(StringSplitter manager, List<FormattedText> text) {
		return (float)text.stream().mapToDouble(manager::stringWidth).max().orElse(0.0);
	}

	private List<FormattedText> findOptimalLines(Component component, int maxWidth) {
		StringSplitter stringsplitter = this.minecraft.font.getSplitter();
		List<FormattedText> list = null;
		float f = Float.MAX_VALUE;

		for (int i : TEST_SPLIT_OFFSETS) {
			List<FormattedText> list1 = stringsplitter.splitLines(component, maxWidth - i, Style.EMPTY);
			float f1 = Math.abs(getMaxWidth(stringsplitter, list1) - (float)maxWidth);
			if (f1 <= 10.0F) {
				return list1;
			}

			if (f1 < f) {
				f = f1;
				list = list1;
			}
		}

		return list;
	}

	@Nullable
	private SkillNodeUI<T> getFirstVisibleParent(SkillNode<T> node) {
		do {
			node = node.parent();
		}
		while (node != null && node.display().isEmpty());

		return node != null && !node.display().isEmpty() ? this.tab.getWidget(node.holder()) : null;
	}

	public void drawConnectivity(GuiGraphics guiGraphics, int x, int y, boolean dropShadow) {
		if (this.parent != null) {
			int i = x + this.parent.x + 13;
			int j = x + this.parent.x + 26 + 4;
			int k = y + this.parent.y + 13;
			int l = x + this.x + 13;
			int i1 = y + this.y + 13;
			int j1 = dropShadow ? -16777216 : -1;
			if (dropShadow) {
				guiGraphics.hLine(j, i, k - 1, j1);
				guiGraphics.hLine(j + 1, i, k, j1);
				guiGraphics.hLine(j, i, k + 1, j1);
				guiGraphics.hLine(l, j - 1, i1 - 1, j1);
				guiGraphics.hLine(l, j - 1, i1, j1);
				guiGraphics.hLine(l, j - 1, i1 + 1, j1);
				guiGraphics.vLine(j - 1, i1, k, j1);
				guiGraphics.vLine(j + 1, i1, k, j1);
			} else {
				guiGraphics.hLine(j, i, k, j1);
				guiGraphics.hLine(l, j, i1, j1);
				guiGraphics.vLine(j, i1, k, j1);
			}
		}

		for (SkillNodeUI<T> widget : this.children) {
			widget.drawConnectivity(guiGraphics, x, y, dropShadow);
		}
	}

	/** @return Value if the node is hovered, null otherwise */
	@Nullable
	public T draw(GuiGraphics guiGraphics, Function<T, Sprites32x32> getSprite32x32, int x, int y, int mouseX, int mouseY) {
		SetIfNonNull<T> hovered = new SetIfNonNull<>();
//		if (!this.display.isHidden() || this.progress != null && this.progress.isDone()) {
			Sprites32x32 frameSprites = getSprite32x32.apply(getValue());
			ResourceLocation frameSpriteBG = frameSprites.background();
			ResourceLocation frameSpriteFG = frameSprites.foreground();
			int nodeX = this.x + x;
			int nodeY = this.y + y;
			if (frameSpriteBG != null) {
				guiGraphics.blitSprite(frameSpriteBG, nodeX, nodeY - 3, 32, 32);
			}
			T value = node.value();
			renderIcon(value, guiGraphics, nodeX, nodeY);
			if (frameSpriteFG != null) {
				guiGraphics.blitSprite(frameSpriteFG, nodeX, nodeY - 3, 32, 32);
			}
			boolean isHovered = 
					mouseX >= nodeX && mouseX <= nodeX + 26 && 
					mouseY >= nodeY && mouseY <= nodeY + 26;
			if (isHovered) {
				hovered.set(value);
			}
//		}

		for (SkillNodeUI<T> child : this.children) {
			hovered.set(child.draw(guiGraphics, getSprite32x32, x, y, mouseX, mouseY));
		}
		return hovered.get();
	}
	
	protected void renderIcon(T value, GuiGraphics guiGraphics, int x, int y) {
		renderSkillIcon((UnlockableSkill) value, guiGraphics, x, y);
	}
	
	public static void renderSkillIcon(UnlockableSkill skill, GuiGraphics guiGraphics, int x, int y) {
		AbilityIconSprites abilityIconSprites = StandSkinsLoader.getInstance().abilityIcons;
		StandPower standPower = ClientPowerCache.getPower(PowerClass.STAND);
		StandSkin standSkin = StandSkinsLoader.getInstance().getSkin(standPower);
		TextureAtlasSprite icon = abilityIconSprites.getAbilityIcon(skill.skillName, standSkin);
		BlitFloat.blit(guiGraphics.pose(), Minecraft.getInstance(), icon, 
				x + 8, y + 5, 16, 16, 0, BlitFloat.NO_TINT);
	}

	public int getWidth() {
		return this.width;
	}

	public void addChild(SkillNodeUI<T> widget) {
		this.children.add(widget);
	}

	public void drawHover(GuiGraphics guiGraphics, int x, int y, float fade, int width, int height) {
//		boolean flag = width + x + this.x + this.width + 26 >= this.tab.getScreen().width;
//		Component component = this.progress == null ? null : this.progress.getProgressText();
//		int i = component == null ? 0 : this.minecraft.font.width(component);
//		boolean flag1 = 113 - y - this.y - 26 <= 6 + this.description.size() * 9;
//		float f = this.progress == null ? 0.0F : this.progress.getPercent();
//		int j = Mth.floor(f * (float)this.width);
//		AdvancementWidgetType widgetType;
//		AdvancementWidgetType widgetType2;
//		AdvancementWidgetType widgetType3;
//		if (f >= 1.0F) {
//			j = this.width / 2;
//			widgetType = AdvancementWidgetType.OBTAINED;
//			widgetType2 = AdvancementWidgetType.OBTAINED;
//			widgetType3 = AdvancementWidgetType.OBTAINED;
//		} else if (j < 2) {
//			j = this.width / 2;
//			widgetType = AdvancementWidgetType.UNOBTAINED;
//			widgetType2 = AdvancementWidgetType.UNOBTAINED;
//			widgetType3 = AdvancementWidgetType.UNOBTAINED;
//		} else if (j > this.width - 2) {
//			j = this.width / 2;
//			widgetType = AdvancementWidgetType.OBTAINED;
//			widgetType2 = AdvancementWidgetType.OBTAINED;
//			widgetType3 = AdvancementWidgetType.UNOBTAINED;
//		} else {
//			widgetType = AdvancementWidgetType.OBTAINED;
//			widgetType2 = AdvancementWidgetType.UNOBTAINED;
//			widgetType3 = AdvancementWidgetType.UNOBTAINED;
//		}
//
//		int k = this.width - j;
//		RenderSystem.enableBlend();
//		int l = y + this.y;
//		int i1;
//		if (flag) {
//			i1 = x + this.x - this.width + 26 + 6;
//		} else {
//			i1 = x + this.x;
//		}
//
//		int j1 = 32 + this.description.size() * 9;
//		if (!this.description.isEmpty()) {
//			if (flag1) {
//				guiGraphics.blitSprite(TITLE_BOX_SPRITE, i1, l + 26 - j1, this.width, j1);
//			} else {
//				guiGraphics.blitSprite(TITLE_BOX_SPRITE, i1, l, this.width, j1);
//			}
//		}
//
//		guiGraphics.blitSprite(widgetType.boxSprite(), 200, 26, 0, 0, i1, l, j, 26);
//		guiGraphics.blitSprite(widgetType2.boxSprite(), 200, 26, 200 - k, 0, i1 + j, l, k, 26);
//		guiGraphics.blitSprite(widgetType3.frameSprite(this.display.getType()), x + this.x + 3, y + this.y, 26, 26);
//		if (flag) {
//			guiGraphics.drawString(this.minecraft.font, this.title, i1 + 5, y + this.y + 9, -1);
//			if (component != null) {
//				guiGraphics.drawString(this.minecraft.font, component, x + this.x - i, y + this.y + 9, -1);
//			}
//		} else {
//			guiGraphics.drawString(this.minecraft.font, this.title, x + this.x + 32, y + this.y + 9, -1);
//			if (component != null) {
//				guiGraphics.drawString(this.minecraft.font, component, x + this.x + this.width - i - 5, y + this.y + 9, -1);
//			}
//		}
//
//		if (flag1) {
//			for (int k1 = 0; k1 < this.description.size(); k1++) {
//				guiGraphics.drawString(this.minecraft.font, this.description.get(k1), i1 + 5, l + 26 - j1 + 7 + k1 * 9, -5592406, false);
//			}
//		} else {
//			for (int l1 = 0; l1 < this.description.size(); l1++) {
//				guiGraphics.drawString(this.minecraft.font, this.description.get(l1), i1 + 5, y + this.y + 9 + 17 + l1 * 9, -5592406, false);
//			}
//		}

		// TODO render icon
	}

	public boolean isMouseOver(int x, int y, int mouseX, int mouseY) {
//		if (!this.display.isHidden() || this.progress != null && this.progress.isDone()) {
//			int i = x + this.x;
//			int j = i + 26;
//			int k = y + this.y;
//			int l = k + 26;
//			return mouseX >= i && mouseX <= j && mouseY >= k && mouseY <= l;
//		} else {
			return false;
//		}
	}

	public void attachToParent() {
		if (this.parent == null && this.node.parent() != null) {
			this.parent = this.getFirstVisibleParent(this.node);
			if (this.parent != null) {
				this.parent.addChild(this);
			}
		}
	}

	public int getY() {
		return this.y;
	}

	public int getX() {
		return this.x;
	}
}
