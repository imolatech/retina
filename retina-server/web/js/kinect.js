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
	$.imola.bind( "message", function( aEvt, aToken ) {
		var kinectData = $.parseJSON(aToken.data);
		if ("com.imolatech.kinect" === kinectData.ns) {
			if ("USER_IN" === kinectData.type) {
				$('#log-div').append('<p>New user in,userId = ' + kinectData.userId + '</p>');
			} else if ("USER_OUT" === kinectData.type) {
				$('#log-div').append('<p>New user out,userId = ' + kinectData.userId + '</p>');
			} else if ("TRACKED_USERS" === kinectData.type) {
				$.each(kinectData.users, function(i, user) {
					$('#log-div').append('<p>User skeletons,userId = ' + user.id + '</p>');
					$('#log-div').append('<p>Head position is  ' + user.joints.HEAD.position.X + '</p>');
				});
			}
		}
		
	});
	$('#stop-button').click(function() {
		$.imola.submit(
				"com.imolatech.kinect",
				"stop"
			);
	});
});
