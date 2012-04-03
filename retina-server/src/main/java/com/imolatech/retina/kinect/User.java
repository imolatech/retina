package com.imolatech.retina.kinect;

import java.util.HashMap;

import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;

public class User {
	private Integer id;
	private String centerOfMass;
	private boolean active;
	private HashMap<SkeletonJoint, SkeletonJointPosition> joints;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getCenterOfMass() {
		return centerOfMass;
	}
	public void setCenterOfMass(String centerOfMass) {
		this.centerOfMass = centerOfMass;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public HashMap<SkeletonJoint, SkeletonJointPosition> getJoints() {
		return joints;
	}
	public void setJoints(HashMap<SkeletonJoint, SkeletonJointPosition> joints) {
		this.joints = joints;
	}
	
}
