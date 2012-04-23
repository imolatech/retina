package com.imolatech.kinect;

public enum GestureName {
   HORIZ_WAVE, VERT_WAVE,                          // waving
   HANDS_NEAR,                                     // two hands
   LEAN_LEFT, LEAN_RIGHT, LEAN_FWD, LEAN_BACK,     // leaning
   TURN_RIGHT, TURN_LEFT,                          // turning
   LH_LHIP, RH_RHIP,                               // touching
   RH_UP, RH_FWD, RH_OUT, RH_IN, RH_DOWN,          // righ hand position
   LH_UP, LH_FWD, LH_OUT, LH_IN, LH_DOWN,             // left hand position
   //the above is not used any more
   //the following is the postures we supported
   HANDS_JOINED, LEFT_HAND_OVER_HEAD, RIGHT_HAND_OVER_HEAD,
   LEFT_HELLO, RIGHT_HELLO
}