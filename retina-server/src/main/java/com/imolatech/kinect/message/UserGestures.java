package com.imolatech.kinect.message;

import java.util.ArrayList;
import java.util.List;
import com.imolatech.kinect.GestureName;

public class UserGestures {
	private Integer userId;
	private List<GestureName> gestures = new ArrayList<GestureName>();
	
	public UserGestures(Integer userId, List<GestureName> gestures) {
		this.userId = userId;
		this.gestures = gestures;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer id) {
		this.userId = id;
	}
	public List<GestureName> getGestures() {
		return gestures;
	}
	public void setGestures(List<GestureName> gestures) {
		this.gestures = gestures;
	}
	
	
}
