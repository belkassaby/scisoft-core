/*
 * Copyright © 2011 Diamond Light Source Ltd.
 * Contact :  ScientificSoftware@diamond.ac.uk
 * 
 * This is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 * 
 * This software is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this software. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.scisoft.analysis.optimize;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.direct.NelderMead;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;


/**
 * Class which wraps the Apache Commons Nelder Mead routine
 * and makes it compatible with the scisoft fitting routines
 */
public class ApacheNelderMead implements IOptimizer {

	@Override
	public void optimize(IDataset[] coords, IDataset data, final IFunction function) throws Exception {
		
		// Pull out the data which is required from the inputs
		final int numCoords = coords.length;
		final DoubleDataset[] newCoords = new DoubleDataset[numCoords];
		for (int i = 0; i < numCoords; i++) {
			newCoords[i] = (DoubleDataset) DatasetUtils.convertToAbstractDataset(coords[i]).cast(AbstractDataset.FLOAT64);
		}

		final DoubleDataset values = (DoubleDataset) DatasetUtils.convertToAbstractDataset(data).cast(AbstractDataset.FLOAT64);

		// create an instance of the fitter
		NelderMead nm = new NelderMead();
		
		// provide the fitting function which wrappers all the normal fitting functionality
		MultivariateRealFunction f1 = new MultivariateRealFunction() {
			
			@Override
			public double value(double[] arg0) throws FunctionEvaluationException, IllegalArgumentException {
				function.setParameterValues(arg0);
				return function.residual(true, values, newCoords);
			}
		};
			
		// preform the optimisation
		double[] start = function.getParameterValues();
		
		RealPointValuePair result = nm.optimize(f1, GoalType.MINIMIZE, start);
		
		// set the input functions parameters to be the result before finishing.
		function.setParameterValues(result.getPoint());
		

	}

}
