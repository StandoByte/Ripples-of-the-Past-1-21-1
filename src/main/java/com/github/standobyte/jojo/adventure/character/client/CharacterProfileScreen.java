package com.github.standobyte.jojo.adventure.character.client;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import com.github.standobyte.jojo.adventure.character.CharacterPersonData;
import com.github.standobyte.jojo.client.firstperson.LivingLayersAccess;
import com.github.standobyte.jojo.client.standskin.text.StandNameSetColor;
import com.github.standobyte.jojo.client.ui.screen_jojomenu.IJojoMenuScreen;
import com.github.standobyte.jojo.client.ui.screen_jojomenu.Tab;
import com.github.standobyte.jojo.client.ui.screen_jojomenu.TabCategory;
import com.github.standobyte.jojo.client.ui.utils.BlitFloat;
import com.github.standobyte.jojo.client.ui.utils.ScrollingText;
import com.github.standobyte.jojo.client.util.functions.ClientUtil;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.power.ModPlayerPowers;
import com.github.standobyte.jojo.mechanics.clothes.client.layer.HumanoidClothesLayer;
import com.github.standobyte.jojo.powersystem.PowerData;
import com.github.standobyte.jojo.powersystem.PowerType;
import com.github.standobyte.jojo.powersystem.playerpower.PlayerPower;
import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.github.standobyte.jojo.powersystem.standpower.StandPower;
import com.github.standobyte.jojo.util.OOPMoment;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.common.NeoForge;

public class CharacterProfileScreen extends Screen implements IJojoMenuScreen {
	public LivingEntity character;
	
	protected ResourceLocation texture;
	protected TabCategory category;
	protected Tab tab;
	
	protected ScrollingText infoLines;
	protected int tickCount = 0;
	public static int rand;

	public CharacterProfileScreen(TabCategory category, Tab tab) {
		super(Component.empty());
		this.category = category;
		this.tab = tab;
		this.texture = JojoMod.resLoc("textures/gui/paper_style/character_profile.png");
		CharacterProfileScreen.rand = Math.abs(OOPMoment.RANDOM.nextInt());
	}
	
	@Override
	public void init() {
		super.init();
		character = minecraft.player;
		infoLines = new ScrollingText(getWindowX(this), getWindowY(this) + 132, getWindowWidth() - 20, 94);
		infoLines.lineHeight = 11;
	}

	@Override
	public TabCategory getTabCategory() {
		return category;
	}

	@Override
	public Tab getTab() {
		return tab;
	}
	
	@Override
	public void tick() { this.tickCount++; }

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float p_283123_) {
		super.render(guiGraphics, mouseX, mouseY, p_283123_);

		int x = getWindowX(this);
		int y = getWindowY(this);
		int width = getWindowWidth();
		int height = getWindowHeight();
		BlitFloat.blit(guiGraphics.pose(), Minecraft.getInstance(), texture, 
				x, y, width, height, 0, 
				0, 0, width, height, 256, 256, 
				BlitFloat.NO_TINT);
		
		if (character != null) {
			renderContents(character, guiGraphics, mouseX, mouseY,
					minecraft.player, true, true, true);
		}
		
		renderTabs(guiGraphics, this);
		renderTabTooltip(guiGraphics, this, mouseX, mouseY);
	}
	
	protected void renderContents(LivingEntity entity, GuiGraphics guiGraphics, int mouseX, int mouseY,
			LivingEntity user, boolean knownStand, boolean knownUser, boolean knownStats) {
		int x = getWindowX(this);
		int y = getWindowY(this);
		int width = getWindowWidth();
		
		renderEntityInInventoryFollowsMouse(guiGraphics, 
				x, y, x + 100, y + 160, 
				45, 0, mouseX, mouseY, entity,
				tickCount + ClientUtil.partialTick(minecraft.getTimer(), true));

		Component characterName = entity.getName();
		guiGraphics.drawString(minecraft.font, characterName, 
				x + (width - minecraft.font.width(characterName)) / 2, y + 10, 
				0xFF000000, false);
		
		CharacterPersonData characterData = CharacterPersonData.get(entity);
		PlayerPower power = PlayerPower.get(entity);
		PowerData powerData = power != null ? power.getCurTypeData() : null;
		StandPower standPower = StandPower.get(entity);
		
		List<FormattedCharSequence> textLines = new ArrayList<>();
		if (characterData != null) {
			addLine(textLines, Component.translatable("jojo_ripples.char_profile.species", 
					characterData.getSpecies().getResultingName(entity, power)));
		}
		
		if (powerData != null) {
			PowerType powerType = powerData.getPowerType();
			if (powerType == ModPlayerPowers.HAMON.get() || powerType.getId().getPath().contains("spin")) {
				addLine(textLines, Component.translatable("jojo_ripples.char_profile.human_power", power.getName()));
			}
			else if (powerType == ModPlayerPowers.PILLAR_MAN.get()) {
				// add Pillar Man Mode name
			}
		}
		
		if (standPower != null) {
			StandInstance stand = standPower.getStandInstance().orElse(null);
			if (stand != null) {
				addLine(textLines, Component.translatable("jojo_ripples.char_profile.stand", 
						StandNameSetColor.fromSkin(stand, stand.getStandName(), true)));
			}
		}
		
//		textLines.add(CommonComponents.EMPTY.getVisualOrderText());
//		
//		addLine(textLines, Component.translatable("jojo_ripples.char_profile.blood_type", ""));
//		int cm = (int) (187.5f * entity.getAttributeValue(Attributes.SCALE));
//		addLine(textLines, Component.translatable("jojo_ripples.char_profile.height", cm));
//		addLine(textLines, Component.translatable("jojo_ripples.char_profile.body_type", ""));
//		addLine(textLines, Component.translatable("jojo_ripples.char_profile.eye_color", ""));
//		addLine(textLines, Component.translatable("jojo_ripples.char_profile.day_of_spawn", ""));
//		addLine(textLines, Component.translatable("jojo_ripples.char_profile.spawn_biome", ""));
//		textLines.add(CommonComponents.EMPTY.getVisualOrderText());
//		
//		addLine(textLines, Component.translatable("jojo_ripples.char_profile.pets", ""));
//		addLine(textLines, Component.translatable("jojo_ripples.char_profile.fav_food", ""));
//		addLine(textLines, Component.translatable("jojo_ripples.char_profile.fav_weapon", ""));
//		addLine(textLines, Component.translatable("jojo_ripples.char_profile.fav_block", ""));
		
		infoLines.setText(textLines);
		infoLines.draw(10, Math.max(infoLines.height - infoLines.scrolling.contentsHeight, 0), guiGraphics, font, 0xFF000000, false);
	}
	
	void addLine(List<FormattedCharSequence> list, Component line) {
		list.addAll(minecraft.font.split(line, infoLines.width - 10));
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (clickTab(mouseX, mouseY, button, this)) return true;
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
    	return infoLines.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }



	// Copypaste of InventoryScreen.renderEntityInInventoryFollowsMouse, but without the other layers
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
			LivingEntity entity,
			float ticks
			) {
		float f = (float)(x1 + x2) / 2.0F;
		float f1 = (float)(y1 + y2) / 2.0F;
		float f2 = (float)Math.atan((double)((f - mouseX) / 40.0F));
		float f3 = (float)Math.atan((double)((f1 - mouseY) / 40.0F));
		renderEntityInInventoryFollowsAngle(guiGraphics, x1, y1, x2, y2, scale, yOffset, f2, f3, entity, ticks);
	}

	public static void renderEntityInInventoryFollowsAngle(
			GuiGraphics p_282802_,
			int p_275688_,
			int p_275245_,
			int p_275535_,
			int p_294406_,
			int p_294663_,
			float p_275604_,
			float angleXComponent,
			float angleYComponent,
			LivingEntity p_275689_,
			float ticks
			) {
		float f = (float)(p_275688_ + p_275535_) / 2.0F;
		float f1 = (float)(p_275245_ + p_294406_) / 2.0F;
		p_282802_.enableScissor(p_275688_, p_275245_, p_275535_, p_294406_);
		float f2 = angleXComponent;
		float f3 = angleYComponent;
		Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
		Quaternionf quaternionf1 = new Quaternionf().rotateX(f3 * 20.0F * (float) (Math.PI / 180.0));
		quaternionf.mul(quaternionf1);
		float f4 = p_275689_.yBodyRot;
		float f5 = p_275689_.getYRot();
		float f6 = p_275689_.getXRot();
		float f7 = p_275689_.yHeadRotO;
		float f8 = p_275689_.yHeadRot;
		p_275689_.yBodyRot = 180.0F + f2 * 20.0F;
		p_275689_.setYRot(180.0F + f2 * 40.0F);
		p_275689_.setXRot(-f3 * 20.0F);
		p_275689_.yHeadRot = p_275689_.getYRot();
		p_275689_.yHeadRotO = p_275689_.getYRot();
		float f9 = p_275689_.getScale();
		Vector3f vector3f = new Vector3f(0.0F, p_275689_.getBbHeight() / 2.0F + p_275604_ * f9, 0.0F);
		float f10 = (float)p_294663_ / f9;
		renderEntityInInventory(p_282802_, f, f1, f10, vector3f, quaternionf, quaternionf1, p_275689_, ticks);
		p_275689_.yBodyRot = f4;
		p_275689_.setYRot(f5);
		p_275689_.setXRot(f6);
		p_275689_.yHeadRotO = f7;
		p_275689_.yHeadRot = f8;
		p_282802_.disableScissor();
	}

	public static <E extends LivingEntity> void renderEntityInInventory(
			GuiGraphics guiGraphics,
			float x,
			float y,
			float scale,
			Vector3f translate,
			Quaternionf pose,
			@Nullable Quaternionf cameraOrientation,
			E entity,
			float ticks
			) {
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate((double)x, (double)y, 50.0);
		guiGraphics.pose().scale(scale, scale, -scale);
		guiGraphics.pose().translate(translate.x, translate.y, translate.z);
		guiGraphics.pose().mulPose(pose);
		Lighting.setupForEntityInInventory();
		EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
		if (cameraOrientation != null) {
			entityrenderdispatcher.overrideCameraOrientation(cameraOrientation.conjugate(new Quaternionf()).rotateY((float) Math.PI));
		}

		entityrenderdispatcher.setRenderShadow(false);
		RenderSystem.runAsFancy(() -> {
			EntityRenderer<? super E> entityrenderer = entityrenderdispatcher.getRenderer(entity);

			float partialTicks = 1;
			PoseStack poseStack = guiGraphics.pose();
			MultiBufferSource buffer = guiGraphics.bufferSource();
			int packedLight = ClientUtil.MAX_LIGHT;
			
			Vec3 renderOffset = entityrenderer.getRenderOffset(entity, partialTicks);
			poseStack.pushPose();
			poseStack.translate(renderOffset.x, renderOffset.y, renderOffset.z);
			
			renderEntity((LivingEntityRenderer) entityrenderer, entity, 0, ticks, partialTicks, poseStack, buffer, packedLight);

			poseStack.popPose();
		});
		guiGraphics.flush();
		entityrenderdispatcher.setRenderShadow(true);
		guiGraphics.pose().popPose();
		Lighting.setupFor3DItems();
	}
	
	// i want to die
	public static <E extends LivingEntity, M extends EntityModel<E>> void renderEntity(LivingEntityRenderer<E, M> renderer,
			E entity, float entityYaw, float ticks, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		PlayerRenderer playerRenderer = renderer instanceof PlayerRenderer __ ? __ : null;
		M model = renderer.getModel();
		if (playerRenderer != null) {
			PlayerModel<AbstractClientPlayer> playermodel = (PlayerModel<AbstractClientPlayer>) model;
			AbstractClientPlayer clientPlayer = (AbstractClientPlayer) entity;
			playermodel.setAllVisible(true);
			playermodel.hat.visible = clientPlayer.isModelPartShown(PlayerModelPart.HAT);
			playermodel.jacket.visible = clientPlayer.isModelPartShown(PlayerModelPart.JACKET);
			playermodel.leftPants.visible = clientPlayer.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
			playermodel.rightPants.visible = clientPlayer.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
			playermodel.leftSleeve.visible = clientPlayer.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
			playermodel.rightSleeve.visible = clientPlayer.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
			playermodel.crouching = clientPlayer.isCrouching();
			playermodel.rightArmPose = HumanoidModel.ArmPose.EMPTY;
			playermodel.leftArmPose = HumanoidModel.ArmPose.EMPTY;
		}
		if (
				playerRenderer != null && NeoForge.EVENT_BUS.post(new RenderPlayerEvent.Pre(
						(Player) entity, playerRenderer, partialTicks, poseStack, buffer, packedLight)).isCanceled()
				|| NeoForge.EVENT_BUS.post(new RenderLivingEvent.Pre<E, M>(
						entity, renderer, partialTicks, poseStack, buffer, packedLight)).isCanceled()) return;

		poseStack.pushPose();
		model.attackTime = 0;
		model.riding = false;
		model.young = entity.isBaby();
		float yBodyRot = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
		float yHeadRot = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
		float headYaw = yHeadRot - yBodyRot;

		float headPitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
		if (LivingEntityRenderer.isEntityUpsideDown(entity)) {
			headPitch *= -1.0F;
			headYaw *= -1.0F;
		}

		headYaw = Mth.wrapDegrees(headYaw);
		float scale = entity.getScale();
		poseStack.scale(scale, scale, scale);

		if (LivingEntityRenderer.isEntityUpsideDown(entity)) {
			poseStack.translate(0.0F, (entity.getBbHeight() + 0.1F) / scale, 0.0F);
			poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
		}

		poseStack.scale(-1.0F, -1.0F, 1.0F);
		poseStack.translate(0.0F, -1.501F, 0.0F);
		float limbSwingAmount = 0.0F;
		float limbSwing = 0.0F;

		model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
		model.setupAnim(entity, limbSwing, limbSwingAmount, ticks, headYaw, headPitch);
		Minecraft minecraft = Minecraft.getInstance();
		RenderType rendertype = model.renderType(renderer.getTextureLocation(entity));
		if (rendertype != null) {
			VertexConsumer vertexconsumer = buffer.getBuffer(rendertype);
			model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
		}

		List<RenderLayer<E, M>> layers = ((LivingLayersAccess<E, M>) renderer).jojo_ripples$allLayers();
		for (RenderLayer<E, M> renderlayer : layers) {
			if (renderlayer instanceof HumanoidClothesLayer) {
				renderlayer.render(poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, partialTicks, ticks, headYaw, headPitch);
			}
		}

		poseStack.popPose();
		NeoForge.EVENT_BUS.post(new RenderLivingEvent.Post<E, M>(entity, renderer, partialTicks, poseStack, buffer, packedLight));
		if (playerRenderer != null) {
			NeoForge.EVENT_BUS.post(new RenderPlayerEvent.Post((Player) entity, playerRenderer, partialTicks, poseStack, buffer, packedLight));
		}
	}

}
