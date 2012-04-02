package com.imolatech.retina.kinect;

import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;

public class UserSkeletonJoint {
	private SkeletonJoint joint;
	private SkeletonJointPosition position;
	
	public SkeletonJoint getJoint() {
		return joint;
	}
	public void setJoint(SkeletonJoint joint) {
		this.joint = joint;
	}
	public SkeletonJointPosition getPosition() {
		return position;
	}
	public void setPosition(SkeletonJointPosition position) {
		this.position = position;
	}
}
