package com.imolatech.retina.kinect;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.util.HashMap;

import org.OpenNI.Point3D;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;
import org.junit.Test;

import com.google.gson.Gson;

public class UserTest {

	@Test
	public void verifyJsonFormat() {
		User user = buildUser();
		Gson gson = new Gson();
		String expected = "{\"id\":100,\"centerOfMass\":\"centerOfMass\",\"active\":true,\"joints\":{\"HEAD\":{\"position\":{\"X\":10.0,\"Y\":10.0,\"Z\":10.0},\"confidence\":0.8}}}";
		assertThat(gson.toJson(user), is(equalTo(expected)));
	}

	private User buildUser() {
		User user = new User();
		user.setActive(true);
		user.setCenterOfMass("centerOfMass");
		user.setId(100);
		
		HashMap<SkeletonJoint, SkeletonJointPosition> joints = 
				new HashMap<SkeletonJoint, SkeletonJointPosition>();
		Point3D p = new Point3D(10.0f, 10.0f, 10.0f);
		SkeletonJointPosition position = new SkeletonJointPosition(p, 0.8f); 
		joints.put(SkeletonJoint.HEAD, position);
		user.setJoints(joints);
		return user;
	}
}
