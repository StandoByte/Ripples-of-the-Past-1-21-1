package com.github.standobyte.jojo.client.ui.skill_tree.more_complex;

import com.github.standobyte.jojo.client.ui.skill_tree.SkillNodeUI;
import com.github.standobyte.jojo.client.ui.utils.Orientation;

import net.minecraft.client.gui.GuiGraphics;

public class NodeConnectivityLineUI {
	public SkillNodeUI<?> parent;
	public SkillNodeUI<?> child;
	public Orientation direction;
	
	public NodeConnectivityLineUI(SkillNodeUI<?> parent, SkillNodeUI<?> child, Orientation direction) {
		this.parent = parent;
		this.child = child;
		this.direction = direction;
	}
	
	public void draw(GuiGraphics guiGraphics, int x, int y, boolean dropShadow) {
		int color = dropShadow ? 0xFF000000 : 0xFFFFFFFF;
		switch (direction) {
			case HORIZONTAL -> {
				int xStart = x + parent.x + 16;
				int yStart = y + parent.y + 13;
				int xEnd = x + child.x + 16;
				int yEnd = y + child.y + 13;
				int xTurn = xStart + (child.x - parent.x) / 2 + (child.x < parent.x ? -1 : 0);
				
				if (dropShadow) {
					int line1_x0 = Math.min(xTurn, xStart) - 1;
					int line1_x1 = Math.max(xTurn, xStart) + 1;
					int line2_x0 = Math.min(xEnd, xTurn) - 1;
					int line2_x1 = Math.max(xEnd, xTurn) + 1;
					int line3_y0 = Math.min(yEnd, yStart) - 1;
					int line3_y1 = Math.max(yEnd, yStart) + 1;
					
					guiGraphics.hLine(line1_x0, line1_x1, yStart - 1, color);
					guiGraphics.hLine(line1_x0, line1_x1, yStart + 1, color);
					guiGraphics.hLine(line2_x0, line2_x1, yEnd - 1, color);
					guiGraphics.hLine(line2_x0, line2_x1, yEnd + 1, color);
					guiGraphics.vLine(xTurn - 1, line3_y0, line3_y1, color);
					guiGraphics.vLine(xTurn + 1, line3_y0, line3_y1, color);
				} else {
					guiGraphics.hLine(xTurn, xStart, yStart, color);
					guiGraphics.hLine(xEnd, xTurn, yEnd, color);
					guiGraphics.vLine(xTurn, yEnd, yStart, color);
				}
			}
			case VERTICAL -> {
				int xStart = x + parent.x + 16;
				int yStart = y + parent.y + 13;
				int xEnd = x + child.x + 16;
				int yEnd = y + child.y + 13;
				int yTurn = yStart + (child.y - parent.y) / 2 + (child.y < parent.y ? -1 : 0);
				
				if (dropShadow) {
					int line1_y0 = Math.min(yTurn, yStart) - 1;
					int line1_y1 = Math.max(yTurn, yStart) + 1;
					int line2_y0 = Math.min(yEnd, yTurn) - 1;
					int line2_y1 = Math.max(yEnd, yTurn) + 1;
					int line3_x0 = Math.min(xStart, xEnd) - 1;
					int line3_x1 = Math.max(xStart, xEnd) + 1;
					
					guiGraphics.vLine(xStart - 1, line1_y0, line1_y1, color);
					guiGraphics.vLine(xStart + 1, line1_y0, line1_y1, color);
					guiGraphics.vLine(xEnd - 1, line2_y0, line2_y1, color);
					guiGraphics.vLine(xEnd + 1, line2_y0, line2_y1, color);
					guiGraphics.hLine(line3_x0, line3_x1, yTurn - 1, color);
					guiGraphics.hLine(line3_x0, line3_x1, yTurn + 1, color);
				} else {
					guiGraphics.vLine(xStart, yTurn, yStart, color);
					guiGraphics.vLine(xEnd, yEnd, yTurn, color);
					guiGraphics.hLine(xStart, xEnd, yTurn, color);
				}
			}
		}
	}
	
}
