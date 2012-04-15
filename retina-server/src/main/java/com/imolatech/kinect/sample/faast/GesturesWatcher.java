package com.imolatech.kinect.sample.faast;


// GesturesWatcher.java

/* used to ensure that a watcher class can be contacted when
   a skeleton gesture starts (or stops)
*/

public interface GesturesWatcher 
{
  void pose(int userID, GestureName gest, boolean isActivated);
}
