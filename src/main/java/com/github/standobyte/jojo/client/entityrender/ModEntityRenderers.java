package com.github.standobyte.jojo.client.entityrender;

import java.util.Optional;

import com.github.standobyte.jojo.client.entityrender.entities.BlockShardRenderer;
import com.github.standobyte.jojo.client.entityrender.entities.MannequinRenderer;
import com.github.standobyte.jojo.client.entityrender.entities.SimpleEntityModel;
import com.github.standobyte.jojo.client.entityrender.entities.SimpleEntityRenderer;
import com.github.standobyte.jojo.client.entityrender.entities.v1_21_2plus.MannequinModel_1_21_2plus;
import com.github.standobyte.jojo.client.entityrender.stand.StandEntityRenderer;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.init.ModEntityTypes;
import com.github.standobyte.jojo.mechanics.clothes.client.layer.HumanoidClothesLayer;
import com.github.standobyte.jojo.mechanics.entity_like_player.npc.client.CharacterMobRenderer;
import com.github.standobyte.jojoimpl.stands.crazydiamond.client.CrazyDBlockBulletRenderer;
import com.github.standobyte.v1_21_4_stuff.Reminder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class ModEntityRenderers {
	
	// Entity renderers
	
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(ModEntityTypes.CHARACTER.get(), CharacterMobRenderer::new);
		event.registerEntityRenderer(ModEntityTypes.HUMANOID_STAND.get(), StandEntityRenderer::new);
		event.registerEntityRenderer(ModEntityTypes.MANNEQUIN.get(), MannequinRenderer::new);
		event.registerEntityRenderer(ModEntityTypes.BLOCK_SHARD.get(), BlockShardRenderer::new);
		event.registerEntityRenderer(ModEntityTypes.NUGGET_BEARING.get(), ctx -> new ThrownItemRenderer<>(ctx, 0.5f, false));
		event.registerEntityRenderer(ModEntityTypes.CD_BLOOD_CUTTER.get(), ctx -> new SimpleEntityRenderer<>(ctx)
				.initTexture(JojoMod.resLoc("textures/entity/blood_cutter.png"), true)
				.initResourceModel(JojoMod.resLoc("blood_cutter"), SimpleEntityModel::new, true));
		event.registerEntityRenderer(ModEntityTypes.CD_BLOCK_BULLET.get(), ctx -> new CrazyDBlockBulletRenderer(ctx)
				.initResourceModel(JojoMod.resLoc("block_bullet"), SimpleEntityModel::new, true));
	}
	
	// Hardcoded models
	
	public static final ModelLayerLocation MANNEQUIN = mainLayer(ResourceLocation.fromNamespaceAndPath(JojoMod.MOD_ID, "mannequin"));
	public static final ModelLayerLocation MANNEQUIN_SLIM = mainLayer(ResourceLocation.fromNamespaceAndPath(JojoMod.MOD_ID, "mannequin_slim"));
//	public static final ModelLayerLocation MANNEQUIN_SMALL = mainLayer(ResourceLocation.fromNamespaceAndPath(JojoMod.MOD_ID, "mannequin_small"));
//	public static final ModelLayerLocation MANNEQUIN_SLIM_SMALL = mainLayer(ResourceLocation.fromNamespaceAndPath(JojoMod.MOD_ID, "mannequin_slim_small"));
	
	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		LayerDefinition mannequin = MannequinModel_1_21_2plus.createMesh(CubeDeformation.NONE, false);
		LayerDefinition mannequinSlim = MannequinModel_1_21_2plus.createMesh(CubeDeformation.NONE, true);
		event.registerLayerDefinition(ModEntityRenderers.MANNEQUIN, () -> mannequin);
		event.registerLayerDefinition(ModEntityRenderers.MANNEQUIN_SLIM, () -> mannequinSlim);
		Reminder.toRegisterBabyModels();
//		event.registerLayerDefinition(ModEntityRenderers.MANNEQUIN_SMALL, () -> mannequin.apply(HumanoidModel.BABY_TRANSFORMER));
//		event.registerLayerDefinition(ModEntityRenderers.MANNEQUIN_SLIM_SMALL, () -> mannequinSlim.apply(HumanoidModel.BABY_TRANSFORMER));
	}
	
	public static ModelLayerLocation mainLayer(ResourceLocation modelPath) {
		return new ModelLayerLocation(modelPath, "main");
	}
	
	// Entity render state extensions
	
//	public static final ContextKey<HumanoidClothesRSExtension> CLOTHES_CONTEXT = new ContextKey<>(JojoMod.resLoc("clothes"));
//
//	@SuppressWarnings("serial")
//	@SubscribeEvent
//	public static void registerRSModifiers(RegisterRenderStateModifiersEvent event) {
//		event.registerEntityModifier(
//				new TypeToken<LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?>>(){},
//				(entity, state) -> {
//					if (HumanoidClothesRSExtension.reusedInstance.extract(entity)) {
//						state.setRenderData(CLOTHES_CONTEXT, HumanoidClothesRSExtension.reusedInstance);
//					}
//				});
//	}
	
	// Entity renderer layers
	
	@SubscribeEvent
	public static void addLayers(EntityRenderersEvent.AddLayers event) {
		var renderers = Minecraft.getInstance().getEntityRenderDispatcher();
		for (var renderer : renderers.renderers.values()) {
			castToHumanoid(renderer).ifPresent(ModEntityRenderers::addHumanoidLayers);
		}
		for (var playerRenderer : renderers.getSkinMap().values()) {
			castToHumanoid(playerRenderer).ifPresent(ModEntityRenderers::addHumanoidLayers);
		}
	}
	
	private static <T extends LivingEntity, M extends HumanoidModel<T>> void addHumanoidLayers(LivingEntityRenderer<T, M> renderer) {
		renderer.addLayer(new HumanoidClothesLayer<>(renderer));
	}
	
	
	
	@SuppressWarnings("unchecked")
	public static <T extends LivingEntity, M extends HumanoidModel<T>> Optional<LivingEntityRenderer<T, M>> castToHumanoid(EntityRenderer<?> renderer) {
		if (renderer instanceof LivingEntityRenderer livingRenderer && livingRenderer.getModel() instanceof HumanoidModel /* && renderer.reusedState instanceof HumanoidRenderState*/) {
			var humanoid = (LivingEntityRenderer<T, M>) renderer;
			return Optional.of(humanoid);
		}
		return Optional.empty();
	}
}
