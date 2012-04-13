package com.imolatech.kinect.serializer;

import org.junit.Test;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import com.google.gson.Gson;
import com.imolatech.kinect.message.UserInMessage;
import com.imolatech.kinect.serializer.NewUserSerializer;

public class NewUserSerializerTest {
	
	@Test
	public void userInMessageToJson() {
		UserInMessage message = new UserInMessage(1);
		Gson gson = new Gson();
		String expected = "{\"userId\":1,\"type\":\"USER_IN\",\"ns\":\"com.imolatech.kinect\"}";
		assertThat(gson.toJson(message), is(equalTo(expected)));
	}
	
	@Test
	public void toJson() {
		NewUserSerializer serializer = new NewUserSerializer(1);
		String json = serializer.toJson();
		assertThat(json.indexOf("{\"userId\":1"), is(greaterThanOrEqualTo(0)));
		assertThat(json.indexOf("\"ns\":\"com.imolatech.kinect\""), is(greaterThanOrEqualTo(0)));
		assertThat(json.indexOf("\"timestamp\":"), is(greaterThanOrEqualTo(0)));
	}
}
