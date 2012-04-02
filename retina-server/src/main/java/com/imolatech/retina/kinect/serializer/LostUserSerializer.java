package com.imolatech.retina.kinect.serializer;

import java.util.Date;

import com.google.gson.Gson;
import com.imolatech.retina.kinect.message.UserOutMessage;

/**
 * Convert a new user in event to a json protocol 
 * which client could understand. See protocol document
 * for detail.
 */
public class LostUserSerializer implements MotionDataSerializer {
	private Integer userId;
	public LostUserSerializer(Integer userId) {
		this.userId = userId;
	}
	@Override
	public String toJson() {
		if (userId == null) return null;
		UserOutMessage message = new UserOutMessage(userId);
		message.setTimestamp((new Date()).getTime());
		Gson gson = new Gson();
		return gson.toJson(message);
	}
	
}
