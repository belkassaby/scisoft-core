/*-
 * Copyright 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.fitting.functions;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IParameter;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;

/**
 * Simply a learning exercise for fitting z = (a_0) + (a_1)x+(a_2)y+(a_3)xy
 * This will be inelegant!
 */


public class Plane2D extends AFunction{

	private static final String NAME = "Plane";
	private static final String DESCRIPTION = "Fits a flat plane."
			+ "\n z(y,x) = (a_0)+ (a_1)x+(a_2)y+(a_3)xy ";
	//private static final String[] PARAM_NAMES = new String[]{"c"};
	private transient int nparams;
	
	public Plane2D(){
		super(4);		
	}	
	
	@Override
	protected void setNames() {
		if (isDirty() && nparams < getNoOfParameters()) {
			nparams = getNoOfParameters();
		}
		String[] paramNames = new String[nparams];
		for (int i = 0; i < nparams; i++) {
			paramNames[i] = "a_" + i;
		}

		setNames(NAME, DESCRIPTION, paramNames);
	}
	
	@Override
	public double val(double... values) {
		Dataset[] v = new Dataset[values.length];
		
		for (int i = 0; i < values.length; i++) {
			v[i] = DatasetFactory.createFromObject(values[i]);
		}
		
		DoubleDataset out = calculateValues(v);
		
		return out.get();
	}

	@Override
	public void fillWithValues(DoubleDataset data, CoordinatesIterator it) {
		
		double[] d = getParameterValues();
		it.reset();
		double[] coords = it.getCoordinates();
		int i = 0;
		double[] buffer = data.getData();
		
		while (it.hasNext()) {
			double z = getPlaneValue(coords, d);
			buffer[i++] = z;
		}
		
	}

	private double getPlaneValue(double[] coords, double[] d) {
		double z = d[0] + d[1]*coords[0] + d[2]*coords[1] +d[3]*coords[0]*coords[1];
		return z;
	}

	
	
	
	
	
	
	
	
	
	
	
	
}
