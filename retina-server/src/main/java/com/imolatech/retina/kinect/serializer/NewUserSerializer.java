package com.imolatech.retina.kinect.serializer;

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
		String json = "{\"userId\":\"" + userId + 
				"\",\"status\":\"" + "NEW" +
				"\"}";
		return json;
	}
	
}
