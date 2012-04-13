package com.imolatech.kinect.message;

public class UserOutMessage extends KinectMessage {
	private Integer userId;

	public UserOutMessage(Integer userId) {
		super.type = MessageType.USER_OUT;
		this.userId = userId;
	}
	
	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}
}
