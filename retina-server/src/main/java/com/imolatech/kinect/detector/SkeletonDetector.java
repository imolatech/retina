package com.imolatech.kinect.detector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.OpenNI.DepthGenerator;
import org.OpenNI.Point3D;
import org.OpenNI.SkeletonCapability;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;
import org.OpenNI.SkeletonProfile;
import org.OpenNI.StatusException;
import org.OpenNI.UserGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.imolatech.kinect.MessageDispatcher;
import com.imolatech.kinect.serializer.TrackedUsersSerializer;

/**
 * Detect Users' skeleton position and dispatch the skeleton data.
 * @author Wenhu
 *
 */
public class SkeletonDetector implements UserObserver {
	private static final Logger logger = LoggerFactory.getLogger(SkeletonDetector.class);
	private MessageDispatcher dispatcher;
	private List<SkeletonObserver> observers = new ArrayList<SkeletonObserver>();
	private DepthGenerator depthGenerator;
	private UserGenerator userGenerator;
	private SkeletonCapability skeletonCapability;
	private boolean updating = false;
	private TrackedUsersSerializer serializer = new TrackedUsersSerializer();
	
	//userSkeletons maps user IDs --> a joints map (i.e. a skeleton) skeleton maps
	//joints --> positions (was positions + orientations)
	private HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> userSkeletons;
	
	public SkeletonDetector(UserGenerator userGenerator, 
			DepthGenerator depthGenerator, 
			MessageDispatcher dispatcher) {
		this.userGenerator = userGenerator;
		this.depthGenerator = depthGenerator;
		this.dispatcher = dispatcher;
		userSkeletons = new HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>>();
	} 
	
	// register observer for UserDetector
	public void register(UserDetector userDetector) {
		userDetector.addObserver(this);
	}

	public void unRegister(UserDetector userDetector) {
		userDetector.removeObserver(this);
	}
	
	@Override
	public void onUserIn(int userId) {
		logger.debug("New user identified.");
	}

	@Override
	public void onUserOut(int userId) {
		userSkeletons.remove(userId); // remove user from userSkeletons
	}

	@Override
	public void onUserTracked(int userId) {
		userSkeletons
		.put(new Integer(userId),
				new HashMap<SkeletonJoint, SkeletonJointPosition>());
	}
	
	// add SkeletonObservers
	public void addObserver(SkeletonObserver observer) {
		observers.add(observer);
	}
	
	public void removeObserver(SkeletonObserver observer) {
		observers.remove(observer);
	}
	
	public void notifySkeletonDataUpdate(int userId) {
		HashMap<SkeletonJoint, SkeletonJointPosition> skel = userSkeletons.get(userId);
		if (skel == null) return;
		for (SkeletonObserver observer : observers) {
			observer.onUpdateSkeleton(userId, skel);
		}
	}
	
	public boolean isUpdating() {
		return updating;
	}
	
	public void init() throws StatusException {
		skeletonCapability = userGenerator.getSkeletonCapability();
		skeletonCapability.setSkeletonProfile(SkeletonProfile.ALL);
		// other possible values: UPPER_BODY, LOWER_BODY, HEAD_HANDS
	} 
	
	// --------------- updating ----------------------------
	// update skeleton of each user
	public void update() {
		updating = true;
		try {
			// there may be many users in the scene
			int[] userIds = userGenerator.getUsers(); 
			for (int i = 0; i < userIds.length; ++i) {
				int userId = userIds[i];
				if (skeletonCapability.isSkeletonCalibrating(userId))
					continue; // test to avoid occassional crashes with
								// isSkeletonTracking()
				if (skeletonCapability.isSkeletonTracking(userId)) {
					updateJoints(userId);
				}
				notifySkeletonDataUpdate(userId);
				//gestureSequences.checkSeqs(userId);
				//skeletonGestures.checkGests(userId);
			}
			//now we need to convert userSkeletons to json and send skeleton data to client
			serializer.setUsersSkeletons(userSkeletons);//do not new object,should reuse the serializer
			dispatcher.dispatch(serializer.toJson());
		} catch (Exception e) {
			logger.warn("Error while receiving data from kinect.", e);
		}
		updating = false;
	} 

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
				logger.debug("{} not available for updates", joint);
				return;
			}

			SkeletonJointPosition pos = skeletonCapability.getSkeletonJointPosition(
					userId, joint);
			if (pos == null) {
				logger.debug("No update for {}", joint);
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
			logger.warn("Error while recieving skeleton data.", e);
		}
	} // end of updateJoint()
}
