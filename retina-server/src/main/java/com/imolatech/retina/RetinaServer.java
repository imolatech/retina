/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imolatech.retina;

import com.imolatech.retina.kinect.KinectListener;

/**
 * The main application to start a websocket server.
 * 
 * @author Wenhu
 */
public class RetinaServer {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        WebSocketServer server = new WebSocketServer();
        server.start();
        KinectListener listener = new KinectListener();
        if (listener.start()) {
        	listener.listen();
        }
        for (int i=0; i<10; i++) {
            sleep();
            server.pushMessage(String.valueOf(i));
        }
    }
    
    private static void sleep() {
        try {
            Thread.sleep(10000);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }   
   
}
