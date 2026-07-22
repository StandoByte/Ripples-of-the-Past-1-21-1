package com.github.standobyte.jojo.mechanics.clothes.client.ui;

import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.clothes.EntityClothesInventory;
import com.github.standobyte.jojo.mechanics.clothes.container.PlayerClothesMenu;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.StoryCharacter;
import com.github.standobyte.jojo.subsystems.StoryPart;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;

// XXX (clothes) when the ui is opened, seamlessly move the mouse cursor to the same position
// XXX (clothes) interact with nearby mannequins
public class PlayerClothesScreen extends EffectRenderingInventoryScreen<PlayerClothesMenu> {
	public static final ResourceLocation SCREEN_TEXTURE = JojoMod.resLoc("textures/gui/container/clothes/inventory_clothes.png");
	private float xMouse;
	private float yMouse;
//	private final RecipeBookComponent recipeBookComponent = new RecipeBookComponent();
	private boolean widthTooNarrow;
	private boolean buttonClicked;

	public PlayerClothesScreen(PlayerClothesMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.titleLabelX = 97;
	}

//	@Override
//	public void containerTick() {
//		this.recipeBookComponent.tick();
//	}

	@Override
	protected void init() {
		super.init();
		this.imageHeight += 12;
		this.widthTooNarrow = this.width < 379;
//		this.recipeBookComponent.init(this.width, this.height, this.minecraft, this.widthTooNarrow, this.menu);
//		this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
//		this.addRenderableWidget(
//				new ImageButton(this.leftPos + 104, this.height / 2 - 22, 20, 18, RecipeBookComponent.RECIPE_BUTTON_SPRITES, p_313434_ -> {
//					this.recipeBookComponent.toggleVisibility();
//					this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
//					p_313434_.setPosition(this.leftPos + 104, this.height / 2 - 22);
//					this.buttonClicked = true;
//				}));
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		EntityClothesInventory clothes = menu.clothesInventory;
		Holder<StoryCharacter> character = clothes.getCharacter();
		if (character != null) {
			Holder<StoryPart> storyPart = clothes.getStoryPart();
			Component characterName = character.value().getName(true);
			if (storyPart != null) {
				characterName = characterName.copy().append(storyPart.value().getPartIconAsText());
			}
			int x = font.width(characterName) / 2;
			int y = -6;
			guiGraphics.drawString(font, Component.translatable("jojo_ripples.menu.player.clothes.cur_character", characterName), x, y, 4210752, false);
		}
		
//		guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
//		guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
	}

	/**
	 * Renders the graphical user interface (GUI) element.
	 *
	 * @param guiGraphics the GuiGraphics object used for rendering.
	 * @param mouseX      the x-coordinate of the mouse cursor.
	 * @param mouseY      the y-coordinate of the mouse cursor.
	 * @param partialTick the partial tick time.
	 */
	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
//		if (this.recipeBookComponent.isVisible() && this.widthTooNarrow) {
//			this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
//			this.recipeBookComponent.render(guiGraphics, mouseX, mouseY, partialTick);
//		} else {
			super.render(guiGraphics, mouseX, mouseY, partialTick);
//			this.recipeBookComponent.render(guiGraphics, mouseX, mouseY, partialTick);
//			this.recipeBookComponent.renderGhostRecipe(guiGraphics, this.leftPos, this.topPos, false, partialTick);
//		}

		this.renderTooltip(guiGraphics, mouseX, mouseY);
//		this.recipeBookComponent.renderTooltip(guiGraphics, this.leftPos, this.topPos, mouseX, mouseY);
		this.xMouse = (float)mouseX;
		this.yMouse = (float)mouseY;
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
		int i = this.leftPos;
		int j = this.topPos;
		guiGraphics.blit(SCREEN_TEXTURE, i, j - 12, 0, 0, this.imageWidth, this.imageHeight);
		renderEntityInInventoryFollowsMouse(guiGraphics, i + 26, j + 8, i + 75, j + 78, 30, 0.0625F, this.xMouse, this.yMouse, this.minecraft.player);
	}

	public static void renderEntityInInventoryFollowsMouse(
			GuiGraphics guiGraphics,
			int x1,
			int y1,
			int x2,
			int y2,
			int scale,
			float yOffset,
			float mouseX,
			float mouseY,
			LivingEntity entity
			) {
		float f = (float)(x1 + x2) / 2.0F;
		float f1 = (float)(y1 + y2) / 2.0F;
		float f2 = (float)Math.atan((double)((f - mouseX) / 40.0F));
		float f3 = (float)Math.atan((double)((f1 - mouseY) / 40.0F));
		// Forge: Allow passing in direct angle components instead of mouse position
		InventoryScreen.renderEntityInInventoryFollowsAngle(guiGraphics, x1, y1, x2, y2, scale, yOffset, f2, f3, entity);
	}

//	/**
//	 * Called when a keyboard key is pressed within the GUI element.
//	 * <p>
//	 * @return {@code true} if the event is consumed, {@code false} otherwise.
//	 *
//	 * @param keyCode   the key code of the pressed key.
//	 * @param scanCode  the scan code of the pressed key.
//	 * @param modifiers the keyboard modifiers.
//	 */
//	@Override
//	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
//		return this.recipeBookComponent.keyPressed(keyCode, scanCode, modifiers) ? true : super.keyPressed(keyCode, scanCode, modifiers);
//	}
//
//	/**
//	 * Called when a character is typed within the GUI element.
//	 * <p>
//	 * @return {@code true} if the event is consumed, {@code false} otherwise.
//	 *
//	 * @param codePoint the code point of the typed character.
//	 * @param modifiers the keyboard modifiers.
//	 */
//	@Override
//	public boolean charTyped(char codePoint, int modifiers) {
//		return this.recipeBookComponent.charTyped(codePoint, modifiers) ? true : super.charTyped(codePoint, modifiers);
//	}
//
//	@Override
//	protected boolean isHovering(int x, int y, int width, int height, double mouseX, double mouseY) {
//		return (!this.widthTooNarrow || !this.recipeBookComponent.isVisible()) && super.isHovering(x, y, width, height, mouseX, mouseY);
//	}
//
//	/**
//	 * Called when a mouse button is clicked within the GUI element.
//	 * <p>
//	 * @return {@code true} if the event is consumed, {@code false} otherwise.
//	 *
//	 * @param mouseX the X coordinate of the mouse.
//	 * @param mouseY the Y coordinate of the mouse.
//	 * @param button the button that was clicked.
//	 */
//	@Override
//	public boolean mouseClicked(double mouseX, double mouseY, int button) {
//		if (this.recipeBookComponent.mouseClicked(mouseX, mouseY, button)) {
//			this.setFocused(this.recipeBookComponent);
//			return true;
//		} else {
//			return this.widthTooNarrow && this.recipeBookComponent.isVisible() ? false : super.mouseClicked(mouseX, mouseY, button);
//		}
//	}
//
//	/**
//	 * Called when a mouse button is released within the GUI element.
//	 * <p>
//	 * @return {@code true} if the event is consumed, {@code false} otherwise.
//	 *
//	 * @param mouseX the X coordinate of the mouse.
//	 * @param mouseY the Y coordinate of the mouse.
//	 * @param button the button that was released.
//	 */
//	@Override
//	public boolean mouseReleased(double mouseX, double mouseY, int button) {
//		if (this.buttonClicked) {
//			this.buttonClicked = false;
//			return true;
//		} else {
//			return super.mouseReleased(mouseX, mouseY, button);
//		}
//	}
//
//	@Override
//	protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton) {
//		boolean flag = mouseX < (double)guiLeft
//				|| mouseY < (double)guiTop
//				|| mouseX >= (double)(guiLeft + this.imageWidth)
//				|| mouseY >= (double)(guiTop + this.imageHeight);
//				return this.recipeBookComponent.hasClickedOutside(mouseX, mouseY, this.leftPos, this.topPos, this.imageWidth, this.imageHeight, mouseButton) && flag;
//	}
//
//	/**
//	 * Called when the mouse is clicked over a slot or outside the gui.
//	 */
//	@Override
//	protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
//		super.slotClicked(slot, slotId, mouseButton, type);
//		this.recipeBookComponent.slotClicked(slot);
//	}

}
