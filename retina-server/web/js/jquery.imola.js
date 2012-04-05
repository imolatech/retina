/* ImolaTech kinect js library. Adapted from jwebsocket example
 * Copyright (c) 2011-2012
 * Version: 1.0 
 * Author : Wenhu Guan
 * Requires: jQuery v1.3.2 or later, jWebSocket.js
 */
(function($){
	$.imola = $({});
    
	$.imola.open = function( jwsServerURL, aTokenClient, timeout){
		if(jws.browserSupportsWebSockets()){
			var url = jwsServerURL || jws.getDefaultServerURL();
            
			if(aTokenClient)
				$.imola.aTokenClient = aTokenClient;
			else
				$.imola.aTokenClient = new jws.jWebSocketJSONClient();
            
			$.imola.aTokenClient.open(url, {
				OnOpen: function(aToken){
					$.imola.trigger('open', aToken);
					$.imola.aTokenClient.addPlugIn($.imola);
				},
				OnMessage: function( aEvent, aToken ) {
					$.imola.trigger('message',[aEvent, aToken]);
				},
				OnClose: function(){
					$.imola.trigger('close');
				},
				OnTimeout: function(){
					$.imola.trigger('timeout');
				}
			});
			if(timeout)
				this.setDefaultTimeOut(timeout);
		}
		else{
			var lMsg = jws.MSG_WS_NOT_SUPPORTED;
			alert(lMsg);
		}
	};
        
	
	$.imola.submit = function(ns, type, args, callbacks, options){
		var lToken = {};
		if (args){
			lToken = args;
		}
		lToken.ns   = ns;
		lToken.type = type;
                        
		var lTimeout;
                        
		if(options)
			if(options.timeout)
				lTimeout = options.timeout;
                        
		this.aTokenClient.sendToken( lToken, {
			timeout: lTimeout,
			callbacks: callbacks,
			OnResponse: function( aToken ) {
				if( callbacks != undefined ) { 
					if (aToken .code == -1
						&& callbacks.failure)
						return callbacks.failure(aToken );
					else if (aToken .code == 0
						&& callbacks.success )
						return callbacks.success(aToken );
				}	
			},
			OnTimeOut: function(){
				if( callbacks != undefined
					&& callbacks.timeout) { 
					return callbacks.timeout();
				}
			}
		});
	};
        
	$.imola.processToken = function(aToken){
		$.imola.trigger('all:all', aToken);
		$.imola.trigger('all:' + aToken.type, aToken);
		$.imola.trigger(aToken.ns + ':all', aToken);
		$.imola.trigger(aToken.ns + ':' + aToken.type, aToken);
	};
        
	$.imola.getDefaultServerURL = function(){
		if(this.aTokenClient)
			return this.aTokenClient.getDefaultServerURL();
		else
			return jws.getDefaultServerURL();
	};
        
	$.imola.setDefaultTimeOut = function(timeout){
		if(this.aTokenClient)
			this.aTokenClient.DEF_RESP_TIMEOUT = timeout;
		else
			jws.DEF_RESP_TIMEOUT = timeout;
	};
        
	$.imola.close = function(){
		this.aTokenClient.close();
	};
})(jQuery);
