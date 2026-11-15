package com.github.standobyte.jojo.client.ui.skill_tree;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ui.skill_tree.StandSkillsStructured.Sprites32x32;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;

// net.minecraft.client.gui.screens.advancements.AdvancementTab copypaste
public class SkillTreeUI<T> {
	private final Minecraft minecraft;
	private final Screen screen;
	public final SkillNode<T> rootNode;
	public final SkillNodeUI<T> root;
	public final Map<SkillHolder<T>, SkillNodeUI<T>> widgets = new LinkedHashMap<>();
	private int minX = Integer.MAX_VALUE;
	private int minY = Integer.MAX_VALUE;
	private int maxX = Integer.MIN_VALUE;
	private int maxY = Integer.MIN_VALUE;
	private float fade;

	public SkillTreeUI(Minecraft minecraft, Screen screen, SkillNode<T> rootNode, SkillDisplayInfo rootDisplayInfo) {
		this.minecraft = minecraft;
		this.screen = screen;
		this.rootNode = rootNode;
		this.root = new SkillNodeUI<T>(this, minecraft, rootNode, rootDisplayInfo);
		this.addWidget(this.root, rootNode.holder());
	}

	public SkillNode<T> getRootNode() {
		return this.rootNode;
	}

	/** @return Hovered node's value */
	public T drawContents(GuiGraphics guiGraphics, Function<T, Sprites32x32> getSprite32x32, int x, int y, int mouseX, int mouseY) {
//		if (!this.centered) {
//			this.scrollX = (double)(117 - (this.maxX + this.minX) / 2);
//			this.scrollY = (double)(56 - (this.maxY + this.minY) / 2);
//			this.centered = true;
//		}

		//guiGraphics.enableScissor(x, y, x + 234, y + 113);
		guiGraphics.pose().pushPose();
		int i = x/* + Mth.floor(this.scrollX)*/;
		int j = y/* + Mth.floor(this.scrollY)*/;

		this.root.drawConnectivity(guiGraphics, i, j, true);
		this.root.drawConnectivity(guiGraphics, i, j, false);
		T hoveredValue = this.root.draw(guiGraphics, getSprite32x32, i, j, mouseX, mouseY);
		guiGraphics.pose().popPose();
		//guiGraphics.disableScissor();

		return hoveredValue;
	}

	public void drawTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY, int width, int height) {
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0.0F, 0.0F, -200.0F);
		guiGraphics.fill(0, 0, 234, 113, Mth.floor(this.fade * 255.0F) << 24);
		boolean flag = false;
		int i = 0;//Mth.floor(this.scrollX);
		int j = 0;//Mth.floor(this.scrollY);
		if (mouseX > 0 && mouseX < 234 && mouseY > 0 && mouseY < 113) {
			for (SkillNodeUI<T> widget : this.widgets.values()) {
				if (widget.isMouseOver(i, j, mouseX, mouseY)) {
					flag = true;
					widget.drawHover(guiGraphics, i, j, this.fade, width, height);
					break;
				}
			}
		}

		guiGraphics.pose().popPose();
		if (flag) {
			this.fade = Mth.clamp(this.fade + 0.02F, 0.0F, 0.3F);
		} else {
			this.fade = Mth.clamp(this.fade - 0.04F, 0.0F, 1.0F);
		}
	}

	@Nullable
	public static <T> SkillTreeUI<T> create(Minecraft minecraft, Screen screen, SkillNode<T> rootNode) {
		Optional<SkillDisplayInfo> optional = rootNode.display();
		if (optional.isEmpty()) {
			return null;
		}
		else {
			return new SkillTreeUI<>(minecraft, screen, rootNode, optional.get());
		}
	}

//	public void scroll(double dragX, double dragY) {
//		if (this.maxX - this.minX > 234) {
//			this.scrollX = Mth.clamp(this.scrollX + dragX, (double)(-(this.maxX - 234)), 0.0);
//		}
//
//		if (this.maxY - this.minY > 113) {
//			this.scrollY = Mth.clamp(this.scrollY + dragY, (double)(-(this.maxY - 113)), 0.0);
//		}
//	}

	public void addNode(SkillNode<T> node) {
		Optional<SkillDisplayInfo> optional = node.display();
		if (!optional.isEmpty()) {
			SkillNodeUI<T> skillWidget = new SkillNodeUI<T>(this, this.minecraft, node, optional.get());
			this.addWidget(skillWidget, node.holder());
		}
	}

	private void addWidget(SkillNodeUI<T> widget, SkillHolder<T> value) {
		this.widgets.put(value, widget);
		int i = widget.getX();
		int j = i + 28;
		int k = widget.getY();
		int l = k + 27;
		this.minX = Math.min(this.minX, i);
		this.maxX = Math.max(this.maxX, j);
		this.minY = Math.min(this.minY, k);
		this.maxY = Math.max(this.maxY, l);

		for (SkillNodeUI<T> widgets : this.widgets.values()) {
			widgets.attachToParent();
		}
	}

	@Nullable
	public SkillNodeUI<T> getWidget(SkillHolder<T> holder) {
		return this.widgets.get(holder);
	}

	public Screen getScreen() {
		return this.screen;
	}
	
	public int getWidth() {
		return maxX - minX;
	}
	
	public int getHeight() {
		return maxY - minY;
	}
}
