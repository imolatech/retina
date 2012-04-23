package com.imolatech.kinect;

import java.util.HashMap;

import org.OpenNI.Point3D;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.imolatech.kinect.posture.FullBodyPostureDetector;
import com.imolatech.kinect.posture.FullBodyPostureDetector2;
import com.imolatech.kinect.posture.PostureDetector;
import com.imolatech.kinect.posture.AlgorithmicPostureDetector;

public class GestureCapturer implements SkeletonObserver, UserObserver,
		GestureWatcher {
	private static final Logger logger = LoggerFactory
			.getLogger(GestureCapturer.class);
	private MessageDispatcher dispatcher;
	private FullBodyPostureDetector postureDetector;
	//private FullBodyPostureDetector2 postureDetector2;
	private GestureContext gestureContext;
	
	public GestureCapturer(MessageDispatcher dispatcher) {
		this.dispatcher = dispatcher;
		// gestureSequences = new GestureSequences(this);
		postureDetector = new FullBodyPostureDetector(this);
		//postureDetector2 = new FullBodyPostureDetector2(this);
		gestureContext = new GestureContext();//we will reuse this object for performance reason
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
	}

	@Override
	public void onUserOut(int userId) {
		// gestureSequences.removeUser(userId);
	}

	@Override
	public void onUserTracked(int userId) {
		// gestureSequences.addUser(userId);
	}

	@Override
	public void onUpdateSkeleton(int userId,
			HashMap<SkeletonJoint, SkeletonJointPosition> skeleton) {
		
		if (skeleton == null) {
			return;
		}
		
		// gestureSequences.checkSeqs(userId);
		gestureContext.calcSkelLengths(userId, skeleton);
		// calcSkelLengths(skeleton);
		//detectGestures(userId, skeleton);
		detectPostures(userId, gestureContext, skeleton);
	}

	private void detectPostures(int userId, GestureContext context,
			HashMap<SkeletonJoint, SkeletonJointPosition> skeleton) {
		postureDetector.detectPostures(userId, context, skeleton);
		//postureDetector2.detectPostures(userId, context, skeleton);
	}

	public void detectGestures(int userId,
			HashMap<SkeletonJoint, SkeletonJointPosition> skel) {

	}

	// ------------GesturesWatcher.pose() -----------------------------

	// called by the gesture detectors
	public void pose(int userID, GestureName gest, boolean isActivated) {
		if (isActivated)
			logger.debug(gest + " " + userID + " on");
		else
			logger.debug("                        " + gest + " " + userID
					+ " off");
	}
}
