package com.imolatech.kinect.posture;

import java.util.HashMap;

import org.OpenNI.Point3D;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.imolatech.kinect.GestureContext;
import com.imolatech.kinect.GestureName;
import com.imolatech.kinect.GestureWatcher;
import com.imolatech.kinect.SkeletonUtility;

public class FullBodyPostureDetector {
	private static final Logger logger = LoggerFactory
			.getLogger(FullBodyPostureDetector.class); 
	private GestureWatcher watcher;
	 
	 public FullBodyPostureDetector(GestureWatcher watcher) {
		 logger.debug("init");
		 this.watcher = watcher;
	 }
	 
	 public void detectPostures(int userId, GestureContext context,
				HashMap<SkeletonJoint, SkeletonJointPosition> skeleton) {
		 detectTwoHandsNear(userId, context, skeleton);
		 detectRightHandUp(userId, context, skeleton);
		 detectRightHandFwd(userId, context, skeleton);
		 detectRightHandOut(userId, context, skeleton);
		 detectRightHandIn(userId, context, skeleton);
		 //detectRightHandDown(userId, context, skeleton);
		 detectLeftHandUp(userId, context, skeleton);
	 }
	 
	// are the user's hand close together on the x-axis?
	private void detectTwoHandsNear(int userId, GestureContext context,
			HashMap<SkeletonJoint, SkeletonJointPosition> skel) {
		Point3D leftHandPt = SkeletonUtility.getJointPosition(skel, SkeletonJoint.LEFT_HAND);
		Point3D rightHandPt = SkeletonUtility.getJointPosition(skel, SkeletonJoint.RIGHT_HAND);
		if ((leftHandPt == null) || (rightHandPt == null)) return;

		float xDiff = rightHandPt.getX() - leftHandPt.getX();
		
		// System.out.println(xDiff);
		if (xDiff < context.getNeckLength()) { // near
			if (!context.isHandsNear()) {
				watcher.pose(userId, GestureName.HANDS_NEAR, true); // start
				context.setHandsNear(true);
			}
		} else { // not near
			if (context.isHandsNear()) {
				watcher.pose(userId, GestureName.HANDS_NEAR, false); // stopped
				context.setHandsNear(false);
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
	private void detectRightHandUp(int userId, GestureContext context,
			HashMap<SkeletonJoint, SkeletonJointPosition> skel) {
		Point3D rightHandPt = SkeletonUtility.getJointPosition(skel, SkeletonJoint.RIGHT_HAND);
		Point3D headPt = SkeletonUtility.getJointPosition(skel, SkeletonJoint.HEAD);
		
		if ((rightHandPt == null) || (headPt == null)) return;
		//logger.debug("Right hand pt:{}", "x-" + rightHandPt.getX() +
		//		", y-" + rightHandPt.getY() +  ",z-" + rightHandPt.getZ());
		//logger.debug("Head pt:{}", "x-" + headPt.getX() +
		//		", y-" + headPt.getY() +  ",z-" + headPt.getZ());
		if (rightHandPt.getY() <= headPt.getY()) { // above
			if (!context.isRightHandUp()) {
				watcher.pose(userId, GestureName.RH_UP, true); // stopped
				//gestureSequences.addUserGest(userID, GestureName.RH_UP); 
				context.setRightHandUp(true);
			}
		} else { // not above
			if (context.isRightHandUp()) {
				watcher.pose(userId, GestureName.RH_UP, false); // stopped
				context.setRightHandUp(false);
			}
		}
	}

	// is the user's right hand forward of his right shoulder?
	private void detectRightHandFwd(int userId, GestureContext context,
			HashMap<SkeletonJoint, SkeletonJointPosition> skel) {
		Point3D rightHandPt = SkeletonUtility.getJointPosition(skel, SkeletonJoint.RIGHT_HAND);
		Point3D shoulderPt = SkeletonUtility.getJointPosition(skel, SkeletonJoint.RIGHT_SHOULDER);
		if ((rightHandPt == null) || (shoulderPt == null)) return;

		float zDiff = rightHandPt.getZ() - shoulderPt.getZ();
		// System.out.println("diff: " + zDiff);

		if (zDiff < -1 * (context.getArmLength() * 0.95f)) { // is forward
			// System.out.println("  armLength: " + armLength);
			if (!context.isRightHandFwd()) {
				watcher.pose(userId, GestureName.RH_FWD, true); // started
				//gestureSequences.addUserGest(userID, GestureName.RH_FWD); 
				context.setRightHandFwd(true);
			}
		} else { // not forward
			if (context.isRightHandFwd()) {
				watcher.pose(userId, GestureName.RH_FWD, false); // stopped
				context.setRightHandFwd(false);
			}
		}
	} 

	// is the user's right hand out to the right of the his right elbow?
	private void detectRightHandOut(int userId, GestureContext context,
			HashMap<SkeletonJoint, SkeletonJointPosition> skel) {
		Point3D rightHandPt = SkeletonUtility.getJointPosition(skel, SkeletonJoint.RIGHT_HAND);
		Point3D elbowPt = SkeletonUtility.getJointPosition(skel, SkeletonJoint.RIGHT_ELBOW);
		if ((rightHandPt == null) || (elbowPt == null))
			return;

		float xDiff = rightHandPt.getX() - elbowPt.getX();

		if (xDiff > (context.getLowerArmLength() * 0.6f)) { // out to the right
			if (!context.isRightHandOut()) {
				watcher.pose(userId, GestureName.RH_OUT, true); // started
				//gestureSequences.addUserGest(userID, GestureName.RH_OUT); 
				context.setRightHandOut(true);
			}
		} else { // not out to the right
			if (context.isRightHandOut()) {
				watcher.pose(userId, GestureName.RH_OUT, false); // stopped
				context.setRightHandOut(false);
			}
		}
	} 

	// is the user's right hand inside (left) of his right elbow?
	private void detectRightHandIn(int userId, GestureContext context,
			HashMap<SkeletonJoint, SkeletonJointPosition> skel) {
		Point3D rightHandPt = SkeletonUtility.getJointPosition(skel, SkeletonJoint.RIGHT_HAND);
		Point3D elbowPt = SkeletonUtility.getJointPosition(skel, SkeletonJoint.RIGHT_ELBOW);
		if ((rightHandPt == null) || (elbowPt == null)) return;

		float xDiff = rightHandPt.getX() - elbowPt.getX();

		if (xDiff < -1 * (context.getLowerArmLength() * 0.6f)) { // inside
			if (!context.isRightHandIn()) {
				watcher.pose(userId, GestureName.RH_IN, true); // started
				//gestureSequences.addUserGest(userID, GestureName.RH_IN); 
				context.setRightHandIn(true);
			}
		} else { // not inside
			if (context.isRightHandIn()) {
				watcher.pose(userId, GestureName.RH_IN, false); // stopped
				context.setRightHandIn(false);
			}
		}
	} 

	// is the user's right hand at hip level or below?
	private void detectRightHandDown(int userId, GestureContext context,
			HashMap<SkeletonJoint, SkeletonJointPosition> skel) {
		Point3D rightHandPt = SkeletonUtility.getJointPosition(skel, SkeletonJoint.RIGHT_HAND);
		Point3D hipPt = SkeletonUtility.getJointPosition(skel, SkeletonJoint.RIGHT_HIP);
		if ((rightHandPt == null) || (hipPt == null)) return;

		if (rightHandPt.getY() >= hipPt.getY()) { // below
			if (!context.isRightHandDown()) {
				watcher.pose(userId, GestureName.RH_DOWN, true); // started
				//gestureSequences.addUserGest(userID, GestureName.RH_DOWN); 
				context.setRightHandDown(true);
			}
		} else { // not below
			if (context.isRightHandDown()) {
				watcher.pose(userId, GestureName.RH_DOWN, false); // stopped
				context.setRightHandDown(false);
			}
		}
	} 

	// -------------------------- left hand ----------------------------------

	// is the user's left hand at head level or above?
	private void detectLeftHandUp(int userID, GestureContext context,
			HashMap<SkeletonJoint, SkeletonJointPosition> skel) {
		Point3D leftHandPt = SkeletonUtility.getJointPosition(skel, SkeletonJoint.LEFT_HAND);
		Point3D headPt = SkeletonUtility.getJointPosition(skel, SkeletonJoint.NECK);
		if ((leftHandPt == null) || (headPt == null)) return;

		if (leftHandPt.getY() <= headPt.getY()) { // above
			if (!context.isLeftHandUp()) {
				watcher.pose(userID, GestureName.LH_UP, true); // started
				context.setLeftHandUp(true);
			}
		} else { // not above
			if (context.isLeftHandUp()) {
				watcher.pose(userID, GestureName.LH_UP, false); // started
				context.setLeftHandUp(false);
			}
		}
	} 

}
