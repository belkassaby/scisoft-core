/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.optimize;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IParameter;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;


import uk.ac.diamond.scisoft.analysis.fitting.functions.AFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CoordinatesIterator;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Parameter;

public abstract class AbstractOptimizer implements IOptimizer {

	protected IFunction function;
	protected ArrayList<IParameter> params; // list of free parameters
	protected int n; // number of free parameters
	protected DoubleDataset[] coords;
	protected DoubleDataset data;
	protected DoubleDataset weight;
	protected int paramSetFlag = 0;

	public AbstractOptimizer() {
		params = new ArrayList<IParameter>();
		weight = null;
	}
	
	public int setNumberOfParameters(int d, double[] parameters,IFunction function){
		
		this.function = function;
		n=d;
		try{
			this.initializeParameters1(d);
		}
		catch(ArrayIndexOutOfBoundsException exc){
			
		}
		
		
		if (parameters.length > n) {
			throw new IllegalArgumentException("Number of parameters should match number of unfixed and unique parameters in function");
		}
//		
//		IParameter[] paramArray = new IParameter[parameters.length];
//		
//		for(int k =0; k<parameters.length;k++){
//			double mi = parameters[k];
//			paramArray[k].setValue(mi);
//		}
		
		Parameter p = new Parameter(0);
		
		
		int stuff = params.size();
		
		if (stuff<parameters.length){
			for (int j = 0 ;j<(parameters.length - stuff); j++){
				params.add(p);
				//params.get(stuff+j).setValue(0);
			}
		}
		
		if (d>parameters.length){
			for (int j = 0 ;j<(d - parameters.length); j++){
				params.add(p);
				//params.add(null);
				//params.get((parameters.length) +j).setValue(0);
			}
		}
		
		
		for (int i = 0; i < parameters.length; i++) {
			//System.out.println("Abstract Optimizer setParameters, i:  " + i + "@@@@@@@@@");
			params.get(i).setValue(parameters[i]);
		}
		paramSetFlag =1;
		return n;
	}

	/**
	 * initialize parameters by finding unique and unfixed ones
	 */
	private void initializeParameters() {
		params.clear();
		n = function.getNoOfParameters();
		for (int i = 0; i < n; i++) {
			IParameter p = function.getParameter(i);
			if (p.isFixed())
				continue;

			boolean found = false;
			for (IParameter op : params) {
				if (p == op) {
					found = true;
					break;
				}
			}
			if (!found)
				params.add(p);
		}
		n = params.size();
		
	}
	
	private void initializeParameters1(int n1) {
		params.clear();
		
		for (int i = 0; i < n1; i++) {
			IParameter p = function.getParameter(i);
			if (p.isFixed())
				continue;

			boolean found = false;
			for (IParameter op : params) {
				if (p == op) {
					found = true;
					break;
				}
			}
			if (!found)
				params.add(p);
		}
		n = params.size();
		
	}

	public void setWeight(DoubleDataset weight) {
		this.weight = weight;
	}

	@Override
	public void optimize(IDataset[] coordinates, IDataset data, IFunction function) throws Exception {
		this.function = function;
		int nc = coordinates.length;
		this.coords = new DoubleDataset[nc];
		for (int i = 0; i < nc; i++) {
			coords[i] = (DoubleDataset) DatasetUtils.cast(coordinates[i], Dataset.FLOAT64);
		}

		this.data = (DoubleDataset) DatasetUtils.cast(data, Dataset.FLOAT64);
		if (paramSetFlag == 0){
			initializeParameters();
		}
		else{
		}
		
		int stuff = params.size();
		
		System.out.println(">>>>>>>>>>>Optimizer start parameters:  <<<<<<<<<<<<<<<<");
		
		for (int i = 0; i < stuff; i++) {
			System.out.println("parameter "+ i + " : " + params.get(i));
		}
		
		
		internalOptimize();
	}

	public DoubleDataset[] getCoords() {
		return coords;
	}

	public DoubleDataset getData() {
		return data;
	}

	public IFunction getFunction() {
		return function;
	}

	/**
	 * @return list of unfixed and unique parameters
	 */
	public List<IParameter> getParameters() {
		return params;
	}

	/**
	 * @return array of unfixed and unique parameter values
	 */
	public double[] getParameterValues() {
		double[] values = new double[n];
		for (int i = 0; i < n; i++) {
			values[i] = params.get(i).getValue();
		}
		return values;
	}

	/**
	 * Set parameter values back in function
	 * @param parameters
	 */
	public void setParameterValues(double[] parameters) {
		if (parameters.length > n) {
			throw new IllegalArgumentException("Number of parameters should match number of unfixed and unique parameters in function");
		}
		
//		IParameter[] paramArray = new IParameter[parameters.length];
		
//		for(int k =0; k<parameters.length;k++){
//			paramArray[k].setValue(parameters[k]);
//		}
		
		int stuff = params.size();
		
		if (stuff<parameters.length){
			for (int j = 0 ;j<(parameters.length - stuff); j++){
				params.add(null);
			}
		}
		
		for (int i = 0; i < parameters.length; i++) {
			//System.out.println("Abstract Optimizer setParameters, i:  " + i + "@@@@@@@@@");
			params.get(i).setValue(parameters[i]);
		}
		function.setDirty(true);
	}

	public DoubleDataset calculateValues() {
		return (DoubleDataset) DatasetUtils.cast(function.calculateValues(coords), Dataset.FLOAT64);
	}

	public double calculateResidual(double[] parameters) {
		setParameterValues(parameters);
		return function.residual(true, data, weight, coords);
	}

	public double calculateResidual() {
		return function.residual(true, data, weight, coords);
	}

	private final static double DELTA = 1/256.; // initial value
	private final static double DELTA_FACTOR = 0.25;

	private int indexOfParameter(IParameter parameter) {
		for (int i = 0; i < n; i++) {
			if (parameter == params.get(i))
				return i;
		}
		return -1;
	}

	public double calculateResidualDerivative(IParameter parameter, double[] parameters) {
		if (indexOfParameter(parameter) < 0)
			return 0;

		setParameterValues(parameters);
		CoordinatesIterator it = CoordinatesIterator.createIterator(data == null ? null : data.getShapeRef(), coords);
		DoubleDataset result = new DoubleDataset(it.getShape());

		return calculateNumericalDerivative(1e-15, 1e-9, parameter, result, it);
	}

	private static final double SMALLEST_DELTA = Double.MIN_NORMAL * 1024 * 1024;

	private double calculateNumericalDerivative(double abs, double rel, IParameter parameter, DoubleDataset result, CoordinatesIterator it) {
		double delta = DELTA;
		double previous = evaluateNumericalDerivative(delta, parameter, result, it);
		double current = 0;

		while (delta >= SMALLEST_DELTA) {
			delta *= DELTA_FACTOR;
			current = evaluateNumericalDerivative(delta, parameter, result, it);
			if (Math.abs(current - previous) <= Math.max(abs, rel*Math.max(Math.abs(current), Math.abs(previous))))
				break;
			previous = current;
		}
		if (delta <= SMALLEST_DELTA) {
			System.err.println("Did not converge!");
		}

		return current;
	}

	private double evaluateNumericalDerivative(double delta, IParameter parameter, DoubleDataset result, CoordinatesIterator it) {
		double v = parameter.getValue();
		double dv = delta * (v != 0 ? v : 1);

		double d = 0;
		parameter.setValue(v + dv);
		function.setDirty(true);
		if (function instanceof AFunction) {
			((AFunction) function).fillWithValues(result, it);
			d = data.residual(result, weight, false);
		} else {
			d = function.residual(true, data, weight, coords);
		}

		parameter.setValue(v - dv);
		function.setDirty(true);
		if (function instanceof AFunction) {
			((AFunction) function).fillWithValues(result, it);
			d -= data.residual(result, weight, false);
		} else {
			d -= function.residual(true, data, weight, coords);
		}

		parameter.setValue(v);
		function.setDirty(true);

		return d * 0.5/dv;
	}

	/**
	 * This should use do the work and set the parameters
	 */
	abstract void internalOptimize() throws Exception;
}
