package com.imolatech.kinect.detector;

import java.util.HashMap;

import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;

public interface SkeletonObserver {
	void onUpdateSkeleton(int userId, 
			HashMap<SkeletonJoint, SkeletonJointPosition> skeleton);
}
