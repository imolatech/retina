package com.imolatech.kinect.posture;

import org.OpenNI.Point3D;
import org.OpenNI.SkeletonJoint;

import com.imolatech.kinect.GestureName;
import com.imolatech.kinect.GestureWatcher;
import com.imolatech.kinect.Skeleton;

/**
 * detect if the user's right hand out to the right of his right elbow,
 * or left hand out to the left of his left elbow.
 * @author Wenhu
 *
 */
public class HandOutDetectionStrategy extends BasePostureDetectionStrategy {
	private boolean isRightHand;
	
	public HandOutDetectionStrategy(boolean isRightHand, GestureWatcher watcher) {
		super(watcher);
		this.isRightHand = isRightHand;
	}

	@Override
	public GestureName getGestureName() {
		if (isRightHand) return GestureName.RH_OUT;
		return GestureName.LH_OUT;
	}
	
	@Override
	public void detect(Skeleton skeleton) {
		Point3D handPt;
		Point3D elbowPt;
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
		if (xDiff > (skeleton.getLowerArmLength() * 0.6f)) { // out to the right
			if (!isDetected()) {
				postureDidStart(skeleton);
			}
		} else if (isDetected()) {
			postureDidStop(skeleton);
		}
	}

}
