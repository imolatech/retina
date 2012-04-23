package com.imolatech.kinect.message;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Messages we sent to client.
 * Note ns + type unique identify the message.
 * @author Wenhu
 *
 */
public abstract class KinectMessage {
	// gson will only look for property, not getter
	protected MessageType type;
	private Long timestamp;
	private String ns = "com.imolatech.kinect"; //name space
	
	public MessageType getType() {
		return type;
	}
	
	protected void setType(MessageType type) {
		this.type = type;
	}
	
	public Long getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	
	public String getNs() {
		return ns;
	}
	
	protected void setNs(String ns) {
		this.ns = ns;
	}
	@Override
	public String toString() {
	    return ToStringBuilder.reflectionToString(this);
	}

}
