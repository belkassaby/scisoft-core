package uk.ac.diamond.scisoft.xpdf.xrmc;

import org.eclipse.dawnsci.analysis.api.fitting.functions.IOperator;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IParameter;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;

import uk.ac.diamond.scisoft.analysis.fitting.functions.AFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CoordinatesIterator;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Parameter;

public class XRMCBackgroundFunction extends AFunction {

	// Named indices for the parameters
	private static final int C = 0, X0 = 2, Y0 = 2, A1 = 3, GX1 = 4, GY1 = 5, A2 = 6, GX2 = 7, GY2 = 8, A3 = 9, GX3 = 10, GY3 = 11, NPARAMS = 12;
	
	
	/**
	 * 20171120
	 */
	private static final long serialVersionUID = 6510111306118833450L;

	public XRMCBackgroundFunction() {
		super(NPARAMS);
		parameters[C] = new Parameter(0.0, -Double.MAX_VALUE, Double.MAX_VALUE);
		parameters[X0] = new Parameter(0.0, -Double.MAX_VALUE, Double.MAX_VALUE);
		parameters[Y0] = new Parameter(0.0, -Double.MAX_VALUE, Double.MAX_VALUE);
		parameters[A1] = new Parameter(0.0, -Double.MAX_VALUE, Double.MAX_VALUE);
		parameters[GX1] = new Parameter(1.0, 0.0, Double.MAX_VALUE);
		parameters[GY1] = new Parameter(1.0, 0.0, Double.MAX_VALUE);
		parameters[A2] = new Parameter(0.0, -Double.MAX_VALUE, Double.MAX_VALUE);
		parameters[GX2] = new Parameter(1.0, 0.0, Double.MAX_VALUE);
		parameters[GY2] = new Parameter(1.0, 0.0, Double.MAX_VALUE);
		parameters[A3] = new Parameter(0.0, -Double.MAX_VALUE, Double.MAX_VALUE);
		parameters[GX3] = new Parameter(1.0, 0.0, Double.MAX_VALUE);
		parameters[GY3] = new Parameter(1.0, 0.0, Double.MAX_VALUE);
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String newName) {
		name = newName;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String newDescription) {
		description = newDescription;
	}

	@Override
	public IParameter getParameter(int index) {
		return parameters[index];
	}

	@Override
	public IParameter[] getParameters() {
		return parameters;
	}

	@Override
	public int getNoOfParameters() {
		return NPARAMS;
	}

	@Override
	public double getParameterValue(int index) {
		return parameters[index].getValue();
	}

	@Override
	public void setParameter(int index, IParameter parameter) {
		parameters[index] = parameter;
	}

	@Override
	public void setParameterValues(double... params) {
		for (int i  = 0; i < params.length; i++) {
			parameters[i].setValue(params[i]);
		}
	}

	@Override
	public double val(double... values) {
		if (values.length < 2) throw new IllegalArgumentException("XRMCBackground is a two dimensional function");
		double x = values[0];
		double y = values[1];
		
		double l1 = evaluateSingleLorentzian2D(x, y, parameters[X0].getValue(), parameters[Y0].getValue(), parameters[A1].getValue(), parameters[GX1].getValue(), parameters[GY1].getValue());
		double l2 = evaluateSingleLorentzian2D(x, y, parameters[X0].getValue(), parameters[Y0].getValue(), parameters[A2].getValue(), parameters[GX2].getValue(), parameters[GY2].getValue());
		double l3 = evaluateSingleLorentzian2D(x, y, parameters[X0].getValue(), parameters[Y0].getValue(), parameters[A3].getValue(), parameters[GX3].getValue(), parameters[GY3].getValue());
		
		
		return parameters[C].getValue() + l1 + l2 + l3;
	}

	@Override
	protected void setNames() {
		final String theName = "XRMC Background";
		final String theDesc = "A sum of three two-dimensional Lorentzians and a background. Provides an empirical fit to XRMC images.";
		// Set up the parameters
		String[] paramNames = new String[]{"c", "x₀", "y₀", "a₁", "γ₁", "ϝ₁", "a₂", "γ₂", "ϝ₂", "a₃", "γ₃", "ϝ₃"};
		setNames(theName, theDesc, paramNames);
	}

	@Override
	public void fillWithValues(DoubleDataset data, CoordinatesIterator it) {
		
		it.reset();
		int bufferIndex = 0;
		double[] buffer = data.getData();
		
		while(it.hasNext()) {
			double x = it.getCoordinates()[0];
			double y = it.getCoordinates()[1];
			double z = parameters[C].getValue();
			z += evaluateSingleLorentzian2D(x, y, 0, 0, parameters[A1].getValue(), parameters[GX1].getValue(), parameters[GY1].getValue());
			z += evaluateSingleLorentzian2D(x, y, 0, 0, parameters[A2].getValue(), parameters[GX2].getValue(), parameters[GY2].getValue());
			z += evaluateSingleLorentzian2D(x, y, 0, 0, parameters[A3].getValue(), parameters[GX3].getValue(), parameters[GY3].getValue());
			
			buffer[bufferIndex] = z;
			bufferIndex++;
		}
		
	}

	// For reasons of normalization, the 2D Lorentzian is the product
	// of two 1D Lorentzians.
	private double evaluateSingleLorentzian2D(double x, double y, double x0, double y0, double a, double gamma, double digamma) {
		return a/(1 + square((x-x0)/gamma))/(1 + square((y-y0)/digamma));
	}
	
	private double square(double x) {
		return x*x;
	}
	
}
