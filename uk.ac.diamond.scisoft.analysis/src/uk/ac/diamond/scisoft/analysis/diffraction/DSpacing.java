/*-
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.diffraction;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.vecmath.Vector3d;

import uk.ac.diamond.scisoft.analysis.roi.EllipticalROI;

/**
 * Utility class to hold methods that calculate or use d-spacings
 */
public class DSpacing {

	/**
	 * Calculate d-spacings from given positions of Bragg diffraction spots
	 * @param detector
	 * @param diffExp
	 * @param pos
	 *            An array of x,y positions of spots on the detector in pixels. There must be an even number of values
	 * @return array of inter-spot distances (d spacing) in angstroms
	 */
	public static double[] dSpacingsFromPixelCoords(DetectorProperties detector, DiffractionCrystalEnvironment diffExp,
			int... pos) {
		double[] dpos = new double[pos.length];
		for (int i = 0; i < pos.length; i++)
			dpos[i] = pos[i];
		return dSpacingsFromPixelCoords(detector, diffExp, dpos);
	}

	static class Pair {
		final double x;
		final double y;

		public Pair(double x, double y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public boolean equals(Object obj) {
			return Double.doubleToRawLongBits(x) == Double.doubleToRawLongBits(y);
		}

		@Override
		public int hashCode() {
			return (int) (Double.doubleToRawLongBits(x) * 17 + Double.doubleToRawLongBits(y));
		}
	}

	/**
	 * Calculate d-spacings from given positions of Bragg diffraction spots
	 * @param detector
	 * @param diffExp
	 * @param pos
	 *            An array of x,y positions of spots on the detector in pixels. There must be an even number of values
	 * @return array of inter-spot distances (d spacing) in angstroms
	 */
	public static double[] dSpacingsFromPixelCoords(DetectorProperties detector, DiffractionCrystalEnvironment diffExp,
			double... pos) {
		if (pos.length % 2 != 0) {
			throw new IllegalArgumentException("The number of values must be even");
		}

		// unique-fy coords
		Set<Pair> coords = new LinkedHashSet<Pair>();
		for (int i = 0; i < pos.length; i += 2) {
			Pair p = new Pair(pos[i], pos[i+1]);
			coords.add(p);
		}

		Vector3d q = new Vector3d();
		QSpace qspace = new QSpace(detector, diffExp);
		double[] spacings = new double[coords.size()];
		Iterator<Pair> it = coords.iterator();
		Pair p2 = it.next();
		int i = 0;
		while (it.hasNext()) {
			Pair p1 = p2;
			p2 = it.next();
			q.sub(qspace.qFromPixelPosition(p1.x, p1.y), qspace.qFromPixelPosition(p2.x, p2.y));
			spacings[i++] = 2 * Math.PI / q.length();
		}
		return spacings;
	}

	/**
	 * Calculate radius of circle assuming the detector is normal to the beam vector
	 * 
	 * @param detector
	 * @param difExp
	 * @param dSpacing
	 * @return radius of circle in PIXELS
	 */
	public static double radiusFromDSpacing(DetectorProperties detector, DiffractionCrystalEnvironment difExp,
			double dSpacing) {
		double theta = 2*(Math.asin(difExp.getWavelength() / (2 * dSpacing)));
		Vector3d radiusVector = new Vector3d(0, Math.sin(theta), Math.cos(theta));
		Vector3d beam = new Vector3d(detector.getBeamVector());
		Vector3d normal = detector.getNormal();
		
		// scale vectors
		radiusVector.scale(detector.getOrigin().dot(normal) / radiusVector.dot(normal));
		beam.scale(detector.getOrigin().dot(normal) / beam.dot(normal));

		radiusVector.sub(beam);
		return radiusVector.length() / detector.getVPxSize();
	}

	/**
	 * Calculate an ellipse
	 * @param detector
	 * @param difExp
	 * @param dSpacing
	 * @return elliptical roi
	 */
	public static EllipticalROI ellipseFromDSpacing(DetectorProperties detector, DiffractionCrystalEnvironment difExp,
			double dSpacing) {
		double alpha = 2*(Math.asin(difExp.getWavelength() / (2 * dSpacing)));
		Vector3d beam = new Vector3d(detector.getBeamVector());
		Vector3d normal = detector.getNormal();

		Vector3d major = new Vector3d();
		Vector3d minor = new Vector3d();
		minor.cross(normal, beam);
		double eta = minor.length();
		if (eta == 0) {
			major = detector.getVerticalVector();
		} else {
			major.cross(normal, minor);
		}
		double angle = major.angle(detector.getHorizontalVector());

		Vector3d intersect = detector.getBeamCentrePosition();
		double r = intersect.length();
		double se = Math.sin(eta);
		double ce = Math.cos(eta);
		double sa = Math.sin(alpha);
		double ca = Math.cos(alpha);

		double x = r*se*sa*sa/(ca*ca - se*se);
		major.scale(x/major.length());
		intersect.sub(major);
		Vector3d centre = new Vector3d();

		r /= detector.getVPxSize();
		double a = r*ce*sa*ca/(ca*ca - se*se);
		double te = se/ce;
		double b = r*sa/Math.sqrt(ca*ca - sa*sa*te*te);

		detector.pixelCoords(intersect, centre);
		return new EllipticalROI(a, b, angle, centre.x, centre.y);
	}
}
