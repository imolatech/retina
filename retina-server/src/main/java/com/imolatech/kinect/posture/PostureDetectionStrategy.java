package com.imolatech.kinect.posture;

import com.imolatech.kinect.GestureName;
import com.imolatech.kinect.Skeleton;

public interface PostureDetectionStrategy {
	void detect(Skeleton skeleton);
	boolean isDetected();
	void setDetected(boolean detected);
	GestureName getGestureName();
}
