package com.github.standobyte.jojo.mechanics.jojopose;

import java.util.List;

import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose;
import com.github.standobyte.jojo.client.ui.screen_widgets.ScrolleableButtonList;
import com.github.standobyte.jojo.client.ui.screen_widgets.ScrolleableButtonList.EntryWithButtons;
import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.clothes.EntityClothesInventory;
import com.github.standobyte.jojo.mechanics.clothes.client.layer.HumanoidClothesLayer;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.StoryCharacter;
import com.github.standobyte.jojo.mechanics.jojopose.resource.ClientJojoPoseLoader;
import com.github.standobyte.jojo.mechanics.jojopose.resource.JojoPose;
import com.github.standobyte.jojo.subsystems.StoryPart;
import com.github.standobyte.v1_21_4_stuff.renderstate.ExtractRSExtensionManually;
import com.github.standobyte.v1_21_4_stuff.renderstate.HumanoidRenderState;
import com.github.standobyte.v1_21_4_stuff.renderstate.LivingEntityRenderState;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ClientJojoPoseChatUI {

	@SubscribeEvent
	public static void addPoseButtons(ScreenEvent.Init.Post event) {
		Screen screen = event.getScreen();
		if (screen.getClass() == ChatScreen.class) {
			EntityClothesInventory clothes = EntityClothesInventory.getExisting(event.getScreen().getMinecraft().player);
			if (clothes != null) {
				Holder<StoryCharacter> character = clothes.getCharacter();
				Holder<StoryPart> storyPart = clothes.getStoryPart();
				
				List<JojoPose> poses = ClientJojoPoseLoader.getInstance().getPosesForCharacter(character, storyPart).toList();
				if (!poses.isEmpty()) {
					int x = screen.width - 74;
					
					Minecraft mc = Minecraft.getInstance();
					/* scissors don't work correctly with entity models, 
					 * so we'll just put the list all the way to the bottom
					 * to make it look not as scuffed
					 */
					ScrolleableButtonList posesListUI = new ScrolleableButtonList(mc, x, 0, 74, screen.height/* - 16*/, 68) {
						@Override // this makes it so that the buttons don't get focused when we press arrow keys
						public ComponentPath nextFocusPath(FocusNavigationEvent event) {
							return null;
						}
						
						@Override protected void renderListBackground(GuiGraphics guiGraphics) {}
						@Override protected void renderListSeparators(GuiGraphics guiGraphics) {}
					};

					ChatScreenWithPosesList chatScreen = (ChatScreenWithPosesList) screen;
					for (JojoPose jojoPoseDefinition : poses) {
						ResourceLocation animSetId = jojoPoseDefinition.animSet;
						String poseName = jojoPoseDefinition.animName;
						AnimFramePose modelPose = jojoPoseDefinition.getPose();
						JojoPoseWidget button = new JojoPoseWidget(chatScreen, 0, 0, modelPose, animSetId, poseName);
						posesListUI.addEntry(new EntryWithButtons().add(button, 0, 0));
					}
					
					int screenHeight = posesListUI.getHeight();
					int actualListHeight = posesListUI.getMaxPosition() + 4;
					if (actualListHeight < posesListUI.getHeight()) {
						posesListUI.setY(screenHeight - actualListHeight);
						posesListUI.setHeight(actualListHeight);
					}
					event.addListener(posesListUI);
					((ChatScreenWithPosesList) screen).jojo_ripples$setJojoPoseScrolleableList(posesListUI);
				}
			}
		}
	}
	
	public static interface ChatScreenWithPosesList {
		public void jojo_ripples$setJojoPoseScrolleableList(ScrolleableButtonList list);
		public void jojo_ripples$simulatePressingEnter(); // ChatScreen#input doesn't even have a getter
	}
	
	public static boolean scrollPoseList(Screen chatScreen, ScrolleableButtonList jojoPosesList, 
			double mouseX, double mouseY, double scrollX, double scrollY) {
		if (jojoPosesList.isMouseOver(mouseX, mouseY)) {
			return jojoPosesList.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
		}
		return false;
	}
	
	
	
	public static class JojoPoseWidget extends AbstractWidget {
		protected static final WidgetSprites SPRITES = new WidgetSprites(
				JojoMod.resLoc("widget/pose_slot"),
				JojoMod.resLoc("widget/pose_slot"),
				JojoMod.resLoc("widget/pose_slot_selection"));
		protected ChatScreenWithPosesList chatScreen;
		protected AnimFramePose pose;
		protected ResourceLocation animationSet;
		protected String animName;

		public JojoPoseWidget(ChatScreenWithPosesList chatScreen, int x, int y, AnimFramePose pose, 
				ResourceLocation animationSet, String animName) {
			super(x, y, 64, 64, CommonComponents.EMPTY);
			this.chatScreen = (ChatScreenWithPosesList) chatScreen;
			this.pose = pose;
			this.animationSet = animationSet;
			this.animName = animName;
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
			defaultButtonNarrationText(narrationElementOutput);
		}

		@Override
		protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
			Minecraft mc = Minecraft.getInstance();
			guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
			RenderSystem.enableBlend();
			RenderSystem.enableDepthTest();
			
			guiGraphics.blitSprite(SPRITES.get(this.active, false), 
					this.getX(), this.getY(), this.getWidth(), this.getHeight());

        	PoseStack poseStack = guiGraphics.pose();
        	poseStack.pushPose();
        	poseStack.translate(getX() + 32, getY() + 18, 100);
        	poseStack.mulPose(Axis.YP.rotationDegrees(180));
        	poseStack.scale(-27f, 27f, 27f);
        	
			EntityRenderDispatcher entityRenderDispatcher = mc.getEntityRenderDispatcher();
			AbstractClientPlayer player = mc.player;
			PlayerRenderer renderer = (PlayerRenderer) (Object) entityRenderDispatcher.getRenderer(player);
			PlayerModel<?> model = renderer.getModel();

			model.setAllVisible(true);
			model.hat.visible = false;
			model.jacket.visible = false;
			model.leftPants.visible = false;
			model.rightPants.visible = false;
			model.leftSleeve.visible = false;
			model.rightSleeve.visible = false;
			
        	HumanoidRenderState renderState = RenderModelWithPose.clearSetupPose(model, pose, player.tickCount + partialTick);
        	renderState.isBaby = player.isBaby();
        	LivingEntityRenderState.setUpsideDown(renderState, player);
        	ExtractRSExtensionManually.extractClothes(player);
        	
        	RenderModelWithPose.apply(renderState, model);
        	
        	MultiBufferSource buffers = guiGraphics.bufferSource();
        	ResourceLocation playerTexture = renderer.getTextureLocation(player);
        	VertexConsumer buffer = buffers.getBuffer(RenderType.entityTranslucent(playerTexture));
        	
        	RenderSystem.runAsFancy(() -> {
        		model.renderToBuffer(poseStack, buffer, ClientUtil.MAX_LIGHT, OverlayTexture.NO_OVERLAY);
        		HumanoidClothesLayer.render(model, poseStack, buffers, ClientUtil.MAX_LIGHT, OverlayTexture.NO_OVERLAY);
        	});
        	RenderModelWithPose.afterRender();
        	ExtractRSExtensionManually.resetClothes();
        	
			poseStack.popPose();
        	
			if (this.active && this.isHoveredOrFocused()) {
				guiGraphics.blitSprite(SPRITES.get(true, true), 
						this.getX(), this.getY(), this.getWidth(), this.getHeight());
			}
			
			guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
		}
		
		public void onPress() {
			Minecraft mc = Minecraft.getInstance();
			PacketDistributor.sendToServer(ClJojoPoseActionPacket.start(
					mc.player.getId(), animationSet, animName));
			if (chatScreen != null) {
				chatScreen.jojo_ripples$simulatePressingEnter();
			}
			if (chatScreen == null || mc.screen == chatScreen) {
				mc.setScreen(null);
			}
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			onPress();
		}

		@Override
		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
			if (!this.active || !this.visible) {
				return false;
			}
			else if (CommonInputs.selected(keyCode)) {
				onPress();
				return true;
			}
			else {
				return false;
			}
		}
	}
	
}
