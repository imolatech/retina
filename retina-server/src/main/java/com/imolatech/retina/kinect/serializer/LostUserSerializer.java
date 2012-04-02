package com.imolatech.retina.kinect.serializer;

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
		String json = "{\"userId\":\"" + userId + 
				"\",\"status\":\"" + "LOST" +
				"\"}";
		return json;
	}
	
}
