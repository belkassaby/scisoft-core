/*-
 * Copyright 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawnsci.surfacescatter;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.dataset.SliceND;

public class LinearLeastSquaresServicesForDialog {

	
	public static Dataset polynomial2DLinearLeastSquaresMatrixGenerator (int degree, 
																	Dataset xValues, 
																	Dataset yValues){
		
		int noParams = (int) Math.pow(degree+1, 2);
		int datasize = xValues.getShape()[0];
		
//		Dataset testMatrix = DatasetFactory.ones(new int[] {datasize, noParams}, Dataset.FLOAT64);
		
		DoubleDataset testMatrix = DatasetFactory.zeros(datasize, noParams);
		
		int p = 0;
		
		double check = (degree+1)*(degree+1);
		
		for (int k =0; k<datasize; k++){
			
			double x = xValues.getDouble(k);
			double y = yValues.getDouble(k);
			
			for (int j =0; j<degree+1 ; j++){
				
				for (int i=0; i<degree+1; i++){
					
					double f =  Math.pow(x, j)*Math.pow(y, i);
					
					testMatrix.set(f, k, p);
					
					p++;
					if(p == check){
						p=0;
					}
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
	
	
	public static Dataset exponential2DLinearLeastSquaresMatrixGenerator (Dataset xValues, 
																		  Dataset yValues, 
																		  Dataset zValues){
		
		int noParams = 5;
		int datasize = xValues.getShape()[0];
		
		
		Dataset testMatrix = DatasetFactory.ones(new int[] {datasize, noParams}, Dataset.FLOAT64);
		
		int[] pos = new int[]{0,0};
		for (int i = 0; i < datasize; i++) {
			testMatrix.set(xValues.getObject(i), pos);
			pos[0]++;
		}
		
//		int p = 0;
		
//		double check = (degree+1)*(degree+1);

		for (int k =0; k<datasize; k++){
	
			double obs = zValues.getDouble(k);
			
			if (obs> 1.0){
				
				double w = 0;
				
				try{
					w = - Math.log(1-1/zValues.getDouble(k));
				}
				catch(Exception i){
//					System.out.println(i.getMessage());
				}
				if (Double.isInfinite(w)){
//					System.out.println("error at k1:  " + k + "  w1:   " + w);
					w = -900000000;
				}
				else if (Double.isNaN(w)){
//					System.out.println("error at k1:  " + k + "  w1:   " + w);
					w = 0;
				}
				
				testMatrix.set(w, k, 0);	
			
			}
			
			else{
			
				testMatrix.set(0, k, 0);
			}
		
			testMatrix.set(1, k, 1);	
			
			double x = xValues.getDouble(k);
					
			testMatrix.set(x, k, 2);
			
			double y = yValues.getDouble(k);
					
			testMatrix.set(y, k, 3);
			
			double xy = x*y;
			
			testMatrix.set(xy, k, 4);
					
		}
	
	return testMatrix;
	
	}
	
	public  static Dataset exponential2DLinearLeastSquaresSigmaGenerator (Dataset Z){
		
		
		int datasize = Z.getShape()[0];
		
		Dataset sigmaMatrix = DatasetFactory.ones(new int[] {datasize}, Dataset.FLOAT64);
		
		
		for (int k =0; k<datasize; k++){

			double z = Z.getDouble(k);
			double zSigma = Math.pow(z, 0.5);
				
			sigmaMatrix.set(zSigma, k);
		
		}
		
		
		
	return sigmaMatrix;
	
	}
	
	
	public static Dataset refinedExponential2DLinearLeastSquaresMatrixGenerator (Dataset xValues, 
																				 Dataset yValues, 
//																				 Dataset zValues,
																				 double[] paramsPoly,
																				 double[] paramsExp){
	
		int noParams = 2;
		int datasize = xValues.getShape()[0];
		
		double[] a = paramsExp;
		double[] b = paramsPoly;
		
		Dataset testMatrix = DatasetFactory.ones(new int[] {datasize, noParams}, Dataset.FLOAT64);
		
		int[] pos = new int[]{0,0};
		for (int i = 0; i < datasize; i++) {
			testMatrix.set(xValues.getObject(i), pos);
			pos[0]++;
		}
		
		for (int k =0; k<datasize; k++){
			
			
			double x = xValues.getDouble(k);
			double y = yValues.getDouble(k);
			
			
			double w = b[0] 
					+ b[1]*x
					+ b[2]*y 
					+ b[3]*x*y;
				
			if (Double.isInfinite(w)){
//				System.out.println("error at k1:  " + k + "  w:   " + w);
				w = 1000000000;
			}
//			System.out.println("  w:   " + w);
			testMatrix.set(w, k, 0);	
		
			double w1 = -Math.exp(a[0]) + Math.exp(a[1])*Math.exp(a[2]* x + a[3]* y + a[4]*x* y);
			
			if (Double.isInfinite(w1)){
//				System.out.println("error at k1:  " + k + "  w1:   " + w1);
				w1 = 900000000;
			}
			else if (Double.isNaN(w1)){
//				System.out.println("error at k1:  " + k + "  w1:   " + w1);
				w1 = 0;
			}
//			System.out.println("  w1:   " + w1);
			testMatrix.set(w1, k, 1);	
		
		}
		
		return testMatrix;
		
	}
	
	public  static Dataset refinedExponential2DLinearLeastSquaresSigmaGenerator (Dataset Z){
	
	
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
//TEST