package com.imolatech.kinect.posture;

import java.util.HashMap;

import org.OpenNI.Point3D;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.imolatech.kinect.Skeleton;
import com.imolatech.kinect.GestureName;
import com.imolatech.kinect.GestureWatcher;

public class FullBodyPostureDetector {
	private static final Logger logger = LoggerFactory
			.getLogger(FullBodyPostureDetector.class); 
	private GestureWatcher watcher;
	 
	 public FullBodyPostureDetector(GestureWatcher watcher) {
		 logger.debug("init");
		 this.watcher = watcher;
	 }
	 public void detectPostures(Skeleton skeleton) {
		 detectTwoHandsNear(skeleton);
		 detectRightHandUp(skeleton);
		 detectRightHandFwd(skeleton);
		 detectRightHandOut(skeleton);
		 detectRightHandIn(skeleton);
		 //detectRightHandDown(skeleton);
		 detectLeftHandUp(skeleton);
	 }
	 
	 
	// are the user's hand close together on the x-axis?
	private void detectTwoHandsNear(Skeleton skeleton) {
		
		Point3D leftHandPt = skeleton.getJointPosition(SkeletonJoint.LEFT_HAND);
		Point3D rightHandPt = skeleton.getJointPosition(SkeletonJoint.RIGHT_HAND);
		if ((leftHandPt == null) || (rightHandPt == null)) return;

		float xDiff = rightHandPt.getX() - leftHandPt.getX();
		
		// System.out.println(xDiff);
		if (xDiff < skeleton.getNeckLength()) { // near
			if (!skeleton.isHandsNear()) {
				watcher.pose(skeleton.getUserId(), GestureName.HANDS_NEAR, true); // start
				skeleton.setHandsNear(true);
			}
		} else { // not near
			if (skeleton.isHandsNear()) {
				watcher.pose(skeleton.getUserId(), GestureName.HANDS_NEAR, false); // stopped
				skeleton.setHandsNear(false);
			}
		}
	}
	
	
		
		
	// -------------------------- right hand ----------------------------------
	/*
	 * the right hand gesture checking methods notify the GestureSequences
	 * object of an gesture start so that it can update the user's gesture
	 * sequence.
	 * 
	 * All the other gesture checking methods could do this, but I've kept
	 * things simple by only considering the right hand.
	 */

	// is the user's right hand at head level or above?
	private void detectRightHandUp(Skeleton  skeleton) {
		Point3D rightHandPt = skeleton.getJointPosition(SkeletonJoint.RIGHT_HAND);
		Point3D headPt = skeleton.getJointPosition(SkeletonJoint.HEAD);
		
		if ((rightHandPt == null) || (headPt == null)) return;
		//logger.debug("Right hand pt:{}", "x-" + rightHandPt.getX() +
		//		", y-" + rightHandPt.getY() +  ",z-" + rightHandPt.getZ());
		//logger.debug("Head pt:{}", "x-" + headPt.getX() +
		//		", y-" + headPt.getY() +  ",z-" + headPt.getZ());
		if (rightHandPt.getY() <= headPt.getY()) { // above
			if (!skeleton.isRightHandUp()) {
				watcher.pose(skeleton.getUserId(), GestureName.RH_UP, true); // stopped
				//gestureSequences.addUserGest(userID, GestureName.RH_UP); 
				skeleton.setRightHandUp(true);
			}
		} else { // not above
			if (skeleton.isRightHandUp()) {
				watcher.pose(skeleton.getUserId(), GestureName.RH_UP, false); // stopped
				skeleton.setRightHandUp(false);
			}
		}
	}

	// is the user's right hand forward of his right shoulder?
	private void detectRightHandFwd(Skeleton skeleton) {
		Point3D rightHandPt = skeleton.getJointPosition(SkeletonJoint.RIGHT_HAND);
		Point3D shoulderPt = skeleton.getJointPosition(SkeletonJoint.RIGHT_SHOULDER);
		if ((rightHandPt == null) || (shoulderPt == null)) return;

		float zDiff = rightHandPt.getZ() - shoulderPt.getZ();
		// System.out.println("diff: " + zDiff);

		if (zDiff < -1 * (skeleton.getArmLength() * 0.95f)) { // is forward
			// System.out.println("  armLength: " + armLength);
			if (!skeleton.isRightHandFwd()) {
				watcher.pose(skeleton.getUserId(), GestureName.RH_FWD, true); // started
				//gestureSequences.addUserGest(userID, GestureName.RH_FWD); 
				skeleton.setRightHandFwd(true);
			}
		} else { // not forward
			if (skeleton.isRightHandFwd()) {
				watcher.pose(skeleton.getUserId(), GestureName.RH_FWD, false); // stopped
				skeleton.setRightHandFwd(false);
			}
		}
	} 

	// is the user's right hand out to the right of the his right elbow?
	private void detectRightHandOut(Skeleton skeleton) {
		Point3D rightHandPt = skeleton.getJointPosition(SkeletonJoint.RIGHT_HAND);
		Point3D elbowPt = skeleton.getJointPosition(SkeletonJoint.RIGHT_ELBOW);
		if ((rightHandPt == null) || (elbowPt == null))
			return;

		float xDiff = rightHandPt.getX() - elbowPt.getX();

		if (xDiff > (skeleton.getLowerArmLength() * 0.6f)) { // out to the right
			if (!skeleton.isRightHandOut()) {
				watcher.pose(skeleton.getUserId(), GestureName.RH_OUT, true); // started
				//gestureSequences.addUserGest(userID, GestureName.RH_OUT); 
				skeleton.setRightHandOut(true);
			}
		} else { // not out to the right
			if (skeleton.isRightHandOut()) {
				watcher.pose(skeleton.getUserId(), GestureName.RH_OUT, false); // stopped
				skeleton.setRightHandOut(false);
			}
		}
	} 

	// is the user's right hand inside (left) of his right elbow?
	private void detectRightHandIn(Skeleton skeleton) {
		Point3D rightHandPt = skeleton.getJointPosition(SkeletonJoint.RIGHT_HAND);
		Point3D elbowPt = skeleton.getJointPosition(SkeletonJoint.RIGHT_ELBOW);
		if ((rightHandPt == null) || (elbowPt == null)) return;

		float xDiff = rightHandPt.getX() - elbowPt.getX();

		if (xDiff < -1 * (skeleton.getLowerArmLength() * 0.6f)) { // inside
			if (!skeleton.isRightHandIn()) {
				watcher.pose(skeleton.getUserId(), GestureName.RH_IN, true); // started
				//gestureSequences.addUserGest(userID, GestureName.RH_IN); 
				skeleton.setRightHandIn(true);
			}
		} else { // not inside
			if (skeleton.isRightHandIn()) {
				watcher.pose(skeleton.getUserId(), GestureName.RH_IN, false); // stopped
				skeleton.setRightHandIn(false);
			}
		}
	} 

	// is the user's right hand at hip level or below?
	private void detectRightHandDown(Skeleton skeleton) {
		Point3D rightHandPt = skeleton.getJointPosition(SkeletonJoint.RIGHT_HAND);
		Point3D hipPt = skeleton.getJointPosition(SkeletonJoint.RIGHT_HIP);
		if ((rightHandPt == null) || (hipPt == null)) return;

		if (rightHandPt.getY() >= hipPt.getY()) { // below
			if (!skeleton.isRightHandDown()) {
				watcher.pose(skeleton.getUserId(), GestureName.RH_DOWN, true); // started
				//gestureSequences.addUserGest(userID, GestureName.RH_DOWN); 
				skeleton.setRightHandDown(true);
			}
		} else { // not below
			if (skeleton.isRightHandDown()) {
				watcher.pose(skeleton.getUserId(), GestureName.RH_DOWN, false); // stopped
				skeleton.setRightHandDown(false);
			}
		}
	} 

	// -------------------------- left hand ----------------------------------

	// is the user's left hand at head level or above?
	private void detectLeftHandUp(Skeleton skeleton) {
		Point3D leftHandPt = skeleton.getJointPosition(SkeletonJoint.LEFT_HAND);
		Point3D headPt = skeleton.getJointPosition(SkeletonJoint.NECK);
		if ((leftHandPt == null) || (headPt == null)) return;

		if (leftHandPt.getY() <= headPt.getY()) { // above
			if (!skeleton.isLeftHandUp()) {
				watcher.pose(skeleton.getUserId(), GestureName.LH_UP, true); // started
				skeleton.setLeftHandUp(true);
			}
		} else { // not above
			if (skeleton.isLeftHandUp()) {
				watcher.pose(skeleton.getUserId(), GestureName.LH_UP, false); // started
				skeleton.setLeftHandUp(false);
			}
		}
	} 

}
