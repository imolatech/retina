/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imolatech.retina;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.kit.WebSocketServerEvent;
import org.jwebsocket.listener.WebSocketServerTokenEvent;
import org.jwebsocket.listener.WebSocketServerTokenListener;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;

/**
 *
 * @author Wenhu
 */
public class JWebSocketClient implements WebSocketServerTokenListener {

    private TokenServer tokenServer;
    
    public TokenServer getTokenServer() {
        return tokenServer;
    }
    
    public void init() {
        try {
            JWebSocketFactory.start();
            tokenServer = (TokenServer)JWebSocketFactory.getServer("ts0");
            if (tokenServer != null) {
                tokenServer.addListener(this);
            } else {
                System.out.println("Cannot find token server");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void processToken(WebSocketServerTokenEvent wsste, Token token) {
        System.out.println("got token " + token.toString());
    }

    @Override
    public void processOpened(WebSocketServerEvent wsse) {
    	System.out.println("process opened.");
    }

    @Override
    public void processPacket(WebSocketServerEvent wsse, WebSocketPacket wsp) {
    	System.out.println("process packet.");
    }

    @Override
    public void processClosed(WebSocketServerEvent wsse) {
    	System.out.println("process closed.");
    }
    
    public void pushMessage(String message) {
    	Map connectorMap = getTokenServer().getAllConnectors();
    	Collection<WebSocketConnector> connectors = connectorMap.values();
    	
    	for (WebSocketConnector connector : connectors) {
    		Map<String, Object> item = new HashMap<String, Object>();
    		item.put("port", connector.getRemotePort());
    		item.put("nodeid", connector.getNodeId());
    		item.put("username", connector.getUsername());
    		item.put("isToken", connector.getBoolean(TokenServer.VAR_IS_TOKENSERVER));
    		item.put("message", message);
    		sendPacket(connector, item);
    	}
    }
    
    public void sendPacket(WebSocketConnector connector, Map<String, Object> item) {
    	String json = convertToJson(item);
    	WebSocketPacket wsPacket = new RawPacket(json);
    	getTokenServer().sendPacket(connector, wsPacket);
    }

	private String convertToJson(Map<String, Object> item) {
		String json = "{\"nodeid\":\"" + item.get("nodeid") + 
				"\",\"username\":\"" + item.get("username") +
				"\",\"message\":\"" + item.get("message") +
				"\"}";
		System.out.println("Send message as following:" + json);
		return json;
	}
}
