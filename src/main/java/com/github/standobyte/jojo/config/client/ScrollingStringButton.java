package com.github.standobyte.jojo.config.client;

import com.github.standobyte.jojo.client.ui.utils.Alignment;
import com.github.standobyte.v1_21_4_stuff.GuiScissor;

import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

// yes, the modern versions have scrolling text too, but this one renders full text when the button is hovered
public class ScrollingStringButton extends Button {
    private Alignment alignment = Alignment.LEFT;
    
    public ScrollingStringButton(int pX, int pY, int pWidth, int pHeight, Component pMessage,
    		Button.OnPress pOnPress) {
        super(new Button.Builder(pMessage, pOnPress).bounds(pX, pY, pWidth, pHeight));
    }
    
    public ScrollingStringButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, 
    		Button.OnPress pOnPress, Tooltip pOnTooltip) {
        super(new Button.Builder(pMessage, pOnPress).bounds(pX, pY, pWidth, pHeight).tooltip(pOnTooltip));
    }
    
    public ScrollingStringButton setAlignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    @Override
    protected void renderScrollingString(GuiGraphics guiGraphics, Font font, int width, int color) {
    	int x0 = this.getX() + width;
    	int x1 = this.getX() + this.getWidth() - width;
    	int y0 = this.getY();
    	int y1 = this.getY() + this.getHeight();

    	Component text = getMessage();
        int textWidth = font.width(text);
        int y = (y0 + y1 - 9) / 2 + 1;
        int buttonWidth = x1 - x0;
        if (textWidth > buttonWidth && isHovered()) {
            switch (alignment) {
            	case LEFT -> guiGraphics.drawString(font, text, x0, y, color);
            	case CENTER -> guiGraphics.drawString(font, text, (x0 + x1 - textWidth) / 2, y, color);
            	case RIGHT -> guiGraphics.drawString(font, text, x1 - textWidth, y, color);
            }
        }
        else {
        	_renderScrollingString(guiGraphics, font, text, x0, y0, x1, y1, color, categoryOpenedTimestampSoThatScrollingDoesntSuck);
        }
    }

    public static void _renderScrollingString(GuiGraphics guiGraphics, Font font, 
    		Component text, int minX, int minY, int maxX, int maxY, 
    		int color, long startingTime) {
    	_renderScrollingString(guiGraphics, font, 
    			text, Alignment.CENTER, minX, minY, maxX, maxY, 
    			color, true, startingTime);
    }

    public static void _renderScrollingString(GuiGraphics guiGraphics, Font font, 
    		Component text, Alignment alignment, int minX, int minY, int maxX, int maxY, 
    		int color, boolean dropShadow, long startingTime) {
    	int textWidth = font.width(text);
    	int y = (minY + maxY - 9) / 2 + 1;
    	int maxWidth = maxX - minX;
    	if (textWidth > maxWidth) {
    		int l = textWidth - maxWidth;
    		double d0 = (double)(Util.getMillis() - startingTime) / 1000.0;
    		double d1 = Math.max((double)l * 0.5, 3.0);
    		double d2 = 1 - (Math.sin((Math.PI / 2) * Math.cos((Math.PI * 2) * d0 / d1)) / 2.0 + 0.5);
    		double d3 = Mth.lerp(d2, 0.0, (double)l);
    		GuiScissor.enableScissor(guiGraphics, minX, minY, maxX, maxY);
    		guiGraphics.drawString(font, text, minX - (int)d3, y, color, dropShadow);
    		guiGraphics.disableScissor();
    	} else {
    		int x = switch (alignment) {
    			case LEFT -> minX;
    			case CENTER -> Mth.clamp((minX + maxX) / 2, minX + textWidth / 2, maxX - textWidth / 2) - textWidth / 2;
    			case RIGHT -> maxX - textWidth;
    		};
    		guiGraphics.drawString(font, text, x, y, color, dropShadow);
    	}
    }
    
    
    static long categoryOpenedTimestampSoThatScrollingDoesntSuck;
    
    public static void onScreenOpened() {
		categoryOpenedTimestampSoThatScrollingDoesntSuck = Util.getMillis();
    }

}
