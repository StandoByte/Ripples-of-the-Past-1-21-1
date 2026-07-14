package com.github.standobyte.jojo.mechanics.jojopose;

import java.util.HashMap;

import com.github.standobyte.jojo.client.entityanim.AnimationSet;
import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition;
import com.github.standobyte.jojo.client.entityanim.RotpAnimDefinition.SavedPose;
import com.github.standobyte.jojo.client.entityanim.pose.AnimFramePose;
import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.mechanics.clothes.EntityClothesInventory;
import com.github.standobyte.jojo.mechanics.clothes.client.layer.HumanoidClothesLayer;
import com.github.standobyte.jojo.mechanics.clothes.itemdata.StoryCharacter;
import com.github.standobyte.jojo.mechanics.jojopose.resource.ClientJojoPoseLoader;
import com.github.standobyte.jojo.subsystems.StoryPart;
import com.github.standobyte.v1_21_4_stuff.renderstate.ExtractRSExtensionManually;
import com.github.standobyte.v1_21_4_stuff.renderstate.HumanoidRenderState;
import com.github.standobyte.v1_21_4_stuff.renderstate.LivingEntityRenderState;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
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
				
				var poses = ClientJojoPoseLoader.getInstance().getForCharacter(character, storyPart).toList();
				if (!poses.isEmpty()) {
					int x = screen.width - 74;
					int y = screen.height - 84;
					for (var animSetEntry : poses) {
						ResourceLocation animSetId = animSetEntry.getKey();
						AnimationSet anims = animSetEntry.getValue().anims();
						for (var poseEntry : anims.namedAnimations.entrySet()) {
							String poseName = poseEntry.getKey();
							RotpAnimDefinition anim = poseEntry.getValue().getSingle();
							AnimFramePose pose = getPose(anim);
							JojoPoseWidget button = new JojoPoseWidget(x, y, pose, animSetId, poseName);
							event.addListener(button);
							y -= 68;
						}
					}
				}
			}
		}
	}
	
	
	public static AnimFramePose getPose(RotpAnimDefinition anim) {
		if (anim.poses == null || anim.poses.isEmpty()) {
			if (anim.poses == null) {
				anim.poses = HashMap.newHashMap(1);
			}
			
			AnimFramePose frame = new AnimFramePose();
			anim.calcAnimPose(frame, anim.lengthInSeconds, 1, null, null);
			anim.poses.put("lastPose", new SavedPose(frame, true));
		}
		
		return anim.poses.values().iterator().next().pose();
	}
	
	
	
	public static class JojoPoseWidget extends AbstractWidget {
		protected static final WidgetSprites SPRITES = new WidgetSprites(
				JojoMod.resLoc("widget/pose_slot"),
				JojoMod.resLoc("widget/pose_slot"),
				JojoMod.resLoc("widget/pose_slot_selection"));
		protected AnimFramePose pose;
		protected ResourceLocation animationSet;
		protected String animName;

		public JojoPoseWidget(int x, int y, AnimFramePose pose, 
				ResourceLocation animationSet, String animName) {
			super(x, y, 64, 64, CommonComponents.EMPTY);
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
			mc.setScreen(null);
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
