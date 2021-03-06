/*-
 * Copyright 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.processing.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.dawnsci.analysis.dataset.slicer.SourceInformation;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IndexIterator;
import org.eclipse.january.metadata.MaskMetadata;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.diffraction.powder.PixelIntegrationUtils;
import uk.ac.diamond.scisoft.analysis.io.DiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.processing.operations.mask.CoordinateMaskModel;
import uk.ac.diamond.scisoft.analysis.processing.operations.mask.CoordinateMaskOperation;
import uk.ac.diamond.scisoft.analysis.processing.operations.mask.CoordinateMaskOperation.MaskAxis;
import uk.ac.diamond.scisoft.analysis.processing.operations.twod.DiffractionMetadataImportModel;
import uk.ac.diamond.scisoft.analysis.processing.operations.twod.DiffractionMetadataImportOperation;

public class CoordinateMaskingTest {

	@Test
	public void testProcessOutside() {

		final double lowerLimit = 0.1, upperLimit = 25.0;
		
		Dataset data = DatasetFactory.zeros(DoubleDataset.class, 2048, 2048);
		
		data.setMetadata(new SliceFromSeriesMetadata(new SourceInformation("/dev/null", "", null)));
		
		IDataset[] iDSArray = setDiffraction(data);
		Dataset qData = DatasetUtils.convertToDataset(iDSArray[0]),
				intermediateData = DatasetUtils.convertToDataset(iDSArray[1]);
		
		CoordinateMaskModel model = new CoordinateMaskModel();
		
		model.setCoordinateType(MaskAxis.Q);
		model.setCoordinateRange(new double[] {lowerLimit, upperLimit});
		
		Dataset mask = DatasetUtils.convertToDataset(getMask(intermediateData, model));
		model.setMaskedInside(true);
		Dataset maskInside = DatasetUtils.convertToDataset(getMask(intermediateData, model));
		
		// Test the masks against the q array
		IndexIterator it = qData.getIterator();
		while (it.hasNext()) {
			double q = qData.getElementDoubleAbs(it.index);
			assertEquals("Q value masking incorrect, inside mask. q = " + q, !(q<upperLimit && q>lowerLimit), (boolean) maskInside.getObjectAbs(it.index));
			assertEquals("Q value masking incorrect, outside mask. q = " + q, !(q>upperLimit || q<lowerLimit), (boolean) mask.getObjectAbs(it.index));
		}
		
	}

	@Test
	public void testGetId() {
		assertEquals("Coordinate masking Operation ID String not as expected", "uk.ac.diamond.scisoft.analysis.processing.operations.CoordinateMaskOperation", new CoordinateMaskOperation().getId());
	}

	@Test
	public void testGetInputRank() {
		assertEquals("Coordinate masking Operation input rank not as expected:", OperationRank.TWO, new CoordinateMaskOperation().getInputRank());
	}

	@Test
	public void testGetOutputRank() {
		assertEquals("Coordinate masking Operation output rank not as expected:", OperationRank.TWO, new CoordinateMaskOperation().getOutputRank());
	}
	
	@Test
	public void testMetadatalessPixelMasking() {
		final double lowerLimit = 400, upperLimit = 800;
		final int size = 2048, halfSize = size/2;
		double sqrt2 = 1.4142135623730951;

		Dataset data = DatasetFactory.zeros(DoubleDataset.class, size, size);
		data.setMetadata(new SliceFromSeriesMetadata(new SourceInformation("/dev/null", "", null)));
		
		CoordinateMaskModel model = new CoordinateMaskModel();
		
		model.setCoordinateType(MaskAxis.PIXEL);
		model.setCoordinateRange(new double[] {lowerLimit, upperLimit});
		model.setMaskedInside(true);

		Dataset ringMask = DatasetUtils.convertToDataset(getMask(data, model));
		
		// Test some points
		assertTrue("Central pixel incorrectly masked", getMaskRelativeToCentre(ringMask, 0, 0, halfSize));
		assertTrue("Inner edge pixel incorrectly masked", getMaskRelativeToCentre(ringMask, (int) lowerLimit-1, 0, halfSize));
		assertTrue("Inner edge pixel incorrectly masked", getMaskRelativeToCentre(ringMask, (int) (lowerLimit/sqrt2), (int) (lowerLimit/sqrt2), halfSize));
		assertTrue("Inner edge pixel incorrectly unmasked", !getMaskRelativeToCentre(ringMask, (int) lowerLimit+1, 0, halfSize));
		assertTrue("Inner edge pixel incorrectly unmasked", !getMaskRelativeToCentre(ringMask, (int) (lowerLimit/sqrt2) + 1, (int) (lowerLimit/sqrt2) + 1, halfSize));
		assertTrue("Outer edge pixel incorrectly masked", !getMaskRelativeToCentre(ringMask, (int) upperLimit-1, 0, halfSize));
		assertTrue("Outer edge pixel incorrectly masked", !getMaskRelativeToCentre(ringMask, (int) (upperLimit/sqrt2), (int) (upperLimit/sqrt2), halfSize));
		assertTrue("Outer edge pixel incorrectly unmasked", getMaskRelativeToCentre(ringMask, (int) (upperLimit+1), 0, halfSize));
		assertTrue("Outer edge pixel incorrectly unmasked", getMaskRelativeToCentre(ringMask, (int) (upperLimit/sqrt2 + 1), (int) (upperLimit/sqrt2 + 1), halfSize));
		
		
	}
	
	private boolean getMaskRelativeToCentre(IDataset mask, int iCentre, int jCentre, int halfsize) {
		return mask.getBoolean(iCentre + halfsize, jCentre + halfsize);
	}
	
	// sets diffraction metadata, gets the q array (element 0) and the data to pass on (element 1) 
	private IDataset[] setDiffraction(Dataset data) {
		// Add the XPDF detector calibration to the data
		DiffractionMetadataImportOperation dMIO = new DiffractionMetadataImportOperation();
		DiffractionMetadataImportModel dMIM = new DiffractionMetadataImportModel();
		
		dMIM.setFilePath("/dls/science/groups/das/ExampleData/i15-1/integration/CeO2_NIST_8s_19slices_averaged_fixedE_calibration.nxs");
		dMIO.setModel(dMIM);
		
		IDataset intermediateData = dMIO.execute(data, null).getData();
		
		// get the diffraction metadata produced
		DiffractionMetadata dM;
		try {
			dM = intermediateData.getMetadata(DiffractionMetadata.class).get(0);
		} catch (Exception e) {
			fail("Diffraction metadata not found");
			return null;
		}

		Dataset q = PixelIntegrationUtils.generateQArray(dM);
		return new IDataset[] {q, intermediateData};
	}

	private IDataset getMask(IDataset intermediateData, CoordinateMaskModel model) {
		CoordinateMaskOperation op = new CoordinateMaskOperation();
		op.setModel(model);
		IDataset maskedData = op.execute(intermediateData, null).getData();
		
		// Get the mask produced
		MaskMetadata maskMD;
		try {
			maskMD = maskedData.getMetadata(MaskMetadata.class).get(0);
		} catch (Exception e) {
			fail("Mask metadata not found.");
			return null;
		}
		return maskMD.getMask();
		
	}
	
}
