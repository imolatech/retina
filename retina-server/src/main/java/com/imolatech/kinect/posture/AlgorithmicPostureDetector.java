package com.imolatech.kinect.posture;

import java.util.HashMap;

import org.OpenNI.Point3D;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.imolatech.kinect.GestureContext;
import com.imolatech.kinect.GestureWatcher;
import com.imolatech.kinect.SkeletonUtility;

public class AlgorithmicPostureDetector extends PostureDetector {
	private static final Logger logger = LoggerFactory
			.getLogger(AlgorithmicPostureDetector.class);
	private float epsilon;
	private float maxRange;

	public AlgorithmicPostureDetector(GestureWatcher watcher) {
		super(watcher);
		epsilon = 10f;//0.1
		maxRange = 25f;//0.25
	}

	@Override
	public void detectPostures(int userId, GestureContext context,
			HashMap<SkeletonJoint, SkeletonJointPosition> skeleton) {
		Point3D headPosition, leftHandPosition, rightHandPosition;
		headPosition = SkeletonUtility.getJointPosition(skeleton,
				SkeletonJoint.HEAD);
		leftHandPosition = SkeletonUtility.getJointPosition(skeleton,
				SkeletonJoint.LEFT_HAND);
		rightHandPosition = SkeletonUtility.getJointPosition(skeleton,
				SkeletonJoint.RIGHT_HAND);
		epsilon = context.getNeckLength();
		maxRange = context.getLowerArmLength();
		// HandsJoined
		if (checkHandsJoined(rightHandPosition, leftHandPosition)) {
			raisePostureDetected(userId, "HANDS_JOINED");
			return;
		}

		// LeftHandOverHead
		if (checkHandOverHead(headPosition, leftHandPosition)) {
			raisePostureDetected(userId, "LEFT_HAND_OVER_HEAD");
			return;
		}

		// RightHandOverHead
		if (checkHandOverHead(headPosition, rightHandPosition)) {
			raisePostureDetected(userId, "RIGHT_HAND_OVER_HEAD");
			return;
		}

		// LeftHello
		if (checkHello(headPosition, leftHandPosition)) {
			raisePostureDetected(userId, "LEFT_HELLO");
			return;
		}

		// RightHello
		if (checkHello(headPosition, rightHandPosition)) {
			raisePostureDetected(userId, "RIGHT_HELLO");
			return;
		}
		reset();
	}

	private boolean checkHandOverHead(Point3D headPosition, Point3D handPosition) {
		if (handPosition == null || headPosition == null)
			return false;

		if (handPosition.getY() < headPosition.getY())
			return false;

		if (Math.abs(handPosition.getX() - headPosition.getX()) > maxRange)
			return false;

		if (Math.abs(handPosition.getZ() - headPosition.getZ()) > maxRange)
			return false;
		
		return true;
	}

	private boolean checkHello(Point3D headPosition, Point3D handPosition) {
		if (handPosition == null || headPosition == null)
			return false;

		if (Math.abs(handPosition.getX() - headPosition.getX()) < maxRange)
			return false;

		if (Math.abs(handPosition.getY() - headPosition.getY()) > maxRange)
			return false;

		if (Math.abs(handPosition.getZ() - headPosition.getZ()) > maxRange)
			return false;
		
		return true;
	}

	private boolean checkHandsJoined(Point3D leftHandPosition,
			Point3D rightHandPosition) {
		if (leftHandPosition == null || rightHandPosition == null)
			return false;

		float distance = SkeletonUtility.distApart(leftHandPosition,
				rightHandPosition);
		
		if (distance > epsilon)
			return false;
		
		return true;
	}
}
