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

import com.imolatech.retina.Messenger;

public class UserTracker {
	private Messenger messenger;
	// OpenNI
	private UserGenerator userGenerator;
	private DepthGenerator depthGenerator;

	// OpenNI capabilities used by UserGenerator
	private SkeletonCapability skeletonCapability;
	// to output skeletal data, including the location of the joints
	private PoseDetectionCapability poseDetectionCapability;
	// to recognize when the user is in a specific position

	private String calibPoseName = null;

	private HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> userSkeletons;

	// was SkeletonJointTransformation
	/*
	 * userSkels maps user IDs --> a joints map (i.e. a skeleton) skeleton maps
	 * joints --> positions (was positions + orientations)
	 */

	public UserTracker(UserGenerator userGen, DepthGenerator depthGen, Messenger messenger) {
		this.userGenerator = userGen;
		this.depthGenerator = depthGen;
		this.messenger = messenger;
		configure();
		userSkeletons = new HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>>();
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
			poseDetectionCapability = userGenerator.getPoseDetectionCapability();

			skeletonCapability = userGenerator.getSkeletonCapability();
			// the 'psi' pose
			calibPoseName = skeletonCapability.getSkeletonCalibrationPose(); 
			skeletonCapability.setSkeletonProfile(SkeletonProfile.ALL);
			// other possible values: UPPER_BODY, LOWER_BODY, HEAD_HANDS

			// set up four observers
			userGenerator.getNewUserEvent().addObserver(new NewUserObserver()); 
			userGenerator.getLostUserEvent().addObserver(new LostUserObserver()); 

			poseDetectionCapability.getPoseDetectedEvent().addObserver(
					new PoseDetectedObserver());
			// for when a pose is detected

			skeletonCapability.getCalibrationCompleteEvent().addObserver(
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
			// there may be many users in the scene
			int[] userIds = userGenerator.getUsers(); 
			for (int i = 0; i < userIds.length; ++i) {
				int userId = userIds[i];
				if (skeletonCapability.isSkeletonCalibrating(userId))
					continue; // test to avoid occassional crashes with
								// isSkeletonTracking()
				if (skeletonCapability.isSkeletonTracking(userId))
					updateJoints(userId);
			}
			//now we need to convert userSkeletons to json and send skeleton data to client
			//messenger.send();
		} catch (StatusException e) {
			System.out.println(e);
		}
	} // end of update()

	// update all the joints for this userID in userSkels
	private void updateJoints(int userId) {
		HashMap<SkeletonJoint, SkeletonJointPosition> skel = userSkeletons
				.get(userId);

		updateJoint(skel, userId, SkeletonJoint.HEAD);
		updateJoint(skel, userId, SkeletonJoint.NECK);

		updateJoint(skel, userId, SkeletonJoint.LEFT_SHOULDER);
		updateJoint(skel, userId, SkeletonJoint.LEFT_ELBOW);
		updateJoint(skel, userId, SkeletonJoint.LEFT_HAND);

		updateJoint(skel, userId, SkeletonJoint.RIGHT_SHOULDER);
		updateJoint(skel, userId, SkeletonJoint.RIGHT_ELBOW);
		updateJoint(skel, userId, SkeletonJoint.RIGHT_HAND);

		updateJoint(skel, userId, SkeletonJoint.TORSO);

		updateJoint(skel, userId, SkeletonJoint.LEFT_HIP);
		updateJoint(skel, userId, SkeletonJoint.LEFT_KNEE);
		updateJoint(skel, userId, SkeletonJoint.LEFT_FOOT);

		updateJoint(skel, userId, SkeletonJoint.RIGHT_HIP);
		updateJoint(skel, userId, SkeletonJoint.RIGHT_KNEE);
		updateJoint(skel, userId, SkeletonJoint.RIGHT_FOOT);
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
			HashMap<SkeletonJoint, SkeletonJointPosition> skel, int userId,
			SkeletonJoint joint) {
		try {
			// report unavailable joints (should not happen)
			if (!skeletonCapability.isJointAvailable(joint)
					|| !skeletonCapability.isJointActive(joint)) {
				System.out.println(joint + " not available for updates");
				return;
			}

			SkeletonJointPosition pos = skeletonCapability.getSkeletonJointPosition(
					userId, joint);
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
						depthGenerator.convertRealWorldToProjective(pos.getPosition()),
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
			String json = "{\"userId\":\"" + args.getId() + 
					"\",\"status\":\"" + "NEW" +
					"\"}";
			messenger.send(json);
			System.out.println("Detected new user " + args.getId());
			try {
				// try to detect a pose for the new user
				//poseDetectionCapability
				//		.startPoseDetection(calibPoseName, args.getId()); 
				//since new openni, we do not need manually calibrate anymore
				skeletonCapability.requestSkeletonCalibration(args.getId(), false);
			} catch (StatusException e) {
				e.printStackTrace();
			}
		}
	} // end of NewUserObserver inner class

	class LostUserObserver implements IObserver<UserEventArgs> {
		public void update(IObservable<UserEventArgs> observable,
				UserEventArgs args) {
			System.out.println("Lost track of user " + args.getId());
			userSkeletons.remove(args.getId()); // remove user from userSkels
			messenger.send("userId:" + args.getId() + ";status:LOST");
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
				poseDetectionCapability.stopPoseDetection(userID); // big-S ?
				skeletonCapability.requestSkeletonCalibration(userID, true);
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
					skeletonCapability.startTracking(userID);
					userSkeletons
							.put(new Integer(userID),
									new HashMap<SkeletonJoint, SkeletonJointPosition>());
					// create new skeleton map for the user in userSkels
				} else
					// calibration failed; return to pose detection
					poseDetectionCapability.startPoseDetection(calibPoseName, userID); // big-S
																				// ?
			} catch (StatusException e) {
				e.printStackTrace();
			}
		}
	} // end of CalibrationCompleteObserver inner class

} // end of Skeletons class

