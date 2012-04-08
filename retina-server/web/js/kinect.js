jQuery(function($){
	
	$.imola.open();
		
	$.imola.bind( "org.jwebsocket.plugins.system:welcome", function( aEvt, aToken ) {
		$.imola.submit(
			"org.jwebsocket.plugins.system",
			"login",
			{	username: jws.GUEST_USER_LOGINNAME,
				password: jws.GUEST_USER_PASSWORD
			}
		);
	});	
	
	$.imola.bind( "org.jwebsocket.plugins.system:response", function( aEvt, aToken ) {
		if( "login" == aToken.reqType && 0 == aToken.code ) {
			$.imola.submit(
				"com.imolatech.kinect",
				"register",
				{	stream: "userStream"
				}
			);
		} 
	});
	
	
	$.imola.bind( "com.imolatech.kinect.MESSAGE", function(event, aEvt, aToken) {
		$('#log-div').append('<p>Event Data is: ' + aEvt.data + '</p>');
		//$('#log-div').append('<p>Token Data is: ' + aToken.data + '</p>');
	});
	
	$.imola.bind( "com.imolatech.kinect.USER_IN", function(event,userId) {
		$('#log-div').append('<p>New User is coming: ' + userId + '</p>');
		
	});
	
	$.imola.bind( "com.imolatech.kinect.USER_OUT", function(event, userId) {
		$('#log-div').append('<p>User is out: ' + userId + '</p>');
		
	});
	
	$.imola.bind( "com.imolatech.kinect.SKELETONS", function(event, users) {
		
		$.each(users, function(i, user) {
			$('#log-div').append('<p>User skeletons,userId = ' + user.id + '</p>');
			$('#log-div').append('<p>Head position is  ' + user.joints.HEAD.position.X + '</p>');
		});
		
	});
	
	$('#stop-button').click(function() {
		$.imola.submit(
				"com.imolatech.kinect",
				"stop"
			);
	});
});
