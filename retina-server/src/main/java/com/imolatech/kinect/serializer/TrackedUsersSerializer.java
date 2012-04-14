package com.imolatech.kinect.serializer;

import java.util.Date;
import java.util.HashMap;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;

import com.google.gson.Gson;
import com.imolatech.kinect.message.TrackedUsersMessage;

/**
 * Convert a new user in event to a json protocol 
 * which client could understand. See protocol document
 * for detail.
 */
public class TrackedUsersSerializer implements MotionDataSerializer {
	private HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> usersSkeletons;
	public TrackedUsersSerializer(HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> usersSkeletons) {
		this.usersSkeletons = usersSkeletons;
	}
	public TrackedUsersSerializer() {
		
	}
	public void setUsersSkeletons(HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> usersSkeletons) {
		this.usersSkeletons = usersSkeletons;
	}
	@Override
	public String toJson() {
		if (usersSkeletons == null || usersSkeletons.isEmpty()) return null;
		TrackedUsersMessage message = new TrackedUsersMessage(usersSkeletons);
		message.setTimestamp((new Date()).getTime());
		//message.setActiveUserId
		Gson gson = new Gson();
		return gson.toJson(message);
	}
}
