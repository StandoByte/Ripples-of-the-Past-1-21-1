package com.github.standobyte.jojo.mechanics.clothes.sewing.client;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joml.Quaternionf;

import com.github.standobyte.jojo.client.ui.screen_widgets.FilterList;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.clothes.client.layer.HumanoidClothesLayer;
import com.github.standobyte.jojo.mechanics.clothes.client.layer.HumanoidClothesRSExtension;
import com.github.standobyte.jojo.mechanics.clothes.client.ui.PlayerClothesScreen;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSet;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.ClothesSlotType;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.StoryCharacter;
import com.github.standobyte.jojo.mechanics.clothes.sewing.SewingMachineContainer;
import com.github.standobyte.jojo.subsystems.StoryPart;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.inventory.Slot;

@SuppressWarnings("deprecation")
public class SewingMachineScreen extends AbstractContainerScreen<SewingMachineContainer> {
	public static SewingMachineScreenSettings settingsInstance = null;
	public static final ResourceLocation TEXTURE = JojoMod.resLoc("textures/gui/container/sewing_machine.png");
	protected static final int WINDOW_WIDTH = 331;
	protected static final int WINDOW_HEIGHT = 258;

	protected Map<StoryPart, StoryPartFilterToggle> partFilters;
	protected ClothesSetSearchField searchBox;
	FilterList<SelectCharacterButton> charactersList;
	protected Component characterName;
	protected final Map<ClothesSet, ClothesSetButton> clothesSelection = new LinkedHashMap<>();

	public SewingMachineScreen(SewingMachineContainer pMenu, Inventory pPlayerInventory, Component pTitle) {
		super(pMenu, pPlayerInventory, CommonComponents.EMPTY);
		if (settingsInstance == null) {
			settingsInstance = new SewingMachineScreenSettings();
		}
	}
	
	public static void onReload() {
		settingsInstance = null;
	}

	@Override
	protected void init() {
		leftPos = (this.width - WINDOW_WIDTH) / 2 - 18;
		topPos = (this.height - WINDOW_HEIGHT) / 2;

		imageWidth = WINDOW_WIDTH + 18;
		imageHeight = WINDOW_HEIGHT;
		titleLabelX = 26;
		titleLabelY = 28;
		inventoryLabelX = 26;
		inventoryLabelY = imageHeight - 93;

		getSettings().initNewScreen(this);

		partFilters = Util.make(new LinkedHashMap<>(), map -> {
			StoryPart[] presentParts = getSettings().getPresentParts();
			for (int i = 0; i < presentParts.length; i++) {
				StoryPart part = presentParts[i];
				StoryPartFilterToggle toggle = new StoryPartFilterToggle(
						getWindowX() + 162 + (i + 9 - presentParts.length) * 18, getWindowY() + 5, 18, 18, 
						() -> getSettings().isPartIncluded(part),
						newState -> getSettings().setFilter(this, part, newState), part);
				map.put(part, toggle);
				addWidget(toggle);
			}
		});

		addWidget(searchBox = Util.make(new ClothesSetSearchField(minecraft.font, getWindowX() + 24, getWindowY() + 7, 106, 10, 
				Component.translatable("jojo.clothes.search")),
				box -> {
					box.setMaxLength(50);
					box.setBordered(false);
					box.setTextColor(0xFFFFFF);
				}));

		List<SelectCharacterButton> characters = new ArrayList<>();
		getSettings().forEachCharacter((character, clothes) -> {
			SelectCharacterButton button = new SelectCharacterButton(this, clothes);
			button.x = leftPos;
			characters.add(button);
		});
		charactersList = addWidget(new FilterList<>(characters, 
				leftPos, topPos + 18, 18, topPos + imageHeight, 18, FilterList.AlignmentY.TOP));

		getSettings().updateFilter(this);

		if (getSettings().getSelectedCharUI() != null) {
			getSettings().getSelectedCharUI().setupClothesSelectionUI();
		}
	}

	protected final int getWindowX() {
		return leftPos + 18;
	}

	protected final int getWindowY() {
		return topPos;
	}


//	protected Collection<AbstractWidget> getWidgets() {
//		return buttons;
//	}

	public SewingMachineScreenSettings getSettings() {
		return settingsInstance;
	}



	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		if (onNextFrame != null) {
			onNextFrame.run();
			onNextFrame = null;
		}

		this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

		StoryCharacter selectedCharacter = getSettings().getSelectedCharacter();
		characterName = selectedCharacter != null ? selectedCharacter.getName(false) : CommonComponents.EMPTY;

		super.render(guiGraphics, mouseX, mouseY, partialTick);
		
		partialTick = ClientUtil.partialTick(minecraft.getTimer(), false);
		
		PoseStack poseStack = guiGraphics.pose();
		int x = getWindowX();
		int y = getWindowY();

//		ClientUtil.enableGlScissor(x + 81, y + 41, 145, 104);
		for (ClothesSetButton clothesSelButton : clothesSelection.values()) {
			if (getSettings().getSelectedCharUI().getSelectedSet() == clothesSelButton.clothesSet) {
				BlitFloat.blit(poseStack, minecraft, TEXTURE, 
						x + 81, y + clothesSelButton.getY() + 12, 4, 7, 0, 
						349, 155, 4, 7, 512, 512, 
						BlitFloat.NO_TINT);
			}
			clothesSelButton.render(guiGraphics, mouseX, mouseY, partialTick);
		}
//		ClientUtil.disableGlScissor();

		PlayerClothesScreen.renderEntityInInventoryFollowsMouse(
				guiGraphics, 
				x + 26, y + 178, x + 75, y + 248, 
				30, 0.0625f, mouseX, mouseY, 
				this.minecraft.player);

		if (selectedCharacter != null) {
			if (getSettings().getSelectedSet() != null) {
				renderSetShowcase(x + 43, y + 138, 
						minecraft.player, 
						menu.craftingSlots.slots,
						partialTick, 0, 43);
			}
		}

		charactersList.render(guiGraphics, minecraft, mouseX, mouseY, partialTick);
		renderUnlockBars(guiGraphics, partialTick);

		renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
		RenderSystem.enableBlend();

		PoseStack poseStack = guiGraphics.pose();
		int x = getWindowX();
		int y = getWindowY();
		BlitFloat.blit(poseStack, minecraft, TEXTURE, 
				x, y, WINDOW_WIDTH, WINDOW_HEIGHT, 0, 
				18, 0, WINDOW_WIDTH, WINDOW_HEIGHT, 512, 512, 
				BlitFloat.NO_TINT);

		if (clothesSelection != null) {
			BlitFloat.blit(poseStack, minecraft, TEXTURE, 
					x + 87, y + 40, 153, 106, 0, 
					349, 0, 153, 106, 512, 512, 
					BlitFloat.NO_TINT);
		}

//		RenderSystem.enableRescaleNormal();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableBlend();
	}

	protected void renderUnlockBars(GuiGraphics guiGraphics, float partialTick) {
//		minecraft.textureManager.bind(TEXTURE);
//		RenderSystem.enableBlend();
//		RenderSystem.defaultBlendFunc();
//
//		int unlockBarsX = getWindowX() + (WINDOW_WIDTH - 182) / 2;
//		int unlockBarsY = getWindowY() + WINDOW_HEIGHT;
//		int texY = 263;
//		for (Rarity rarity : Rarity.values()) {
//			int unlocked = getSettings().getUnlockedSetsOfRarity(rarity);
//			int all = ModClothes.BY_RARITY.getOrDefault(rarity, Collections.emptyList()).size();
//			if (all > 0) {
//				int barWidth = unlocked >= all ? 182 : unlocked == 0 ? 0 : 2 + (int) (178 * unlocked / (float) all);
//				blit(guiGraphics, unlockBarsX, unlockBarsY, 330, texY,      182, 5, 512, 512);
//				blit(guiGraphics, unlockBarsX, unlockBarsY, 330, texY + 5,  barWidth, 5, 512, 512);
//				blit(guiGraphics, unlockBarsX, unlockBarsY, 330, 258,       182, 5, 512, 512);
//				unlockBarsY += 6;
//				texY += 10;
//			}
//		}
//
//		RenderSystem.disableBlend();
	}

	@Override
	protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		super.renderTooltip(guiGraphics, mouseX, mouseY);
//		for (AbstractWidget widget : getWidgets()) {
//			renderWidgetTooltips(guiGraphics, mouseX, mouseY, widget);
//		}
		charactersList.forEachRendered(charButton -> {
			if (charButton.isVisible && charButton.isMouseOver(mouseX, mouseY)) {
				setTooltipForNextRenderPass(charButton.character.getCharacter().value().getName(true));
			}
		});

//		int unlockBarsX = getWindowX() + (WINDOW_WIDTH - 182) / 2;
//		int unlockBarsY = getWindowY() + WINDOW_HEIGHT;
//		if (mouseX >= unlockBarsX && mouseX <= unlockBarsX + 182 && mouseY >= unlockBarsY) {
//			List<Component> unlockedSetsTooltip = new ArrayList<>();
//			for (Rarity rarity : Rarity.values()) {
//				int unlocked = getSettings().getUnlockedSetsOfRarity(rarity);
//				int all = ModClothes.BY_RARITY.getOrDefault(rarity, Collections.emptyList()).size();
//				if (all > 0) {
//					MutableComponent line = Component.translatable("jojo.sewing_machine.sets_unlocked." + rarity.name().toLowerCase(), unlocked, all);
//					line.withStyle(rarity.getStyleModifier());
//					unlockedSetsTooltip.add(line);
//				}
//			}
//			if (!unlockedSetsTooltip.isEmpty() && mouseY <= unlockBarsY + 6 * unlockedSetsTooltip.size() - 1) {
//				renderComponentTooltip(guiGraphics, unlockedSetsTooltip, mouseX, mouseY);
//			}
//		}
	}

//	protected void renderWidgetTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY, AbstractWidget widget) {
//		if (widget.isHovered()) {
//			widget.renderToolTip(guiGraphics, mouseX, mouseY);
//			if (widget instanceof ContainerEventHandler nested) {
//				for (GuiEventListener child : nested.children()) {
//					if (child instanceof AbstractWidget childWidget && childWidget.isHovered()) {
//						renderWidgetTooltips(guiGraphics, mouseX, mouseY, childWidget);
//					}
//				}
//			}
//		}
//	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
		guiGraphics.drawString(font, characterName, titleLabelX, titleLabelY, 0xFF404040, false);
		guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFF404040, false);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	protected Runnable onNextFrame;
	public void setClothesSelection(ClothesCharacterUIEntry character, List<Holder<ClothesSet>> allClothes) {
		onNextFrame = () -> { // to avoid ConcurrentModificationException of children list
			clothesSelection.values().forEach(this::removeWidget);
			clothesSelection.clear();

			if (character != null) {
				int x = getWindowX() + 90;
				int y = getWindowY() + 43;
				for (Holder<ClothesSet> holder : allClothes) {
					ClothesSet clothes = holder.value();
					ClothesSetButton button = new ClothesSetButton(x, y, 134, 15, clothes.getName(), b -> {
						character.setSelectedSet(holder);
					}, clothes);
					y += 17;
					clothesSelection.put(clothes, button);
					addWidget(button);
				}
			}
		};
	}



	public static void renderSetShowcase(float x, float y, 
			LocalPlayer player, List<Slot> slots, 
			float partialTick, float yRotation, float scale) {
		PoseStack poseStack = new PoseStack();
		poseStack.translate(x, y, 1050);
		poseStack.scale(1, 1, -1);
		poseStack.translate(0, 0, 1000);
		poseStack.scale(scale, scale, scale);

		Quaternionf quaternion = Axis.ZP.rotationDegrees(180.0F);

		poseStack.mulPose(quaternion);
		float f2 = player.yBodyRot;
		float f3 = player.getYRot();
		float f4 = player.getXRot();
		float f5 = player.yHeadRotO;
		float f6 = player.yHeadRot;

		float setYRot = 157.5F + yRotation;
		player.setYRot(setYRot);
		player.setXRot(0);
		player.yHeadRot = setYRot;
		player.yHeadRotO = setYRot;
		player.yBodyRot = setYRot;

		EntityRenderDispatcher rendererManager = Minecraft.getInstance().getEntityRenderDispatcher();
		quaternion.conjugate();
		rendererManager.overrideCameraOrientation(quaternion);

		rendererManager.setRenderShadow(false);
		MultiBufferSource.BufferSource irendertypebuffer$impl = Minecraft.getInstance().renderBuffers().bufferSource();

		poseStack.last().normal().rotation(Axis.YP.rotationDegrees(180));
		RenderSystem.runAsFancy(() -> {
			PlayerRenderer renderer = (PlayerRenderer) rendererManager.getRenderer(player);
			renderBaseModelAndClothes(renderer, player, poseStack, irendertypebuffer$impl, slots, 0);
		});

		irendertypebuffer$impl.endBatch();
		rendererManager.setRenderShadow(true);
		player.yBodyRot = f2;
		player.setYRot(f3);
		player.setXRot(f4);
		player.yHeadRotO = f5;
		player.yHeadRot = f6;
	}

	protected static <T extends LivingEntity, M extends HumanoidModel<T>> void renderBaseModelAndClothes(LivingEntityRenderer<T, M> renderer, 
			T entity, PoseStack poseStack, MultiBufferSource buffer, List<Slot> slots, float partialTick) {
		poseStack.pushPose();

		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - entity.yBodyRot));
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		poseStack.translate(0.0D, (double)-1.501F, 0.0D);
		
		M entityModel = renderer.getModel();
		boolean slimModel = entity instanceof AbstractClientPlayer player && player.getSkin().model() == PlayerSkin.Model.SLIM;
		HumanoidClothesRSExtension clothesRS = HumanoidClothesRSExtension.reusedInstance;
		
		clothesRS.hasClothesComponent = true;
		for (ClothesSlotType slot : ClothesSlotType.values()) {
			clothesRS.items.put(slot, SewingMachineContainer.CraftSlots.getItem(slots, slot));
		}
		clothesRS.slimModel = slimModel;
		
		ResourceLocation entityTexture = renderer.getTextureLocation(entity);
		RenderType rendertype = entityModel.renderType(entityTexture);
		if (rendertype != null) {
			poseStack.pushPose();
			entityModel.young = entity.isBaby();
			setIdle(entityModel, entity, entity.tickCount + partialTick);
			if (entityModel instanceof PlayerModel playerModel) {
				HumanoidClothesLayer.disablePlayerOuterLayer(playerModel, HumanoidClothesRSExtension.reusedInstance);
			}

			VertexConsumer ivertexbuilder = buffer.getBuffer(rendertype);
			entityModel.renderToBuffer(poseStack, ivertexbuilder, ClientUtil.MAX_LIGHT, OverlayTexture.NO_OVERLAY, BlitFloat.NO_TINT);
		}
		HumanoidClothesLayer.render(entityModel, poseStack, buffer, ClientUtil.MAX_LIGHT, OverlayTexture.NO_OVERLAY);
		
		poseStack.popPose();
	}
	
	
	public static void setIdle(HumanoidModel<?> model, LivingEntity entity, float ticks) {
		model.attackTime = 0;
		model.riding = false;
		model.swimAmount = 0;
		
		model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
		model.leftArmPose = HumanoidModel.ArmPose.EMPTY;
		model.crouching = false;

		model.head.yRot = 0;
		model.head.xRot = 0;

		model.body.yRot = 0.0F;
		model.rightArm.z = 0.0F;
		model.rightArm.x = -5.0F;
		model.leftArm.z = 0.0F;
		model.leftArm.x = 5.0F;

		model.rightArm.xRot = 0.0F;
		model.leftArm.xRot = 0.0F;
		model.rightArm.zRot = 0.0F;
		model.leftArm.zRot = 0.0F;
		model.rightLeg.xRot = 0.0F;
		model.leftLeg.xRot = 0.0F;
		model.rightLeg.yRot = 0.005F;
		model.leftLeg.yRot = -0.005F;
		model.rightLeg.zRot = 0.005F;
		model.leftLeg.zRot = -0.005F;

		model.rightArm.yRot = 0.0F;
		model.leftArm.yRot = 0.0F;

		model.body.xRot = 0.0F;
		model.rightLeg.z = 0.0F;
		model.leftLeg.z = 0.0F;
		model.rightLeg.y = 12.0F;
		model.leftLeg.y = 12.0F;
		model.head.y = 0.0F;
		model.body.y = 0.0F;
		model.leftArm.y = 2.0F;
		model.rightArm.y = 2.0F;

		AnimationUtils.bobModelPart(model.rightArm, ticks, 1);
		AnimationUtils.bobModelPart(model.leftArm, ticks, -1);

		model.hat.copyFrom(model.head);

		if (model instanceof PlayerModel playerModel) {
			playerModel.setAllVisible(true);
			if (entity instanceof AbstractClientPlayer player) {
				playerModel.hat.visible = player.isModelPartShown(PlayerModelPart.HAT);
				playerModel.jacket.visible = player.isModelPartShown(PlayerModelPart.JACKET);
				playerModel.leftPants.visible = player.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
				playerModel.rightPants.visible = player.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
				playerModel.leftSleeve.visible = player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
				playerModel.rightSleeve.visible = player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
			}

			playerModel.leftPants.copyFrom(model.leftLeg);
			playerModel.rightPants.copyFrom(model.rightLeg);
			playerModel.leftSleeve.copyFrom(model.leftArm);
			playerModel.rightSleeve.copyFrom(model.rightArm);
			playerModel.jacket.copyFrom(model.body);
		}
	}

}
