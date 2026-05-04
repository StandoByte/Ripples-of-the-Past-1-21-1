package com.github.standobyte.jojo.client.shader.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

import javax.annotation.Nullable;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.machinezoo.noexception.optional.OptionalBoolean;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.GsonHelper;

public class ManualInitPostChain implements AutoCloseable {
	protected static final String MAIN_RENDER_TARGET = "minecraft:main";
	public RenderTarget screenTarget;
	protected final ResourceProvider resourceProvider;
	protected final String name;
	public final List<PostPass> passes = Lists.newArrayList();
	public final Map<String, RenderTarget> customRenderTargets = Maps.newHashMap();
	public final List<RenderTarget> fullSizedTargets = Lists.newArrayList();
	protected Matrix4f shaderOrthoMatrix;
	protected int screenWidth;
	protected int screenHeight;
	protected float time;
	protected float lastStamp;

	public ManualInitPostChain(TextureManager textureManager, ResourceProvider resourceProvider, RenderTarget screenTarget, ResourceLocation resourceLocation) {
		this.resourceProvider = resourceProvider;
		this.screenTarget = screenTarget;
		this.time = 0.0F;
		this.lastStamp = 0.0F;
		this.screenWidth = screenTarget.viewWidth;
		this.screenHeight = screenTarget.viewHeight;
		this.name = resourceLocation.toString();
		this.updateOrthoMatrix();
	}

	public void afterInit(Minecraft mc) {
		Window window = mc.getWindow();
		this.resize(window.getWidth(), window.getHeight());
	}


	public RenderTarget getTempTarget(String attributeName) {
		return this.customRenderTargets.get(attributeName);
	}

	public void addTempTarget(String name, int width, int height) {
		RenderTarget rendertarget = new TextureTarget(width, height, true, Minecraft.ON_OSX);
		rendertarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
		if (screenTarget.isStencilEnabled()) { rendertarget.enableStencil(); }
		this.customRenderTargets.put(name, rendertarget);
		if (width == this.screenWidth && height == this.screenHeight) {
			this.fullSizedTargets.add(rendertarget);
		}
	}

	@Override
	public void close() {
		for (RenderTarget rendertarget : this.customRenderTargets.values()) {
			rendertarget.destroyBuffers();
		}

		for (PostPass postpass : this.passes) {
			postpass.close();
		}

		this.passes.clear();
	}

	public PostPass addPass(String name, RenderTarget inTarget, RenderTarget outTarget, boolean useLinearFilter) throws IOException {
		PostPass postpass = new PostPass(this.resourceProvider, name, inTarget, outTarget, useLinearFilter);
		this.passes.add(this.passes.size(), postpass);
		return postpass;
	}

	protected void updateOrthoMatrix() {
		this.shaderOrthoMatrix = new Matrix4f().setOrtho(0.0F, (float)this.screenTarget.width, 0.0F, (float)this.screenTarget.height, 0.1F, 1000.0F);
	}

	public void resize(int width, int height) {
		this.screenWidth = this.screenTarget.width;
		this.screenHeight = this.screenTarget.height;
		this.updateOrthoMatrix();

		for (PostPass postpass : this.passes) {
			postpass.setOrthoMatrix(this.shaderOrthoMatrix);
		}

		for (RenderTarget rendertarget : this.fullSizedTargets) {
			rendertarget.resize(width, height, Minecraft.ON_OSX);
		}
	}

	protected void setFilterMode(int filterMode) {
		this.screenTarget.setFilterMode(filterMode);

		for (RenderTarget rendertarget : this.customRenderTargets.values()) {
			rendertarget.setFilterMode(filterMode);
		}
	}
	
	public void process(float partialTick) {
		process(this.passes, partialTick);
	}

	public void process(Iterable<PostPass> passes, float partialTicks) {
		this.time += partialTicks;

		while (this.time > 20.0F) {
			this.time -= 20.0F;
		}

		int i = 9728;

		for (PostPass postpass : passes) {
			int j = postpass.getFilterMode();
			if (i != j) {
				this.setFilterMode(j);
				i = j;
			}

			postpass.process(this.time / 20.0F);
		}

		this.setFilterMode(9728);
	}

	public void setUniform(String name, float backgroundBlurriness) {
		for (PostPass postpass : this.passes) {
			postpass.getEffect().safeGetUniform(name).set(backgroundBlurriness);
		}
	}

	public final String getName() {
		return this.name;
	}

	@Nullable
	protected RenderTarget getRenderTarget(@Nullable String target) {
		if (target == null) {
			return null;
		} else {
			return target.equals(MAIN_RENDER_TARGET) ? this.screenTarget : this.customRenderTargets.get(target);
		}
	}
	
	
	public void setMainBuffer(RenderTarget target) {
		for (PostPass pass : passes) {
			if (pass.inTarget == this.screenTarget) pass.inTarget = target;
			if (pass.outTarget == this.screenTarget) pass.outTarget = target;
		}
		this.screenTarget = target;
	}



	public static record PostChainDefinition(TempTargetDefinition[] targets, PassDefinition[] passes) {}

	public static record TempTargetDefinition(String name, OptionalInt width, OptionalInt height) {
		public TempTargetDefinition(String name) { this(name, OptionalInt.empty(), OptionalInt.empty()); }
	}

	public static record PassDefinition(String name, String intarget, String outtarget, boolean use_linear_filter, 
			@Nullable AuxTargetDefinition[] auxtargets, @Nullable UniformDefinition[] uniforms) {}

	public static record AuxTargetDefinition(String name, String id, OptionalInt width, OptionalInt height, OptionalBoolean bilinear) {}

	public static record UniformDefinition(String name, float[] values) {}

	public static void init(ManualInitPostChain postChain, PostChainDefinition definition, TextureManager textureManager) throws IOException {
		if (definition.targets != null) {
			for (TempTargetDefinition target : definition.targets) {
				addTempTarget(postChain, target);
			}
		}

		if (definition.passes != null) {
			for (PassDefinition passDef : definition.passes) {
				addPassNode(postChain, passDef, textureManager);
			}
		}
	}

	public static RenderTarget addTempTarget(ManualInitPostChain postChain, TempTargetDefinition target) {
		postChain.addTempTarget(target.name, 
				target.width.orElse(postChain.screenWidth), 
				target.height.orElse(postChain.screenHeight));
		return postChain.customRenderTargets.get(target.name);
	}

	public static PostPass addPassNode(ManualInitPostChain postChain, PassDefinition pass, TextureManager textureManager) throws IOException {
		RenderTarget inTarget = postChain.getRenderTarget(pass.intarget);
		RenderTarget outTarget = postChain.getRenderTarget(pass.outtarget);
		if (inTarget == null) {
			throw new IllegalStateException("Input target '" + pass.intarget + "' does not exist");
		} else if (outTarget == null) {
			throw new IllegalStateException("Output target '" + pass.outtarget + "' does not exist");
		} else {
			PostPass postpass = postChain.addPass(pass.name, inTarget, outTarget, pass.use_linear_filter);
			if (pass.auxtargets != null) {
				for (AuxTargetDefinition auxtarget : pass.auxtargets) {
					boolean isDepthBuffer;
					String bufferName;
					if (auxtarget.id.endsWith(":depth")) {
						isDepthBuffer = true;
						bufferName = auxtarget.id.substring(0, auxtarget.id.lastIndexOf(':'));
					} else {
						isDepthBuffer = false;
						bufferName = auxtarget.id;
					}

					RenderTarget auxTarget = postChain.getRenderTarget(bufferName);
					if (auxTarget == null) {
						if (isDepthBuffer) {
							throw new IllegalStateException("Render target '" + bufferName + "' can't be used as depth buffer");
						}

						ResourceLocation texPath = ResourceLocation.tryParse(bufferName).withPath(path -> "textures/effect/" + path + ".png");
						postChain.resourceProvider.getResource(texPath).orElseThrow(
								() -> new IllegalStateException("Render target or texture '" + bufferName + "' does not exist"));
						RenderSystem.setShaderTexture(0, texPath);
						textureManager.bindForSetup(texPath);
						AbstractTexture abstracttexture = textureManager.getTexture(texPath);
						if (auxtarget.bilinear.orElse(false)) {
							RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
							RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
						} else {
							RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
							RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
						}

						postpass.addAuxAsset(auxtarget.name, abstracttexture::getId, auxtarget.width.getAsInt(), auxtarget.height.getAsInt());
					} else if (isDepthBuffer) {
						postpass.addAuxAsset(auxtarget.name, auxTarget::getDepthTextureId, auxTarget.width, auxTarget.height);
					} else {
						postpass.addAuxAsset(auxtarget.name, auxTarget::getColorTextureId, auxTarget.width, auxTarget.height);
					}
				}
			}

			if (pass.uniforms != null) {
				for (UniformDefinition uniformDef : pass.uniforms) {
					Uniform uniform = postpass.getEffect().getUniform(uniformDef.name);
					if (uniform == null) {
						throw new IllegalStateException("Uniform '" + uniformDef.name + "' does not exist");
					} else {
						switch (uniformDef.values.length) {
							case 0:
							default:
								break;
							case 1:
								uniform.set(uniformDef.values[0]);
								break;
							case 2:
								uniform.set(uniformDef.values[0], uniformDef.values[1]);
								break;
							case 3:
								uniform.set(uniformDef.values[0], uniformDef.values[1], uniformDef.values[2]);
								break;
							case 4:
								uniform.set(uniformDef.values[0], uniformDef.values[1], uniformDef.values[2], uniformDef.values[3]);
						}
					}
				}
			}
			
			return postpass;
		}
	}


	
	public static PostChainDefinition parsePostChain(JsonObject json) {
		List<TempTargetDefinition> targets = new ArrayList<>();
		if (GsonHelper.isArrayNode(json, "targets")) {
			JsonArray targetsJson = json.getAsJsonArray("targets");
			for (JsonElement targetJson : targetsJson) {
				TempTargetDefinition target = parseTargetNode(targetJson);
				if (target != null) {
					targets.add(target);
				}
			}
		}

		List<PassDefinition> passes = new ArrayList<>();
		if (GsonHelper.isArrayNode(json, "passes")) {
			JsonArray passesJson = json.getAsJsonArray("passes");
			for (JsonElement passJson : passesJson) {
				PassDefinition pass = parsePassNode(GsonHelper.convertToJsonObject(passJson, "pass"));
				if (pass != null) {
					passes.add(pass);
				}
			}
		}
		
		return new PostChainDefinition(
				targets.toArray(TempTargetDefinition[]::new), 
				passes.toArray(PassDefinition[]::new));
	}

	public static TempTargetDefinition parseTargetNode(JsonElement json) {
		if (GsonHelper.isStringValue(json)) {
			return new TempTargetDefinition(json.getAsString());
		}
		else {
			JsonObject target = GsonHelper.convertToJsonObject(json, "target");
			String name = GsonHelper.getAsString(target, "name");
			return new TempTargetDefinition(name, 
					optionalInt(target, "width"), optionalInt(target, "height"));
		}
	}

	public static PassDefinition parsePassNode(JsonObject json) {
		String name = GsonHelper.getAsString(json, "name");
		String inTargetName = GsonHelper.getAsString(json, "intarget");
		String outTargetName = GsonHelper.getAsString(json, "outtarget");
		boolean useLinearFilter = GsonHelper.getAsBoolean(json, "use_linear_filter", false);
		
		List<AuxTargetDefinition> auxtargets = new ArrayList<>();
		JsonArray auxtargetsJson = GsonHelper.getAsJsonArray(json, "auxtargets", null);
		if (auxtargetsJson != null) {
			for (JsonElement auxtargetJson : auxtargetsJson) {
				AuxTargetDefinition auxtarget = parseAuxAsset(auxtargetJson);
				if (auxtarget != null) {
					auxtargets.add(auxtarget);
				}
			}
		}

		List<UniformDefinition> uniforms = new ArrayList<>();
		JsonArray uniformsJson = GsonHelper.getAsJsonArray(json, "uniforms", null);
		if (uniformsJson != null) {
			for (JsonElement uniformJson : uniformsJson) {
				UniformDefinition uniform = parseUniformNode(GsonHelper.convertToJsonObject(uniformJson, "uniform"));
				if (uniform != null) {
					uniforms.add(uniform);
				}
			}
		}
		
		return new PassDefinition(name, inTargetName, outTargetName, useLinearFilter, 
				auxtargets.toArray(AuxTargetDefinition[]::new), uniforms.toArray(UniformDefinition[]::new));
	}

	public static AuxTargetDefinition parseAuxAsset(JsonElement json) {
		JsonObject auxObjJson = GsonHelper.convertToJsonObject(json, "auxtarget");
		String auxName = GsonHelper.getAsString(auxObjJson, "name");
		String auxId = GsonHelper.getAsString(auxObjJson, "id");
		AuxTargetDefinition auxTarget = new AuxTargetDefinition(auxName, auxId,
				optionalInt(auxObjJson, "width"), optionalInt(auxObjJson, "height"), optionalBoolean(auxObjJson, "bilinear"));
		return auxTarget;
	}

	public static UniformDefinition parseUniformNode(JsonObject json)  {
		String name = GsonHelper.getAsString(json, "name");
		float[] parsed = new float[4];
		int i = 0;

		for (JsonElement value : GsonHelper.getAsJsonArray(json, "values")) {
			parsed[i] = GsonHelper.convertToFloat(value, "value");
			i++;
		}

		float[] values = switch (i) {
			case 0 -> new float[] {};
			case 1 -> new float[] { parsed[0] };
			case 2 -> new float[] { parsed[0], parsed[1] };
			case 3 -> new float[] { parsed[0], parsed[1], parsed[2] };
			default -> new float[] { parsed[0], parsed[1], parsed[2], parsed[3] };
		};
		return new UniformDefinition(name, values);
	}
	
	
	public static OptionalInt optionalInt(JsonObject json, String elementName) {
		return json.has(elementName) ? OptionalInt.of(GsonHelper.convertToInt(json.get(elementName), elementName)) : OptionalInt.empty();
	}
	
	public static OptionalBoolean optionalBoolean(JsonObject json, String elementName) {
		return json.has(elementName) ? OptionalBoolean.of(GsonHelper.convertToBoolean(json.get(elementName), elementName)) : OptionalBoolean.empty();
	}

}
