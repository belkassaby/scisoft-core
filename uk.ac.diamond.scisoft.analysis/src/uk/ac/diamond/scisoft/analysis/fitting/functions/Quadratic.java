/*-
 * Copyright 2011 Diamond Light Source Ltd.
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

import uk.ac.diamond.scisoft.analysis.dataset.AbstractCompoundDataset;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;


/**
 * Class that wrappers the function y(x) = ax^2 + bx + c
 */
public class Quadratic extends AFunction {
	private static String cname = "Quadratic";
	private static String[] paramNames = new String[]{"A", "B", "C"};
	private static String cdescription = "y(x) = ax^2 + bx + c";

	/**
	 * Basic constructor, not advisable to use
	 */
	public Quadratic() {
		super(3);

		setNames();
	}

	public Quadratic(IParameter... params) {
		super(params);

		setNames();
	}

	/**
	 * Constructor that allows for the positioning of all the parameter bounds
	 * 
	 * @param minA
	 *            minimum boundary for the A parameter
	 * @param maxA
	 *            maximum boundary for the A parameter
	 * @param minB
	 *            minimum boundary for the B parameter
	 * @param maxB
	 *            maximum boundary for the B parameter
	 * @param minC
	 *            minimum boundary for the C parameter
	 * @param maxC
	 *            maximum boundary for the C parameter
	 */
	public Quadratic(double minA, double maxA, double minB, double maxB, double minC, double maxC) {
		super(3);

		getParameter(0).setLowerLimit(minA);
		getParameter(0).setUpperLimit(maxA);
		getParameter(0).setValue((minA + maxA) / 2.0);

		getParameter(1).setLowerLimit(minB);
		getParameter(1).setUpperLimit(maxB);
		getParameter(1).setValue((minB + maxB) / 2.0);

		getParameter(2).setLowerLimit(minC);
		getParameter(2).setUpperLimit(maxC);
		getParameter(2).setValue((minC + maxC) / 2.0);

		setNames();
	}
	
	/**
	 * A very simple constructor which just specifies the values, not the bounds
	 * @param Params
	 */
	public Quadratic(double[] Params) {
		super(Params);

		setNames();
	}

	private void setNames() {
		name = cname;
		description = cdescription;
		for (int i = 0; i < paramNames.length; i++) {
			IParameter p = getParameter(i);
			p.setName(paramNames[i]);
		}
	}

	double a, b, c;
	private void calcCachedParameters() {
		a = getParameterValue(0);
		b = getParameterValue(1);
		c = getParameterValue(2);

		setDirty(false);
	}

	@Override
	public double val(double... values) {
		if (isDirty())
			calcCachedParameters();

		double position = values[0];
		return a * position * position + b * position + c;
	}

	@Override
	public void fillWithValues(DoubleDataset data, CoordinatesIterator it) {
		if (isDirty())
			calcCachedParameters();

		double[] coords = it.getCoordinates();
		int i = 0;
		double[] buffer = data.getData();
		while (it.hasNext()) {
			double p = coords[0];
			buffer[i++] = a * p * p + b * p + c;
		}
	}

	@Override
	public double partialDeriv(int parameter, double... position) {
		final double pos = position[0];
		switch (parameter) {
		case 0:
			return pos * pos;
		case 1:
			return pos;
		case 2:
			return 1.0;
		default:
			throw new IndexOutOfBoundsException("Parameter index is out of bounds");
		}
	}

	@Override
	public void fillWithPartialDerivativeValues(IParameter param, DoubleDataset data, CoordinatesIterator it) {
		int i = indexOfParameter(param);
		AbstractDataset pos = DatasetUtils.convertToAbstractDataset(it.getValues()[0]);
		if (pos instanceof AbstractCompoundDataset) {
			pos = ((AbstractCompoundDataset) pos).asNonCompoundDataset();
		}
		switch (i) {
		case 0:
			data.fill(Maths.square(pos));
			break;
		case 1:
			data.fill(pos);
			break;
		case 2:
			data.fill(1);
			break;
		default:
			throw new IndexOutOfBoundsException("Parameter index is out of bounds");
		}
	}
}
