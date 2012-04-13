/*
 * Init a WebSocketServer and also response for initialize a kinect module.
 */
package com.imolatech.kinect;

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
    private KinectEngine engine;
    
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
    	logger.debug("process token.id={};nodeId={}", wsste.getConnector().getId(), wsste.getConnector().getNodeId());
    	logger.debug("got token {}", token.toString());
        if (token != null && "com.imolatech.kinect".equals(token.getNS())) {
        	if ("register".equals(token.getType())) {
        		//token.getType is token.getString("type")
        		logger.debug("starting kinect: {}", token.getString("stream"));
        		startKinectEngine();
        	} else if ("stop".equals(token.getType())) {
        		//token.getType is token.getString("type")
        		logger.debug("stop kinect.");
        		stopKinectEgnine();
        	}
        }
    }

    private void startKinectEngine() {
    	if (engine == null) {
    		engine = new KinectEngine(this);
    	}
    	if (engine.isRunning()) return;
        if (engine.start()) {
        	engine.listen();
        }
	}

    private void stopKinectEgnine() {
    	if (engine != null) {
    		engine.stop();
    	}
	}
    
	@Override
    public void processOpened(WebSocketServerEvent wsse) {
		//nodeId is always null,connector.getId is something like 01.51325.1
		//we could use connector.id to identify a client and cache it so that we 
		//might stop the websocket server if it is closed (not needed right now).
    	logger.debug("process opened.id={};nodeId={}", wsse.getConnector().getId(), wsse.getConnector().getNodeId());
    }

    @Override
    public void processPacket(WebSocketServerEvent wsse, WebSocketPacket wsp) {
    	logger.debug("process packet.id={};nodeId={}", wsse.getConnector().getId(), wsse.getConnector().getNodeId());
    
    	logger.debug("process packet: {}.", wsp.getString());
    }

    @Override
    public void processClosed(WebSocketServerEvent wsse) {
    	logger.debug("process closed.id={};nodeId={}", wsse.getConnector().getId(), wsse.getConnector().getNodeId());
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
