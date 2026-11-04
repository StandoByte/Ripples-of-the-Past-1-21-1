package com.github.standobyte.jojo.client.ui.screen_widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.github.standobyte.jojo.client.ui.screen_widgets.ScrolleableButtonList.EntryWithButtons.EntryWidgetOffset;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;

public class ScrolleableButtonList extends ContainerObjectSelectionList<ScrolleableButtonList.EntryWithButtons> {
	public static final int SCROLL_BAR_WIDTH = AbstractSelectionList.SCROLLBAR_WIDTH;
	public int scrollbarPosOffset = 0;
	public boolean disableBackground = false;
	public boolean disableSeparators = false;
	public List<EntryWidgetOffset> alwaysRender = new ArrayList<>();
	
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
	
	/** For renderable objects that span more than one row (for example, the Stand stats circle) */
	public <T extends Renderable & LayoutElement> void addAlwaysRenderedObject(
			T object, int xOffset, int yOffset) {
		this.alwaysRender.add(new EntryWidgetOffset(object, xOffset, yOffset));
	}

	@Override
	public int getMaxPosition() {
		return super.getMaxPosition();
	}
	
	@Override
	public int getRowTop(int index) {
		return super.getRowTop(index);
	}

	@Override
	protected void renderListBackground(GuiGraphics guiGraphics) {
		if (!disableBackground) {
			super.renderListBackground(guiGraphics);
		}
	}
	
	@Override
	protected void renderListSeparators(GuiGraphics guiGraphics) {
		if (!disableSeparators) {
			super.renderListSeparators(guiGraphics);
		}
	}


	@Override
    protected void renderListItems(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int left = this.getRowLeft();
        int top = this.getY() + 4 - (int)this.getScrollAmount() + this.headerHeight;
        
		for (EntryWidgetOffset objectWithOffset : alwaysRender) {
			Object renderable = objectWithOffset.renderable;
			int x = left + objectWithOffset.xOffset;
			int y = top + objectWithOffset.yOffset;
			((LayoutElement) renderable).setPosition(x, y);
			((Renderable) renderable).render(guiGraphics, mouseX, mouseY, partialTick);
		}
		
		super.renderListItems(guiGraphics, mouseX, mouseY, partialTick);
    }
	
	@Override
	protected boolean isValidMouseClick(int button) {
		return true;
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return active && visible && super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (!active || !visible) return false;
		
		if (isMouseOver(mouseX, mouseY)) {
			ScrolleableButtonList.EntryWithButtons entry = getEntryAtPosition(mouseX, mouseY);
			if (entry != null && entry.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
				return true;
			}
		}
		
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}


	public static class EntryWithButtons extends ContainerObjectSelectionList.Entry<EntryWithButtons> {
		protected List<AbstractWidget> children = new ArrayList<>();
		protected List<EntryWidgetOffset> renderables = new ArrayList<>();

		public EntryWithButtons() {}
		
		public <T extends Renderable & LayoutElement> EntryWithButtons addRenderable(
				T renderable, int xOffset, int yOffset) {
			renderables.add(new EntryWidgetOffset(renderable, xOffset, yOffset));
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
			for (EntryWidgetOffset objectWithOffset : renderables) {
				Object renderable = objectWithOffset.renderable;
				int x = left + objectWithOffset.xOffset;
				int y = top + objectWithOffset.yOffset;
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
			protected Object renderable;
			protected int xOffset;
			protected int yOffset;
			
			public EntryWidgetOffset(Object renderable, int xOffset, int yOffset) {
				this.renderable = renderable;
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
