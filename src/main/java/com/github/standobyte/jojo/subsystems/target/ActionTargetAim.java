package com.github.standobyte.jojo.subsystems.target;

/**
 * Aim target that is constantly synchronized from player client to server
 */
public class ActionTargetAim {
	protected ActionTarget target = ActionTarget.EMPTY;
	protected ActionTarget targetPrev = ActionTarget.EMPTY;
	
	public void setTarget(ActionTarget target) {
		this.target = target;
	}
	
	public boolean checkDirty() {
		boolean dirty = !target.equals(targetPrev);
		this.targetPrev = target;
		return dirty;
	}
	
	public ActionTarget getTarget() {
		return target;
	}
	
}
