/*
 * Init a WebSocketServer and also response for initialize a kinect module.
 */
package com.imolatech.retina;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.factory.JWebSocketFactory;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.kit.WebSocketServerEvent;
import org.jwebsocket.listener.WebSocketServerTokenEvent;
import org.jwebsocket.listener.WebSocketServerTokenListener;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Wenhu
 */
public class WebSocketServer implements WebSocketServerTokenListener, Messenger {
	private static final Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
    private TokenServer tokenServer;
    
    public TokenServer getTokenServer() {
        return tokenServer;
    }
    
    public void start() {
        try {
        	logger.debug("Start websocket server...");
            JWebSocketFactory.start();
            tokenServer = (TokenServer)JWebSocketFactory.getServer("ts0");
            if (tokenServer != null) {
                tokenServer.addListener(this);
            } else {
                logger.error("Cannot find token server");
            }
        } catch(Exception e) {
            logger.error("Error when starting websocket server.", e);
        }
    }
    @Override
    public void processToken(WebSocketServerTokenEvent wsste, Token token) {
        logger.debug("got token {}", token.toString());
    }

    @Override
    public void processOpened(WebSocketServerEvent wsse) {
    	logger.debug("process opened.");
    }

    @Override
    public void processPacket(WebSocketServerEvent wsse, WebSocketPacket wsp) {
    	logger.debug("process packet.");
    }

    @Override
    public void processClosed(WebSocketServerEvent wsse) {
    	logger.debug("process closed.");
    }
    
    //suppose message coming in is in json format
    public void send(String message) {
    	if (StringUtils.isBlank(message)) return;
    	Map<String, WebSocketConnector> connectorMap = getTokenServer().getAllConnectors();
    	Collection<WebSocketConnector> connectors = connectorMap.values();
    	
    	for (WebSocketConnector connector : connectors) {
    		WebSocketPacket wsPacket = new RawPacket(message);
        	getTokenServer().sendPacket(connector, wsPacket);
    	}
    }
    
    public void pushMessage(String message) {
    	Map<String, WebSocketConnector>  connectorMap = getTokenServer().getAllConnectors();
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
		logger.debug("Send message as following:" + json);
		return json;
	}
}
