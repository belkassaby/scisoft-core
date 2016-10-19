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

// Imports from org.apache
import org.apache.commons.beanutils.ConvertUtils;

// Imports from org.eclipse
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperation;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.metadata.MaskMetadata;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RingROI;

// Imports from uk.ac.diamond
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
//import uk.ac.diamond.scisoft.ncd.processing.NcdOperationUtils;
//import uk.ac.diamond.scisoft.analysis.processing.io.NexusNcdMetadataReader;

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
		//double piMultiplier = model.getIntegrationRange.getValue();
		
		//double hermanPiROI = piMultiplier * Math.PI;
		// First get the region that we're interested in examining
		IROI roiToIntegrate = model.getRegion();

		// Now that we have a potentially valid ROI, let's check that it is valid
		// Did the user provide co-ordinates
		if (model.getRegion() == null) {
			try {
				// Or provide a file path?
				//NexusNcdMetadataReader reader = new NexusNcdMetadataReader(model.getFilePath());
				//roiToIntegrate = reader.getROIDataFromFile();
				// Or fail to provide an ROI
				if (roiToIntegrate == null) {
					throw new Exception("ROI must be defined for this operation");
				}
			// If they have failed, let's catch the error here
			} catch (Exception e) {
				throw new OperationException(this, e);
			}
		}

		// Next up, if they have provided an ROI, was it the right kind of ROI
		if (!(roiToIntegrate instanceof RingROI)) {
			throw new OperationException(this, new IllegalArgumentException("The ROI must be a ring ROI"));
		}

		//
		RingROI ringRoi = (RingROI) roiToIntegrate;



		// Let's just work with the region of interest now


		// Extract out some information about our data from dataset
		//int[] frames = NcdOperationUtils.addDimension(dataset.getShape());
		// 2D data, should match our input rank
		int dimension = 2;
		// Well now...
		//int[] areaShape = (int[]) ConvertUtils.convert(Arrays.copyOfRange(frames, frames.length - dimension, frames.length), int[].class);

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

		RingROI a;



		IDataset userData = dataset;




		OperationData toReturn = new OperationData();
		return toReturn;	
	}


}
