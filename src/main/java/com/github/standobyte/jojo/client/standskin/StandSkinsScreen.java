package com.github.standobyte.jojo.client.standskin;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.jetbrains.annotations.ApiStatus;
import org.joml.Quaternionf;
import org.lwjgl.glfw.GLFW;

import com.github.standobyte.jojo.client.ClientProxy;
import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityRenderState;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityRenderer;
import com.github.standobyte.jojo.client.input.InputHandler;
import com.github.standobyte.jojo.client.ui.jojomenu.IJojoMenuScreen;
import com.github.standobyte.jojo.client.ui.jojomenu.JojoMenuTabs;
import com.github.standobyte.jojo.client.ui.jojomenu.StandInfoScreen;
import com.github.standobyte.jojo.client.ui.jojomenu.Tab;
import com.github.standobyte.jojo.client.ui.jojomenu.TabCategory;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.core.packet.fromclient.ClSetStandSkinPacket;
import com.github.standobyte.jojo.powersystem.PowerClass;
import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.powersystem.standpower.entity.EntityStandType;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.github.standobyte.jojo.util.CommonEnums.Direction2D;
import com.github.standobyte.jojo.util.MathUtil;
import com.github.standobyte.v1_21_4_stuff.GuiScissor;
import com.google.common.collect.Streams;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;

public class StandSkinsScreen extends Screen implements IJojoMenuScreen {
	private static final ResourceLocation TEXTURE_BG = ResourceLocation.fromNamespaceAndPath(
			JojoMod.MOD_ID, "textures/gui/paper_style/stand_skins_bg.png");
	private static final ResourceLocation TEXTURE_ELEMENTS = ResourceLocation.fromNamespaceAndPath(
			JojoMod.MOD_ID, "textures/gui/paper_style/stand_skins_ui.png");
	
	private static class SkinBoxes {
		static GuiIcon[] TOP = new GuiIcon[] {
				new GuiIcon(TEXTURE_ELEMENTS,	6,		19,		64, 90,		512, 512),
				new GuiIcon(TEXTURE_ELEMENTS,	75,		19,		64, 95,		512, 512),
				new GuiIcon(TEXTURE_ELEMENTS,	144,	19,		64, 100,	512, 512),
		};
		static GuiIcon[] EVEN = new GuiIcon[] {
				new GuiIcon(TEXTURE_ELEMENTS,	6,		112,	64, 106,	512, 512),
				new GuiIcon(TEXTURE_ELEMENTS,	75,		117,	64, 99,		512, 512),
				new GuiIcon(TEXTURE_ELEMENTS,	144,	122,	64, 92,		512, 512),
		};
		static GuiIcon[] ODD = new GuiIcon[] {
				new GuiIcon(TEXTURE_ELEMENTS,	6,		223,	64, 92,		512, 512),
				new GuiIcon(TEXTURE_ELEMENTS,	75,		221,	64, 99,		512, 512),
				new GuiIcon(TEXTURE_ELEMENTS,	144,	219,	64, 106,	512, 512),
		};
		static GuiIcon[] EVEN_BOTTOM = new GuiIcon[] {
				new GuiIcon(TEXTURE_ELEMENTS,	213,	112,	64, 106,	512, 512),
				new GuiIcon(TEXTURE_ELEMENTS,	282,	117,	64, 101,	512, 512),
				new GuiIcon(TEXTURE_ELEMENTS,	351,	122,	64, 96,		512, 512),
		};
		static GuiIcon[] ODD_BOTTOM = new GuiIcon[] {
				new GuiIcon(TEXTURE_ELEMENTS,	213,	223,	64, 102,	512, 512),
				new GuiIcon(TEXTURE_ELEMENTS,	282,	221,	64, 104,	512, 512),
				new GuiIcon(TEXTURE_ELEMENTS,	351,	219,	64, 106,	512, 512),
		};
	}
	
	private static final int WINDOW_WIDTH = IJojoMenuScreen.DEFAULT_WIDTH;
	private static final int WINDOW_HEIGHT = IJojoMenuScreen.DEFAULT_HEIGHT;
	private static final int WINDOW_INSIDE_X = 7;
	private static final int WINDOW_INSIDE_WIDTH = WINDOW_WIDTH - WINDOW_INSIDE_X - 22;
	private static final int WINDOW_INSIDE_Y = 20;
	private static final int WINDOW_INSIDE_HEIGHT = WINDOW_HEIGHT - WINDOW_INSIDE_Y - 7;
	
	private static final int SKINS_IN_ROW = 3;
	
	private static ResourceLocation latestStand = null;
	private static int latestScroll;
	
	private StandPower standCap;
	private List<SkinView> skins;
	private int tickCount = 0;
	private int scroll;
	private List<SkinView> skinsVisible;
	
	@Nullable
	private SkinFullView skinFullView;
	
	public StandSkinsScreen(StandPower power) {
		super(CommonComponents.EMPTY);
		setStandCap(power);
	}
	
	public static void openScreen() {
		PowerClass.STAND.getOptional(ClientProxy.getClientPlayer()).ifPresent(playerStand -> {
			if (playerStand.hasPower()) {
				StandSkinsScreen screen = new StandSkinsScreen(playerStand);
				Minecraft.getInstance().setScreen(screen);
			}
		});
	}
	
	private void setStandCap(StandPower standCap) {
		this.standCap = standCap;
		StandType standType = standCap.getPowerType();
		List<StandSkin> skins = StandSkinsLoader.getInstance().getStandSkinsView(standType.getId());
		int rowsCount = (skins.size() - 1) / SKINS_IN_ROW + 1;
		this.skins = Streams.mapWithIndex(skins.stream(), (skin, index) -> {
					int row = (int) (index / SKINS_IN_ROW);
					int column = (int) (index % SKINS_IN_ROW);
					int rowHeight = SkinView.boxHeight + 12;
					int x = column * (SkinView.boxWidth + 4);
					int y = row * rowHeight;
					int yOffset = 0;
					if (row % 2 == 1) {
						if (column == 0) yOffset = -10;
						else if (column == 1) yOffset = -5;
					}
					else if (row > 0) {
						if (column == 0) yOffset = -2;
						else if (column == 1) yOffset = -4;
						else if (column == 2) yOffset = -6;
					}
					return standType.makeSkinUIElement(skin, this, x, y + yOffset, y, row, column, row == rowsCount - 1);
				})
				.collect(Collectors.toList());
		setScroll(0);
	}
	
	@Override
	public void init() {
	}
	
	@Override
	public TabCategory getTabCategory() {
		return JojoMenuTabs.CATEGORY_STAND;
	}
	
	@Override
	public Tab getTab() {
		return JojoMenuTabs.STAND_SKINS;
	}
	
	@Override
	public void tick() {
		tickCount++;
	}

	@Override
	public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		if (!standCap.hasPower()) {
			onClose();
			return;
		}
		partialTick = ClientUtil.partialTick(Minecraft.getInstance().getTimer(), true);
		
		renderBackground(gui, mouseX, mouseY, partialTick);
		renderWindow(gui);
		renderContents(gui, mouseX, mouseY, partialTick);
		
		renderTabs(gui, this);
		renderTabTooltip(gui, this, mouseX, mouseY);
		
		for (Renderable renderable : renderables) {
			renderable.render(gui, mouseX, mouseY, partialTick);
		}
	}
	
	private boolean isSkinSelected(StandSkin skin) {
		StandInstance stand = standCap.getStandInstance().get();
		return stand.getSelectedSkin().equals(skin.nonDefaultId);
	}
	
//	private void renderBgPattern(GuiGraphics matrixStack) {
//		RenderSystem.pushMatrix();
//		RenderSystem.translatef(getWindowX() + 4, getWindowY() + 4, 0);
//		minecraft.getTextureManager().bind(TEXTURE_BG);
//		
//		int x = getWindowX() + WINDOW_INSIDE_X;
//		int y = getWindowY() + WINDOW_INSIDE_Y;
//		ClientUtil.enableGlScissor(x, y, WINDOW_INSIDE_WIDTH, WINDOW_INSIDE_HEIGHT);
//		int l = -scroll % 16;
//		for (int i1 = -1; i1 <= 12; ++i1) {
//			for (int j1 = -1; j1 <= 11; ++j1) {
//				blit(matrixStack, 5 + 16 * i1, l + 16 * j1, 0.0F, 0.0F, 16, 16, 16, 16);
//			}
//		}
//		ClientUtil.disableGlScissor();
//		
//		RenderSystem.popMatrix();
//	}

	private void renderWindow(GuiGraphics gui) {
		int x = getWindowX(this);
		int y = getWindowY(this);
		int width = getWindowWidth();
		int height = getWindowHeight();
		BlitFloat.blit(gui.pose(), Minecraft.getInstance(), TEXTURE_BG, 
				x, y, width, height, 0, 
				0, 0, width, height, 256, 256, 
				BlitFloat.NO_TINT);
	}
	
	private void renderContents(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		int x = getWindowX() + WINDOW_INSIDE_X;
		int y = getWindowY() + WINDOW_INSIDE_Y;
		float ticks = tickCount + partialTick;
		gui.pose().pushPose();
		gui.pose().translate(x, y, 0);
		GuiScissor.enableScissor(gui, 0, 0, WINDOW_INSIDE_WIDTH, WINDOW_INSIDE_HEIGHT);
		if (skinFullView != null) {
			skinFullView.render(gui, mouseX, mouseY, ticks);
		}
		else {
			gui.pose().translate(0, -scroll, 0);
			Optional<SkinView> hoveredSkin = getSkinAt(mouseX, mouseY);
			for (SkinView skin : skinsVisible) {
				skin.render(gui, mouseX, mouseY, ticks, 
						hoveredSkin.map(hovered -> skin == hovered).orElse(false));
			}

			for (SkinView skin : skinsVisible) {
				skin.renderAdditional(gui, mouseX, mouseY, ticks, 
						hoveredSkin.map(hovered -> skin == hovered).orElse(false));
			}
		}
		gui.disableScissor();
		gui.pose().popPose();
	}
	
	private Optional<SkinView> getSkinAt(int mouseX, int mouseY) {
		if (skinFullView != null) return Optional.empty();
		
		int x = mouseX - (getWindowX() + WINDOW_INSIDE_X);
		int y = mouseY - (getWindowY() + WINDOW_INSIDE_Y);
		if (
				x >= 0 && x <= WINDOW_INSIDE_WIDTH && 
				y >= 0 && y <= WINDOW_INSIDE_HEIGHT) {
			int yWithScroll = y + scroll;
			
			return skins.stream().filter(skin -> 
			x		   > skin.x && x		   <= skin.x + SkinView.boxWidth && 
			yWithScroll > skin.standY && yWithScroll <= skin.standY + SkinView.boxHeight)
					.findFirst();
		}
		else {
			return Optional.empty();
		}
	}
	
	private boolean isSkinBoxVisible(SkinView skin) {
		int y = skin.y - scroll;
		return y + SkinView.boxHeight >= 0 && y < WINDOW_INSIDE_HEIGHT;
	}
	
	private int getWindowX() { return (width - WINDOW_WIDTH) / 2; }
	private int getWindowY() { return (height - WINDOW_HEIGHT) / 2; }
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		if (clickTab(mouseX, mouseY, mouseButton, this)) return true;
		
		Optional<SkinView> hoveredBox = getSkinAt((int) mouseX, (int) mouseY);
		if (skinFullView == null) {
			switch (mouseButton) {
				case GLFW.GLFW_MOUSE_BUTTON_1:
					if (hoveredBox.isPresent()) {
						selectSkin(hoveredBox.get().skin);
						return true;
					}
					break;
				case GLFW.GLFW_MOUSE_BUTTON_2:
					return hoveredBox.map(skinBox -> {
						setFullViewSkin(skinBox);
						return true;
					}).orElse(false);
			}
		}
		
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double xOffset, double yOffset) {
		if (super.mouseScrolled(mouseX, mouseY, xOffset, yOffset)) {
			return true;
		}
		
		double scrollDelta = xOffset != 0 ? xOffset : yOffset;
		if (skinFullView != null) {
			return skinFullView.mouseScrolled(mouseX, mouseY, xOffset, yOffset);
		}
		else {
			addScroll((int) (-scrollDelta * 10));
		}
		
		return false;
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
		if (super.mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY)) {
			return true;
		}
		
		if (skinFullView != null) {
			return skinFullView.mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY);
		}
		
		return false;
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			if (skinFullView != null) {
				selectSkin(skinFullView.skin);
				return true;
			}
			else {
				
			}
		}
		
		else {
			Direction2D arrowKey = InputHandler.getArrowKey(keyCode);
			if (arrowKey != null) {
				if (skinFullView != null) {
					return skinFullView.keyPressed(arrowKey);
				}
				else {
					switch (arrowKey) {
						case RIGHT -> {}
						case DOWN -> {}
						case LEFT -> {}
						case UP -> {}
					}
					return true;
				}
			}
		}
		
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public void onClose() {
		if (skinFullView != null) {
			setFullViewSkin(null);
		}
		else {
			super.onClose();
		}
	}
	
	private void selectSkin(StandSkin skin) {
		PacketDistributor.sendToServer(new ClSetStandSkinPacket(skin.nonDefaultId, skin.standTypeId));
	}
	
	private void addScroll(int scroll) {
		setScroll(this.scroll + scroll);
	}
	
	private void setScroll(int scroll) {
		this.scroll = Mth.clamp(scroll, 0, getMaxScroll());
		this.skinsVisible = skins.stream()
				.filter(this::isSkinBoxVisible)
				.collect(Collectors.toList());
	}
	
	public int getMaxScroll() {
		int rowsCount = (skins.size() - 1) / SKINS_IN_ROW + 1;
		return Math.max((SkinView.boxHeight + 12) * rowsCount - 8 - WINDOW_INSIDE_HEIGHT, 0);
	}
	
	
	private void setFullViewSkin(@Nullable SkinView skin) {
		if (skin != null) {
			skinFullView = new SkinFullView(this, skin.skin, skin, skins.indexOf(skin));
		}
		else {
			skinFullView = null;
		}
	}
	
	public static class SkinView {
		public final StandType standType;
		public final StandSkin skin;
		public final StandSkinsScreen screen;
		public final int x;
		public final int y;
		public final int standY;
		public static final int boxWidth = 64;
		public static final int boxHeight = 88;
		
		public final int row;
		public final int column;
		public final boolean isBottomRow;
		
		public SkinView(StandType standType, StandSkin skin, StandSkinsScreen screen, int x, int y, int standY, int row, int column, boolean isBottomRow) {
			this.standType = standType;
			this.skin = skin;
			this.screen = screen;
			this.x = x;
			this.y = y;
			this.standY = standY;
			this.row = row;
			this.column = column;
			this.isBottomRow = isBottomRow;
		}
		
		@ApiStatus.NonExtendable
		public void render(GuiGraphics gui, int mouseX, int mouseY, float ticks, boolean isHovered) {
			GuiIcon[] skinBoxRow;
			if (row == 0)			skinBoxRow = SkinBoxes.TOP;
			else if (row % 2 == 1)	skinBoxRow = isBottomRow ? SkinBoxes.EVEN_BOTTOM : SkinBoxes.EVEN;
			else					skinBoxRow = isBottomRow ? SkinBoxes.ODD_BOTTOM : SkinBoxes.ODD;
			GuiIcon skinBox = skinBoxRow[column];

			int color = isHovered ? skin.getColor() : 0x80FFFFFF;
			skinBox.render(gui.pose(), x, y, color);

			renderStand(gui, mouseX, mouseY, ticks, isHovered, 
					x + boxWidth / 2, standY + boxHeight / 2 + 35, 30, 1, 
					0, 0, 0, 0);
		}
		
		public void renderStand(GuiGraphics gui, int mouseX, int mouseY, float ticks, boolean isHovered, 
				float posX, float posY, float scale, float scaleZoom, 
				float yRot, float xRot, float xOffsetRatio, float yOffsetRatio) {
			if (standType instanceof EntityStandType) {
				renderStandModel(gui, posX, posY, scale, scaleZoom, 
						yRot, xRot, xOffsetRatio, yOffsetRatio, 
						(EntityStandType) standType, skin, ticks, 0xFFFFFFFF);
			}
		}
		
		// XXX set it to one of the stand summon poses
		public void renderInStandInfo(GuiGraphics gui, int mouseX, int mouseY, float ticks, 
				float windowX, float windowY, float scale) {
			if (standType instanceof EntityStandType) {
				PoseStack poseStack = gui.pose();
				float angle = (float) -Math.PI / 12;
				
				windowY += StandInfoScreen.spHairTmpCrutch(standType);
				poseStack.pushPose();
				poseStack.translate(0, 0, -100);
				renderStandModel(gui, windowX + 60, windowY + 150, scale, 1, 
						(float) Math.PI + angle, 0, 0, 0, 
						(EntityStandType) standType, skin, ticks, 0xFFB0B0B0);
				
				poseStack.popPose();
				renderStandModel(gui, windowX + 45, windowY + 150, scale, 1, 
						angle, 0, 0, 0, 
						(EntityStandType) standType, skin, ticks, 0xFFFFFFFF);
			}
		}

		@ApiStatus.NonExtendable
		public void renderAdditional(GuiGraphics gui, int mouseX, int mouseY, 
				float ticks, boolean isHovered) {
			if (screen.isSkinSelected(skin)) {
				BlitFloat.blit(gui.pose(), Minecraft.getInstance(), TEXTURE_ELEMENTS, 
						x + 1, y + 2, 16, 16, 0, 
						213,   18,    16, 16, 512, 512, 
						BlitFloat.NO_TINT);
			}
		}
	}
	
	
	public static class SkinFullView {
		public final StandSkinsScreen screen;
		public final StandSkin skin;
		public final SkinView skinView;
		public final int skinIndex;
		public float yRot = 0;
		public float xRot = 0;
		public float scale = 1;
		public float xOffset = 0;
		public float yOffset = 0;
		
		public SkinFullView(StandSkinsScreen screen, StandSkin skin, SkinView skinView, int skinIndex) {
			this.screen = screen;
			this.skin = skin;
			this.skinView = skinView;
			this.skinIndex = skinIndex;
		}

		// TODO (stans skins UI) stand icon
		public void render(GuiGraphics gui, int mouseX, int mouseY, float ticks) {
//			ResourceLocation standIcon = JojoModUtil.makeTextureLocation("power", 
//					skinFullView.skin.standTypeId.getNamespace(), skinFullView.skin.standTypeId.getPath());
//			standIcon = skinFullView.skin.getRemappedResPath(standIcon).or(standIcon);
//			minecraft.getTextureManager().bind(standIcon);
//			blit(matrixStack, 4, 4, 0, 0, 16, 16, 16, 16);
			
			skinView.renderStand(gui, mouseX, mouseY, ticks, true, 
					WINDOW_WIDTH / 2 - 15, 180, 70, scale, 
					yRot * MathUtil.PI, xRot * MathUtil.PI, xOffset, yOffset);
			
			if (screen.isSkinSelected(skin)) {
				BlitFloat.blit(gui.pose(), Minecraft.getInstance(), TEXTURE_ELEMENTS, 
						WINDOW_INSIDE_WIDTH - 20, 4,  16, 16, 0, 
						213,                      18, 16, 16, 512, 512, 
						BlitFloat.NO_TINT);

			}

			RenderSystem.enableBlend();
		}
		
		public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
			switch (mouseButton) {
				case GLFW.GLFW_MOUSE_BUTTON_1:
					this.yRot -= dragX / WINDOW_INSIDE_WIDTH * 2;
					this.xRot -= dragY / WINDOW_INSIDE_WIDTH * 2;
					break;
				case GLFW.GLFW_MOUSE_BUTTON_2:
					this.xOffset += dragX;
					this.yOffset += dragY;
					break;
			}
			return true;
		}
		
		public boolean mouseScrolled(double mouseX, double mouseY, double xOffset, double yOffset) {
			this.yRot += xOffset * 0.05F;
			float prevScale = this.scale;
			this.scale = Mth.clamp(this.scale + (float) yOffset * 0.05f * this.scale, 1, 20);
			
			if (yOffset < 0) {
				float offsetRatio = this.scale / prevScale; // good enough
				this.xOffset *= offsetRatio;
				this.yOffset *= offsetRatio;
			}
			
			return true;
		}
		
		public boolean keyPressed(Direction2D arrowKey) {
			SkinFullView prev = this;
			List<SkinView> skins = screen.skins;
			switch (arrowKey) {
				case DOWN, RIGHT -> screen.setFullViewSkin(skins.get((this.skinIndex + 1) % skins.size()));
				case UP, LEFT -> screen.setFullViewSkin(skins.get((this.skinIndex - 1 + skins.size()) % skins.size()));
			}
			screen.skinFullView.copyFromPrev(prev);
			return true;
		}
		
		public void copyFromPrev(SkinFullView prev) {
			this.yRot = prev.yRot;
			this.xRot = prev.xRot;
			this.scale = prev.scale;
			this.xOffset = prev.xOffset;
			this.yOffset = prev.yOffset;
		}
	}

	public static <S extends StandEntityRenderState> void renderStandModel(GuiGraphics gui, float posX, float posY, 
			float scale, float scaleZoom, float yRot, float xRot, float xOffsetRatio, float yOffsetRatio, 
			EntityStandType standType, StandSkin standSkin, float ticks, int tint) {
		Quaternionf rotation = new Quaternionf()
				.rotateX(-xRot)
				.rotateY(-yRot);
		
		gui.pose().pushPose();
		gui.pose().translate(posX, posY, 350.0);
		gui.pose().translate(xOffsetRatio, yOffsetRatio, 0);
		gui.pose().scale(scale, -scale, scale);
		gui.pose().translate(0, 1.25, 0);
		gui.pose().scale(scaleZoom, scaleZoom, scaleZoom);
		gui.pose().mulPose(rotation);
		gui.pose().translate(0, -1.25, 0);
		gui.flush();
		Lighting.setupForEntityInInventory();
		EntityRenderDispatcher renderManager = Minecraft.getInstance().getEntityRenderDispatcher();
		
		@SuppressWarnings("unchecked")
		StandEntityRenderer<?, S, ?> renderer = (StandEntityRenderer<?, S, ?>) renderManager.renderers.get(standType.getEntityType());
		renderManager.setRenderShadow(false);
//		gui.drawSpecial(bufferSource -> renderer.renderWithRenderState(renderState -> {
//			renderer.extractSkinMenuRenderState(renderState, standSkin, standType.getId(), ticks);
//		}, gui.pose(), bufferSource, 0xF000F0));
		RenderSystem.runAsFancy(() -> renderer.renderForStandSkinUI(standSkin, standType.getId(), ticks, 
				gui.pose(), Minecraft.getInstance().renderBuffers().bufferSource(), tint));
		
		gui.flush();
		renderManager.setRenderShadow(true);
		gui.pose().popPose();
		
		Lighting.setupFor3DItems();
	}
}
