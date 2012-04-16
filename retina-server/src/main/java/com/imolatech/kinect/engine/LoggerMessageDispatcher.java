package com.imolatech.kinect.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.imolatech.kinect.MessageDispatcher;

public class LoggerMessageDispatcher implements MessageDispatcher {
	private static final Logger logger = LoggerFactory.getLogger(LoggerMessageDispatcher.class); 
	
	@Override
	public void dispatch(String message) {
		if (message ==null) return;
		logger.debug("Message Received:{}", message);
	}

}
