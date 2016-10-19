/*-
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.processing.operations;

// Imports from java
import java.util.List;
import java.util.Arrays;

// Imports from org.eclipse
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperation;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.metadata.MaskMetadata;

// Imports from uk.ac.diamond
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
import uk.ac.diamond.scisoft.ncd.processing.NcdOperationUtils;
import uk.ac.diamond.scisoft.analysis.processing.io.NexusNcdMetadataReader;

// Might not need this, we shall see...
//import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;


// The operation for a DAWN process to perform a Herman Orientation calculation on a given image
public class HermanOrientationOperation extends AbstractOperation<HermanOrientationModel, OperationData> {


	// Let's give this process an ID tag
	@Override
	public String getId() {
		return "uk.ac.diamond.scisoft.analysis.processing.operations.HermanOrientationOperation";
	}


	// Before we start, let's make sure we know how many dimensions of data are going in...
	@Override
	public OperationRank getInputRank() {
		return OperationRank.TWO;
	}


	// ...and out
	@Override
	public OperationRank getOutputRank() {
		return OperationRank.TWO;
	}


	// Now let's define the 'meat' of the process
	@Override
	public OperationData process(IDataset dataset, IMonitor monitor) throws OperationException {

	// We will need to change the integration range as given here into an ROI at some point...
		int hermanPiROI = model.getIntegrationRange();
		// First get the region that we're interested in examining
		IROI regionOfInterest = 1; // This is where the magic will need to come in.
		// e.g. IROI roi = model.getRegion();

		// Let's just work with the region of interest now
		SectorROI sectorRoi = (SectorROI) hermanPiROI;

		// Extract out some information about our data from dataset
		int[] frames = NcdOperationUtils.addDimension(dataset.getShape());
		int dimension = 2; // Matching our input rank
		int[] areaShape = (int[]) ConvertUtils.convert(Arrays.copyOfRange(frames, frames.length - dimension, frames.length), int[].class);

		// Create a home for the mask metadata and try to fetch it, if not possible raise an exception
		List<MaskMetadata> mask;
		try {
			mask = dataset.getMetadata(MaskMetadata.class);
		} catch (Exception e) {
			throw new OperationException(this, e);
		}

		// Create a null mask and if possible, fill it with a mask from our dataset
		Dataset maskDataset = null;
		if (mask != null) {
			maskDataset = DatasetUtils.convertToDataset(mask.get(0).getMask()).getSlice();
		}




		IDataset userData = dataset;




		OperationData toReturn = new OperationData();
		return toReturn;	
	}


}
