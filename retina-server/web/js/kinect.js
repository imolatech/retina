jQuery(function($){
	
	$.jws.open();
    
	$( "#time" ).text( "yyyy-mm-dd hh:mm:ss" ).css({
			"text-align":"center"
		});
		
		$.jws.bind( "org.jwebsocket.plugins.system:welcome", function( aEvt, aToken ) {
			$.jws.submit(
				"org.jwebsocket.plugins.system",
				"login",
				{	username: jws.GUEST_USER_LOGINNAME,
					password: jws.GUEST_USER_PASSWORD
				}
			);
		});	
		
		$.jws.bind( "message", function( aEvt, aToken ) {
    			alert(aEvt.data + "\n" + aToken.data);
    			
    	});

});
