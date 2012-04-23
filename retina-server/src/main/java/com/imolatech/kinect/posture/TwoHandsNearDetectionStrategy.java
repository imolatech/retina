package com.imolatech.kinect.posture;

import org.OpenNI.Point3D;
import org.OpenNI.SkeletonJoint;

import com.imolatech.kinect.GestureName;
import com.imolatech.kinect.GestureWatcher;
import com.imolatech.kinect.Skeleton;

/**
 * detect if the user's hands are close together on the x-axis?
 * @author Wenhu
 *
 */
public class TwoHandsNearDetectionStrategy extends BasePostureDetectionStrategy {

	public TwoHandsNearDetectionStrategy(GestureWatcher watcher) {
		super(watcher);
	}

	@Override
	public GestureName getGestureName() {
		return GestureName.HANDS_NEAR;
	}
	
	@Override
	public void detect(Skeleton skeleton) {
		Point3D leftHandPt = skeleton.getJointPosition(SkeletonJoint.LEFT_HAND);
		Point3D rightHandPt = skeleton.getJointPosition(SkeletonJoint.RIGHT_HAND);
		
		if ((leftHandPt == null) || (rightHandPt == null)) return;

		float xDiff = rightHandPt.getX() - leftHandPt.getX();
		
		if (xDiff < skeleton.getNeckLength()) { // near
			if (!isDetected()) {
				postureDidStart(skeleton);
			}
		} else if (isDetected()) {
			postureDidStop(skeleton);
		}
	}

}
