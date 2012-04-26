package com.imolatech.kinect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.imolatech.kinect.posture.HandForwardDetectionStrategy;
import com.imolatech.kinect.posture.HandInDetectionStrategy;
import com.imolatech.kinect.posture.HandOutDetectionStrategy;
import com.imolatech.kinect.posture.HandUpDetectionStrategy;
import com.imolatech.kinect.posture.PostureDetectionStrategy;
import com.imolatech.kinect.posture.TwoHandsNearDetectionStrategy;
import com.imolatech.kinect.serializer.UsersGesturesSerializer;

public class GestureCapturer implements SkeletonObserver, UserObserver,
		GestureWatcher {
	private static final Logger logger = LoggerFactory
			.getLogger(GestureCapturer.class);
	private MessageDispatcher dispatcher;
	private List<PostureDetectionStrategy> postureDetectors;
	private Skeleton skeleton;
	private Map<Integer, List<GestureName>> usersGestures;
	private UsersGesturesSerializer serializer = new UsersGesturesSerializer();
	public GestureCapturer(MessageDispatcher dispatcher) {
		this.dispatcher = dispatcher;
		// gestureSequences = new GestureSequences(this);
		//postureDetector = new FullBodyPostureDetector(this);
		initPostureDetectors();
		//postureDetector2 = new FullBodyPostureDetector2(this);
		skeleton = new Skeleton();//we will reuse this object for performance reason
		usersGestures = new HashMap<Integer, List<GestureName>>();
	}

	private void initPostureDetectors() {
		postureDetectors = new ArrayList<PostureDetectionStrategy>();
		postureDetectors.add(new HandUpDetectionStrategy(true, this));
		postureDetectors.add(new HandForwardDetectionStrategy(true, this));
		postureDetectors.add(new HandOutDetectionStrategy(true, this));
		postureDetectors.add(new HandInDetectionStrategy(true, this));
		postureDetectors.add(new HandUpDetectionStrategy(false, this));
		postureDetectors.add(new HandForwardDetectionStrategy(false, this));
		postureDetectors.add(new HandOutDetectionStrategy(false, this));
		postureDetectors.add(new HandInDetectionStrategy(false, this));
		postureDetectors.add(new TwoHandsNearDetectionStrategy(this));
	}

	// register observer for SkeletonDetector
	public void register(SkeletonCapturer detector) {
		detector.addObserver(this);
	}

	public void unRegister(SkeletonCapturer detector) {
		detector.removeObserver(this);
	}

	// register observer for UserDetector
	public void register(UserCapturer userDetector) {
		userDetector.addObserver(this);
	}

	public void unRegister(UserCapturer userDetector) {
		userDetector.removeObserver(this);
	}

	@Override
	public void onUserIn(int userId) {
		logger.debug("New user detected.");
		usersGestures.put(new Integer(userId), new ArrayList<GestureName>());
	}

	@Override
	public void onUserOut(int userId) {
		// gestureSequences.removeUser(userId);
		usersGestures.remove(new Integer(userId));
	}

	@Override
	public void onUserTracked(int userId) {
		// gestureSequences.addUser(userId);
	}

	@Override
	public void onUpdateSkeleton(int userId,
			HashMap<SkeletonJoint, SkeletonJointPosition> joints) {
		
		if (joints == null) {
			return;
		}
		
		// gestureSequences.checkSeqs(userId);
		skeleton.init(userId, joints);
		//detectGestures(userId, skeleton);
		detectPostures(skeleton);
	}
	
	@Override
	public void onUpdateSkeletons(
			HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> skeletons) {
		for (Integer userId : skeletons.keySet()) {
			onUpdateSkeleton(userId, skeletons.get(userId));
		}
		dispatchUserGestures();
	}

	private void detectPostures(Skeleton skeleton) {
		for (PostureDetectionStrategy detector : postureDetectors) {
			detector.detect(skeleton);
		}
	}

	public void detectGestures(int userId,
			HashMap<SkeletonJoint, SkeletonJointPosition> skel) {

	}

	// ------------GesturesWatcher.pose() -----------------------------

	// called by the gesture detectors
	public void pose(int userId, GestureName gest, boolean isActivated) {
		if (isActivated) {
			logger.debug(gest + " " + userId + " on");
			if (canDispatchGesture(gest)) {
				usersGestures.get(new Integer(userId)).add(gest);
			}
			
		} else {
			logger.debug("                        " + gest + " " + userId
					+ " off");
		}
	}

	private boolean canDispatchGesture(GestureName gest) {
		return gest.equals(GestureName.LH_UP) ||
				gest.equals(GestureName.RH_UP) ||
				gest.equals(GestureName.HANDS_NEAR);
	}
	
	private boolean isUserGesturesEmpty() {
		for (Integer userId : usersGestures.keySet()) {
			if (!usersGestures.get(userId).isEmpty()) {
				return false;
			}
		}
		return true;
	}
	private void dispatchUserGestures() {
		if (isUserGesturesEmpty()) return;
		serializer.setUsersGestures(usersGestures);
		dispatcher.dispatch(serializer.toJson());
		cleanUsersGestures();
		
	}

	private void cleanUsersGestures() {
		for (Integer userId : usersGestures.keySet()) {
			usersGestures.get(userId).clear();
		}
	}
}
