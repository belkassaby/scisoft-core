/*-
 * Copyright 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.fitting.functions;

import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.optimize.ApacheOptimizer;
import uk.ac.diamond.scisoft.analysis.optimize.ApacheOptimizer.Optimizer;
import uk.ac.diamond.scisoft.analysis.optimize.IOptimizer;



public class planeFitTest {
	
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
	
	@Test
	public void test2Dx2y2FitLMoptimizer() throws Exception {
		
			IOptimizer optimizer = new ApacheOptimizer(Optimizer.LEVENBERG_MARQUARDT);
			
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
			
			System.out.println("2D LM optimiser output");
			System.out.println(g2.toString());
	}
}	


