package com.imolatech.kinect;

import java.util.HashMap;

import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;

public interface SkeletonObserver {
	void onUpdateSkeleton(int userId, 
			HashMap<SkeletonJoint, SkeletonJointPosition> skeleton);
	void onUpdateSkeletons(HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> skeletons);
}
