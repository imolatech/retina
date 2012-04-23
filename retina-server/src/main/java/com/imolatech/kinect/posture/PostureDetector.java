package com.imolatech.kinect.posture;

import java.util.HashMap;

import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.imolatech.kinect.Skeleton;
import com.imolatech.kinect.GestureName;
import com.imolatech.kinect.GestureWatcher;

public abstract class PostureDetector {
	private static final Logger logger = LoggerFactory
			.getLogger(PostureDetector.class); 
	private int accumulatorTarget = 10;
    private String previousPosture = "";
    private int accumulator;
    private String accumulatedPosture = "";
    private GestureWatcher watcher;
    
    protected PostureDetector(GestureWatcher watcher, int accumulators)  {
        accumulatorTarget = accumulators;
        this.watcher = watcher;
    }
    
    protected PostureDetector(GestureWatcher watcher)  {
        this.watcher = watcher;
    }
    
    public String getCurrentPosture() {
       return previousPosture; 
    }
    
    public void setCurrentPosture(String posture) {
    	this.previousPosture = posture;
    }
    public abstract void detectPostures(int userId, Skeleton context,
			HashMap<SkeletonJoint, SkeletonJointPosition> skeleton);
    
    protected void raisePostureDetected(int userId, String posture) {
        if (accumulator < accumulatorTarget) {
        	logger.debug("1");
            if (accumulatedPosture != posture) {
            	logger.debug("12");
                accumulator = 0;
                accumulatedPosture = posture;
            }
            logger.debug("13");
            accumulator++;
            return;
        }
        logger.debug("2");
        if (previousPosture == posture)
            return;
        logger.debug("3");
        previousPosture = posture;
        if (watcher != null)
            watcher.pose(userId, GestureName.valueOf(posture), true);

        accumulator = 0;
    }

    protected void reset() {
        previousPosture = "";
        accumulator = 0;
    }
}
