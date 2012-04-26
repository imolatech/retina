package com.imolatech.kinect.message;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.imolatech.kinect.GestureName;


public class UsersGesturesMessage extends KinectMessage {
	private List<UserGestures> gestures = new ArrayList<UserGestures>();
	
	public UsersGesturesMessage() {
		super.type = MessageType.GESTURES;
	}
	
	public UsersGesturesMessage(Map<Integer, List<GestureName>> usersGestures) {
		super.type = MessageType.GESTURES;
		for (Integer userId : usersGestures.keySet()) {
			UserGestures ug = new UserGestures(userId, usersGestures.get(userId));
			gestures.add(ug);
		}
	}

	public List<UserGestures> getGestures() {
		return gestures;
	}
	
	public void setGestures(List<UserGestures> gestures) {
		this.gestures = gestures;
	}
	
	@Override
	public String toString() {
	    return ToStringBuilder.reflectionToString(this);
	}
}
