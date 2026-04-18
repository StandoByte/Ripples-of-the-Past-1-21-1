package com.github.standobyte.jojo.client.ui.screen_widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;

public class ScrolleableButtonList extends ContainerObjectSelectionList<ScrolleableButtonList.EntryWithButtons> {
	public int scrollbarPosOffset = 0;
	
	public ScrolleableButtonList(Minecraft minecraft, int x, int y, 
			int width, int height, int itemHeight) {
		super(minecraft, width, height, y, itemHeight);
		setX(x);
	}

	@Override
    protected int getScrollbarPosition() {
    	return getX() + getRowWidth() - 6 + scrollbarPosOffset;
    }

	@Override
	public int getRowWidth() {
		return width;
	}

	// what's the point of making it protected...
	@Override
	public int addEntry(EntryWithButtons entry) {
		return super.addEntry(entry);
	}

	public static class EntryWithButtons extends ContainerObjectSelectionList.Entry<EntryWithButtons> {
		protected List<AbstractWidget> children = new ArrayList<>();
		protected List<Object> renderables = new ArrayList<>();
		protected List<EntryWidgetOffset> offsets = new ArrayList<>();

		public EntryWithButtons() {}
		
		public <T extends Renderable & LayoutElement> EntryWithButtons addRenderable(
				T renderable, int xOffset, int yOffset) {
			renderables.add(renderable);
			offsets.add(new EntryWidgetOffset(xOffset, yOffset));
			return this;
		}
		
		public EntryWithButtons add(AbstractWidget widget, int xOffset, int yOffset) {
			children.add(widget);
			return addRenderable(widget, xOffset, yOffset);
		}

		@Override
		public void render(GuiGraphics guiGraphics, int index, 
				int top, int left, int width, int height, 
				int mouseX, int mouseY, boolean hovering, float partialTick) {
			for (int i = 0; i < renderables.size(); i++) {
				Object renderable = renderables.get(i);
				EntryWidgetOffset offset = offsets.get(i);
				int x = left + offset.xOffset;
				int y = top + offset.yOffset;
				((LayoutElement) renderable).setPosition(x, y);
				((Renderable) renderable).render(guiGraphics, mouseX, mouseY, partialTick);
			}
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return children;
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return children;
		}
		
		// not making it a record to keep the fields mutable just in case
		protected static class EntryWidgetOffset {
			protected int xOffset;
			protected int yOffset;
			
			public EntryWidgetOffset(int xOffset, int yOffset) {
				this.xOffset = xOffset;
				this.yOffset = yOffset;
			}
		}

	}
	
	public abstract static class Renderable2 implements Renderable, LayoutElement {
		protected int x;
		protected int y;
		protected int width;
		protected int height;
		
		public Renderable2(int width, int height) {
			this.width = width;
			this.height = height;
		}
		
		@Override public void setX(int x) { this.x = x; }
		@Override public void setY(int y) { this.y = y; }
		@Override public int getX() { return x; }
		@Override public int getY() { return y; }
		@Override public int getWidth() { return width; }
		@Override public int getHeight() { return height; }
		@Override public void visitWidgets(Consumer<AbstractWidget> consumer) {}
	}

}
