package com.imolatech.retina.kinect.serializer;

import java.util.HashMap;

import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;

import com.google.gson.Gson;

/**
 * Convert a new user in event to a json protocol 
 * which client could understand. See protocol document
 * for detail.
 */
public class UserTrackingSerializer implements MotionDataSerializer {
	private HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> usersSkeletons;
	public UserTrackingSerializer(HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> usersSkeletons) {
		this.usersSkeletons = usersSkeletons;
	}
	@Override
	public String toJson() {
		if (usersSkeletons == null || usersSkeletons.isEmpty()) return null;
		Gson gson = new Gson();
		return gson.toJson(usersSkeletons);
	}
	
}
