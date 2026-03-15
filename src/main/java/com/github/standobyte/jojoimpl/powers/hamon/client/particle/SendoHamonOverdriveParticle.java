package com.github.standobyte.jojoimpl.powers.hamon.client.particle;

import com.github.standobyte.jojo.util.functions.MathUtil;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SendoHamonOverdriveParticle extends HamonSparkParticle {
	private Direction.Axis blockAxis;

	public SendoHamonOverdriveParticle(ClientLevel level, double x, double y, double z, 
			double xd, double yd, double zd, Direction.Axis blockAxis) {
		super(level, x, y, z, xd, yd, zd);
		this.xd = xd;
		this.yd = yd;
		this.zd = zd;
		this.blockAxis = blockAxis;
	}

	@Override
	public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
		if (blockAxis != null) {
			BlockPos blockPos;
			switch (blockAxis) {
			case X:
				blockPos = new BlockPos(MathUtil.round(x), Mth.floor(y), Mth.floor(z));
				if (
						!hasFaceAt(blockPos,                  Direction.WEST) && 
						!hasFaceAt(blockPos.offset(-1, 0, 0), Direction.EAST)) 
					return;
				break;
			case Y:
				blockPos = new BlockPos(Mth.floor(x), MathUtil.round(y), Mth.floor(z));
				if (
						!hasFaceAt(blockPos,                  Direction.DOWN) && 
						!hasFaceAt(blockPos.offset(0, -1, 0), Direction.UP)) 
					return;
				break;
			case Z:
				blockPos = new BlockPos(Mth.floor(x), Mth.floor(y), MathUtil.round(z));
				if (
						!hasFaceAt(blockPos,                  Direction.NORTH) && 
						!hasFaceAt(blockPos.offset(0, 0, -1), Direction.SOUTH)) 
					return;
				break;
			}
		}

		super.render(pBuffer, pRenderInfo, pPartialTicks);
	}

	protected boolean hasFaceAt(BlockPos blockPos, Direction faceDir) {
		BlockState blockState = level.getBlockState(blockPos);
		VoxelShape shape = blockState.getCollisionShape(level, blockPos);
		if (shape.isEmpty()) {
			return false;
		}
		VoxelShape face = shape.getFaceShape(faceDir);
		if (face.isEmpty()) {
			return false;
		}

		return true;
	}

	@Override
	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.age++ >= this.lifetime) {
			this.remove();
		} else {
			this.move(this.xd, this.yd, this.zd);
		}
	}
}
