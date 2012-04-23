package com.imolatech.kinect;

import org.OpenNI.Point3D;
import org.OpenNI.SkeletonJointPosition;
import org.apache.commons.lang.builder.ToStringBuilder;

public class Joint {
	private String name;
	private Point3D position;
	private float confidence;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Point3D getPosition() {
		return position;
	}
	public float getConfidence() {
		return confidence;
	}
	public void setPosition(SkeletonJointPosition position) {
		this.position = position.getPosition();
		this.confidence = position.getConfidence();
	}
	@Override
	public String toString() {
	    return ToStringBuilder.reflectionToString(this);
	}
}
