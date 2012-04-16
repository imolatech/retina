package com.imolatech.kinect.sample.gesturetracker;

/*  Use OpenNI's Gesture Generator and Hands Generator with
 some of NITE's detectors (controls, listeners)
 getting data every frame. They are connected to a source
 of their required data (usually the Session Manager). 

 1. Wave Detector 
 - recognizes hand movement as a wave. A wave is usually
 4 direction changes within a specified time limit

 2. Push Detector 
 - recognizes hand movement as a push, towards the sensor and away 

 3. Swipe Detector 
 - recognizes hand movement as a swipe, either up, 
 down, left or right, followed by the hand resting

 4. Circle Detector 
 - recognizes hand movement as a circular motion;
 - needs a full circle in either direction to start;
 - clockwise is positive, anti-clockwise is negative.
 ** NOTE: the listener in initCircleDetector() is commented out 
 because CircleDetector.getCircleEvent() is declared private 
 in the API

 5. Steady Detector 
 - recognizes when a hand isn't moving for some time 

 All these are subclasses of PointControl.
 */
import org.OpenNI.*;

import com.imolatech.kinect.GestureName;
import com.imolatech.kinect.GestureWatcher;
import com.imolatech.kinect.HandPositionInfo;
import com.imolatech.kinect.detector.UserTracker;
import com.imolatech.kinect.engine.LoggerMessageDispatcher;
import com.primesense.NITE.*;

public class GestureTracker implements GestureWatcher {
	// OpenNI and NITE vars
	private Context context;
	private SessionManager sessionMan;
	private UserTracker userTracker;
	private boolean isRunning = true;
	private boolean updating = false;
	private HandPositionInfo pi = null; // for storing current hand point info
	private GestureGenerator gestureGen;
	
	public GestureTracker() {
		try {
			configOpenNI();
			configNITE();
			//context.startGeneratingAll();
			System.out.println();
			System.out.println("Make a click gesture to start the session");
			while (isRunning) {
				context.waitAnyUpdateAll();
				//context.waitOneUpdateAll(gestureGen);
				if (!userTracker.isUpdating()) {
					userTracker.update();
				}
				//if (!updating) {
				//	update(context);
				//}
			}
			context.release();
		} catch (GeneralException e) {
			e.printStackTrace();
		}
	} // end of GestureDetect()

	public void update(Context context) throws StatusException {
		updating = true;
		sessionMan.update(context);
		updating = false;
	}
	private void configOpenNI()
	// set up the Gesture and Hands Generators in OpenNI
	{
		try {
			context = new Context();

			// add the NITE Licence
			License licence = new License("PrimeSense",
					"0KOIk2JeIBYClPWVnMoRKn5cdY4=");
			context.addLicense(licence);

			// Depth and User to generate skeleton data
			DepthGenerator depthGenerator = DepthGenerator.create(context);
			// xRes, yRes and FPS
			MapOutputMode mapMode = new MapOutputMode(640, 480, 30); 
			depthGenerator.setMapOutputMode(mapMode);

			context.setGlobalMirror(true); // set mirror mode
			UserGenerator userGenerator = UserGenerator.create(context);
			
			userTracker = new UserTracker(userGenerator, depthGenerator, new LoggerMessageDispatcher(), this);
			userTracker.init();
						
			HandsGenerator handsGen = HandsGenerator.create(context); // OpenNI
			handsGen.SetSmoothing(0.1f);
			// 0-1: 0 means no smoothing, 1 means 'infinite'
			setHandEvents(handsGen);

			gestureGen = GestureGenerator.create(context); // OpenNI
			setGestureEvents(gestureGen);

			context.startGeneratingAll();
			System.out.println("Started context generating...");
		} catch (GeneralException e) {
			e.printStackTrace();
			System.exit(1);
		}
	} // end of configOpenNI()

	private void configNITE()
	// set up the NITE detectors
	{
		try {
			sessionMan = new SessionManager(context, "Click", "RaiseHand"); // NITE
			// main focus gesture(s), quick refocus gesture(s)
			setSessionEvents(sessionMan);

			// create point, wave, push, swipe, circle, steady NITE detectors;
			// connect them to the session manager
			PointControl pointCtrl = initPointControl();
			sessionMan.addListener(pointCtrl);

			WaveDetector wd = initWaveDetector();
			sessionMan.addListener(wd);

			PushDetector pd = initPushDetector();
			sessionMan.addListener(pd);

			SwipeDetector sd = initSwipeDetector();
			sessionMan.addListener(sd);

			CircleDetector cd = initCircleDetector();
			sessionMan.addListener(cd);

			SteadyDetector sdd = initSteadyDetector();
			sessionMan.addListener(sdd);
		} catch (GeneralException e) {
			e.printStackTrace();
			System.exit(1);
		}
	} // end of configNITE()

	// ------------GesturesWatcher.pose() -----------------------------

	// called by the gesture detectors
	public void pose(int userID, GestureName gest, boolean isActivated) {
		if (isActivated)
			System.out.println(gest + " " + userID + " on");
		else
			System.out.println("                        " + gest + " " + userID
					+ " off");
	} // end of pose()
	
	// -------- set event processing callbacks -------------------------

	private void setHandEvents(HandsGenerator handsGen)
	// create HandsGenerator callbacks
	{
		try {
			// when hand is created
			handsGen.getHandCreateEvent().addObserver(
					new IObserver<ActiveHandEventArgs>() {
						public void update(
								IObservable<ActiveHandEventArgs> observable,
								ActiveHandEventArgs args) {
							int id = args.getId();
							Point3D pt = args.getPosition();
							float time = args.getTime();
							System.out
									.printf("Hand %d located at (%.0f, %.0f, %.0f), at %.0f secs\n",
											id, pt.getX(), pt.getY(),
											pt.getZ(), time);
						}
					});

			// when hand is destroyed
			handsGen.getHandDestroyEvent().addObserver(
					new IObserver<InactiveHandEventArgs>() {
						public void update(
								IObservable<InactiveHandEventArgs> observable,
								InactiveHandEventArgs args) {
							int id = args.getId();
							float time = args.getTime();
							System.out.printf(
									"Hand %d destroyed at %.0f secs \n", id,
									time);
						}
					});
		} catch (StatusException e) {
			e.printStackTrace();
		}
	} // end of setHandEvents()

	private void setGestureEvents(GestureGenerator gestureGen)
	// create GestureGenerator callback
	{
		try {
			// when gesture is recognized
			gestureGen.getGestureRecognizedEvent().addObserver(
					new IObserver<GestureRecognizedEventArgs>() {
						public void update(
								IObservable<GestureRecognizedEventArgs> observable,
								GestureRecognizedEventArgs args) {
							String gestureName = args.getGesture();
							Point3D idPt = args.getIdPosition();
							// hand position when gesture was identified
							Point3D endPt = args.getEndPosition();
							// hand position at the end of the gesture
							System.out
									.printf("Gesture \"%s\" recognized at (%.0f, %.0f, %.0f); ended at (%.0f, %.0f, %.0f)\n",
											gestureName, idPt.getX(),
											idPt.getY(), idPt.getZ(),
											endPt.getX(), endPt.getY(),
											endPt.getZ());
						}
					});
		} catch (StatusException e) {
			e.printStackTrace();
		}
	} // end of setGestureEvents()

	private void setSessionEvents(SessionManager sessionMan)
	// create session callbacks
	{
		try {
			// when a focus gesture has started to be recognized
			sessionMan.getSessionFocusProgressEvent().addObserver(
					new IObserver<StringPointValueEventArgs>() {
						public void update(
								IObservable<StringPointValueEventArgs> observable,
								StringPointValueEventArgs args) {
							Point3D focusPt = args.getPoint();
							float progress = args.getValue();
							String focusName = args.getName();
							System.out
									.printf("Session focused at (%.0f, %.0f, %.0f) on %s [progress %.2f]\n",
											focusPt.getX(), focusPt.getY(),
											focusPt.getZ(), focusName, progress);
						}
					});

			// session started
			sessionMan.getSessionStartEvent().addObserver(
					new IObserver<PointEventArgs>() {
						public void update(
								IObservable<PointEventArgs> observable,
								PointEventArgs args) {
							Point3D focusPt = args.getPoint();
							System.out.printf(
									"Session started at (%.0f, %.0f, %.0f)\n",
									focusPt.getX(), focusPt.getY(),
									focusPt.getZ());
						}
					});

			// session end
			sessionMan.getSessionEndEvent().addObserver(
					new IObserver<NullEventArgs>() {
						public void update(
								IObservable<NullEventArgs> observable,
								NullEventArgs args) {
							System.out.println("Session ended");
							isRunning = false; // causes loop in constructor to
												// end, so program exits
						}
					});
		} catch (StatusException e) {
			e.printStackTrace();
		}
	} // end of setSessionEvents()

	// ------------- set up detectors and callbacks --------------------------
	// for point, wave, push, swipe, circle, steady

	private PointControl initPointControl() {
		PointControl pointCtrl = null;
		try {
			pointCtrl = new PointControl();

			// create new hand point
			pointCtrl.getPointCreateEvent().addObserver(
					new IObserver<HandEventArgs>() {
						public void update(
								IObservable<HandEventArgs> observable,
								HandEventArgs args) {
							pi = new HandPositionInfo(args.getHand());
							System.out.println(pi);
						}
					});

			// hand point has moved
			pointCtrl.getPointUpdateEvent().addObserver(
					new IObserver<HandEventArgs>() {
						public void update(
								IObservable<HandEventArgs> observable,
								HandEventArgs args) {
							HandPointContext handContext = args.getHand();
							if (pi == null)
								pi = new HandPositionInfo(handContext);
							else
								pi.update(handContext);
							// System.out.println(pi); // commented out to
							// reduce output
						}
					});

			// destroy hand point
			pointCtrl.getPointDestroyEvent().addObserver(
					new IObserver<IdEventArgs>() {
						public void update(IObservable<IdEventArgs> observable,
								IdEventArgs args) {
							int id = args.getId();
							System.out.printf("Point %d destroyed:\n", id);
							if (pi.getID() == id)
								pi = null;
						}
					});

		} catch (GeneralException e) {
			e.printStackTrace();
		}
		return pointCtrl;
	} // end of initPointControl()

	private WaveDetector initWaveDetector() {
		WaveDetector waveDetector = null;
		try {
			waveDetector = new WaveDetector();

			// some wave settings; change with set
			int flipCount = waveDetector.getFlipCount();
			int flipLen = waveDetector.getMinLength();
			System.out.println("Wave settings -- no. of flips: " + flipCount
					+ "; min length: " + flipLen + "mm");

			// callback
			waveDetector.getWaveEvent().addObserver(
					new IObserver<NullEventArgs>() {
						public void update(
								IObservable<NullEventArgs> observable,
								NullEventArgs args) {
							System.out.println("Wave detected");
							System.out.println("  " + pi); // show current hand
															// point
						}
					});
		} catch (GeneralException e) {
			e.printStackTrace();
		}
		return waveDetector;
	} // end of initWaveDetector()

	private PushDetector initPushDetector() {
		PushDetector pushDetector = null;
		try {
			pushDetector = new PushDetector();

			// some push settings; change with set
			float minVel = pushDetector.getPushImmediateMinimumVelocity();
			// minimum velocity in the time span to define as push, in m/s

			float duration = pushDetector.getPushImmediateDuration();
			// time used to detect push, in ms

			float angleZ = pushDetector
					.getPushMaximumAngleBetweenImmediateAndZ();
			// max angle between immediate direction and Z-axis, in degrees

			System.out
					.printf("Push settings -- min velocity: %.1f m/s; min duration: %.1f ms; max angle to z-axis: %.1f degs \n",
							minVel, duration, angleZ);

			// callback
			pushDetector.getPushEvent().addObserver(
					new IObserver<VelocityAngleEventArgs>() {
						public void update(
								IObservable<VelocityAngleEventArgs> observable,
								VelocityAngleEventArgs args) {
							System.out
									.printf("Push: velocity %.1f m/s, angle %.1f degs \n",
											args.getVelocity(), args.getAngle());
							System.out.println("  " + pi); // show current hand
															// point
						}
					});
		} catch (GeneralException e) {
			e.printStackTrace();
		}
		return pushDetector;
	} // end of initPushDetector()

	private SwipeDetector initSwipeDetector() {
		SwipeDetector swipeDetector = null;
		try {
			swipeDetector = new SwipeDetector();

			// some swipe settings; change with set
			System.out.println("Swipe setting -- min motion time: "
					+ swipeDetector.getMotionTime() + " ms");

			// general swipe callback
			swipeDetector.getGeneralSwipeEvent().addObserver(
					new IObserver<DirectionVelocityAngleEventArgs>() {
						public void update(
								IObservable<DirectionVelocityAngleEventArgs> observable,
								DirectionVelocityAngleEventArgs args) {
							System.out
									.printf("Swipe %s: velocity %.1f m/s, angle %.1f degs \n",
											args.getDirection(),
											args.getVelocity(), args.getAngle());
							System.out.println("  " + pi); // show current hand
															// point
						}
					});

			// callback for left swipes only;
			swipeDetector.getSwipeLeftEvent().addObserver(
					new IObserver<VelocityAngleEventArgs>() {
						public void update(
								IObservable<VelocityAngleEventArgs> observable,
								VelocityAngleEventArgs args) {
							System.out
									.printf("*Left* Swipe: velocity %.1f m/s, angle %.1f degs \n",
											args.getVelocity(), args.getAngle());
						}
					});
		} catch (GeneralException e) {
			e.printStackTrace();
		}
		return swipeDetector;
	} // end of initSwipeDetector()

	private CircleDetector initCircleDetector() {
		CircleDetector circleDetector = null;
		try {
			circleDetector = new CircleDetector();

			// print some circle settings
			System.out.println("Circle setting -- min-max radius: "
					+ circleDetector.getMinRadius() + " - "
					+ circleDetector.getMaxRadius() + " mm");

			// callback: PROBLEM: getCircleEvent() is defined as private!
			/*
			 * circleDetector.getCircleEvent().addObserver( new
			 * IObserver<CircleEventArgs>() { public void
			 * update(IObservable<CircleEventArgs> observable, CircleEventArgs
			 * args) { Circle circle = args.getCircle(); Point3D center =
			 * circle.getCenter(); System.out.printf(
			 * "Circle: center (%.0f, %.0f, %.0f), radius %.0f, times %d\n",
			 * center.getX(), center.getY(), center.getZ(), circle.getRadius(),
			 * args.getTimes()); } });
			 */
		} catch (GeneralException e) {
			e.printStackTrace();
		}
		return circleDetector;
	} // end of initCircleDetector()

	private SteadyDetector initSteadyDetector() {
		SteadyDetector steadyDetector = null;
		try {
			steadyDetector = new SteadyDetector();
			System.out.println("Steady settings -- min duration: "
					+ steadyDetector.getDetectionDuration() + " ms");
			System.out.printf("                   max movement: %.3f mm\n",
					steadyDetector.getMaxDeviationForSteady());

			// callback
			steadyDetector.getSteadyEvent().addObserver(
					new IObserver<IdValueEventArgs>() {
						public void update(
								IObservable<IdValueEventArgs> observable,
								IdValueEventArgs args) {
							System.out.printf(
									"Hand %d is steady: movement %.3f\n",
									args.getId(), args.getValue());
							System.out.println("  " + pi); // show current hand
															// point
						}
					});
		} catch (GeneralException e) {
			e.printStackTrace();
		}
		return steadyDetector;
	} // end of initSteadyDetector()

	// ----------------------------------

	public static void main(String args[]) {
		new GestureTracker();
	}

} // end of GestureDetect class
