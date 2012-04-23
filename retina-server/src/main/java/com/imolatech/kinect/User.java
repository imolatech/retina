package com.imolatech.kinect;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

public class User {
	private Integer id;
	private String centerOfMass;
	private boolean active;
	//private HashMap<SkeletonJoint, SkeletonJointPosition> joints;
	private List<Joint> joints;
	
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
	public List<Joint> getJoints() {
		return joints;
	}
	public void setJoints(List<Joint> joints) {
		this.joints = joints;
	}
	@Override
	public String toString() {
	    return ToStringBuilder.reflectionToString(this);
	}

}
