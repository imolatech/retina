package com.imolatech.kinect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerMessenger implements Messenger {
	private static final Logger logger = LoggerFactory.getLogger(LoggerMessenger.class); 
	
	@Override
	public void send(String message) {
		if (message ==null) return;
		logger.debug("Message Received:{}", message);
	}

}
