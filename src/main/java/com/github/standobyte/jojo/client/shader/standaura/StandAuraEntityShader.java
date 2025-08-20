package com.github.standobyte.jojo.client.shader.standaura;

import com.github.standobyte.jojo.client.ClientUtil;
import com.github.standobyte.jojo.client.shader.EntityShaders;
import com.github.standobyte.jojo.client.shader.SeparateBufferEntityShader;
import com.github.standobyte.jojo.core.JojoMod;
import com.github.standobyte.jojo.powersystem.standpower.entity.StandEntity;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;

@EventBusSubscriber(modid = JojoMod.MOD_ID, value = Dist.CLIENT)
public class StandAuraEntityShader extends SeparateBufferEntityShader {
	protected BufferSourceRecolor auraColor;
	
	public RenderTarget silhouetteBuffer;
	protected BufferSourceRecolor silhouetteBufferSource;
	
	public RenderTarget swapPixelated;
	public float screenRatio;
	
	public StandAuraEntityShader(Minecraft mc, String outputShardName, ResourceLocation postShaderId) {
		super(mc, outputShardName, postShaderId);
	}
	
	@Override
	protected void initTargetBuffer(Minecraft mc) {
		super.initTargetBuffer(mc);
		silhouetteBuffer = createBuffer(mc);
		
		int width = mc.getWindow().getWidth();
		int height = mc.getWindow().getHeight();
		swapPixelated = new TextureTarget(width, height, false, Minecraft.ON_OSX);
		swapPixelated.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
		swapPixelated.clear(Minecraft.ON_OSX);
	}
	
	@Override
	protected void createBufferSource(Minecraft mc, RenderBuffers vanillaRenderBuffers) {
		RenderStateShard targetShard = renderTypeModification();
		auraColor = new BufferSourceRecolor(
				new ByteBufferBuilder(786432), 
				new Object2ObjectLinkedOpenHashMap<>(), 
				targetShard, RenderStateShard.RENDERTYPE_OUTLINE_SHADER);
		bufferSource = auraColor;


		RenderStateShard.OutputStateShard silhouetteTargetShard = createTargetShard(outputShardName + "_silhouette", silhouetteBuffer);
		silhouetteBufferSource = new BufferSourceRecolor(
				new ByteBufferBuilder(786432), 
				new Object2ObjectLinkedOpenHashMap<>(), 
				silhouetteTargetShard, RenderStateShard.RENDERTYPE_OUTLINE_SHADER);
	}
	

	@Override
	protected void frameStart() {
		super.frameStart();
		silhouetteBuffer.clear(Minecraft.ON_OSX);
		swapPixelated.clear(Minecraft.ON_OSX);
		screenRatio = 135f / (float) Minecraft.getInstance().getWindow().getHeight();
	}
	
	@Override
	protected void setupBuffer() {
		super.setupBuffer();
		Minecraft mc = Minecraft.getInstance();
		silhouetteBuffer.copyDepthFrom(mc.getMainRenderTarget());
	}
	
	@Override
	protected void endBatch() {
		super.endBatch();
		((MultiBufferSource.BufferSource) this.silhouetteBufferSource).endBatch();
	}
	
	@Override
	protected void applyEffect() {
		super.applyEffect();
	}
	
	@Override
	protected void resize(int width, int height) {
		super.resize(width, height);
		if (silhouetteBuffer != null) {
			silhouetteBuffer.resize(width, height, Minecraft.ON_OSX);
		}
		
		swapPixelated.resize(width, height, Minecraft.ON_OSX);
	}


	public static int getStandAuraColor(LivingEntity entity) {
		if (entity instanceof Player || entity instanceof StandEntity) {
			return 0xFFFFD000;
		}
		if (entity instanceof Skeleton) {
			return 0xFFCB00FF;
		}
		if (entity instanceof Creeper) {
			return 0xFF009B02;
		}
		if (entity instanceof Sheep) {
			return 0xFFFF00F6;
		}
		return -1;
	}
	
	// FIXME crash (Not building!) when i equip an enchanted item
	// TODO stand aura shader
	/*
	 * alpha in outline color doesn't work (shader pass?)
	 * the entity doesn't render
	 * stand rendering breaks completely
	 */
	
	@SubscribeEvent
	public static <T extends LivingEntity, M extends EntityModel<T>> void afterEntityRender(RenderLivingEvent.Post<T, M> event) {
		StandAuraEntityShader shader = EntityShaders.standAura;
		if (shader._renderingNow) return;
		
		int color = getStandAuraColor(event.getEntity());
		if (color == -1) return;
		shader.auraColor.setColor(color);
		shader.silhouetteBufferSource.setColor(color);

		MultiBufferSource source = shader.useBufferSourceThisFrame();
		shader._renderingNow = true;
		
		Minecraft mc = Minecraft.getInstance();
		shader.frameBuffer.copyDepthFrom(mc.getMainRenderTarget());
		shader.silhouetteBuffer.copyDepthFrom(mc.getMainRenderTarget());
		
		LivingEntityRenderer<T, M> renderer = event.getRenderer();
		T entity = (T) event.getEntity();
		float partialTick = event.getPartialTick();
		float entityYaw = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
		PoseStack poseStack = event.getPoseStack();
		AuraUtil.inflateEachCube = 4.0f;
		renderer.render(entity, entityYaw, partialTick, poseStack, source, ClientUtil.MAX_LIGHT);
		AuraUtil.inflateEachCube = null;
		renderer.render(entity, entityYaw, partialTick, poseStack, shader.silhouetteBufferSource, ClientUtil.MAX_LIGHT);

		shader._renderingNow = false;
	}

}
