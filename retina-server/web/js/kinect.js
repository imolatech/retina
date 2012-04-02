jQuery(function($){
	
	$.jws.open();
		
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
		//alert(aEvt.data + "\n" + aToken.data);
		$('#logDiv').append('<p>TokenNS-' + aToken.ns + ';TokenType-'+ aToken.type + 
		  ';TokenName-' + aToken.name  + '</p>');
		$('#logDiv').append('<p>' + aToken.data + '</p>');	
	});

});
