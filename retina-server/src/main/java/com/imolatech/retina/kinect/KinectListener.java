package com.imolatech.retina.kinect;


import org.OpenNI.*;

public class KinectListener implements Runnable {
	private volatile boolean isRunning;
	private Context context;
	private Skeletons skels; // the users' skeletons
	private long totalTime = 0;
	
	public boolean start() {
		try {
			configOpenNI();
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
		return true;
	} 

	public void listen() {
		new Thread(this).start(); 
	}
	/*
	 * create context, depth generator, depth metadata, user generator, scene
	 * metadata, and skeletons
	 */
	private void configOpenNI() throws Exception {
		
			context = new Context();

			// add the NITE Licence
			License license = new License("PrimeSense",
					"0KOIk2JeIBYClPWVnMoRKn5cdY4="); // vendor, key
			context.addLicense(license);

			DepthGenerator depthGen = DepthGenerator.create(context);
			MapOutputMode mapMode = new MapOutputMode(640, 480, 30); // xRes,
																		// yRes,
																		// FPS
			depthGen.setMapOutputMode(mapMode);

			context.setGlobalMirror(true); // set mirror mode
			UserGenerator userGen = UserGenerator.create(context);
			
			skels = new Skeletons(userGen, depthGen);

			context.startGeneratingAll();
			System.out.println("Started context generating...");
		
	} // end of configOpenNI()

	public long getTotalTime() {
		return totalTime;
	}

	public void stop() {
		isRunning = false;
	}
	
	public void run() {
		totalTime = 0;
		isRunning = true;
		while (isRunning) {
			try {
				context.waitAnyUpdateAll();
			} catch (StatusException e) {
				System.out.println(e);
				return;
			}
			long startTime = System.currentTimeMillis();
			skels.update();
			totalTime += (System.currentTimeMillis() - startTime);
		}
		// close down
		try {
			context.stopGeneratingAll();
		} catch (StatusException e) {
		}
		context.release();
		
	} // end of run()

} // end of TrackerPanel class

