function log( aString ) {
				
			}
$(document).bind("mobileinit", function(){
	
	$.jws.open();
    
	//BINDING THIS EVENT IS MORE RECOMMENDED THAN $(document).ready()
	$( "#mainPage" ).live( "pagecreate", function( aEvt ){
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
		
		$.jws.bind( "org.jwebsocket.plugins.system:response", function( aEvt, aToken ) {
			if( "login" == aToken.reqType && 0 == aToken.code ) {
				$.jws.submit(
					"org.jwebsocket.plugins.streaming",
					"register",
					{	stream: "timeStream"
					}
				);
			} 
		});
		
		$.jws.bind( "org.jwebsocket.plugins.streaming:event", function( aEvt, aToken) {
			$( "#time" )
				.text(
					aToken.year
						+ "-" + jws.tools.zerofill( aToken.month, 2 ) 
						+ "-" + jws.tools.zerofill( aToken.day, 2 )
						+ " " + jws.tools.zerofill( aToken.hours, 2 )
						+ ":" + jws.tools.zerofill( aToken.minutes, 2 ) 
						+ ":"	+ jws.tools.zerofill( aToken.seconds, 2 ))
				.css({ "text-align":"center" }
			);
		});
	});

});
