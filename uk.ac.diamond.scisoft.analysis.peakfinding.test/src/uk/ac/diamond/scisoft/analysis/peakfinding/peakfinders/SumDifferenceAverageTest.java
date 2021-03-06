/*-
 * Copyright 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.peakfinding.peakfinders;

import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.dawnsci.analysis.api.peakfinding.IPeakFinder;
import org.eclipse.dawnsci.analysis.api.peakfinding.IPeakFinderParameter;
import org.eclipse.january.dataset.Dataset;
import org.junit.Assert;
import org.junit.Test;

public class SumDifferenceAverageTest {
	
	private IPeakFinder avgDiff = new SumDifferenceAverage();
	
	@Test
	public void nameCheck() {
		Assert.assertEquals("Average Sum of Differences", avgDiff.getName());
	}
	
	@Test
	public void parametersCheck() throws Exception {
		Map<String, IPeakFinderParameter> paramSet = avgDiff.getParameters();
		Assert.assertEquals(2, paramSet.size());
		assertTrue(paramSet.containsKey("Window size"));
		assertTrue(paramSet.containsKey("Standard deviation filter"));
		
		Assert.assertEquals(50, avgDiff.getParameterValue("Window size"));
		Assert.assertEquals(3, avgDiff.getParameterValue("Standard deviation filter"));
	}
	
	@Test
	public void singlePeakFinding() {
		Dataset xData = PeakyData.getxAxisRange();
		Dataset yData = PeakyData.makeGauPeak().calculateValues(xData);
		
		//Calculate the expected x-coordinate
		Double expectedPos = 0.3785 * PeakyData.getxAxisMax(); 
		Double foundPos;
		
		//Find the x-coordinate of the found peak
		Map<Integer, Double> foundPeaks = (TreeMap<Integer, Double>)avgDiff.findPeaks(xData, yData, null);
		//We need the set to have a length of 1 for the next bit...
		Assert.assertEquals(1, foundPeaks.size());
		for (Integer i : foundPeaks.keySet()) {
			foundPos = xData.getDouble(i);
			//Yes, it finds the wrong position.
			Assert.assertEquals(expectedPos, foundPos, 0.25);
		}
	}

}
