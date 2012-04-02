package com.imolatech.retina.kinect.serializer;

import java.util.Date;

import com.google.gson.Gson;
import com.imolatech.retina.kinect.message.UserInMessage;

/**
 * Convert a new user in event to a json protocol 
 * which client could understand. See protocol document
 * for detail.
 */
public class NewUserSerializer implements MotionDataSerializer {
	private Integer userId;
	public NewUserSerializer(Integer userId) {
		this.userId = userId;
	}
	@Override
	public String toJson() {
		if (userId == null) return null;
		UserInMessage message = new UserInMessage(userId);
		message.setTimestamp((new Date()).getTime());
		Gson gson = new Gson();
		return gson.toJson(message);
	}
	
}
