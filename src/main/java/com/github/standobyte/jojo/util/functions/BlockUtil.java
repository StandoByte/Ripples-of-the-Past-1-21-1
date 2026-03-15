package com.github.standobyte.jojo.util.functions;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockUtil {

	public static class BoxShapeHorizontalRot {
		public final Map<Direction, VoxelShape> map = new EnumMap<>(Direction.class);
		
		public static BoxShapeHorizontalRot box(double x1, double y1, double z1, double x2, double y2, double z2) {
			BoxShapeHorizontalRot map = new BoxShapeHorizontalRot();
			x1 /= 16; y1 /= 16; z1 /= 16; x2 /= 16; y2 /= 16; z2 /= 16;
			
			for (Direction dir : Direction.Plane.HORIZONTAL) {
				if (dir != Direction.NORTH) {
					double _x1 = x1;
					double _x2 = x2;
					x1 = 1 - z1;
					x2 = 1 - z2;
					z1 = _x1;
					z2 = _x2;
				}
				VoxelShape shape = Shapes.box(
						Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2), 
						Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2));
				map.map.put(dir, shape);
			}
			
			return map;
		}
		
		public static Map<Direction, VoxelShape> or(BoxShapeHorizontalRot shape1, BoxShapeHorizontalRot... shapes) {
			Map<Direction, VoxelShape> map = new EnumMap<>(Direction.class);
			VoxelShape[] varArgs = new VoxelShape[shapes.length];
			for (Direction dir : Direction.Plane.HORIZONTAL) {
				for (int i = 0; i < varArgs.length; i++) {
					varArgs[i] = shapes[i].map.get(dir);
				}
				map.put(dir, Shapes.or(shape1.map.get(dir), varArgs));
			}
			return map;
		}
	}
}
