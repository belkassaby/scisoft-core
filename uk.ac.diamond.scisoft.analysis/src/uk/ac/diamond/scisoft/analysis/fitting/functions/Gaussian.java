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

import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;


/**
 * Class which expands on the AFunction class to give the properties of a gaussian. A 1D implementation
 */
public class Gaussian extends APeak implements IPeak {
	private static final String NAME = "Gaussian";
	private static final String DESC = "y(x) = A exp(-((x-b)^2)/(2*c^2))";
	private static final String[] PARAM_NAMES = new String[]{"posn", "fwhm", "area"};
	private static final double[] PARAMS = new double[]{0, 0, 0};

	public Gaussian() {
		this(PARAMS);
	}

	/**
	 * Constructor which takes the three properties required, which are
	 * 
	 * <pre>
	 *     Parameter 1	- Position
	 *     Parameter 2 	- FWHM (full width at half maximum)
	 *     Parameter 3 	- Area
	 * </pre>
	 * 
	 * @param params
	 */
	public Gaussian(double... params) {
		super(PARAMS.length);
		// make sure that there are 3 parameters, otherwise, throw a sensible error
		if (params.length != PARAMS.length) 
			throw new IllegalArgumentException("A gaussian peak requires 3 parameters, and it has only been given "+params.length);
		fillParameters(params);
		getParameter(FWHM).setLowerLimit(0.0);

		setNames();
	}

	public Gaussian(IParameter... params) {
		super(PARAMS.length);
		if (params.length != PARAMS.length) 
			throw new IllegalArgumentException("A gaussian peak requires 3 parameters, and it has only been given "+params.length);
		fillParameters(params);
		getParameter(FWHM).setLowerLimit(0.0);

		setNames();
	}

	public Gaussian(IdentifiedPeak peakParameters) {
		super(PARAMS.length); 
		double range = peakParameters.getMaxXVal() - peakParameters.getMinXVal();
		double fwhm2 = peakParameters.getFWHM() * 2;
		double pos = peakParameters.getPos();
		double maxArea = peakParameters.getHeight() * range * 4;

		IParameter p;
		p = getParameter(POSN);
		p.setValue(pos);
		p.setLimits(pos - fwhm2, pos + fwhm2);

		p = getParameter(FWHM);
		p.setLimits(0, range*2);
		p.setValue(peakParameters.getFWHM() / 2);
		
		p = getParameter(AREA);
		p.setLimits(-maxArea, maxArea);
		p.setValue(peakParameters.getArea() / 2); // area better fitting is generally found if sigma expands into the peak.

		setNames();
	}

	/**
	 * Constructor which takes more sensible values for the parameters, which also incorporates the limits which they
	 * can be in, reducing the overall complexity of the problem
	 * 
	 * @param minPeakPosition
	 *            The minimum value the peak position of the Gaussian
	 * @param maxPeakPosition
	 *            The maximum value of the peak position
	 * @param maxFWHM
	 *            Full width at half maximum
	 * @param maxArea
	 *            The maximum area of the peak
	 */
	public Gaussian(double minPeakPosition, double maxPeakPosition, double maxFWHM, double maxArea) {
		super(PARAMS.length);

		internalSetPeakParameters(minPeakPosition, maxPeakPosition, maxFWHM, maxArea);

		setNames();
	}

	private void setNames() {
		name = NAME;
		description = DESC;
		for (int i = 0; i < PARAM_NAMES.length; i++) {
			IParameter p = getParameter(i);
			p.setName(PARAM_NAMES[i]);
		}
	}

	private static final double CONST = Math.sqrt(4. * Math.log(2.));

	double pos, sigma;

	@Override
	protected void calcCachedParameters() {
		pos = getParameterValue(POSN);
		sigma = getParameterValue(FWHM) / CONST;
		double area = getParameterValue(AREA);
		height = area / (Math.sqrt(Math.PI) * sigma);

		setDirty(false);
	}

	@Override
	public double val(double... values) {
		if (isDirty())
			calcCachedParameters();

		double arg = (values[0] - pos) / sigma; 

		return height * Math.exp(- arg * arg);
	}

	@Override
	public void fillWithValues(DoubleDataset data, CoordinatesIterator it) {
		if (isDirty())
			calcCachedParameters();

		double[] coords = it.getCoordinates();
		int i = 0;
		double[] buffer = data.getData();
		while (it.hasNext()) {
			double arg = (coords[0] - pos) / sigma; 

			buffer[i++] = height * Math.exp(- arg * arg);
		}
	}
}
