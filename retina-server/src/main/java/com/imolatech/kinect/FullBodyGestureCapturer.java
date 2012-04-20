package com.imolatech.kinect;

import java.util.HashMap;

import org.OpenNI.Point3D;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FullBodyGestureCapturer implements SkeletonObserver,
		UserObserver, GestureWatcher {
	private static final Logger logger = LoggerFactory
			.getLogger(FullBodyGestureCapturer.class);
	private MessageDispatcher dispatcher;
	private GestureSequences gestureSequences;

	// standard skeleton lengths
	private static final float NECK_LEN = 50.0f;
	private static final float LOWER_ARM_LEN = 150.0f;
	private static final float ARM_LEN = 400.0f;

	/*
	 * skeleton lengths between joint pairs, used when judging the distance
	 * between other joints
	 */
	private float neckLength = NECK_LEN; // neck to shoulder length
	private float lowerArmLength = LOWER_ARM_LEN; // hand to elbow length
	private float armLength = ARM_LEN; // hand to shoulder length

	// booleans set when gestures are being performed

	private boolean areHandsNear = false; // two hands

	private boolean isLeanLeft = false; // leaning
	private boolean isLeanRight = false;
	private boolean isLeanFwd = false;
	private boolean isLeanBack = false;

	private boolean isTurnLeft = false; // turning
	private boolean isTurnRight = false;

	private boolean lHandTouchlHip = false; // touching
	private boolean rHandTouchrHip = false;

	private boolean isRightHandUp = false; // right hand
	private boolean isRightHandFwd = false;
	private boolean isRightHandOut = false;
	private boolean isRightHandIn = false;
	private boolean isRightHandDown = false;

	private boolean isLeftHandUp = false; // left hand

	public FullBodyGestureCapturer(MessageDispatcher dispatcher) {
		this.dispatcher = dispatcher;
		gestureSequences = new GestureSequences(this);
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
		gestureSequences.removeUser(userId);
	}

	@Override
	public void onUserTracked(int userId) {
		gestureSequences.addUser(userId);
	}

	@Override
	public void onUpdateSkeleton(int userId,
			HashMap<SkeletonJoint, SkeletonJointPosition> skeleton) {
		if (skeleton == null)
			return;
		gestureSequences.checkSeqs(userId);
		calcSkelLengths(skeleton);
		detectGestures(userId, skeleton);
	}

	public void detectGestures(int userId,
			HashMap<SkeletonJoint, SkeletonJointPosition> skel) {
		// uncomment the gestures that you want to detect...

		// twoHandsNear(userID, skel);

		leanLeft(userId, skel);
		leanRight(userId, skel);
		leanFwd(userId, skel);
		leanBack(userId, skel);

		turnLeft(userId, skel);
		// turnRight(userID, skel);

		// leftHandTouchHip(userID, skel);
		// rightHandTouchHip(userID, skel);
		/*
		 * rightHandUp(userID, skel); rightHandFwd(userID, skel);
		 * rightHandOut(userID, skel); rightHandIn(userID, skel);
		 * rightHandDown(userID, skel);
		 */
		leftHandUp(userId, skel);
	}

	/**
	 * calculate lengths between certain joint pairs for this skeleton; these
	 * values are used later to judge the distances between other joints.
	 * repeatedly calculate lengths since the size of a skeleton *on-screen*
	 * will change if the user moves closer or further away. This overhead would
	 * disappear if skeletons were stored using real-world coordinates instead
	 * of screen-based values.
	 * 
	 * @param skel
	 */
	private void calcSkelLengths(
			HashMap<SkeletonJoint, SkeletonJointPosition> skel) {
		Point3D neckPt = getJointPos(skel, SkeletonJoint.NECK);
		Point3D shoulderPt = getJointPos(skel, SkeletonJoint.RIGHT_SHOULDER);
		Point3D handPt = getJointPos(skel, SkeletonJoint.RIGHT_HAND);
		Point3D elbowPt = getJointPos(skel, SkeletonJoint.RIGHT_ELBOW);

		if ((neckPt != null) && (shoulderPt != null) && (handPt != null)
				&& (elbowPt != null)) {
			neckLength = distApart(neckPt, shoulderPt); // neck to shoulder
														// length
			// System.out.println("Neck Length: " + neckLength);

			armLength = distApart(handPt, shoulderPt); // hand to shoulder
														// length
			// System.out.println("Arm length: " + armLength);

			lowerArmLength = distApart(handPt, elbowPt); // hand to elbow length
			// System.out.println("Lower arm length: " + lowerArmLength);
		}
	}

	/**
	 * the Euclidian distance between the two points.
	 */
	private float distApart(Point3D p1, Point3D p2) {
		float dist = (float) Math.sqrt((p1.getX() - p2.getX())
				* (p1.getX() - p2.getX()) + (p1.getY() - p2.getY())
				* (p1.getY() - p2.getY()) + (p1.getZ() - p2.getZ())
				* (p1.getZ() - p2.getZ()));
		return dist;
	}

	/**
	 * get the (x, y, z) coordinate for the joint (or return null).
	 */
	private Point3D getJointPos(
			HashMap<SkeletonJoint, SkeletonJointPosition> skel, SkeletonJoint j) {
		SkeletonJointPosition pos = skel.get(j);
		if (pos == null)
			return null;

		if (pos.getConfidence() == 0)
			return null;

		return pos.getPosition();
	}

	// ------------GesturesWatcher.pose() -----------------------------

	// called by the gesture detectors
	public void pose(int userID, GestureName gest, boolean isActivated) {
		if (isActivated)
			System.out.println(gest + " " + userID + " on");
		else
			System.out.println("                        " + gest + " " + userID
					+ " off");
	}

	// ------------Gestures --------------------------------------------
	// -------------------------- two hands ----------------------------------

	// are the user's hand close together on the x-axis?
	private void twoHandsNear(int userID,
			HashMap<SkeletonJoint, SkeletonJointPosition> skel) {
		Point3D leftHandPt = getJointPos(skel, SkeletonJoint.LEFT_HAND);
		Point3D rightHandPt = getJointPos(skel, SkeletonJoint.RIGHT_HAND);
		if ((leftHandPt == null) || (rightHandPt == null))
			return;

		float xDiff = rightHandPt.getX() - leftHandPt.getX();
		// System.out.println(xDiff);
		if (xDiff < neckLength) { // near
			if (!areHandsNear) {
				pose(userID, GestureName.HANDS_NEAR, true); // started
				areHandsNear = true;
			}
		} else { // not near
			if (areHandsNear) {
				pose(userID, GestureName.HANDS_NEAR, false); // stopped
				areHandsNear = false;
			}
		}
	}

	// -------------------------- leaning ----------------------------------

	// is the user's head to the left of his left hip?
	private void leanLeft(int userID,
			HashMap<SkeletonJoint, SkeletonJointPosition> skel) {
		Point3D leftHipPt = getJointPos(skel, SkeletonJoint.LEFT_HIP);
		Point3D headPt = getJointPos(skel, SkeletonJoint.HEAD);
		if ((leftHipPt == null) || (headPt == null))
			return;

		if (headPt.getX() <= leftHipPt.getX()) { // to the left
			if (!isLeanLeft) {
				pose(userID, GestureName.LEAN_LEFT, true); // started
				isLeanLeft = true;
			}
		} else { // not to the left
			if (isLeanLeft) {
				pose(userID, GestureName.LEAN_LEFT, false); // stopped
				isLeanLeft = false;
			}
		}
	}

	// is the user's head to the right of his right hip?
	private void leanRight(int userID,
			HashMap<SkeletonJoint, SkeletonJointPosition> skel) {
		Point3D rightHipPt = getJointPos(skel, SkeletonJoint.RIGHT_HIP);
		Point3D headPt = getJointPos(skel, SkeletonJoint.HEAD);
		if ((rightHipPt == null) || (headPt == null))
			return;

		if (rightHipPt.getX() <= headPt.getX()) { // to the right
			if (!isLeanRight) {
				pose(userID, GestureName.LEAN_RIGHT, true); // started
				isLeanRight = true;
			}
		} else { // not to the right
			if (isLeanRight) {
				pose(userID, GestureName.LEAN_RIGHT, false); // stopped
				isLeanRight = false;
			}
		}
	}

	// is the user's head forward of his torso?
	private void leanFwd(int userID,
			HashMap<SkeletonJoint, SkeletonJointPosition> skel) {
		Point3D torsoPt = getJointPos(skel, SkeletonJoint.TORSO);
		Point3D headPt = getJointPos(skel, SkeletonJoint.HEAD);
		if ((torsoPt == null) || (headPt == null))
			return;

		float zDiff = headPt.getZ() - torsoPt.getZ();
		// System.out.println(zDiff);

		if (zDiff < -1 * lowerArmLength) { // head is forward
			if (!isLeanFwd) {
				pose(userID, GestureName.LEAN_FWD, true); // started
				isLeanFwd = true;
			}
		} else { // not forward
			if (isLeanFwd) {
				pose(userID, GestureName.LEAN_FWD, false); // stopped
				isLeanFwd = false;
			}
		}
	}

	// is the user's head leaning back relative to his torso?
	private void leanBack(int userID,
			HashMap<SkeletonJoint, SkeletonJointPosition> skel) {
		Point3D torsoPt = getJointPos(skel, SkeletonJoint.TORSO);
		Point3D headPt = getJointPos(skel, SkeletonJoint.HEAD);
		if ((torsoPt == null) || (headPt == null))
			return;

		float zDiff = headPt.getZ() - torsoPt.getZ();
		// System.out.println(zDiff);

		if (zDiff > lowerArmLength) { // head is behind
			if (!isLeanBack) {
				pose(userID, GestureName.LEAN_BACK, true); // started
				isLeanBack = true;
			}
		} else { // not behind
			if (isLeanBack) {
				pose(userID, GestureName.LEAN_BACK, false); // stopped
				isLeanBack = false;
			}
		}
	} // end of leanBack()

	// -------------------------- turning ----------------------------------

	// has the user's right hip turned forward to be in front of his left hip?
	private void turnLeft(int userID,
			HashMap<SkeletonJoint, SkeletonJointPosition> skel) {
		Point3D rightHipPt = getJointPos(skel, SkeletonJoint.RIGHT_HIP);
		Point3D leftHipPt = getJointPos(skel, SkeletonJoint.LEFT_HIP);
		if ((rightHipPt == null) || (leftHipPt == null))
			return;

		float zDiff = leftHipPt.getZ() - rightHipPt.getZ();
		// System.out.println(zDiff);

		if (zDiff > lowerArmLength) { // right hip is forward
			if (!isTurnLeft) {
				pose(userID, GestureName.TURN_LEFT, true); // started
				isTurnLeft = true;
			}
		} else { // not forward
			if (isTurnLeft) {
				pose(userID, GestureName.TURN_LEFT, false); // stopped
				isTurnLeft = false;
			}
		}
	} // end of turnLeft()

	// has the user's left hip turned forward to be in front of his right hip?
	private void turnRight(int userID,
			HashMap<SkeletonJoint, SkeletonJointPosition> skel) {
		Point3D rightHipPt = getJointPos(skel, SkeletonJoint.RIGHT_HIP);
		Point3D leftHipPt = getJointPos(skel, SkeletonJoint.LEFT_HIP);
		if ((rightHipPt == null) || (leftHipPt == null))
			return;

		float zDiff = rightHipPt.getZ() - leftHipPt.getZ();
		// System.out.println(zDiff);

		if (zDiff > lowerArmLength) { // left hip is forward
			if (!isTurnRight) {
				pose(userID, GestureName.TURN_RIGHT, true); // started
				isTurnRight = true;
			}
		} else { // not forward
			if (isTurnRight) {
				pose(userID, GestureName.TURN_RIGHT, false); // stopped
				isTurnRight = false;
			}
		}
	} // end of turnRight()

	// -------------------------- touching ----------------------------------

	// is the user's left hand touching his left hip?
	private void leftHandTouchHip(int userID,
			HashMap<SkeletonJoint, SkeletonJointPosition> skel) {
		Point3D leftHandPt = getJointPos(skel, SkeletonJoint.LEFT_HAND);
		Point3D leftHipPt = getJointPos(skel, SkeletonJoint.LEFT_HIP);
		if ((leftHandPt == null) || (leftHipPt == null))
			return;

		float dist = distApart(leftHipPt, leftHandPt);
		// System.out.println("dist: " + dist);

		if (dist < neckLength) { // is touching
			if (!lHandTouchlHip) {
				pose(userID, GestureName.LH_LHIP, true); // started
				lHandTouchlHip = true;
			}
		} else { // not touching
			if (lHandTouchlHip) {
				pose(userID, GestureName.LH_LHIP, false); // stopped
				lHandTouchlHip = false;
			}
		}
	} // end of leftHandTouchHip()

	// is the user's right hand touching his right hip?
	private void rightHandTouchHip(int userID,
			HashMap<SkeletonJoint, SkeletonJointPosition> skel) {
		Point3D rightHandPt = getJointPos(skel, SkeletonJoint.RIGHT_HAND);
		Point3D rightHipPt = getJointPos(skel, SkeletonJoint.RIGHT_HIP);
		if ((rightHandPt == null) || (rightHipPt == null))
			return;

		float dist = distApart(rightHipPt, rightHandPt);
		// System.out.println("dist: " + dist);

		if (dist < neckLength) { // is touching
			if (!rHandTouchrHip) {
				pose(userID, GestureName.RH_RHIP, true); // started
				rHandTouchrHip = true;
			}
		} else { // not touching
			if (rHandTouchrHip) {
				pose(userID, GestureName.RH_RHIP, false); // stopped
				rHandTouchrHip = false;
			}
		}
	} // end of rightHandTouchHip()

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
	private void rightHandUp(int userID,
			HashMap<SkeletonJoint, SkeletonJointPosition> skel) {
		Point3D rightHandPt = getJointPos(skel, SkeletonJoint.RIGHT_HAND);
		Point3D headPt = getJointPos(skel, SkeletonJoint.HEAD);
		if ((rightHandPt == null) || (headPt == null))
			return;

		if (rightHandPt.getY() <= headPt.getY()) { // above
			if (!isRightHandUp) {
				pose(userID, GestureName.RH_UP, true); // started
				gestureSequences.addUserGest(userID, GestureName.RH_UP); // add
																			// to
																			// gesture
																			// sequence
				isRightHandUp = true;
			}
		} else { // not above
			if (isRightHandUp) {
				pose(userID, GestureName.RH_UP, false); // stopped
				isRightHandUp = false;
			}
		}
	}

	// is the user's right hand forward of his right shoulder?
	private void rightHandFwd(int userID,
			HashMap<SkeletonJoint, SkeletonJointPosition> skel) {
		Point3D rightHandPt = getJointPos(skel, SkeletonJoint.RIGHT_HAND);
		Point3D shoulderPt = getJointPos(skel, SkeletonJoint.RIGHT_SHOULDER);
		if ((rightHandPt == null) || (shoulderPt == null))
			return;

		float zDiff = rightHandPt.getZ() - shoulderPt.getZ();
		// System.out.println("diff: " + zDiff);

		if (zDiff < -1 * (armLength * 0.95f)) { // is forward
			// System.out.println("  armLength: " + armLength);
			if (!isRightHandFwd) {
				pose(userID, GestureName.RH_FWD, true); // started
				gestureSequences.addUserGest(userID, GestureName.RH_FWD); // add
																			// to
																			// gesture
																			// sequence
				isRightHandFwd = true;
			}
		} else { // not forward
			if (isRightHandFwd) {
				pose(userID, GestureName.RH_FWD, false); // stopped
				isRightHandFwd = false;
			}
		}
	} // end of rightHandFwd()

	// is the user's right hand out to the right of the his right elbow?
	private void rightHandOut(int userID,
			HashMap<SkeletonJoint, SkeletonJointPosition> skel) {
		Point3D rightHandPt = getJointPos(skel, SkeletonJoint.RIGHT_HAND);
		Point3D elbowPt = getJointPos(skel, SkeletonJoint.RIGHT_ELBOW);
		if ((rightHandPt == null) || (elbowPt == null))
			return;

		float xDiff = rightHandPt.getX() - elbowPt.getX();

		if (xDiff > (lowerArmLength * 0.6f)) { // out to the right
			if (!isRightHandOut) {
				pose(userID, GestureName.RH_OUT, true); // started
				gestureSequences.addUserGest(userID, GestureName.RH_OUT); // add
																			// to
																			// gesture
																			// sequence
				isRightHandOut = true;
			}
		} else { // not out to the right
			if (isRightHandOut) {
				pose(userID, GestureName.RH_OUT, false); // stopped
				isRightHandOut = false;
			}
		}
	} // end of rightHandOut()

	// is the user's right hand inside (left) of his right elbow?
	private void rightHandIn(int userID,
			HashMap<SkeletonJoint, SkeletonJointPosition> skel) {
		Point3D rightHandPt = getJointPos(skel, SkeletonJoint.RIGHT_HAND);
		Point3D elbowPt = getJointPos(skel, SkeletonJoint.RIGHT_ELBOW);
		if ((rightHandPt == null) || (elbowPt == null))
			return;

		float xDiff = rightHandPt.getX() - elbowPt.getX();

		if (xDiff < -1 * (lowerArmLength * 0.6f)) { // inside
			if (!isRightHandIn) {
				pose(userID, GestureName.RH_IN, true); // started
				gestureSequences.addUserGest(userID, GestureName.RH_IN); // add
																			// to
																			// gesture
																			// sequence
				isRightHandIn = true;
			}
		} else { // not inside
			if (isRightHandIn) {
				pose(userID, GestureName.RH_IN, false); // stopped
				isRightHandIn = false;
			}
		}
	} // end of rightHandIn()

	// is the user's right hand at hip level or below?
	private void rightHandDown(int userID,
			HashMap<SkeletonJoint, SkeletonJointPosition> skel) {
		Point3D rightHandPt = getJointPos(skel, SkeletonJoint.RIGHT_HAND);
		Point3D hipPt = getJointPos(skel, SkeletonJoint.RIGHT_HIP);
		if ((rightHandPt == null) || (hipPt == null))
			return;

		if (rightHandPt.getY() >= hipPt.getY()) { // below
			if (!isRightHandDown) {
				pose(userID, GestureName.RH_DOWN, true); // started
				gestureSequences.addUserGest(userID, GestureName.RH_DOWN); // add
																			// to
																			// gesture
																			// sequence
				isRightHandDown = true;
			}
		} else { // not below
			if (isRightHandDown) {
				pose(userID, GestureName.RH_DOWN, false); // stopped
				isRightHandDown = false;
			}
		}
	} // end of rightHandDown()

	// -------------------------- left hand ----------------------------------

	// is the user's left hand at head level or above?
	private void leftHandUp(int userID,
			HashMap<SkeletonJoint, SkeletonJointPosition> skel) {
		Point3D leftHandPt = getJointPos(skel, SkeletonJoint.LEFT_HAND);
		Point3D headPt = getJointPos(skel, SkeletonJoint.NECK);
		if ((leftHandPt == null) || (headPt == null))
			return;

		if (leftHandPt.getY() <= headPt.getY()) { // above
			if (!isLeftHandUp) {
				pose(userID, GestureName.LH_UP, true); // started
				isLeftHandUp = true;
			}
		} else { // not above
			if (isLeftHandUp) {
				pose(userID, GestureName.LH_UP, false); // stopped
				isLeftHandUp = false;
			}
		}
	} // end of leftHandUp()

	
}
