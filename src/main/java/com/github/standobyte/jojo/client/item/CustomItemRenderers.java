package com.github.standobyte.jojo.client.item;

import java.util.Map;
import java.util.function.UnaryOperator;

import org.joml.Matrix3f;
import org.slf4j.Logger;

import com.github.standobyte.jojo.client.item.custommodel.BakedCustomModel;
import com.github.standobyte.jojo.client.item.custommodel.CustomItemRenderer;
import com.github.standobyte.jojo.client.item.custommodel.ItemRendererProvider;
import com.github.standobyte.jojo.client.item.standdisc.StandDiscRenderer;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class CustomItemRenderers {
	private static final Logger LOGGER = LogUtils.getLogger();


	public static BlockEntityWithoutLevelRenderer modLogoRenderer;
	public static StandDiscRenderer standDiscRenderer;

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void addListener(RegisterClientReloadListenersEvent event) {
		Minecraft mc = Minecraft.getInstance();
		
		modLogoRenderer = new CustomItemRenderer(mc, 
				ResourceLocation.fromNamespaceAndPath(JojoMod.MOD_ID, "mod_logo"),
				ResourceLocation.fromNamespaceAndPath(JojoMod.MOD_ID, "textures/mod_logo_model.png")) {
			@Override
			public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
				poseStack.pushPose();
				poseStack.scale(0.75f, 0.75f, 0.75f);
				poseStack.translate(0.125f, 0f, 0);
				Matrix3f lighting = poseStack.last().normal();
				lighting.rotate(Axis.YP.rotationDegrees(45));
				lighting.rotate(Axis.XP.rotationDegrees(-45));
				lighting.rotate(Axis.ZP.rotationDegrees(-45));
				super.renderByItem(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
				poseStack.popPose();
			}
		};
	}

	@SubscribeEvent
	public static void registerItemRenderers(RegisterClientExtensionsEvent event) {
		Minecraft mc = Minecraft.getInstance();
		
		event.registerItem(new ItemRendererProvider(() -> modLogoRenderer), ModItems.DEBUG_ITEM);
		
		standDiscRenderer = new StandDiscRenderer(mc);
		event.registerItem(new ItemRendererProvider(() -> standDiscRenderer), ModItems.STAND_DISC);
	}

	@SubscribeEvent
	public static void setItemModelsAsCustom(ModelEvent.ModifyBakingResult event) {
		Map<ModelResourceLocation, BakedModel> registry = event.getModels();
		CustomItemRenderers.registerCustomBakedModel(ModItems.DEBUG_ITEM.getId(), registry, model -> new BakedCustomModel(model));
		CustomItemRenderers.registerCustomBakedModel(ModItems.STAND_DISC.getId(), registry, model -> new BakedCustomModel(model));
	}


	public static void registerCustomBakedModel(ResourceLocation itemResLoc, 
			Map<ModelResourceLocation, BakedModel> modelRegistry, UnaryOperator<BakedModel> newModel) {
		ModelResourceLocation modelResLoc = ModelResourceLocation.inventory(itemResLoc);
		BakedModel existingModel = modelRegistry.get(modelResLoc);
		if (existingModel == null) {
			LOGGER.error("Did not find original {} model in registry", modelResLoc);
		}
		else if (existingModel.isCustomRenderer()) {
			LOGGER.error("Tried to replace {} model twice", modelResLoc);
		}
		else {
			modelRegistry.put(modelResLoc, newModel.apply(existingModel));
		}
	}



	@SubscribeEvent
	public static void registerItemColoring(RegisterColorHandlersEvent.Item event) {
		event.register(StandDiscRenderer::getItemModelLayerColor, ModItems.STAND_DISC.get());

//		itemColors.register((stack, layer) -> {
//			if (layer != 1) return -1;
//
//			Optional<DyeColor> dye = CassetteRecordedItem.getCassetteData(stack).map(cap -> cap.getDye());
//			return dye.isPresent() ? dye.get().getColorValue() : 0xeff0e0;
//		}, ModItems.CASSETTE_RECORDED.get());
	}
}
