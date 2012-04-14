package com.imolatech.kinect.sample.handstracker;


// HandTrail.java

/* Store hand coordinates in a list (up to a maximum of MAX_POINTS)
   and draw them as a trail (actually a polyline).

   When MAX_POINTS is reached, the oldest point (at position 0) is
   discarded, causing the rendering to show a trail of previous hand
   points.
*/


import java.awt.*;
import java.io.*;
import java.util.*;

import org.OpenNI.*;


public class HandTrail
{
  private static final int MAX_POINTS = 30;  

  private static final int CIRCLE_SIZE = 25;  
  private static final int STROKE_SIZE = 10;  

  private static final Color POINT_COLORS[] = {
    Color.RED, Color.BLUE, Color.CYAN, Color.GREEN,
    Color.MAGENTA, Color.PINK, Color.YELLOW };

  private Font msgFont;    // for the hand ID string

  private int handID;
  private DepthGenerator depthGen;
  private ArrayList<Point> coords;     // the points that form the trail


  public HandTrail(int id, DepthGenerator dg)
  {
    handID = id;
    depthGen = dg;
    msgFont = new Font("SansSerif", Font.BOLD, 24);

    coords = new ArrayList<Point>();
  }  // end of HandTrail()



  public synchronized void addPoint(Point3D realPt)
  // add hand coordinate to the coords list
  {
    try {
      Point3D pt = depthGen.convertRealWorldToProjective(realPt);
            // convert real-world coordinates to screen form
      if (pt == null)
        return;
      coords.add( new Point((int)pt.getX(), (int)pt.getY()));   // discard z coord
      if (coords.size() > MAX_POINTS) // get rid of the oldest point
        coords.remove(0);
    }
    catch (StatusException e) 
    {  System.out.println("Problem converting point"); }
  }  // end of addPoint()



  public synchronized void draw(Graphics2D g2)
  /* draw trail and large circle on hand (the last point) with the 
     hand ID in the center */
  {
    int numPoints = coords.size();
    if (numPoints == 0)
      return;

    drawTrail(g2, coords, numPoints);

    // draw large circle on hand (the last point)
    Point pt = coords.get(numPoints-1);
    g2.setColor(POINT_COLORS[(handID+1) % POINT_COLORS.length]);
    g2.fillOval(pt.x-CIRCLE_SIZE/2, pt.y-CIRCLE_SIZE/2, CIRCLE_SIZE, CIRCLE_SIZE);
    
    // draw the hand ID
    g2.setColor(Color.WHITE);
    g2.setFont(msgFont);
    g2.drawString("" + handID, pt.x-6, pt.y+6);   // roughly centered
  }  // end of draw()



  private void drawTrail(Graphics2D g2, ArrayList<Point> coords, int numPoints) 
  // draw (x,y) Points list as a polyline (trail)
  {
    int[] xCoords = new int[numPoints];
    int[] yCoords = new int[numPoints];

    // fill the integer arrays for x and y points
    Point pt;
    for (int i=0; i < numPoints; i++) {
      pt = coords.get(i);
      xCoords[i] = pt.x;
      yCoords[i] = pt.y;
    }

    g2.setColor(POINT_COLORS[handID % POINT_COLORS.length]);
    g2.setStroke(new BasicStroke(STROKE_SIZE));

    // System.out.println("No of draw points: " + numPoints);
    g2.drawPolyline(xCoords, yCoords, numPoints);
  }  // end of drawTrail()


}  // end of HandTrail class