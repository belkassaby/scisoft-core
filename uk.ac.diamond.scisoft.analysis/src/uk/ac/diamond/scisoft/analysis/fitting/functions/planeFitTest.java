/*-
 * Copyright 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.fitting.functions;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.IndexIterator;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.optimize.ApacheOptimizer;
import uk.ac.diamond.scisoft.analysis.optimize.ApacheOptimizer.Optimizer;
import uk.ac.diamond.scisoft.analysis.optimize.IOptimizer;



public class planeFitTest {

	
//	public static Dataset makeOffsetPlane(){
//		Dataset offset = DatasetFactory.zeros(new int[]{10,10}, Dataset.FLOAT64);
//		IndexIterator it1 = offset.getIterator();
//		
//		while (it1.hasNext()) {
//			offset.setObjectAbs(it1.index, 0.5);
//		}
//		return offset;
//	}
//	
//	public static Dataset makeInclinePlane(){
//		Dataset inclinePlane= DatasetFactory.zeros(new int[]{10,10}, Dataset.FLOAT64);
//		for (int i=0; i<10; i++){
//			for (int j=0; j<10; j++){
//				double v = 2*i*i;
//				inclinePlane.set(v, i, j);
//			}
//		}
//		
//		return inclinePlane;
//	}
//	
//	public static Dataset makeTwoDFunction(){
//		Dataset twoDFunction= DatasetFactory.zeros(new int[]{10,10}, Dataset.FLOAT64);
//		for (int i=0; i<10; i++){
//			for (int j=0; j<10; j++){
//				double v = 2*i*i + 2*j*j;
//				twoDFunction.set(v, i, j);
//			}
//		}
//		
//		return twoDFunction;
//	}
//	
//	
//	
//	@Test
//	public void testPlaneFit(Dataset input, int[][]coords, double[] values, int degree) {
//		//assertEquals(0.5, a[0]);
//		
////		int[][] coords 
//		
//		Polynomial2D polyquest = new Polynomial2D(3);
//		
//		polyquest.outputAArray(coords, values, degree, 1000, 0.1);
//	
//	}
//	
//	@Test
//	public void test1DFitGaussian() throws Exception {
//			IOptimizer optimizer = new ApacheOptimizer(Optimizer.SIMPLEX_NM);
//			
//			Gaussian g = new Gaussian(5,2,1);
//			
//			IDataset x = DatasetFactory.createRange(10, Dataset.FLOAT64);
//			DoubleDataset y = g.calculateValues(x);
//			
//			Gaussian g2 = new Gaussian(4,2,1);
//			
//			System.out.println(g2.toString());
//			
//			optimizer.optimize(new IDataset[]{x}, y, g2);
//			//for 2D
////			optimizer.optimize(new IDataset[]{x,y}, z, anAFunction);
//			//if z shape = [10,15] and x is 10 unique values, y is 15, x and y are likely to need to be [10,15] in shape.
//			
//			System.out.println(g2.toString());
//	}
//	
//	@Test
//	public void test1DFitPolynomial() throws Exception {
//			
//			IOptimizer optimizer = new ApacheOptimizer(Optimizer.SIMPLEX_NM);
//			
//			Polynomial g = new Polynomial(new double[]{1,2,3,4});
//			
//			System.out.println(g.toString());
//			
//			IDataset x = DatasetFactory.createRange(10, Dataset.FLOAT64);
//			DoubleDataset y = g.calculateValues(x);
//			
//			Polynomial g2 = new Polynomial(5);
//			
//			
//			
//			System.out.println(g2.toString());
//			
//			optimizer.optimize(new IDataset[]{x}, y, g2);
//			//for 2D
////			optimizer.optimize(new IDataset[]{x,y}, z, anAFunction);
//			//if z shape = [10,15] and x is 10 unique values, y is 15, x and y are likely to need to be [10,15] in shape.
//			
//			System.out.println(g2.toString());
//	}	
	
	@Test
	public void test2DPlaneFit() throws Exception {
		
			IOptimizer optimizer = new ApacheOptimizer(Optimizer.SIMPLEX_NM);
			
			Dataset x = DatasetFactory.createRange(10, Dataset.FLOAT64);
			Dataset y = DatasetFactory.createRange(15, Dataset.FLOAT64);
			
			System.out.println("dataset x first value" + x.getDouble(0));
			
			

			List<Dataset> meshGrid = DatasetUtils.meshGrid(x,y);
			
			Dataset z  = DatasetFactory.zeros(new int[]{x.getShape()[0],y.getShape()[0]}, Dataset.FLOAT64);
			z.iadd(0.5);
			
			Polynomial2D g2 = new Polynomial2D(2);

			optimizer.optimize(new Dataset[]{meshGrid.get(0), meshGrid.get(1)}, z, g2);
			
			System.out.println("2D optimiser output");
			System.out.println(g2.toString());
	}
	
	
	@Test
	public void test2Dx2y2Fit() throws Exception {
		
			IOptimizer optimizer = new ApacheOptimizer(Optimizer.SIMPLEX_NM);
			
			Dataset x = DatasetFactory.createRange(10, Dataset.FLOAT64);
			Dataset y = DatasetFactory.createRange(15, Dataset.FLOAT64);
			
			System.out.println("dataset x first value" + x.getDouble(0));
			
			

			List<Dataset> meshGrid = DatasetUtils.meshGrid(x,y);
			
			Dataset z= DatasetFactory.zeros(new int[]{x.getShape()[0],y.getShape()[0]}, Dataset.FLOAT64);
			for (int i=0; i<x.getShape()[0]; i++){
				for (int j=0; j<y.getShape()[0]; j++){
					double v = 2*i*i + 2*j*j;
					z.set(v, i, j);
				}
			}
			Polynomial2D g2 = new Polynomial2D(2);

			optimizer.optimize(new Dataset[]{meshGrid.get(0), meshGrid.get(1)}, z, g2);
			
			System.out.println("2D optimiser output");
			System.out.println(g2.toString());
	}
}	


