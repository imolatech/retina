/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imolatech.kinect.engine;

/**
 * The main application to start a websocket server and 
 * listen on kinect sensor data.
 * Note: the kinect must be connected before this app started.
 * Future enhance: 
 * 1. Detect the connectivity of Kinect Sensor
 * 2. A timer will simulate kinect events if passing a parameter or
 *    by getting a flag from service.property file so that we could
 *    test our client without even connecting a connect sensor.
 * 
 * @author Wenhu
 */
public class MainApplication {
    public static void main(String[] args) {
        WebSocketServer server = new WebSocketServer();
        server.start();
    }
}
