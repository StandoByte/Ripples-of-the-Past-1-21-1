package com.github.standobyte.jojo.client.ui.screen_widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.github.standobyte.jojo.client.ui.screen_widgets.ScrolleableButtonList.EntryWithButtons.EntryWidgetOffset;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ScrolleableButtonList extends ContainerObjectSelectionList<ScrolleableButtonList.EntryWithButtons> {
	public static final int SCROLL_BAR_WIDTH = AbstractSelectionList.SCROLLBAR_WIDTH;
	public static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("widget/scroller");
	public static final ResourceLocation SCROLLER_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("widget/scroller_background");

	public int scrollbarPosOffset = 0;
	public boolean disableBackground = false;
	public boolean disableSeparators = false;
	public ResourceLocation scrollerSprite = SCROLLER_SPRITE;
	@Nullable public ResourceLocation scrollerBGSprite = SCROLLER_BACKGROUND_SPRITE;
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
	
	public void scrollToEntry(int index, int minScrollOffset) {
		double amountMax = index * itemHeight;
		double amountMin = amountMax - getHeight() + itemHeight + minScrollOffset;
		double curAmount = getScrollAmount();
		setScrollAmount(Mth.clamp(curAmount, amountMin, amountMax));
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

	@Override
	public void clearEntries() {
		super.clearEntries();
	}

	@Override
	public void replaceEntries(Collection<EntryWithButtons> entries) {
		super.replaceEntries(entries);
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

	@Nullable
	public EntryWithButtons getEntryAtPos(double mouseX, double mouseY) {
		return super.getEntryAtPosition(mouseX, mouseY);
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return super.isMouseOver(mouseX, mouseY) || isMouseAtScrollBar(mouseX, mouseY);
	}
	
	public boolean isMouseAtScrollBar(double mouseX, double mouseY) {
		return mouseY >= getY()
				&& mouseY <= getBottom()
				&& mouseX >= getScrollbarPosition()
				&& mouseX <= getScrollbarPosition() + SCROLLBAR_WIDTH;
	}
	
	
	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		hovered = isMouseOver(mouseX, mouseY) ? getEntryAtPosition(mouseX, mouseY) : null;
		renderListBackground(guiGraphics);
		enableScissor(guiGraphics);
		if (renderHeader) {
			int x = getRowLeft();
			int y = getY() + 4 - (int) getScrollAmount();
			renderHeader(guiGraphics, x, y);
		}

		renderListItems(guiGraphics, mouseX, mouseY, partialTick);
		guiGraphics.disableScissor();
		renderListSeparators(guiGraphics);
		renderScrollBar(guiGraphics);

		renderDecorations(guiGraphics, mouseX, mouseY);
		RenderSystem.disableBlend();
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
	
	protected void renderScrollBar(GuiGraphics guiGraphics) {
		if (scrollbarVisible()) {
			int x = getScrollbarPosition();
			int barHeight = (int)((float)(height * height) / (float) getMaxPosition());
			barHeight = Mth.clamp(barHeight, 32, height - 8);
			int barY = (int) getScrollAmount() * (height - barHeight) / getMaxScroll() + getY();
			if (barY < getY()) {
				barY = getY();
			}

			RenderSystem.enableBlend();
			if (scrollerBGSprite != null) {
				guiGraphics.blitSprite(scrollerBGSprite, x, getY(), SCROLL_BAR_WIDTH, getHeight());
			}
			guiGraphics.blitSprite(scrollerSprite, x, barY, SCROLL_BAR_WIDTH, barHeight);
			RenderSystem.disableBlend();
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
