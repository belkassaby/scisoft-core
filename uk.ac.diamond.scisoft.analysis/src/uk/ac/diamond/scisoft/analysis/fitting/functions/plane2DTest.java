/*-
 * Copyright 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.fitting.functions;

import static org.junit.Assert.*;

import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.optimize.ApacheOptimizer;
import uk.ac.diamond.scisoft.analysis.optimize.IOptimizer;
import uk.ac.diamond.scisoft.analysis.optimize.ApacheOptimizer.Optimizer;

public class plane2DTest {

	@Test
	public void test2DFit() throws Exception {
		
			IOptimizer optimizer = new ApacheOptimizer(Optimizer.SIMPLEX_NM);
			
			Dataset x = DatasetFactory.createRange(10, Dataset.FLOAT64);
			Dataset y = DatasetFactory.createRange(15, Dataset.FLOAT64);
			
			List<Dataset> meshGrid = DatasetUtils.meshGrid(x,y);
			
			Dataset z  = DatasetFactory.zeros(new int[]{x.getShape()[0],y.getShape()[0]}, Dataset.FLOAT64);
			z.fill(0.5);
			
			Plane2D g2 = new Plane2D();
			
			System.out.println("2D optimiser Input");
			System.out.println(g2.toString());
			
			optimizer.optimize(new Dataset[]{meshGrid.get(0), meshGrid.get(1)}, z, g2);
			
			System.out.println("2D optimiser output");
			System.out.println(g2.toString());
	}
	
}
