/*-
 * Copyright 2013 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.fitting.functions;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;

public class GaussianTest {

	private static final double ABS_TOL = 1e-7;

	@Test
	public void testFunction() {
		AFunction f = new Gaussian();
		Assert.assertEquals(3, f.getNoOfParameters());
		f.setParameterValues(23., 2., 1.2);
		Assert.assertArrayEquals(new double[] {23., 2., 1.2}, f.getParameterValues(), ABS_TOL);

		double cArea = 2 * Math.sqrt(Math.PI / (4. * Math.log(2.)));
		double h = 1.2 / cArea;
		Assert.assertEquals(h, f.val(23.), ABS_TOL);

		Assert.assertEquals(0.5 * h, f.val(23. - 1), ABS_TOL);
		Assert.assertEquals(0.5 * h, f.val(23. + 1), ABS_TOL);

		AbstractDataset x = DatasetUtils.linSpace(0, 46, 200, AbstractDataset.FLOAT64);
		AbstractDataset v = DatasetUtils.convertToAbstractDataset(f.calculateValues(x));
		Assert.assertEquals(1.2, ((Number) v.sum()).doubleValue() * Math.abs(x.getDouble(0) - x.getDouble(1)), ABS_TOL);

		DoubleDataset xd = new DoubleDataset(new double[] {23. - 1, 23, 23. + 2});
		DoubleDataset dx;
		dx = f.calculateValues(xd);
		Assert.assertArrayEquals(new double[] {0.5 * h, h, h/16.}, dx.getData(), ABS_TOL);
	}
}
