package com.github.standobyte.jojo.client.itemrender.standdisc;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.github.standobyte.jojo.client.standskin.StandSkin;
import com.github.standobyte.jojo.client.standskin.StandSkinsLoader;
import com.github.standobyte.jojo.client.ui.utils.GuiIcon;
import com.github.standobyte.jojo.init.ModItemDataComponents;
import com.github.standobyte.jojo.mechanics.standdisc.StandWrittenOnDisc;
import com.github.standobyte.jojo.powersystem.standpower.StandInstance;
import com.github.standobyte.jojo.powersystem.standpower.type.StandType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.RenderTypeHelper;

public class StandDiscRenderer extends BlockEntityWithoutLevelRenderer {

	public StandDiscRenderer(Minecraft mc) {
		super(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
	}

	@Override
	public void renderByItem(ItemStack itemStack, ItemDisplayContext displayContext, PoseStack poseStack, 
			MultiBufferSource renderTypeBuffer, int light, int overlay) {
		Minecraft mc = Minecraft.getInstance();
		ItemRenderer ir = mc.getItemRenderer();
		BakedModel pModel = ir.getModel(itemStack, null, null, 0);

		RenderType rendertype = RenderTypeHelper.getFallbackItemRenderType(itemStack, pModel, true);
		VertexConsumer ivertexbuilder = ItemRenderer.getFoilBufferDirect(
				renderTypeBuffer, rendertype, true, itemStack.hasFoil());
		ir.renderModelLists(pModel, itemStack, light, overlay, poseStack, ivertexbuilder);

		StandWrittenOnDisc discStand = itemStack.get(ModItemDataComponents.DISC_STAND.get());
		if (discStand != null) {
			StandInstance stand = discStand.getInstance();
			if (stand != null) {
				int color = mc.getItemColors().getColor(itemStack, 0);
				color |= 0xFFFFFF; // keep the alpha of the main disc model
				renderStandIcon(poseStack, stand, itemStack, renderTypeBuffer, light, overlay, color);
			}
		}
	}

	protected void renderStandIcon(PoseStack poseStack, StandInstance stand, ItemStack discItem, 
			MultiBufferSource buffer, int light, int overlay, int color) {
		StandSkin standSkin = StandSkinsLoader.getInstance().getSkin(stand);
		if (standSkin != null) {
			GuiIcon icon = standSkin.getStandIcon();

			VertexConsumer vertexBuilder = ItemRenderer.getFoilBufferDirect(
					buffer, RenderType.entityTranslucent(icon.file), 
					false, discItem.hasFoil());

			renderIconQuad(poseStack.last(), QUAD_FRONT, vertexBuilder, light, overlay, color);
			renderIconQuad(poseStack.last(), QUAD_BACK, vertexBuilder, light, overlay, color);
		}

	}

	static {
		float x0 = 1;
		float x1 = 7;
		float y0 = 5;
		float y1 = 11;
		float z0 = 7.498F;
		float z1 = 8.502F;
		ModelPart.Vertex vertex7 = new ModelPart.Vertex(
				x0, y0, z0, 0.0F, 0.0F);
		ModelPart.Vertex vertex = new ModelPart.Vertex(
				x1, y0, z0, 0.0F, 8.0F);
		ModelPart.Vertex vertex1 = new ModelPart.Vertex(
				x1, y1, z0, 8.0F, 8.0F);
		ModelPart.Vertex vertex2 = new ModelPart.Vertex(
				x0, y1, z0, 8.0F, 0.0F);
		ModelPart.Vertex vertex3 = new ModelPart.Vertex(
				x0, y0, z1, 0.0F, 0.0F);
		ModelPart.Vertex vertex4 = new ModelPart.Vertex(
				x1, y0, z1, 0.0F, 8.0F);
		ModelPart.Vertex vertex5 = new ModelPart.Vertex(
				x1, y1, z1, 8.0F, 8.0F);
		ModelPart.Vertex vertex6 = new ModelPart.Vertex(
				x0, y1, z1, 8.0F, 0.0F);

		QUAD_FRONT = new ModelPart.Polygon(
				new ModelPart.Vertex[]{
						vertex2, 
						vertex1,
						vertex, 
						vertex7 
				}, 
				0, 0, 16, 16, 
				16, 16, false, Direction.NORTH);

		QUAD_BACK = new ModelPart.Polygon(
				new ModelPart.Vertex[]{ 
						vertex5, 
						vertex6,
						vertex3, 
						vertex4}, 
				0, 0, 16, 16, 
				16, 16, false, Direction.SOUTH);
	}

	protected static ModelPart.Polygon QUAD_FRONT;
	protected static ModelPart.Polygon QUAD_BACK;

	protected void renderIconQuad(PoseStack.Pose poseEntry, ModelPart.Polygon quad, 
			VertexConsumer vertexBuilder, int light, int overlay, int color) {
		Matrix4f pose = poseEntry.pose();
		Vector3f vector3f = new Vector3f();

		Vector3f normal = poseEntry.transformNormal(quad.normal, vector3f);
		float normalX = normal.x();
		float normalY = normal.y();
		float normalZ = normal.z();

		for (int i = 0; i < quad.vertices.length; ++i) {
			ModelPart.Vertex vertex = quad.vertices[i];
			float vertexX = vertex.pos.x() / 16.0F;
			float vertexY = vertex.pos.y() / 16.0F;
			float vertexZ = vertex.pos.z() / 16.0F;

			Vector3f vector3f2 = pose.transformPosition(vertexX, vertexY, vertexZ, vector3f);
			vertexBuilder.addVertex(
					vector3f2.x(), vector3f2.y(), vector3f2.z(), 
					color, vertex.u, vertex.v, 
					overlay, light, normalX, normalY, normalZ);
		}
	}
	
	
	public static int getItemModelLayerColor(ItemStack item, int layer) {
		StandWrittenOnDisc discStand = item.get(ModItemDataComponents.DISC_STAND.get());
		StandInstance stand = discStand != null ? discStand.getInstance() : null;
		int color = switch (layer) {
			case 1 -> lightDiscTint(StandDiscRenderer.getStandColor(stand));
			case 2 -> getStandColor(stand);
			default -> 0xFFFFFFFF;
		};
		
		if (stand != null) {
			StandType standType = stand.getStandType();
			if (standType != null && standType.translucentDisc) {
				color &= 0x60FFFFFF;
			}
		}
		
		return color;
	}
	
	public static int getStandColor(StandInstance stand) {
		if (stand != null) {
			StandSkin skin = StandSkinsLoader.getInstance().getSkin(stand);
			if (skin != null) {
				return skin.getColor();
			}
		}
		
		return -1;
	}

	public static int lightDiscTint(int color) {
		return (((0xFFFFFF - color) & 0xFEFEFE) >> 1) + color;
	}
	
}
