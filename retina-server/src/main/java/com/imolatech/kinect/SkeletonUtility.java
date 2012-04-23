package com.imolatech.kinect;

import java.util.HashMap;

import org.OpenNI.Point3D;
import org.OpenNI.SkeletonJoint;
import org.OpenNI.SkeletonJointPosition;

public class SkeletonUtility {
	/**
	 * the Euclidian distance between the two points.
	 */
	public static float distApart(Point3D p1, Point3D p2) {
		float dist = (float) Math.sqrt((p1.getX() - p2.getX())
				* (p1.getX() - p2.getX()) + (p1.getY() - p2.getY())
				* (p1.getY() - p2.getY()) + (p1.getZ() - p2.getZ())
				* (p1.getZ() - p2.getZ()));
		return dist;
	}

	/**
	 * get the (x, y, z) coordinate for the joint (or return null).
	 */
	public static Point3D getJointPosition(
			HashMap<SkeletonJoint, SkeletonJointPosition> skel, SkeletonJoint j) {
		SkeletonJointPosition pos = skel.get(j);
		if (pos == null || pos.getConfidence() == 0)
			return null;

		return pos.getPosition();
	}
}
