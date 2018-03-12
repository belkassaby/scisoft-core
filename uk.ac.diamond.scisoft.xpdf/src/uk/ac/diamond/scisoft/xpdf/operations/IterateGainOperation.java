/*-
 * Copyright 2018 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.xpdf.operations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.dawnsci.analysis.api.processing.model.EmptyModel;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperation;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.metadata.MaskMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.xpdf.XPDFAbsorptionMaps;
import uk.ac.diamond.scisoft.xpdf.XPDFCalibration;
import uk.ac.diamond.scisoft.xpdf.XPDFComponentForm;
import uk.ac.diamond.scisoft.xpdf.XPDFCoordinates;
import uk.ac.diamond.scisoft.xpdf.XPDFElectronCrossSections;
import uk.ac.diamond.scisoft.xpdf.XPDFQSquaredIntegrator;
import uk.ac.diamond.scisoft.xpdf.XPDFTargetComponent;
import uk.ac.diamond.scisoft.xpdf.metadata.XPDFMetadata;

/**
 * An Operation that subtracts XRMC simulation data from XPDF normalized data
 * to recover the sample diffraction pattern.
 * @author Timothy Spain, timothy.spain@diamond.ac.uk
 *
 */
public class IterateGainOperation extends AbstractOperation<EmptyModel, OperationData> {

	private static final Logger logger = LoggerFactory.getLogger(XPDFCalibration.class);
	
	@Override
	public String getId() {
		return "uk.ac.diamond.scisoft.xpdf.operations.IterateGainOperation";
	}

	@Override
	public OperationRank getInputRank() {
		return OperationRank.TWO;
	}

	@Override
	public OperationRank getOutputRank() {
		return OperationRank.TWO;
	}

	@Override
	protected OperationData process(IDataset input, IMonitor monitor) throws OperationException {
		
		XPDFOperationChecker.checkXPDFMetadata(this, input, true, true, true);
		
		XPDFMetadata xMeta = input.getFirstMetadata(XPDFMetadata.class);
		XPDFCoordinates coordinates = new XPDFCoordinates(DatasetUtils.convertToDataset(input));
		XPDFAbsorptionMaps absMaps = xMeta.getAbsorptionMaps(coordinates.getDelta(), coordinates.getGamma());
		Dataset mask = DatasetUtils.convertToDataset(input.getFirstMetadata(MaskMetadata.class).getMask());
		XPDFQSquaredIntegrator quint = new XPDFQSquaredIntegrator(coordinates, mask);
		
		// Obtain the initial gain
		double gain = 1.0;
		double gainThreshold = 1e-4;
		
		Dataset sampleSubx;
		
		// Iterate the gain
		while(true) {
			// Normalize all data by the estimated detector gain
			List<Dataset> normon = new ArrayList<>();
			normon.add(Maths.divide(input, gain));
			for (XPDFTargetComponent container : xMeta.getContainers())
				normon.add(Maths.divide(xMeta.getContainerTrace(container).getNormalizedTrace(), gain));
			
			System.err.println("scaled sample (512, 512) = " + normon.get(0).getDouble(512, 512));
			// Subtract the XRMC simulated data
			List<Dataset> subx = new ArrayList<>();
			for (int i  = 0; i < normon.size(); i++)
				subx.add(Maths.subtract(normon.get(i), xMeta.getIncoherentScattering(i)));
			
			System.err.println("subx (512, 512) = " + subx.get(0).getDouble(512, 512));
			
			
			// Recursively subtract containers
			while (subx.size() > 1) {
				removeOutermostContainer(subx, absMaps);
			}
			sampleSubx = subx.get(0);
			// De-scale distinct scattering
			
			System.err.println("sample subx (512, 512) = " + sampleSubx.getDouble(512, 512));

			// absorption of sample scattering by all components
			Dataset allComponentTransmission = Maths.subtract(1, absMaps.getAbsorptionMap(0, 0));
			for (int iCont = 0; iCont < xMeta.getContainers().size(); iCont++) {
				allComponentTransmission.imultiply(Maths.subtract(1, absMaps.getAbsorptionMap(0, iCont)));
			}
			sampleSubx.idivide(Maths.subtract(1, allComponentTransmission));
			// detector efficiency (detector transmission correction)
			sampleSubx.idivide(xMeta.getDetector().getTransmissionCorrection(coordinates.getTwoTheta(), xMeta.getBeam().getBeamEnergy()));
			// number of atoms
			sampleSubx.idivide(xMeta.getSampleIlluminatedAtoms());
			// Thomson cross-section
			sampleSubx.idivide(XPDFElectronCrossSections.getThomsonCrossSection(coordinates));
			
			System.err.println("corrected sample subx (512, 512) = " + sampleSubx.getDouble(512, 512));

			double denominator = quint.qSquaredIntegral(sampleSubx);
			double numerator = xMeta.getSample().getKroghMoeSum();
			System.err.println("XRMC gain correction: " + numerator + "/" + denominator + " = " + (numerator/denominator));
			// Recalculate the gain
			double oldGain = gain;
			gain *= numerator/denominator;
			if (Math.abs(gain/oldGain - 1) < gainThreshold) 
				break;
			if (Math.abs(gain) > Double.MAX_VALUE)
				throw new OperationException(this, "Gain larger than " + Double.MAX_VALUE + ", aborting.");
			
			logger.info("IterateGainOperation: Gain = " + gain);
			
		}		
		
		return new OperationData(sampleSubx);
	}

	// Removes the contribution of the outermost container
	private void removeOutermostContainer(List<Dataset> subx, XPDFAbsorptionMaps absMaps) {
		// divide the outermost trace by its own absorption
		int lastIndex = subx.size() - 1;
		Dataset outermostNormed = Maths.divide(subx.get(lastIndex), absMaps.getAbsorptionMap(lastIndex, lastIndex));
		// Transmission of the outermost n components. Here, just the very outermost.
		Dataset transmissionI = Maths.subtract(1, absMaps.getAbsorptionMap(lastIndex, lastIndex));
		for (int i = lastIndex; i >= 0; i--) {
			// update the transmission to include this component
			transmissionI.imultiply(Maths.subtract(1, absMaps.getAbsorptionMap(lastIndex, i)));
			// subtract the contribution of the outermost container, scaled by the absorption
			Dataset absNMN = Maths.subtract(1, transmissionI);
			subx.get(i).isubtract(Maths.multiply(absNMN, outermostNormed));
		}
		// the effect of the outermost container should now be removed
		subx.remove(lastIndex);
	}
	
}
