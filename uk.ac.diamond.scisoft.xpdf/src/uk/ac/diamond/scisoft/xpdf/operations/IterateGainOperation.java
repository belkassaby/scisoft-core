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
import org.eclipse.dawnsci.analysis.api.processing.OperationDataForDisplay;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.dawnsci.analysis.api.processing.model.EmptyModel;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperation;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Maths;

import uk.ac.diamond.scisoft.xpdf.XPDFAbsorptionMaps;
import uk.ac.diamond.scisoft.xpdf.XPDFComponentForm;
import uk.ac.diamond.scisoft.xpdf.XPDFCoordinates;
import uk.ac.diamond.scisoft.xpdf.XPDFElectronCrossSections;
import uk.ac.diamond.scisoft.xpdf.XPDFTargetComponent;
import uk.ac.diamond.scisoft.xpdf.metadata.XPDFMetadata;

/**
 * An Operation that subtracts XRMC simulation data from XPDF normalized data
 * to recover the sample diffraction pattern.
 * @author Timothy Spain, timothy.spain@diamond.ac.uk
 *
 */
public class IterateGainOperation extends AbstractOperation<EmptyModel, OperationData> {

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
				normon.add(Maths.divide(xMeta.getContainerTrace(container), gain));
			
			// Subtract the XRMC simulated data
			List<Dataset> subx = new ArrayList<>();
			for (int i  = 0; i < normon.size(); i++)
				subx.add(Maths.subtract(normon.get(i), xMeta.getIncoherentScattering(i)));
			
			// Recursively subtract containers
			while (subx.size() > 1) {
				removeOutermostContainer(subx, absMaps);
			}
			sampleSubx = subx.get(0);
			// De-scale distinct scattering

			// absorption of sample scattering by all components
			Dataset allComponentTransmission = Maths.subtract(1, absMaps.getAbsorptionMap(0, 0));
			XPDFComponentForm sampleForm = xMeta.getSample().getForm();
			for (XPDFTargetComponent compo : xMeta.getContainers()) {
				allComponentTransmission.imultiply(Maths.subtract(1, absMaps.getAbsorptionMap(sampleForm, compo.getForm())));
			}
			sampleSubx.idivide(Maths.subtract(1, allComponentTransmission));
			// detector efficiency (detector transmission correction)
			sampleSubx.idivide(xMeta.getDetector().getTransmissionCorrection(coordinates.getTwoTheta(), xMeta.getBeam().getBeamEnergy()));
			// number of atoms
			sampleSubx.idivide(xMeta.getSampleIlluminatedAtoms());
			// Thomson cross-section
			sampleSubx.idivide(XPDFElectronCrossSections.getThomsonCrossSection(coordinates));
			
			
			// Recalculate the gain
			double oldGain = gain;
			gain = 1.0;
			if (Math.abs(gain/oldGain - 1) < gainThreshold) 
				break;
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
