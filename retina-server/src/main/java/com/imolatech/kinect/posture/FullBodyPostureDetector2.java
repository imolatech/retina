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

public class FullBodyPostureDetector2  extends PostureDetector {
	private static final Logger logger = LoggerFactory
			.getLogger(FullBodyPostureDetector2.class); 
	 
	 public FullBodyPostureDetector2(GestureWatcher watcher) {
		 super(watcher);
	 }
	 
	 public void detectPostures(int userId, GestureContext context,
				HashMap<SkeletonJoint, SkeletonJointPosition> skeleton) {
		 detectRightHandUp(userId, context, skeleton);
		 //detectRightHandFwd(userId, context, skeleton);
		// detectRightHandOut(userId, context, skeleton);
		 //detectRightHandIn(userId, context, skeleton);
		 //detectRightHandDown(userId, context, skeleton);
		 detectLeftHandUp(userId, context, skeleton);
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
				raisePostureDetected(userId, "RIGHT_HAND_OVER_HEAD");
				//watcher.pose(userId, GestureName.RH_UP, true); // stopped
				//gestureSequences.addUserGest(userID, GestureName.RH_UP); 
				context.setRightHandUp(true);
			}
		} else { // not above
			if (context.isRightHandUp()) {
				//watcher.pose(userId, GestureName.RH_UP, false); // stopped
				context.setRightHandUp(false);
			}
		}
	}

	

	// -------------------------- left hand ----------------------------------

	// is the user's left hand at head level or above?
	private void detectLeftHandUp(int userId, GestureContext context,
			HashMap<SkeletonJoint, SkeletonJointPosition> skel) {
		Point3D leftHandPt = SkeletonUtility.getJointPosition(skel, SkeletonJoint.LEFT_HAND);
		Point3D headPt = SkeletonUtility.getJointPosition(skel, SkeletonJoint.NECK);
		if ((leftHandPt == null) || (headPt == null)) return;

		if (leftHandPt.getY() <= headPt.getY()) { // above
			if (!context.isLeftHandUp()) {
				//watcher.pose(userId, GestureName.LH_UP, true); // started
				raisePostureDetected(userId, "LEFT_HAND_OVER_HEAD");
				context.setLeftHandUp(true);
			}
		} else { // not above
			if (context.isLeftHandUp()) {
				//watcher.pose(userId, GestureName.LH_UP, false); // started
				context.setLeftHandUp(false);
			}
		}
	} 

}
