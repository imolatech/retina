package com.imolatech.retina.kinect.message;

public class UserInMessage extends KinectMessage {
	private Integer userId;

	public UserInMessage(Integer userId) {
		super.type = MessageType.USER_IN;
		this.userId = userId;
	}
	
	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}
}
