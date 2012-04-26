package com.imolatech.kinect.serializer;

import java.util.Date;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.imolatech.kinect.GestureName;
import com.imolatech.kinect.message.UsersGesturesMessage;

/**
 * Convert a new user in event to a json protocol 
 * which client could understand. See protocol document
 * for detail.
 */
public class UsersGesturesSerializer implements MotionDataSerializer {
	private Map<Integer, List<GestureName>> usersGestures;
	public UsersGesturesSerializer(Map<Integer, List<GestureName>> usersGestures) {
		this.usersGestures = usersGestures;
	}
	public UsersGesturesSerializer() {
		
	}
	public void setUsersGestures(Map<Integer, List<GestureName>> usersGestures) {
		this.usersGestures = usersGestures;
	}
	@Override
	public String toJson() {
		if (usersGestures == null || usersGestures.isEmpty()) return null;
		UsersGesturesMessage message = new UsersGesturesMessage(usersGestures);
		message.setTimestamp((new Date()).getTime());
		//message.setActiveUserId
		Gson gson = new Gson();
		return gson.toJson(message);
	}
}
