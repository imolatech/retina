package com.imolatech.retina.kinect.message;

public class UserTrackingMessage extends KinectMessage {
	
	public UserTrackingMessage() {
		super.type = MessageType.USER_OUT;
	}
	
}
