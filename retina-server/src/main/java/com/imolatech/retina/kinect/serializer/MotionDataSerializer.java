package com.imolatech.retina.kinect.serializer;

/**
 * In the future we could add other formats, such as xml.
 * @author Wenhu
 *
 */
public interface MotionDataSerializer {
	/**
	 * Convert any kinect event data to json format 
	 * which client could understand. See protocol document
	 * for detail.
	 * @return the json string result or if anything wrong, return null.
	 */
	String toJson();
	
}
