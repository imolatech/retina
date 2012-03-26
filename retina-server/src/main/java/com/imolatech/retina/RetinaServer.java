/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imolatech.retina;

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
        WebSocketServer client = new WebSocketServer();
        client.init();
        for (int i=0; i<10; i++) {
            sleep();
            client.pushMessage(String.valueOf(i));
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
