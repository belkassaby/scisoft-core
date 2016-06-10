/*-
 * Copyright 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.processing.sxrdrods;

import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;

public class LinearLeastSquaresServicesForSXRD {

	
	public static Dataset polynomial2DLinearLeastSquaresMatrixGenerator (int degree, Dataset X, Dataset Y){
		
		int noParams = (int) Math.pow(degree+1, 2);
		int datasize = X.getShape()[0];
		
		
		Dataset testMatrix = DatasetFactory.ones(new int[] {datasize, noParams}, Dataset.FLOAT64);
		
		
		for (int k =0; k<datasize; k++){
			for (int i =0; i<degree ; i++){
				for (int j=0; j<degree; j++){
					
					double x = X.getDouble(k);
					double y = Y.getDouble(k);
					
					double xFunc = Math.pow(x, i);
					double yFunc = Math.pow(y, i);
										
					testMatrix.set(xFunc*yFunc, k, j+i*j);
				}
			}
		}
		
	return testMatrix;
	
	}
	
	public  static Dataset polynomial2DLinearLeastSquaresSigmaGenerator (Dataset Z){
		
		
		int datasize = Z.getShape()[0];
		
		Dataset sigmaMatrix = DatasetFactory.ones(new int[] {datasize}, Dataset.FLOAT64);
		
		
		for (int k =0; k<datasize; k++){

			double z = Z.getDouble(k);
			double zSigma = Math.pow(z, 0.5);
				
			sigmaMatrix.set(zSigma, k);
		
		}
		
		
		
	return sigmaMatrix;
	
	}
}
