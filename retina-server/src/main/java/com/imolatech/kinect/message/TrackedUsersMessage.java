package com.imolatech.kinect.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;

import com.imolatech.kinect.Joint;
import com.imolatech.kinect.User;

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
		List<Joint> result = new ArrayList<Joint>();
		
		for (SkeletonJoint sj : joints.keySet()) {
			Joint joint = new Joint();
			joint.setName(sj.name());
			joint.setPosition(joints.get(sj));
			result.add(joint);
		}
		user.setJoints(result);
		return user;
	}
	
}
