package com.imolatech.kinect.posture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.imolatech.kinect.GestureName;
import com.imolatech.kinect.GestureWatcher;
import com.imolatech.kinect.Skeleton;

/**
 * detect horizontal wave
 * @author Wenhu
 *
 */
public class SequenceGesturesDetectionStrategy extends BasePostureDetectionStrategy {
	private static final Logger logger = LoggerFactory
			.getLogger(SequenceGesturesDetectionStrategy.class);
	/*
	 * complex gesture sub-sequences that are looked for in the user's full
	 * gesture sequence. Only right hand waving is searched for at the moment.
	 * a horizontal wave is two out-in moves of the right hand
	 */
	private final static GestureName[] HORIZ_WAVE = { GestureName.RH_OUT,
			GestureName.RH_IN, GestureName.RH_OUT, GestureName.RH_IN };
	private Map<Integer, List<GestureName>> userGestureSeqences;
	
	public SequenceGesturesDetectionStrategy(GestureWatcher watcher) {
		super(watcher);
		userGestureSeqences = new HashMap<Integer, List<GestureName>>();
	}
	
	// create a new empty gestures sequence for a user
	public void addUser(int userId) {
		userGestureSeqences.put(new Integer(userId), new ArrayList<GestureName>());
	}

	// remove the gesture sequence for this user
	public void removeUser(int userId) {
		userGestureSeqences.remove(userId);
	}

	@Override
	public GestureName getGestureName() {
		return GestureName.HORIZ_WAVE;
	}
	
	public void addUserGesture(int userId, GestureName gest)	{
		List<GestureName> gestureNames = userGestureSeqences.get(userId);
		if (gestureNames == null) {
			logger.debug("No gestures sequence for user {}", userId);
		} else {
			if (gestureNames.size() > 40) {
				gestureNames.clear();
			}
			gestureNames.add(gest);
		}
	}
	
	/*
	 * look for gesture sub-sequences in the user's full gesture sequence, and
	 * notify the watcher
	 */
	public void detect(Skeleton skeleton) {
		if (skeleton == null) return; 
		int userId = skeleton.getUserId();
		List<GestureName> gestureNames = userGestureSeqences.get(userId);
		if (gestureNames != null) {
			checkSequence(userId, gestureNames);
		}
	} 

	/*
	 * look for gesture sub-sequences. If one is found, then the part of the
	 * user's gesture sequence containing the sub-sequence is deleted.
	 */
	private void checkSequence(int userId, List<GestureName> gestureNames)	{
		int endPos = findSubSequence(gestureNames, HORIZ_WAVE); // look for a horizontal
														// wave
		if (endPos != -1) { // found it
			// printSeq(gestsSeq);
			watcher.pose(userId, GestureName.HORIZ_WAVE, true);
			purgeSequence(gestureNames, endPos);
		}
	} 

	/*
	 * Try to find all the gests[] array GestureName objects inside the list,
	 * and return the position *after* the last object, or -1. The array
	 * elements do not have to be stored contigiously in the list.
	 */
	private int findSubSequence(List<GestureName> gestsSeq, GestureName[] gests) {
		int pos = 0;
		for (GestureName gest : gests) { // iterate through the array
			while (pos < gestsSeq.size()) { // find the gesture in the list
				if (gest == gestsSeq.get(pos))
					break;
				pos++;
			}
			if (pos == gestsSeq.size())
				return -1;
			else
				pos++; // carry on, starting with next gesture in list
		}
		return pos;
	} 

	/*
	 * remove all the elements in the seq between the positions 0 and pos-1
	 */
	private void purgeSequence(List<GestureName> gestsSeq, int pos) {
		for (int i = 0; i < pos; i++) {
			if (gestsSeq.isEmpty())
				return;
			gestsSeq.remove(0);
		}
	} 
}
