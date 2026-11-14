package com.github.standobyte.jojo.modcompat.interfaces.donotcall;

import com.github.standobyte.jojo.modcompat.interfaces.InterfaceIris;

import net.irisshaders.iris.api.v0.IrisApi;

public class InterfaceImplIris implements InterfaceIris {
	private final IrisApi irisApi;
	
	public InterfaceImplIris() {
		irisApi = IrisApi.getInstance();
	}

	@Override
	public boolean isShaderPackInUse() {
		return irisApi.isShaderPackInUse();
	}
}
