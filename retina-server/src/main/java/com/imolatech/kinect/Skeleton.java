package com.imolatech.kinect;

import java.util.Map;

import org.OpenNI.Point3D;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Skeleton {
	// standard skeleton lengths
	private static final float NECK_LEN = 50.0f;
	private static final float LOWER_ARM_LEN = 150.0f;
	private static final float ARM_LEN = 400.0f;

	//skeleton lengths between joint pairs, used when judging the distance
	// between other joints
	private float neckLength = NECK_LEN; // neck to shoulder length
	private float lowerArmLength = LOWER_ARM_LEN; // hand to elbow length
	private float armLength = ARM_LEN; // hand to shoulder length
	private int userId = -1;
	private Map<SkeletonJoint, SkeletonJointPosition> joints;
	
	
	
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public Map<SkeletonJoint, SkeletonJointPosition> getJoints() {
		return joints;
	}
	public void setJoints(Map<SkeletonJoint, SkeletonJointPosition> joints) {
		this.joints = joints;
	}
	public float getNeckLength() {
		return neckLength;
	}
	public void setNeckLength(float neckLength) {
		this.neckLength = neckLength;
	}
	public float getLowerArmLength() {
		return lowerArmLength;
	}
	public void setLowerArmLength(float lowerArmLength) {
		this.lowerArmLength = lowerArmLength;
	}
	public float getArmLength() {
		return armLength;
	}
	public void setArmLength(float armLength) {
		this.armLength = armLength;
	}
	
	
	@Override
	public String toString() {
	    return ToStringBuilder.reflectionToString(this);
	}
	
	public Point3D getJointPosition(SkeletonJoint joint) {
		if (joint == null || joints == null || joints.isEmpty()) {
			return null;
		}
		return SkeletonUtility.getJointPosition(joints, joint);
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
	public void init(int userId,
			Map<SkeletonJoint, SkeletonJointPosition> joints) {
		//if (this.userId >= 0 && this.userId == userId) return;//already detected
		this.userId = userId;
		this.joints = joints;
		Point3D neckPt = SkeletonUtility.getJointPosition(joints, SkeletonJoint.NECK);
		Point3D shoulderPt = SkeletonUtility.getJointPosition(joints, SkeletonJoint.RIGHT_SHOULDER);
		Point3D handPt = SkeletonUtility.getJointPosition(joints, SkeletonJoint.RIGHT_HAND);
		Point3D elbowPt = SkeletonUtility.getJointPosition(joints, SkeletonJoint.RIGHT_ELBOW);

		if ((neckPt != null) && (shoulderPt != null) && (handPt != null)
				&& (elbowPt != null)) {
			 // neck to shoulder length
			neckLength = SkeletonUtility.distApart(neckPt, shoulderPt);
			//logger.debug("Neck Length: {}", neckLength);
			// hand to shoulder length
			armLength = SkeletonUtility.distApart(handPt, shoulderPt); 
			//logger.debug("Arm length: {}", armLength);
			// hand to elbow length
			lowerArmLength = SkeletonUtility.distApart(handPt, elbowPt); 
			//logger.debug("Lower arm length: {}", lowerArmLength);
			if (lowerArmLength * 2 > armLength) armLength = lowerArmLength * 2;
		}
	}
}
