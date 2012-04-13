package com.imolatech.kinect.sample;

/* Different OpenNI viewers are implemented in different 
 versions of the ViewerPanel class.

 Usage:
 > java OpenNIViewer
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class OpenNIViewer extends JFrame {
	private static final long serialVersionUID = 1L;
	private ViewerPanelV3 viewerPanel;

	public OpenNIViewer() {
		super("OpenNI Viewer Example");

		Container c = getContentPane();
		c.setLayout(new BorderLayout());

		viewerPanel = new ViewerPanelV3();
		c.add(viewerPanel, BorderLayout.CENTER);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				viewerPanel.closeDown(); // stop showing images
			}
		});

		pack();
		setResizable(false);
		setLocationRelativeTo(null);
		setVisible(true);
	} // end of OpenNIViewer()

	// -------------------------------------------------------

	public static void main(String args[]) {
		new OpenNIViewer();
	}

} // end of OpenNIViewer class
