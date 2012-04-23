package com.imolatech.kinect.posture;

import com.imolatech.kinect.GestureWatcher;
import com.imolatech.kinect.Skeleton;

public abstract class BasePostureDetectionStrategy implements
		PostureDetectionStrategy {
	private boolean detected;
	protected GestureWatcher watcher;
	
	
	public BasePostureDetectionStrategy(GestureWatcher watcher) {
		super();
		this.watcher = watcher;
	}
	
	@Override
	public boolean isDetected() {
		return detected;
	}

	@Override
	public void setDetected(boolean detected) {
		this.detected = detected;
	}
	
	protected void postureDidStart(Skeleton skeleton) {
		watcher.pose(skeleton.getUserId(), getGestureName(), true); 
		setDetected(true);
	}
	
	protected void postureDidStop(Skeleton skeleton) {
		watcher.pose(skeleton.getUserId(), getGestureName(), false); 
		setDetected(false);
	}
}
