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
	
	$.jws.bind( "org.jwebsocket.plugins.system:response", function( aEvt, aToken ) {
		if( "login" == aToken.reqType && 0 == aToken.code ) {
			$.jws.submit(
				"com.imolatech.kinect",
				"register",
				{	stream: "userStream"
				}
			);
		} 
	});
	$.jws.bind( "message", function( aEvt, aToken ) {
		//alert(aEvt.data + "\n" + aToken.data);
		$('#log-div').append('<p>TokenNS-' + aToken.ns + ';TokenType-'+ aToken.type + 
		  ';TokenName-' + aToken.name  + '</p>');
		$('#log-div').append('<p>' + aToken.data + '</p>');	
	});
	$('#stop-button').click(function() {
		$.jws.submit(
				"com.imolatech.kinect",
				"stop"
			);
	});
});
