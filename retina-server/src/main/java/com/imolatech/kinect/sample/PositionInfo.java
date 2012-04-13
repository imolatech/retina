package com.imolatech.kinect.sample;

/*  Store hand point information obtained from a NITE PointControl.
*/

import org.OpenNI.*;
import com.primesense.NITE.*;


public class PositionInfo
{
  private int id;         // of the hand
  private Point3D pos;    // in real-world coords (mm)
  private float time;     // in secs
  // private float confidence;  // don't bother storing this


  public PositionInfo(HandPointContext hpc)
  {
    id = hpc.getID();
    pos = hpc.getPosition();
    time = hpc.getTime();
  }  // end of PositionInfo()


  public synchronized void update(HandPointContext hpc)
  {
    if (id == hpc.getID()) {
      pos = hpc.getPosition();
      time = hpc.getTime();
    }
  }  // end of update()


  public int getID()     // no need to synchronize since doesn't change
  {  return id;  }


  public synchronized Point3D getPosition()
  {  return pos;  }

  public synchronized float getTime()
  {  return time;  }


  public synchronized String toString()
  {
    return String.format("Hand Point %d at (%.0f, %.0f, %.0f) at %.0f secs", 
                  id, pos.getX(), pos.getY(), pos.getZ(), time);
  }  // end of toString()

}  // end of PositionInfo class
