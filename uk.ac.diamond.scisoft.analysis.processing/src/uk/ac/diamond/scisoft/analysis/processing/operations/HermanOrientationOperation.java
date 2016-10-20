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
//import java.util.List;
//import java.util.Arrays;

// Imports from org.apache
//import org.apache.commons.beanutils.ConvertUtils;

// Imports from org.eclipse
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperation;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.metadata.MaskMetadata;
//import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RingROI;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.FloatDataset;
import org.eclipse.january.dataset.IndexIterator;

import uk.ac.diamond.scisoft.analysis.processing.operations.HermanOrientationModel.NumberOfPis;
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

		// Lets apply an image mask, if present, using NaN's
		Dataset nanMaskDataset = DatasetUtils.convertToDataset(dataset);

		// If there is masking data we shall replaced the masked values by NaN's
		Object nanMaskValue = Double.NaN;

		// Better make sure it's the right type of NaN though
		if (dataset.getFirstMetadata(MaskMetadata.class) != null) {
			if (nanMaskDataset.getClass() == DoubleDataset.class)
				nanMaskValue = Double.NaN;
			else if (nanMaskDataset.getClass() == FloatDataset.class)
				nanMaskValue = Float.NaN;
			else
				nanMaskValue = 0;

			// Loop over the mask and the data and replace masked values by the NaN type chosen above 
			Dataset mask = DatasetUtils.convertToDataset(dataset.getFirstMetadata(MaskMetadata.class).getMask());
			for (IndexIterator iter = nanMaskDataset.getIterator(); iter.hasNext();) {
				if (!(boolean) mask.getElementBooleanAbs(iter.index))
					nanMaskDataset.setObjectAbs(iter.index, nanMaskValue);
			}
		}
		// Now any masked pixel has the value NaN and will not be considered for subsequent evaluation


		// With this in mind, let's move on to the ROI that the HoF calculation will be performed on
		// First, let's check we've got the right kind of ROI
		if (!(model.getRegion() instanceof RingROI)) {
			throw new OperationException(this, new IllegalArgumentException("The ROI must be a ring ROI"));
		}

		SectorROI hermanSector = new SectorROI();
		RingROI modelRingROI = (RingROI) model.getRegion();
		
		// Then we'll set the centre point
		double[] centrePoint = modelRingROI.getPoint();
		hermanSector.setPoint(centrePoint[0], centrePoint[1]);

		// Then set the radius of interest
		double[] radiiPoint = modelRingROI.getRadii();
		hermanSector.setRadii(radiiPoint[0], radiiPoint[1]);
		
		// And finally, let's consider how much of the ring we're going to be evaluating as a function of pi
		NumberOfPis piEnum = model.getIntegrationRange();
		double piMultiplier = ((double) piEnum.getNumberOfPis()) / 2;
		double hermanPiRange = piMultiplier * Math.PI;
		
		// Before setting the angle to investigate
		double integrationStartInRadians = (Math.PI / 180) * model.getIntegrationStartAngle();
		hermanSector.setAngles(integrationStartInRadians, integrationStartInRadians + hermanPiRange);

		// Ok, with the mask applied and the ROI defined it's time to reduce the data
		Dataset[] reducedDataset = ROIProfile.sector(DatasetUtils.convertToDataset(dataset), null, hermanSector, false, true, false);
		
		// Then we can take the data and turn it into single dataset
		IDataset reducedData = reducedDataset[1];
		
		
		//double a = reducedData.getDouble(2);
		

		// TODO this is lazy as we're only going to investigate one frame for now, as a test.
		int dataShape = reducedData.getSize();
		
		// In future use:
		// int[] dataShape = reducedData.getShape();

		// TODO make this 2D later
		
		// Set up the data value array
		double[] hermanValues = new double[dataShape];
		double integrationRadianStep = hermanPiRange / dataShape;
		double fNormal = 0.00;
		double f = 0.00;
		double loopStepRadianValue = 0.00;
		
		//integrationStartInRadians
		
		// Extract out all the values for doing maths - TODO check with Jacob as this may NOT be the most efficient way to do this!
		for(int loopIter = 0; loopIter < dataShape; loopIter++) {
			loopStepRadianValue = integrationStartInRadians + (integrationRadianStep * loopIter);
			fNormal += reducedData.getDouble(loopIter) * Math.sin(loopStepRadianValue);
			// TODO Got to here, finish this.
			f += Math.cos(loopStepRadianValue);
			hermanValues[loopIter] = reducedData.getDouble(loopIter);
		}
		
		
		/*
		 *       for( m = startHermanIntegration; m < endHermanIntegration; m++) {
        // Herman Orientation Factor Integration
        fNormal = fNormal + ourData[m]*sin(angularArrayRadians[angleLoopCounter]);
        f = f + cos(angularArrayRadians[angleLoopCounter])*cos(angularArrayRadians[angleLoopCounter])*ourData[m]*sin(angularArrayRadians[angleLoopCounter]);
        angleLoopCounter = angleLoopCounter + 1;
      }
      
          hermanOrientationFactor = hermanCReciprocal * (((3*(f/fNormal))-1)/2);


		 */
		
		// Let's give DAWN a little something to plot on screen for the user
		OperationData toReturn = new OperationData();
		toReturn.setData(reducedData);

		
		// And then return the data
		return toReturn;	
	}


}
