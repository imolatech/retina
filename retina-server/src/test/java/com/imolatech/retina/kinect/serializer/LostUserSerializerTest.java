package com.imolatech.retina.kinect.serializer;

import org.junit.Test;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import com.google.gson.Gson;
import com.imolatech.retina.kinect.message.UserOutMessage;

public class LostUserSerializerTest {
	@Test
	public void userOutMessageToJson() {
		UserOutMessage message = new UserOutMessage(1);
		Gson gson = new Gson();
		String expected = "{\"userId\":1,\"type\":\"USER_OUT\",\"ns\":\"imolatech.kinect\"}";
		assertThat(gson.toJson(message), is(equalTo(expected)));
	}
	
	@Test
	public void toJson() {
		LostUserSerializer serializer = new LostUserSerializer(1);
		String json = serializer.toJson();
		assertThat(json.indexOf("{\"userId\":1"), is(greaterThanOrEqualTo(0)));
		assertThat(json.indexOf("\"ns\":\"imolatech.kinect\""), is(greaterThanOrEqualTo(0)));
		assertThat(json.indexOf("\"timestamp\":"), is(greaterThanOrEqualTo(0)));
	}
}
