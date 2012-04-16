package com.imolatech.kinect.detector;

import java.util.ArrayList;
import java.util.List;

import org.OpenNI.CalibrationProgressEventArgs;
import org.OpenNI.CalibrationProgressStatus;
import org.OpenNI.IObservable;
import org.OpenNI.IObserver;
import org.OpenNI.PoseDetectionCapability;
import org.OpenNI.PoseDetectionEventArgs;
import org.OpenNI.SkeletonCapability;
import org.OpenNI.SkeletonProfile;
import org.OpenNI.StatusException;
import org.OpenNI.UserEventArgs;
import org.OpenNI.UserGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.imolatech.kinect.MessageDispatcher;
import com.imolatech.kinect.serializer.LostUserSerializer;
import com.imolatech.kinect.serializer.MotionDataSerializer;
import com.imolatech.kinect.serializer.NewUserSerializer;

/**
 * Detect New User(User IN) or Lost User(User OUT).
 * This detector should serve as the basis of all other detectors.
 * 
 * @author Wenhu
 *
 */
public class UserDetector {
	private static final Logger logger = LoggerFactory.getLogger(UserDetector.class);
	private List<UserObserver> observers = new ArrayList<UserObserver>();
	
	private MessageDispatcher dispatcher;
	
	private UserGenerator userGenerator;
	// OpenNI capabilities used by UserGenerator
	private SkeletonCapability skeletonCapability;
	// to output skeletal data, including the location of the joints
	private PoseDetectionCapability poseDetectionCapability;
	// to recognize when the user is in a specific position
	private String calibPoseName = null;
	
	public UserDetector(UserGenerator userGen, MessageDispatcher dispatcher) {
		this.userGenerator = userGen;
		this.dispatcher = dispatcher;
	} 
	
	public void addObserver(UserObserver observer) {
		observers.add(observer);
	}
	
	public void removeObserver(UserObserver observer) {
		observers.remove(observer);
	}
	
	public void notifyUserIn(int userId) {
		for (UserObserver observer : observers) {
			observer.onUserIn(userId);
		}
	}
	
	public void notifyUserOut(int userId) {
		for (UserObserver observer : observers) {
			observer.onUserOut(userId);
		}
	}
	
	public void notifyUserTracked(int userId) {
		for (UserObserver observer : observers) {
			observer.onUserTracked(userId);
		}
	}
	
	/*
	 * create pose and skeleton detection capabilities for the user generator,
	 * and set up observers (listeners)
	 */
	public void init() throws StatusException {
		
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

		// for when a pose is detected
		poseDetectionCapability.getPoseDetectedEvent().addObserver(
				new PoseDetectedObserver());
		

		// for when skeleton calibration is completed, and tracking starts
		skeletonCapability.getCalibrationCompleteEvent().addObserver(
				new CalibrationCompleteObserver());
	} 
	// --------------------- 4 observers -----------------------
	/*
	 * user detection --> pose detection --> skeleton calibration --> skeleton
	 * tracking (and creation of userSkels entry) + may also lose a user (and so
	 * delete its userSkels entry)
	 */

	class NewUserObserver implements IObserver<UserEventArgs> {
		public void update(IObservable<UserEventArgs> observable,
				UserEventArgs args) {
			int userId = args.getId();
			MotionDataSerializer serializer = new NewUserSerializer(userId);
			notifyUserIn(userId);
			dispatcher.dispatch(serializer.toJson());
			logger.debug("Detected new user {}", userId);
			try {
				// try to detect a pose for the new user
				//poseDetectionCapability
				//		.startPoseDetection(calibPoseName, args.getId()); 
				//since new openni, we do not need manually calibrate anymore
				skeletonCapability.requestSkeletonCalibration(userId, false);
			} catch (StatusException e) {
				logger.warn("Error while requesting calibration.", e);
			}
		}
	} // end of NewUserObserver inner class

	class LostUserObserver implements IObserver<UserEventArgs> {
		public void update(IObservable<UserEventArgs> observable,
				UserEventArgs args) {
			int userId = args.getId();
			logger.debug("Lost track of user {}", userId);
			notifyUserOut(userId);
			MotionDataSerializer serializer = new LostUserSerializer(userId);
			dispatcher.dispatch(serializer.toJson());
		}
	} // end of LostUserObserver inner class

	class PoseDetectedObserver implements IObserver<PoseDetectionEventArgs> {
		public void update(IObservable<PoseDetectionEventArgs> observable,
				PoseDetectionEventArgs args) {
			int userId = args.getUser();
			logger.debug("{} pose detected for user {}", args.getPose(),userId);
			try {
				// finished pose detection; switch to skeleton calibration
				poseDetectionCapability.stopPoseDetection(userId); // big-S ?
				skeletonCapability.requestSkeletonCalibration(userId, true);
			} catch (StatusException e) {
				logger.warn("Error while detecting pose.", e);
			}
		}
	} // end of PoseDetectedObserver inner class

	class CalibrationCompleteObserver implements
			IObserver<CalibrationProgressEventArgs> {
		public void update(
				IObservable<CalibrationProgressEventArgs> observable,
				CalibrationProgressEventArgs args) {
			int userId = args.getUser();
			logger.debug("Calibration status: {} for user {}",args.getStatus(), userId);
			try {
				if (args.getStatus() == CalibrationProgressStatus.OK) {
					// calibration succeeeded; move to skeleton tracking
					logger.debug("Starting to track user {}", userId);
					skeletonCapability.startTracking(userId);
					notifyUserTracked(userId);
				} else {
					// calibration failed; return to pose detection
					poseDetectionCapability.startPoseDetection(calibPoseName, userId); // big-S
				}
																				
			} catch (StatusException e) {
				logger.warn("Error while completing calibration.", e);
			}
		}
	} // end of CalibrationCompleteObserver inner class
}
