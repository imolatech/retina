package com.imolatech.kinect;

// GestureSequences.java

/* GestureSequences stores gesture sequences for each user, and detects 
 more complex gestures by looking for specified sub-sequences.

 At the moment only sequences of right hand gestures are stored here,
 and the only complex gestures looked for are vertical and horizontal waving.


 If a complex gesture sub-sequence is found, it is deleted from the user's
 sequence, including any other gestures intersperced between the
 sub-sequence gestures in the sequence.

 GestureSequences is mostly called from the Skeleton class, but SkeletonsGestures
 calls GestureSequences.addUserGest() to add an gesture to a user's sequence.
 */

import java.util.*;


public class GestureSequences {
	/*
	 * complex gesture sub-sequences that are looked for in the user's full
	 * gesture sequence. Only right hand waving is searched for at the moment.
	 */
	private final static GestureName[] HORIZ_WAVE = { GestureName.RH_OUT,
			GestureName.RH_IN, GestureName.RH_OUT, GestureName.RH_IN };
	// a horizontal wave is two out-in moves of the right hand

	private final static GestureName[] VERT_WAVE = { GestureName.RH_UP,
			GestureName.RH_DOWN, GestureName.RH_UP, GestureName.RH_DOWN };
	// a vertical wave is two up-down moves of the right hand

	private GestureWatcher watcher;
	// object that is notified of a complex gesture by calling its pose() method

	private HashMap<Integer, ArrayList<GestureName>> userGestSeqs;

	// gesture sequence for each user

	public GestureSequences(GestureWatcher gw) {
		watcher = gw;
		userGestSeqs = new HashMap<Integer, ArrayList<GestureName>>();
	} // end of GestureSequences()

	// create a new empty gestures sequence for a user
	public void addUser(int userID) {
		userGestSeqs.put(new Integer(userID), new ArrayList<GestureName>());
	}

	// remove the gesture sequence for this user
	public void removeUser(int userID) {
		userGestSeqs.remove(userID);
	}

	// called from SkeletonsGestures: add an gesture to the end of the user's
	// sequence
	public void addUserGest(int userID, GestureName gest)	{
		ArrayList<GestureName> gestsSeq = userGestSeqs.get(userID);
		if (gestsSeq == null)
			System.out.println("No gestures sequence for user " + userID);
		else
			gestsSeq.add(gest);
	} // end of addUserGest()

	public void checkSeqs(int userID)
	/*
	 * look for gesture sub-sequences in the user's full gesture sequence, and
	 * notify the watcher
	 */
	{
		ArrayList<GestureName> gestsSeq = userGestSeqs.get(userID);
		if (gestsSeq != null)
			checkSeq(userID, gestsSeq);
	} // end of checkSeqs()

	/*
	 * look for gesture sub-sequences. If one is found, then the part of the
	 * user's gesture sequence containing the sub-sequence is deleted.
	 */
	private void checkSeq(int userID, ArrayList<GestureName> gestsSeq)	{
		int endPos = findSubSeq(gestsSeq, HORIZ_WAVE); // look for a horizontal
														// wave
		if (endPos != -1) { // found it
			// printSeq(gestsSeq);
			watcher.pose(userID, GestureName.HORIZ_WAVE, true);
			purgeSeq(gestsSeq, endPos);
			// printSeq(gestsSeq);
		}

		endPos = findSubSeq(gestsSeq, VERT_WAVE); // look for a vertical wave
		if (endPos != -1) { // found it
			// printSeq(gestsSeq);
			watcher.pose(userID, GestureName.VERT_WAVE, true);
			purgeSeq(gestsSeq, endPos);
			// printSeq(gestsSeq);
		}
	} // end of checkSeq()

	/*
	 * Try to find all the gests[] array GestureName objects inside the list,
	 * and return the position *after* the last object, or -1. The array
	 * elements do not have to be stored contigiously in the list.
	 */
	private int findSubSeq(ArrayList<GestureName> gestsSeq, GestureName[] gests) {
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
	} // end of findSubSeq()

	/*
	 * remove all the elements in the seq between the positions 0 and pos-1
	 */
	private void purgeSeq(ArrayList<GestureName> gestsSeq, int pos) {
		for (int i = 0; i < pos; i++) {
			if (gestsSeq.isEmpty())
				return;
			gestsSeq.remove(0);
		}
	} // end of purgeSeq()

	private void printSeq(ArrayList<GestureName> gestsSeq) {
		if (gestsSeq.isEmpty())
			System.out.println("Sequence is empty");
		else {
			System.out.print("Sequence: ");
			for (GestureName gest : gestsSeq)
				System.out.print(gest + " ");
			System.out.println();
		}
	} // end of printSeq()

} // end of GestureSequences class

