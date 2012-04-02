package com.imolatech.retina.kinect;

import java.util.List;

public class User {
	private Integer id;
	private String centerOfMass;
	private boolean active;
	private List<UserSkeletonJoint> joints;
	
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
	public List<UserSkeletonJoint> getJoints() {
		return joints;
	}
	public void setJoints(List<UserSkeletonJoint> joints) {
		this.joints = joints;
	}
}
