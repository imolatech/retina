package com.imolatech.kinect.posture;

import org.OpenNI.Point3D;
import org.OpenNI.SkeletonJoint;

import com.imolatech.kinect.GestureName;
import com.imolatech.kinect.GestureWatcher;
import com.imolatech.kinect.Skeleton;

/**
 * detect if the user's right hand at head level or above?
 * @author Wenhu
 *
 */
public class HandUpDetectionStrategy extends BasePostureDetectionStrategy {
	private boolean isRightHand;
	
	public HandUpDetectionStrategy(boolean isRightHand, GestureWatcher watcher) {
		super(watcher);
		this.isRightHand = isRightHand;
	}

	@Override
	public GestureName getGestureName() {
		if (isRightHand) return GestureName.RH_UP;
		return GestureName.LH_UP;
	}
	
	@Override
	public void detect(Skeleton skeleton) {
		Point3D handPt;
		Point3D headPt = skeleton.getJointPosition(SkeletonJoint.HEAD);
		
		if (isRightHand) {
			handPt = skeleton.getJointPosition(SkeletonJoint.RIGHT_HAND);
		} else {
			handPt = skeleton.getJointPosition(SkeletonJoint.LEFT_HAND);
		}
		
		if ((handPt == null) || (headPt == null)) return;
		//logger.debug("Right hand pt:{}", "x-" + rightHandPt.getX() +
		//		", y-" + rightHandPt.getY() +  ",z-" + rightHandPt.getZ());
		//logger.debug("Head pt:{}", "x-" + headPt.getX() +
		//		", y-" + headPt.getY() +  ",z-" + headPt.getZ());
		if (handPt.getY() <= headPt.getY()) { // above
			if (!isDetected()) {
				postureDidStart(skeleton);
				//gestureSequences.addUserGest(userID, GestureName.RH_UP); 
			}
		} else if (isDetected()) {
			postureDidStop(skeleton);
		}
		
	}

}
