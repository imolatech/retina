package com.imolatech.retina.kinect;

/* Skeletons sets up four 'observers' (listeners) so that 
 when a new user is detected in the scene, a standard pose for that 
 user is detected, the user skeleton is calibrated in the pose, and then the
 skeleton is tracked. The start of tracking adds a skeleton entry to userSkels.

 Each call to update() updates the joint positions for each user's
 skeleton.

 */

import java.util.*;

import org.OpenNI.*;

public class Skeletons {

	// OpenNI
	private UserGenerator userGen;
	private DepthGenerator depthGen;

	// OpenNI capabilities used by UserGenerator
	private SkeletonCapability skelCap;
	// to output skeletal data, including the location of the joints
	private PoseDetectionCapability poseDetectionCap;
	// to recognize when the user is in a specific position

	private String calibPoseName = null;

	private HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> userSkels;

	// was SkeletonJointTransformation
	/*
	 * userSkels maps user IDs --> a joints map (i.e. a skeleton) skeleton maps
	 * joints --> positions (was positions + orientations)
	 */

	public Skeletons(UserGenerator userGen, DepthGenerator depthGen) {
		this.userGen = userGen;
		this.depthGen = depthGen;

		configure();
		userSkels = new HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>>();
	} // end of Skeletons()

	/*
	 * create pose and skeleton detection capabilities for the user generator,
	 * and set up observers (listeners)
	 */
	private void configure() {
		try {
			// setup UserGenerator pose and skeleton detection capabilities;
			// should really check these using
			// ProductionNode.isCapabilitySupported()
			poseDetectionCap = userGen.getPoseDetectionCapability();

			skelCap = userGen.getSkeletonCapability();
			calibPoseName = skelCap.getSkeletonCalibrationPose(); // the 'psi'
																	// pose
			skelCap.setSkeletonProfile(SkeletonProfile.ALL);
			// other possible values: UPPER_BODY, LOWER_BODY, HEAD_HANDS

			// set up four observers
			userGen.getNewUserEvent().addObserver(new NewUserObserver()); // new
																			// user
																			// found
			userGen.getLostUserEvent().addObserver(new LostUserObserver()); // lost
																			// a
																			// user

			poseDetectionCap.getPoseDetectedEvent().addObserver(
					new PoseDetectedObserver());
			// for when a pose is detected

			skelCap.getCalibrationCompleteEvent().addObserver(
					new CalibrationCompleteObserver());
			// for when skeleton calibration is completed, and tracking starts
		} catch (Exception e) {
			System.out.println(e);
		}
	} // end of configure()

	// --------------- updating ----------------------------
	// update skeleton of each user
	public void update() {
		try {
			int[] userIDs = userGen.getUsers(); // there may be many users in
												// the scene
			for (int i = 0; i < userIDs.length; ++i) {
				int userID = userIDs[i];
				if (skelCap.isSkeletonCalibrating(userID))
					continue; // test to avoid occassional crashes with
								// isSkeletonTracking()
				if (skelCap.isSkeletonTracking(userID))
					updateJoints(userID);
			}
		} catch (StatusException e) {
			System.out.println(e);
		}
	} // end of update()

	// update all the joints for this userID in userSkels
	private void updateJoints(int userID) {
		HashMap<SkeletonJoint, SkeletonJointPosition> skel = userSkels
				.get(userID);

		updateJoint(skel, userID, SkeletonJoint.HEAD);
		updateJoint(skel, userID, SkeletonJoint.NECK);

		updateJoint(skel, userID, SkeletonJoint.LEFT_SHOULDER);
		updateJoint(skel, userID, SkeletonJoint.LEFT_ELBOW);
		updateJoint(skel, userID, SkeletonJoint.LEFT_HAND);

		updateJoint(skel, userID, SkeletonJoint.RIGHT_SHOULDER);
		updateJoint(skel, userID, SkeletonJoint.RIGHT_ELBOW);
		updateJoint(skel, userID, SkeletonJoint.RIGHT_HAND);

		updateJoint(skel, userID, SkeletonJoint.TORSO);

		updateJoint(skel, userID, SkeletonJoint.LEFT_HIP);
		updateJoint(skel, userID, SkeletonJoint.LEFT_KNEE);
		updateJoint(skel, userID, SkeletonJoint.LEFT_FOOT);

		updateJoint(skel, userID, SkeletonJoint.RIGHT_HIP);
		updateJoint(skel, userID, SkeletonJoint.RIGHT_KNEE);
		updateJoint(skel, userID, SkeletonJoint.RIGHT_FOOT);
	} // end of updateJoints()

	/*
	 * private void updateJoints(int userID) // alternative version // update
	 * all the joints for this userID in userSkels { HashMap<SkeletonJoint,
	 * SkeletonJointPosition> skel = userSkels.get(userID); for(SkeletonJoint j
	 * : SkeletonJoint.values()) updateJoint(skel, userID, j); } // end of
	 * updateJoints() update the position of the specified user's joint by
	 * looking at the skeleton capability
	 */

	private void updateJoint(
			HashMap<SkeletonJoint, SkeletonJointPosition> skel, int userID,
			SkeletonJoint joint) {
		try {
			// report unavailable joints (should not happen)
			if (!skelCap.isJointAvailable(joint)
					|| !skelCap.isJointActive(joint)) {
				System.out.println(joint + " not available for updates");
				return;
			}

			SkeletonJointPosition pos = skelCap.getSkeletonJointPosition(
					userID, joint);
			if (pos == null) {
				System.out.println("No update for " + joint);
				return;
			}

			/*
			 * // the call to getSkeletonJointOrientation() crashes Java!
			 * SkeletonJointOrientation ori =
			 * skelCap.getSkeletonJointOrientation(userID, joint); if (ori ==
			 * null) System.out.println("No orientation for " + joint); else
			 * System.out.println("Orientation for " + joint + ": " + ori);
			 */
			SkeletonJointPosition jPos = null;
			if (pos.getPosition().getZ() != 0) // has a depth position
				jPos = new SkeletonJointPosition(
						depthGen.convertRealWorldToProjective(pos.getPosition()),
						pos.getConfidence());
			else
				// no info found for that user's joint
				jPos = new SkeletonJointPosition(new Point3D(), 0);
			skel.put(joint, jPos);
		} catch (StatusException e) {
			System.out.println(e);
		}
	} // end of updateJoint()

	// --------------------- 4 observers -----------------------
	/*
	 * user detection --> pose detection --> skeleton calibration --> skeleton
	 * tracking (and creation of userSkels entry) + may also lose a user (and so
	 * delete its userSkels entry)
	 */

	class NewUserObserver implements IObserver<UserEventArgs> {
		public void update(IObservable<UserEventArgs> observable,
				UserEventArgs args) {
			System.out.println("Detected new user " + args.getId());
			try {
				// try to detect a pose for the new user
				poseDetectionCap
						.StartPoseDetection(calibPoseName, args.getId()); // big-S
																			// ?
			} catch (StatusException e) {
				e.printStackTrace();
			}
		}
	} // end of NewUserObserver inner class

	class LostUserObserver implements IObserver<UserEventArgs> {
		public void update(IObservable<UserEventArgs> observable,
				UserEventArgs args) {
			System.out.println("Lost track of user " + args.getId());
			userSkels.remove(args.getId()); // remove user from userSkels
		}
	} // end of LostUserObserver inner class

	class PoseDetectedObserver implements IObserver<PoseDetectionEventArgs> {
		public void update(IObservable<PoseDetectionEventArgs> observable,
				PoseDetectionEventArgs args) {
			int userID = args.getUser();
			System.out.println(args.getPose() + " pose detected for user "
					+ userID);
			try {
				// finished pose detection; switch to skeleton calibration
				poseDetectionCap.StopPoseDetection(userID); // big-S ?
				skelCap.requestSkeletonCalibration(userID, true);
			} catch (StatusException e) {
				e.printStackTrace();
			}
		}
	} // end of PoseDetectedObserver inner class

	class CalibrationCompleteObserver implements
			IObserver<CalibrationProgressEventArgs> {
		public void update(
				IObservable<CalibrationProgressEventArgs> observable,
				CalibrationProgressEventArgs args) {
			int userID = args.getUser();
			System.out.println("Calibration status: " + args.getStatus()
					+ " for user " + userID);
			try {
				if (args.getStatus() == CalibrationProgressStatus.OK) {
					// calibration succeeeded; move to skeleton tracking
					System.out.println("Starting tracking user " + userID);
					skelCap.startTracking(userID);
					userSkels
							.put(new Integer(userID),
									new HashMap<SkeletonJoint, SkeletonJointPosition>());
					// create new skeleton map for the user in userSkels
				} else
					// calibration failed; return to pose detection
					poseDetectionCap.StartPoseDetection(calibPoseName, userID); // big-S
																				// ?
			} catch (StatusException e) {
				e.printStackTrace();
			}
		}
	} // end of CalibrationCompleteObserver inner class

} // end of Skeletons class

