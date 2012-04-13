package com.imolatech.kinect.sample;

/* Track Kinect users by displaying the coloured outline
 of their bodies, skeleton limbs, and a rotatable
 "Gorilla" head on each one. (The head can rotate around the 
 z-axis.)

 Based on UserTrackerApplication.java
 from the Java OpenNI UserTracker sample

 Usage:
 > java GorillasTracker
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GorillasTracker extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private TrackerPanel trackPanel;

	public GorillasTracker() {
		super("Gorillas Tracker");

		Container c = getContentPane();
		c.setLayout(new BorderLayout());

		trackPanel = new TrackerPanel();
		c.add(trackPanel, BorderLayout.CENTER);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				trackPanel.closeDown();
			}
		});

		pack();
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	} // end of GorillasTracker()

	// -------------------------------------------------------

	public static void main(String args[]) {
		new GorillasTracker();
	}

} // end of GorillasTracker class
