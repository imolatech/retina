package com.imolatech.kinect.posture;

import org.OpenNI.Point3D;
import org.OpenNI.SkeletonJoint;

import com.imolatech.kinect.GestureName;
import com.imolatech.kinect.GestureWatcher;
import com.imolatech.kinect.Skeleton;

/**
 * detect if the user's hand at hip level or below?
 * @author Wenhu
 *
 */
public class HandDownDetectionStrategy extends BasePostureDetectionStrategy {
	private boolean isRightHand;
	
	public HandDownDetectionStrategy(boolean isRightHand, GestureWatcher watcher) {
		super(watcher);
		this.isRightHand = isRightHand;
	}

	@Override
	public GestureName getGestureName() {
		if (isRightHand) return GestureName.RH_DOWN;
		return GestureName.LH_DOWN;
	}
	
	@Override
	public void detect(Skeleton skeleton) {
		Point3D handPt;
		Point3D hipPt = skeleton.getJointPosition(SkeletonJoint.RIGHT_HIP);
		
		if (isRightHand) {
			handPt = skeleton.getJointPosition(SkeletonJoint.RIGHT_HAND);
		} else {
			handPt = skeleton.getJointPosition(SkeletonJoint.LEFT_HAND);
		}
		
		if ((handPt == null) || (hipPt == null)) return;
		
		if (handPt.getY() >= hipPt.getY()) { // below
			if (!isDetected()) {
				postureDidStart(skeleton);
			}
		} else if (isDetected()) {
			postureDidStop(skeleton);
		}
		
	}

}
