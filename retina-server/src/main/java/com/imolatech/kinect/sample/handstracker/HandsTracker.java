package com.imolatech.kinect.sample.handstracker;

/* Track multiple hands, displaying a disappearing trail of 
   colour behind each hand.

  For multiple hand detection, the Nite.ini file must be edited
  in C:\Program Files\PrimeSense\NITE\Hands_1_4_0\Data (or whatever
  is the latest version of the Hands directory). Remove the ";"s
  from the start of the two property assignment lines:

            [HandTrackerManager]
            AllowMultipleHands=1  
            TrackAdditionalHands=1
          ^^

  With multiple hand tracking, there is no focus gesture 
  for your second hand.

  Once you have gained focus for your first hand, tracking is started
  on your other hand by bringing it near to the first hand, then
  spreading both hands apart a little.

   Usage:
      > java HandsTracker
*/

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;


public class HandsTracker extends JFrame 
{
  private TrackerPanel trackerPanel;


  public HandsTracker()
  {
    super("Hands Tracker");

    Container c = getContentPane();
    c.setLayout( new BorderLayout() );   

    trackerPanel = new TrackerPanel(this); 
                       // the camera image and trails appear here
    c.add( trackerPanel, BorderLayout.CENTER);

    addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent e)
      { trackerPanel.closeDown();  }  // stop rendering
    });

    pack();  
    setResizable(false);
    setLocationRelativeTo(null);
    setVisible(true);
  } // end of HandsTracker()


  // -------------------------------------------------------

  public static void main( String args[] )
  {  new HandsTracker();  }

} // end of HandsTracker class
