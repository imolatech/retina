package com.imolatech.kinect.engine;


import org.OpenNI.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.imolatech.kinect.detector.HandGestureDetector;
import com.imolatech.kinect.detector.UserTracker;
import com.primesense.NITE.SessionManager;


public class KinectEngine implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(KinectEngine.class);
	private volatile boolean running;
	private Context context; //OPENNI
	private SessionManager sessionManager; //NITE
	//tracking users and their skeletons
	private UserTracker userTracker; 
	private HandGestureDetector gestureDetector;
	private long totalTime = 0;
	private Messenger messenger;
	private boolean runGestureDetector = false;
	public KinectEngine(Messenger messenger) {
		this.messenger = messenger;
	}
	public boolean start() {
		try {
			configOpenNI();
			context.startGeneratingAll();
			logger.info("Started context generating...");
		} catch (Exception e) {
			logger.warn("Kinect configuration errror.", e);
			return false;
		}
		logger.info("Kinect connected.");
		return true;
	} 

	public void listen() {
		logger.info("Start to listen on Kinect sensor.");
		new Thread(this).start(); 
	}
	/*
	 * create context, depth generator, depth metadata, user generator, scene
	 * metadata, and skeletons
	 */
	private void configOpenNI() throws Exception {
		if (context == null) {
			context = new Context();
			// add the NITE Licence
			License license = new License("PrimeSense",
					"0KOIk2JeIBYClPWVnMoRKn5cdY4="); // vendor, key
			context.addLicense(license);
			
			// Depth and User to generate skeleton data
			DepthGenerator depthGenerator = DepthGenerator.create(context);
			// xRes, yRes and FPS
			MapOutputMode mapMode = new MapOutputMode(640, 480, 30); 
			depthGenerator.setMapOutputMode(mapMode);

			context.setGlobalMirror(true); // set mirror mode
			UserGenerator userGenerator = UserGenerator.create(context);
			
			userTracker = new UserTracker(userGenerator, depthGenerator, messenger);
			userTracker.init();
			if (runGestureDetector) {
				configGestureDetector();
			}
		}
		
	} // end of configOpenNI()
	private void configGestureDetector() throws Exception {
		// Gesture and hands to generate gesture data
		HandsGenerator handsGenerator = HandsGenerator.create(context); // OpenNI
		// 0-1: 0 means no smoothing, 1 means 'infinite'
		handsGenerator.SetSmoothing(0.1f);
		GestureGenerator gestureGenerator = GestureGenerator.create(context); // OpenNI
		
		sessionManager = new SessionManager(context, "Click", "RaiseHand"); // NITE
		gestureDetector = new HandGestureDetector(handsGenerator, gestureGenerator, sessionManager, messenger);
		
		gestureDetector.init();
	}
	public long getTotalTime() {
		return totalTime;
	}

	/**
	 * Stop generating data, later we can resume it.
	 */
	public void stop() {
		logger.info("Make Kinect Listener stop.");
		running = false;
	}
	
	/**
	 * Completely release the context
	 */
	public void shutdown() {
		context.release();
	}
	public boolean isRunning() {
		return running;
	}
	public void run() {
		totalTime = 0;
		running = true;
		while (running) {
			try {
				context.waitAnyUpdateAll();
				if (runGestureDetector) {
					sessionManager.update(context);
				}
			} catch (StatusException e) {
				logger.error("Error while waiting for context.update", e);
				return;
			}
			long startTime = System.currentTimeMillis();
			userTracker.update();
			totalTime += (System.currentTimeMillis() - startTime);
		}
		logger.info("Stop generating Kinect data");
		// close down
		try {
			context.stopGeneratingAll();
		} catch (StatusException e) {
			logger.warn("Stop Kinect error.", e);
		}
		logger.info("Stopped generating Kinect data");
		//context.release();
	} // end of run()

} // end of TrackerPanel class

