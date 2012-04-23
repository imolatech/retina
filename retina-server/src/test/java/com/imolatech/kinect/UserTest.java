package com.imolatech.kinect;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.OpenNI.Point3D;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;
import org.junit.Test;

import com.google.gson.Gson;
import com.imolatech.kinect.message.Joint;
import com.imolatech.kinect.message.UserSkeleton;

public class UserTest {

	@Test
	public void verifyJsonFormat() {
		UserSkeleton user = buildUser();
		Gson gson = new Gson();
		String expected = "{\"id\":100,\"centerOfMass\":\"centerOfMass\",\"active\":true,\"joints\":[{\"name\":\"HEAD\",\"position\":{\"X\":10.0,\"Y\":10.0,\"Z\":10.0},\"confidence\":0.8}]}";
		assertThat(gson.toJson(user), is(equalTo(expected)));
	}

	private UserSkeleton buildUser() {
		UserSkeleton user = new UserSkeleton();
		user.setActive(true);
		user.setCenterOfMass("centerOfMass");
		user.setId(100);
		List<Joint> r = new ArrayList<Joint>();
		Joint joint = new Joint();
		joint.setName(SkeletonJoint.HEAD.name());
		
		Point3D p = new Point3D(10.0f, 10.0f, 10.0f);
		SkeletonJointPosition position = new SkeletonJointPosition(p, 0.8f); 
		joint.setPosition(position);
		r.add(joint);
		user.setJoints(r);
		return user;
	}
}
