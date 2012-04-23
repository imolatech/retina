package com.imolatech.kinect.posture;

import org.OpenNI.Point3D;
import org.OpenNI.SkeletonJoint;

import com.imolatech.kinect.GestureName;
import com.imolatech.kinect.GestureWatcher;
import com.imolatech.kinect.Skeleton;

/**
 * detect if the user's right hand forward of his right shoulder, or
 * left hand forward of his left shoulder.
 * @author Wenhu
 *
 */
public class HandForwardDetectionStrategy extends BasePostureDetectionStrategy {
	private boolean isRightHand;
	
	public HandForwardDetectionStrategy(boolean isRightHand, GestureWatcher watcher) {
		super(watcher);
		this.isRightHand = isRightHand;
	}

	@Override
	public GestureName getGestureName() {
		if (isRightHand) return GestureName.RH_FWD;
		return GestureName.LH_FWD;
	}
	
	@Override
	public void detect(Skeleton skeleton) {
		
		Point3D handPt;
		Point3D shoulderPt;
		if (isRightHand) {
			handPt = skeleton.getJointPosition(SkeletonJoint.RIGHT_HAND);
			shoulderPt = skeleton.getJointPosition(SkeletonJoint.RIGHT_SHOULDER);
		} else {
			handPt = skeleton.getJointPosition(SkeletonJoint.LEFT_HAND);
			shoulderPt = skeleton.getJointPosition(SkeletonJoint.LEFT_SHOULDER);
		}
		if ((handPt == null) || (shoulderPt == null)) return;

		float zDiff = handPt.getZ() - shoulderPt.getZ();
		// System.out.println("diff: " + zDiff);

		if (zDiff < -1 * (skeleton.getArmLength() * 0.95f)) { // is forward
			if (!isDetected()) {
				postureDidStart(skeleton);
			}
		} else if (isDetected()) {
			postureDidStop(skeleton);
		}
	}

}
