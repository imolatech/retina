jQuery(function($){
	var App = {};
	App.init = function() {
		App.initCanvas("canvas");
		App.initEventBindings();
		$.imola.open();
	}
	App.initCanvas = function(canvasId) {
		App.canvas = document.getElementById(canvasId);
    	App.context = canvas.getContext("2d");
	};
	
	App.drawSkeletons = function(context, canvas, users) {
		context.clearRect(0, 0, canvas.width, canvas.height);
        context.fillStyle = "#FF0000";
        context.beginPath();
		
		//$.each(users, function(i, user) {
		//	$('#log-div').append('<p>User skeletons,userId = ' + user.id + '</p>');
		//});
		//ToDO problem here
		$.each(users, function(i, user) {
			//$('#log-div').append('<p>User skeletons,userId = ' + user.id + '</p>');
			$.each(user.joints, function(j, joint) {
				//$('#log-div').append('<p>position is x=' + joint.position.X + ';y=' + joint.position.Y + '</p>');
				 // Draw!!!
                context.arc(parseFloat(joint.position.X), parseFloat(joint.position.Y), 10, 0, Math.PI * 2, true);
			});
		});
        
        context.closePath();
        context.fill();
	};
	
	App.initEventBindings = function() {
		
			
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
			App.drawSkeletons(App.context, App.canvas, users);
		});
		
		$.imola.bind( "com.imolatech.kinect.GESTURES", function(event, gestures) {
			$.each(gestures, function(i, gesture) {
				$('#log-div').append('<p>User gestures,userId = ' + gesture.userId + '</p>');
				$.each(gesture.gestures, function(j, gestureName) {
					$('#log-div').append('<p>gesture is ' + gestureName + '</p>');
				});
		});
        
		});
		
		$('#stop-button').click(function() {
			$.imola.submit(
					"com.imolatech.kinect",
					"stop"
				);
		});
	};
	
	
	App.init();
});
