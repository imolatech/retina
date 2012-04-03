package com.imolatech.retina.kinect.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;

import com.imolatech.retina.kinect.User;

public class TrackedUsersMessage extends KinectMessage {
	private List<User> users;
	public TrackedUsersMessage(HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> usersSkeletons) {
		super.type = MessageType.TRACKED_USERS;
		users = new ArrayList<User>();
		for (Integer userId : usersSkeletons.keySet()) {
			if (userId != null) {
				users.add(buildUser(userId, usersSkeletons.get(userId)));
			}
		}
	}
	private User buildUser(Integer userId,
			HashMap<SkeletonJoint, SkeletonJointPosition> joints) {
		User user = new User();
		user.setId(userId);
		//TODO need set active user.setActive(false);
		user.setJoints(joints);
		return user;
	}
	
}
