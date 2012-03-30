/* jWebSocket Plugin for jQuery
 * Examples and documentation at: http://jwebsocket.org/wiki/Projects/jQueryPlugin
 * Copyright (c) 2011-2012 M. Alsup
 * Version: 1.0 (June-12-2011)
 * Dual licensed under the MIT and GPL licenses.
 * http://jquery.malsup.com/license.html
 * Requires: jQuery v1.3.2 or later, jWebSocket.js
 */
(function($){
	$.jws = $({});
    
	$.jws.open = function( jwsServerURL, aTokenClient, timeout){
		if(jws.browserSupportsWebSockets()){
			var url = jwsServerURL || jws.getDefaultServerURL();
            
			if(aTokenClient)
				$.jws.aTokenClient = aTokenClient;
			else
				$.jws.aTokenClient = new jws.jWebSocketJSONClient();
            
			$.jws.aTokenClient.open(url, {
				OnOpen: function(aToken){
					$.jws.trigger('open', aToken);
					$.jws.aTokenClient.addPlugIn($.jws);
				},
				OnMessage: function( aEvent, aToken ) {
					$.jws.trigger('message',[aEvent, aToken]);
				},
				OnClose: function(){
					$.jws.trigger('close');
				},
				OnTimeout: function(){
					$.jws.trigger('timeout');
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
        
	
	$.jws.submit = function(ns, type, args, callbacks, options){
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
        
	$.jws.processToken = function(aToken){
		$.jws.trigger('all:all', aToken);
		$.jws.trigger('all:' + aToken.type, aToken);
		$.jws.trigger(aToken.ns + ':all', aToken);
		$.jws.trigger(aToken.ns + ':' + aToken.type, aToken);
	};
        
	$.jws.getDefaultServerURL = function(){
		if(this.aTokenClient)
			return this.aTokenClient.getDefaultServerURL();
		else
			return jws.getDefaultServerURL();
	};
        
	$.jws.setDefaultTimeOut = function(timeout){
		if(this.aTokenClient)
			this.aTokenClient.DEF_RESP_TIMEOUT = timeout;
		else
			jws.DEF_RESP_TIMEOUT = timeout;
	};
        
	$.jws.close = function(){
		this.aTokenClient.close();
	};
})(jQuery);
