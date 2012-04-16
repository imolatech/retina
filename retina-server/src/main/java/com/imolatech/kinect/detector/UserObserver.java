package com.imolatech.kinect.detector;

public interface UserObserver {
	void onUserIn(int userId);
	void onUserOut(int userId);
	void onUserTracked(int userId);
}
