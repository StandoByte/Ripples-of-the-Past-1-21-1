package com.github.standobyte.jojo.client.entityanim.humanoid_bend;

import org.joml.Vector3f;

import com.github.standobyte.jojo.util.functions.MathUtil;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;

public class BendUtil {
	
	public static boolean isSamePivotAsParent(ModelPart modelPart) {
		PartPose initialPose = modelPart.getInitialPose();
		return 
				initialPose.xRot == 0 && initialPose.yRot == 0 && initialPose.zRot == 0 
				&& initialPose.x == 0 && initialPose.y == 0 && initialPose.z == 0;
	}
	
	
	// TODO smoother bends on high bend value
	public static void connectVertices(BendableLimb.LimbHalf limbPart, float bend, 
			float bendX, float bendY, float bendZ, boolean isBendPart) {
		for (ModelPart.Cube _cube : limbPart.cubes()) {
			DeformableCube cube = (DeformableCube) _cube;
			cube.reset();
			
			if (bend != 0) {
				float tan = MathUtil.tan(bend / 2);
				for (RememberingPos vertex : cube.distinctVertices) {
					Vector3f pos = vertex.mutablePos();
					
					float width = pos.z - bendZ;
					float yDiff = width * tan;
					
					if (pos.y == bendY) {
						if (isBendPart)	pos.y += yDiff;
						else			pos.y -= yDiff;
					}
				}
			}
		}
		
	}
	
}
