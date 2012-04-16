package com.imolatech.kinect.detector;

import org.OpenNI.ActiveHandEventArgs;
import org.OpenNI.GeneralException;
import org.OpenNI.GestureGenerator;
import org.OpenNI.GestureRecognizedEventArgs;
import org.OpenNI.HandsGenerator;
import org.OpenNI.IObservable;
import org.OpenNI.IObserver;
import org.OpenNI.InactiveHandEventArgs;
import org.OpenNI.Point3D;
import org.OpenNI.StatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.imolatech.kinect.HandPositionInfo;
import com.imolatech.kinect.MessageDispatcher;
import com.primesense.NITE.CircleDetector;
import com.primesense.NITE.DirectionVelocityAngleEventArgs;
import com.primesense.NITE.HandEventArgs;
import com.primesense.NITE.HandPointContext;
import com.primesense.NITE.IdEventArgs;
import com.primesense.NITE.IdValueEventArgs;
import com.primesense.NITE.NullEventArgs;
import com.primesense.NITE.PointControl;
import com.primesense.NITE.PointEventArgs;
import com.primesense.NITE.PushDetector;
import com.primesense.NITE.SessionManager;
import com.primesense.NITE.SteadyDetector;
import com.primesense.NITE.StringPointValueEventArgs;
import com.primesense.NITE.SwipeDetector;
import com.primesense.NITE.VelocityAngleEventArgs;
import com.primesense.NITE.WaveDetector;

/**
 * Based on NITE hand gesture generators.
 * Somehow it does not work well with the skeleton generator.
 * Always crash.
 * @author Wenhu
 *
 */
public class HandGestureDetector {
	private static final Logger logger = LoggerFactory
			.getLogger(HandGestureDetector.class);
	private MessageDispatcher messenger;
	private HandsGenerator handsGenerator;
	private GestureGenerator gestureGenerator;
	private SessionManager sessionManager;
	// for storing current hand point info
	private HandPositionInfo pi = null;

	public HandGestureDetector(HandsGenerator handsGenerator,
			GestureGenerator gestureGenerator, SessionManager sessionManager,
			MessageDispatcher messenger) {
		this.handsGenerator = handsGenerator;
		this.gestureGenerator = gestureGenerator;
		this.messenger = messenger;
		this.sessionManager = sessionManager;
	}

	public void init() throws GeneralException {
		logger.debug("init...");
		addHandEvents();
		addGestureEvents();
		logger.debug("config nite...");
		configNITE();
	}

	private void configNITE() throws GeneralException {
		addSessionEvents();
		addGestureListeners();
	}

	private void addGestureListeners() throws GeneralException {
		// create point, wave, push, swipe, circle, steady NITE detectors;
		// connect them to the session manager
		PointControl pointCtrl = initPointControl();
		sessionManager.addListener(pointCtrl);

		WaveDetector wd = initWaveDetector();
		sessionManager.addListener(wd);

		PushDetector pd = initPushDetector();
		sessionManager.addListener(pd);

		SwipeDetector sd = initSwipeDetector();
		sessionManager.addListener(sd);

		CircleDetector cd = initCircleDetector();
		sessionManager.addListener(cd);

		SteadyDetector sdd = initSteadyDetector();
		sessionManager.addListener(sdd);
	}

	// create HandsGenerator callbacks
	private void addHandEvents() throws StatusException {

		// when hand is created
		handsGenerator.getHandCreateEvent().addObserver(
				new IObserver<ActiveHandEventArgs>() {
					public void update(
							IObservable<ActiveHandEventArgs> observable,
							ActiveHandEventArgs args) {
						int id = args.getId();
						Point3D pt = args.getPosition();
						float time = args.getTime();
						messenger.dispatch("hand 1 created");
						System.out
								.printf("Hand %d located at (%.0f, %.0f, %.0f), at %.0f secs\n",
										id, pt.getX(), pt.getY(), pt.getZ(),
										time);
					}
				});

		// when hand is destroyed
		handsGenerator.getHandDestroyEvent().addObserver(
				new IObserver<InactiveHandEventArgs>() {
					public void update(
							IObservable<InactiveHandEventArgs> observable,
							InactiveHandEventArgs args) {
						int id = args.getId();
						float time = args.getTime();
						System.out.printf("Hand %d destroyed at %.0f secs \n",
								id, time);
					}
				});

	}

	// create GestureGenerator callback
	private void addGestureEvents() throws StatusException {

		// when gesture is recognized
		gestureGenerator.getGestureRecognizedEvent().addObserver(
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
										gestureName, idPt.getX(), idPt.getY(),
										idPt.getZ(), endPt.getX(),
										endPt.getY(), endPt.getZ());
					}
				});

	}

	// create session callbacks
	private void addSessionEvents() throws StatusException {

		// when a focus gesture has started to be recognized
		sessionManager.getSessionFocusProgressEvent().addObserver(
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
		sessionManager.getSessionStartEvent().addObserver(
				new IObserver<PointEventArgs>() {
					public void update(IObservable<PointEventArgs> observable,
							PointEventArgs args) {
						Point3D focusPt = args.getPoint();
						System.out.printf(
								"Session started at (%.0f, %.0f, %.0f)\n",
								focusPt.getX(), focusPt.getY(), focusPt.getZ());
					}
				});

		// session end
		sessionManager.getSessionEndEvent().addObserver(
				new IObserver<NullEventArgs>() {
					public void update(IObservable<NullEventArgs> observable,
							NullEventArgs args) {
						System.out.println("Session ended");
						// isRunning = false; // causes loop in constructor to
						// end, so program exits
					}
				});

	}

	// ------------- set up detectors and callbacks --------------------------
	// for point, wave, push, swipe, circle, steady

	private PointControl initPointControl() throws GeneralException {
		PointControl pointCtrl = null;

		pointCtrl = new PointControl();

		// create new hand point
		pointCtrl.getPointCreateEvent().addObserver(
				new IObserver<HandEventArgs>() {
					public void update(IObservable<HandEventArgs> observable,
							HandEventArgs args) {
						pi = new HandPositionInfo(args.getHand());
						System.out.println(pi);
					}
				});

		// hand point has moved
		pointCtrl.getPointUpdateEvent().addObserver(
				new IObserver<HandEventArgs>() {
					public void update(IObservable<HandEventArgs> observable,
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

		return pointCtrl;
	} // end of initPointControl()

	private WaveDetector initWaveDetector() throws GeneralException {
		WaveDetector waveDetector = null;

		waveDetector = new WaveDetector();

		// some wave settings; change with set
		int flipCount = waveDetector.getFlipCount();
		int flipLen = waveDetector.getMinLength();
		System.out.println("Wave settings -- no. of flips: " + flipCount
				+ "; min length: " + flipLen + "mm");

		// callback
		waveDetector.getWaveEvent().addObserver(new IObserver<NullEventArgs>() {
			public void update(IObservable<NullEventArgs> observable,
					NullEventArgs args) {
				System.out.println("Wave detected");
				System.out.println("  " + pi); // show current hand
												// point
			}
		});

		return waveDetector;
	} // end of initWaveDetector()

	private PushDetector initPushDetector() throws GeneralException {
		PushDetector pushDetector = null;

		pushDetector = new PushDetector();

		// some push settings; change with set
		float minVel = pushDetector.getPushImmediateMinimumVelocity();
		// minimum velocity in the time span to define as push, in m/s

		float duration = pushDetector.getPushImmediateDuration();
		// time used to detect push, in ms

		float angleZ = pushDetector.getPushMaximumAngleBetweenImmediateAndZ();
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
						System.out.printf(
								"Push: velocity %.1f m/s, angle %.1f degs \n",
								args.getVelocity(), args.getAngle());
						System.out.println("  " + pi); // show current hand
														// point
					}
				});

		return pushDetector;
	} // end of initPushDetector()

	private SwipeDetector initSwipeDetector() throws GeneralException {
		SwipeDetector swipeDetector = null;

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

		return swipeDetector;
	} // end of initSwipeDetector()

	private CircleDetector initCircleDetector() throws GeneralException {
		CircleDetector circleDetector = null;

		circleDetector = new CircleDetector();

		// print some circle settings
		System.out.println("Circle setting -- min-max radius: "
				+ circleDetector.getMinRadius() + " - "
				+ circleDetector.getMaxRadius() + " mm");

		// callback: PROBLEM: getCircleEvent() is defined as private!
		/*
		 * circleDetector.getCircleEvent().addObserver( new
		 * IObserver<CircleEventArgs>() { public void
		 * update(IObservable<CircleEventArgs> observable, CircleEventArgs args)
		 * { Circle circle = args.getCircle(); Point3D center =
		 * circle.getCenter(); System.out.printf(
		 * "Circle: center (%.0f, %.0f, %.0f), radius %.0f, times %d\n",
		 * center.getX(), center.getY(), center.getZ(), circle.getRadius(),
		 * args.getTimes()); } });
		 */

		return circleDetector;
	} // end of initCircleDetector()

	private SteadyDetector initSteadyDetector() throws GeneralException {
		SteadyDetector steadyDetector = null;

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
						System.out.printf("Hand %d is steady: movement %.3f\n",
								args.getId(), args.getValue());
						System.out.println("  " + pi); // show current hand
														// point
					}
				});

		return steadyDetector;
	} // end of initSteadyDetector()

}
