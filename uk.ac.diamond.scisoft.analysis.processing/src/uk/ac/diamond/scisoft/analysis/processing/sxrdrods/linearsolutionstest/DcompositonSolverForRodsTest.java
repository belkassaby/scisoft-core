/*-
 * Copyright 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.processing.sxrdrods.linearsolutionstest;

import static org.junit.Assert.*;

import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.optimize.LinearLeastSquares;

public class DcompositonSolverForRodsTest {
	
	
	
	public static final Dataset TESTMATRIX = DatasetFactory.ones(new int[] {100,4}, Dataset.FLOAT64);
	public static final Dataset TESTDATA= Maths.multiply(10, DatasetFactory.ones(new int[] {100}, Dataset.FLOAT64));
	public static final Dataset TESTSIGMASQ = Maths.multiply(2, DatasetFactory.ones(new int[] {100}, Dataset.FLOAT64));
	
	


	@Test
	public void test2D() {
		
		int k = 0;
				
		for (int i =0; i<10 ; i++){
			for (int j=0; j<10; j++){
				
				TESTMATRIX.set(1, k, 0);
				TESTMATRIX.set(i, k, 1);
				TESTMATRIX.set(j, k, 2);
				TESTMATRIX.set(i*j, k, 3);
				
				int testDataPoint =1+ i+2*j+3*i*j;
				
				TESTDATA.set(testDataPoint, k);
					
				TESTSIGMASQ.set(Maths.power(testDataPoint, 0.5), k);
						
				k++;
			}
		}
		
		LinearLeastSquares testFit = new LinearLeastSquares(.0001);

		double[] testOut = testFit.solve(TESTMATRIX, TESTDATA, TESTSIGMASQ);

		System.out.println("Test values:");
		
		for(int i =0; i<testOut.length; i++){
			System.out.println("test d[" + i + "]:  " + testOut[i]);
		}
		
		
		fail("Not yet implemented");
	}	
	

}
