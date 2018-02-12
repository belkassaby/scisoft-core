/*
 * Copyright (c) 2018 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.xpdf.xrmc;

import javax.vecmath.Vector3d;

import org.eclipse.dawnsci.analysis.api.diffraction.DetectorProperties;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;

import uk.ac.diamond.scisoft.analysis.diffraction.powder.GenericPixelIntegrationCache;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.PixelIntegration;

/**
 * Given detector properties, an intensity dataset and the coordinates of the
 * intensity grid in scattering angle γ and δ, return the intensity on the
 * described detector.
 * 
 * @author Timothy Spain, timothy.spain@diamond.ac.uk
 *
 */
public class IntensityFromDetectorProperties {
	private Dataset realGamma;
	private Dataset realDelta;
	private Dataset xrmcI;
	private Dataset xrmcJ;
	private Dataset gammaAxis;
	private Dataset deltaAxis;
	
	private Dataset xrmcIntensity;
	private Dataset realIntensity;
	
	private DetectorProperties dProp;
	
	public static Dataset calculate(DetectorProperties dPropIn, Dataset intensity, Dataset gammaAxis, Dataset deltaAxis) {
		IntensityFromDetectorProperties obj = new IntensityFromDetectorProperties(dPropIn, intensity, gammaAxis, deltaAxis);
		obj.generateRealGammaDelta();
		obj.generateXRMCij();
		obj.interpolateIntensity();
		
		return obj.realIntensity;
	}
	
	private IntensityFromDetectorProperties() {
	}

	private IntensityFromDetectorProperties(DetectorProperties dPropIn, Dataset intensity, Dataset gammaAxis, Dataset deltaAxis) {
		this.setDetectorProperties(dPropIn);
		this.setXRMCData(intensity, gammaAxis, deltaAxis);
	}
	
	private void setDetectorProperties(DetectorProperties dPropIn) {
		this.dProp = dPropIn;
	}
	
	private void setXRMCData(Dataset intensity, Dataset gammaAxis, Dataset deltaAxis) {
		this.xrmcIntensity = intensity;
		this.gammaAxis = gammaAxis;
		this.deltaAxis = deltaAxis;
	}
	
	// Generates the gamma and delta arrays on the real detector
	private void generateRealGammaDelta() {
		int nX = dProp.getPx();
		int nY = dProp.getPy();
		
		realGamma = DatasetFactory.zeros(nX, nY);
		realDelta = DatasetFactory.zeros(nX, nY);
		
		for (int i = 0; i < nX; i++) {
			for (int j = 0; j < nY; j++) {
				Vector3d r = dProp.pixelPosition(i+0.5, j+0.5);
				realGamma.set(Math.atan2(r.x, r.z), i, j);
				realDelta.set(Math.atan2(r.y, quadrate(r.x, r.z)), i, j);
			}
		}
	}
	
	private void generateXRMCij() {
		GenericPixelIntegrationCache xrmcFromReal = new GenericPixelIntegrationCache(realGamma, realDelta, gammaAxis, deltaAxis);

		xrmcI = DatasetFactory.zeros(gammaAxis.getSize(), deltaAxis.getSize());
		xrmcJ = DatasetFactory.zeros(gammaAxis.getSize(), deltaAxis.getSize());
		
		int nX = dProp.getPx();
		int nY = dProp.getPy();

		Dataset realI = DatasetFactory.zeros(nX, nY);
		Dataset realJ = DatasetFactory.zeros(nX, nY);
		
		for (int i = 0; i < dProp.getPx(); i++) {
			for (int j = 0; j < dProp.getPy(); j++) {
				realI.set(i, i, j);
				realJ.set(j, i, j);
			}
		}
		
		xrmcI = PixelIntegration.integrate(realI, null, xrmcFromReal).get(1);
		xrmcJ = PixelIntegration.integrate(realJ, null, xrmcFromReal).get(1);
		
	}

	private void interpolateIntensity() {
		GenericPixelIntegrationCache realFromXRMC = new GenericPixelIntegrationCache(xrmcI, xrmcJ, DatasetFactory.createRange(dProp.getPx()), DatasetFactory.createRange(dProp.getPy()));
		
		realIntensity = PixelIntegration.integrate(xrmcIntensity, null, realFromXRMC).get(1);
	}
	
	private double quadrate(double x, double y) {
		return Math.sqrt(square(x) + square(y));
	}
	
	private double square(double x) {
		return x*x;
	}
}
