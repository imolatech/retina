package com.imolatech.kinect;



// GesturesWatcher.java

/* used to ensure that a watcher class can be contacted when
   a skeleton gesture starts (or stops)
*/

public interface GestureWatcher {
  void pose(int userID, GestureName gest, boolean isActivated);
}
