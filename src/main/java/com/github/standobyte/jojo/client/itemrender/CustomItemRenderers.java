package com.github.standobyte.jojo.client.itemrender;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.joml.Matrix3f;
import org.slf4j.Logger;

import com.github.standobyte.jojo.client.itemrender.custommodel.BakedCustomModel;
import com.github.standobyte.jojo.client.itemrender.custommodel.CustomBlockRenderer;
import com.github.standobyte.jojo.client.itemrender.custommodel.CustomItemRenderer;
import com.github.standobyte.jojo.client.itemrender.custommodel.ItemRendererProvider;
import com.github.standobyte.jojo.client.itemrender.custommodel.ItemRendererProvider.BlockItemRendererProvider;
import com.github.standobyte.jojo.client.itemrender.items.TommyGunRenderer;
import com.github.standobyte.jojo.client.itemrender.standdisc.StandDiscRenderer;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModBlockEntities;
import com.github.standobyte.jojo.init.ModBlocks;
import com.github.standobyte.jojo.init.ModItems;
import com.github.standobyte.jojo.mechanics.clothes.sewing.SewingMachineBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemOverrides.BakedOverride;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class CustomItemRenderers {
	private static final Logger LOGGER = LogUtils.getLogger();

	public static ItemRendererProvider<CustomItemRenderer> modLogoRenderer;
	public static ItemRendererProvider<StandDiscRenderer> standDiscRenderer;
	public static ItemRendererProvider<TommyGunRenderer> tommyGunRenderer;
	public static ItemRendererProvider<CustomItemRenderer> polaroidRenderer;
	public static BlockItemRendererProvider<CustomBlockRenderer<SewingMachineBlockEntity>, SewingMachineBlockEntity> sewingMachineRenderer;

	@SubscribeEvent
	public static void registerItemRenderers(RegisterClientExtensionsEvent event) {
		Minecraft mc = Minecraft.getInstance();
		
		modLogoRenderer = new ItemRendererProvider<>(() -> new CustomItemRenderer(mc, 
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
		});
		
		standDiscRenderer = new ItemRendererProvider<>(() -> new StandDiscRenderer(mc));
		
		tommyGunRenderer = new ItemRendererProvider<>(() -> new TommyGunRenderer(mc, 
				ResourceLocation.fromNamespaceAndPath(JojoMod.MOD_ID, "tommy_gun"),
				ResourceLocation.fromNamespaceAndPath(JojoMod.MOD_ID, "textures/item/tommy_gun.png"))) {

		    @Override
		    public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
		        return !entityLiving.swinging ? HumanoidModel.ArmPose.CROSSBOW_HOLD : super.getArmPose(entityLiving, hand, itemStack);
		    }
		};
		
		polaroidRenderer = new ItemRendererProvider<>(() -> new CustomItemRenderer(mc, 
				ResourceLocation.fromNamespaceAndPath(JojoMod.MOD_ID, "polaroid"),
				ResourceLocation.fromNamespaceAndPath(JojoMod.MOD_ID, "textures/item/polaroid.png")));
		
		sewingMachineRenderer = new BlockItemRendererProvider<>(ctx -> new CustomBlockRenderer<>(ctx, 
				ResourceLocation.fromNamespaceAndPath(JojoMod.MOD_ID, "sewing_machine"),
				ResourceLocation.fromNamespaceAndPath(JojoMod.MOD_ID, "textures/block/sewing_machine.png"), 
				new SewingMachineBlockEntity(BlockPos.ZERO, ModBlocks.SEWING_MACHINE.get().defaultBlockState())));
		
		
		event.registerItem(modLogoRenderer, ModItems.DEBUG_ITEM);
		event.registerItem(standDiscRenderer, ModItems.STAND_DISC);
		event.registerItem(tommyGunRenderer, ModItems.TOMMY_GUN);
		event.registerItem(polaroidRenderer, ModItems.POLAROID);
		event.registerItem(sewingMachineRenderer, ModItems.SEWING_MACHINE);
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void registerBlockEntityModel(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(ModBlockEntities.SEWING_MACHINE.get(), sewingMachineRenderer);
	}

	@SubscribeEvent
	public static void setItemModelsAsCustom(ModelEvent.ModifyBakingResult event) {
		Map<ModelResourceLocation, BakedModel> registry = event.getModels();
		ModelBakery modelBakery = event.getModelBakery();
		CustomItemRenderers.registerCustomBakedModel(ModItems.DEBUG_ITEM.getId(),				registry, BakedCustomModel::new);
		CustomItemRenderers.registerCustomBakedModel(ModItems.STAND_DISC.getId(),				registry, BakedCustomModel::new);
//		CustomItemRenderers.registerCustomBakedModel(ModItems.GLOVES.getId(),					registry, BakedCustomModel::new);
//		CustomItemRenderers.registerCustomBakedModel(ModItems.GLOVES_SOAP.getId(),				registry, BakedCustomModel::new);
//		CustomItemRenderers.registerCustomBakedModel(ModItems.CLACKERS.getId(),					registry, BakedCustomModel::new);
//		CustomItemRenderers.registerCustomBakedModel(JojoMod.resLoc("tommy_gun_flipped"),		registry, BakedCustomModel::new);
		BakedCustomModel tommyGunModel = 
		CustomItemRenderers.registerCustomBakedModel(ModItems.TOMMY_GUN.getId(),				registry, BakedCustomModel::new);
		CustomItemRenderers.registerCustomBakedModel(ModItems.POLAROID.getId(), 				registry, BakedCustomModel::new);
//		CustomItemRenderers.registerCustomBakedModel(ModItems.ROAD_ROLLER.getId(),				registry, BakedCustomModel::new);
		CustomItemRenderers.registerCustomBakedModel(ModItems.SEWING_MACHINE.getId(),			registry, BakedCustomModel::new);
		
		replaceOverrideWithCustomBakedModel(tommyGunModel.existingModel.getOverrides(), ModItems.TOMMY_GUN.getId(), 
				ModItems.TOMMY_GUN.getId().withPath(path -> path + "_flipped"), BakedCustomModel::new, modelBakery);
	}


	public static <T extends BakedModel> T registerCustomBakedModel(ResourceLocation itemResLoc, 
			Map<ModelResourceLocation, BakedModel> modelRegistry, Function<BakedModel, T> newModel) {
		ModelResourceLocation modelResLoc = ModelResourceLocation.inventory(itemResLoc);
		BakedModel existingModel = modelRegistry.get(modelResLoc);
		if (existingModel == null) {
			LOGGER.error("Did not find original {} model in registry", modelResLoc);
		}
		else if (existingModel.isCustomRenderer()) {
			LOGGER.error("Tried to replace {} model twice", modelResLoc);
		}
		else {
			T model = newModel.apply(existingModel);
			modelRegistry.put(modelResLoc, model);
			return model;
		}
		
		return null;
	}
	
	public static <T extends BakedModel> void replaceOverrideWithCustomBakedModel(ItemOverrides bakedOverrides, ResourceLocation topModelId, 
			ResourceLocation overrideModelId, Function<BakedModel, T> newModel, ModelBakery modelBakery) {
		topModelId = topModelId.withPath(path -> "models/item/" + path + ".json");
		overrideModelId = overrideModelId.withPath(path -> "item/" + path);
		
		Map<ResourceLocation, BlockModel> modelResources = modelBakery.modelResources;
		BlockModel itemModel = modelResources.get(topModelId);
		
		if (itemModel != null) {
			List<ItemOverride> overrideDefinitions = itemModel.getOverrides();
			int i = 0;
			for (int j = overrideDefinitions.size() - 1; j >= 0; j--) {
				ItemOverride overrideDefinition = overrideDefinitions.get(j);
				if (overrideDefinition.getModel().equals(overrideModelId)) {
					BakedOverride override = bakedOverrides.overrides[i];
					override.model = newModel.apply(override.model);
				}
				i++;
			}
		}
	}


	@SubscribeEvent
	public static void registerModelOverrides(FMLClientSetupEvent event) {
		ItemProperties.register(ModItems.KNIFE.get(), JojoMod.resLoc("count"), (itemStack, clientWorld, livingEntity, seed) -> {
			return livingEntity != null ? itemStack.getCount() : 1;
		});
//		ItemProperties.register(ModItems.STONE_MASK.get(), JojoMod.resLoc("stone_mask_activated"), (itemStack, clientWorld, livingEntity, seed) -> {
//			return itemStack.getTag().getByte(StoneMaskItem.NBT_ACTIVATION_KEY) > 0 ? 1 : 0;
//		});
		ItemProperties.register(ModItems.TOMMY_GUN.get(), JojoMod.resLoc("swing"), (itemStack, clientWorld, livingEntity, seed) -> {
			return livingEntity != null && livingEntity.swinging && livingEntity.getItemInHand(livingEntity.swingingArm) == itemStack ? 1 : 0;
		});
//		ItemProperties.register(Items.BOW, JojoMod.resLoc("stand_arrow"), (itemStack, clientWorld, livingEntity, seed) -> {
//			return livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack
//					&& livingEntity.getProjectile(itemStack).getItem() instanceof StandArrowItem ? 1 : 0;
//		});
//		ItemProperties.register(Items.CROSSBOW, JojoMod.resLoc("stand_arrow"), (itemStack, clientWorld, livingEntity, seed) -> {
//			return livingEntity != null && CrossbowItem.isCharged(itemStack) && (
//					CrossbowItem.containsChargedProjectile(itemStack, ModItems.STAND_ARROW.get()) || 
//					CrossbowItem.containsChargedProjectile(itemStack, ModItems.STAND_ARROW_BEETLE.get())) ? 1 : 0;
//		});
//		ItemProperties.register(ModItems.CASSETTE_RECORDED.get(), JojoMod.resLoc("cassette_distortion"), (itemStack, clientWorld, livingEntity, seed) -> {
//			return CassetteRecordedItem.getCassetteData(itemStack)
//					.map(cap -> MathHelper.clamp(cap.getGeneration(), 0, CassetteCap.MAX_GENERATION))
//					.orElse(0).floatValue();
//		});
		ItemProperties.register(ModItems.POLAROID.get(), JojoMod.resLoc("is_held"), (itemStack, clientWorld, livingEntity, seed) -> {
			return livingEntity != null && (livingEntity.getItemInHand(InteractionHand.MAIN_HAND) == itemStack || livingEntity.getItemInHand(InteractionHand.OFF_HAND) == itemStack) ? 1 : 0;
		});
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
