package com.imolatech.kinect.posture;

import org.OpenNI.Point3D;
import org.OpenNI.SkeletonJoint;

import com.imolatech.kinect.GestureName;
import com.imolatech.kinect.GestureWatcher;
import com.imolatech.kinect.Skeleton;

/**
 * detect if the user's right hand inside (left) of his right elbow, 
 * or left hand inside (right) of his left elbow.
 * @author Wenhu
 *
 */
public class HandInDetectionStrategy extends BasePostureDetectionStrategy {
	private boolean isRightHand;
	
	public HandInDetectionStrategy(boolean isRightHand, GestureWatcher watcher) {
		super(watcher);
		this.isRightHand = isRightHand;
	}

	@Override
	public GestureName getGestureName() {
		if (isRightHand) return GestureName.RH_IN;
		return GestureName.LH_IN;
	}
	
	@Override
	public void detect(Skeleton skeleton) {
		Point3D handPt, elbowPt;
		if (isRightHand) {
			handPt = skeleton.getJointPosition(SkeletonJoint.RIGHT_HAND);
			elbowPt = skeleton.getJointPosition(SkeletonJoint.RIGHT_ELBOW);
		} else {
			handPt = skeleton.getJointPosition(SkeletonJoint.LEFT_HAND);
			elbowPt = skeleton.getJointPosition(SkeletonJoint.LEFT_ELBOW);
		}
		if ((handPt == null) || (elbowPt == null))
			return;

		float xDiff = 0;
		
		if (isRightHand) {
			xDiff = handPt.getX() - elbowPt.getX();
		} else {
			xDiff = elbowPt.getX() - handPt.getX();
		}
		if (xDiff < -1 * (skeleton.getLowerArmLength() * 0.6f)) { // inside
			if (!isDetected()) {
				postureDidStart(skeleton);
			}
		} else if (isDetected()) {
			postureDidStop(skeleton);
		}
	}

}
