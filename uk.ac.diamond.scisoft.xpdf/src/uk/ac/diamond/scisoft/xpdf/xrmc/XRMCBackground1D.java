package uk.ac.diamond.scisoft.xpdf.xrmc;

import org.eclipse.january.dataset.DoubleDataset;

import uk.ac.diamond.scisoft.analysis.fitting.functions.AFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CoordinatesIterator;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Parameter;

public class XRMCBackground1D extends AFunction {

	public static final int C = 0, X0 = 1, A1 = 2, G1 = 3, A2 = 4, G2 = 5, A3 = 6, G3 = 7, NPARAMS = 8;

	/**
	 * 20171127
	 */
	private static final long serialVersionUID = 5306569215395858155L;

	public XRMCBackground1D() {
		super(NPARAMS);
		parameters[C] = new Parameter(0.0, -Double.MAX_VALUE, Double.MAX_VALUE);
		parameters[X0] = new Parameter(0.0, -Double.MAX_VALUE, Double.MAX_VALUE);
		parameters[A1] = new Parameter(0.0, -Double.MAX_VALUE, Double.MAX_VALUE);
		parameters[G1] = new Parameter(1.0, 0.0, Double.MAX_VALUE);
		parameters[A2] = new Parameter(0.0, -Double.MAX_VALUE, Double.MAX_VALUE);
		parameters[G2] = new Parameter(1.0, 0.0, Double.MAX_VALUE);
		parameters[A3] = new Parameter(0.0, -Double.MAX_VALUE, Double.MAX_VALUE);
		parameters[G3] = new Parameter(1.0, 0.0, Double.MAX_VALUE);
	}
	
	@Override
	public double val(double... values) {
		if (values.length < 1) throw new IllegalArgumentException("XRMCBackground1D is a one dimensional function.");		
		double x = values[0];
		
		double l1 = evaluateSingleLorentzian(x, parameters[X0].getValue(), parameters[A1].getValue(), parameters[G1].getValue());
		double l2 = evaluateSingleLorentzian(x, parameters[X0].getValue(), parameters[A2].getValue(), parameters[G2].getValue());
		double l3 = evaluateSingleLorentzian(x, parameters[X0].getValue(), parameters[A3].getValue(), parameters[G3].getValue());
		
		return parameters[C].getValue() + l1 + l2 + l3;
	}

	@Override
	protected void setNames() {
		final String theName = "XRMC 1D Background";
		final String theDesc = "A sum of three Lorentzians and a background. Provides an empirical fit along one dimension of an XRMC image.";
		String[] paramNames = new String[] {"c", "x₀", "a₁", "γ₁", "a₂", "γ₂", "a₃", "γ₃"};
		setNames(theName, theDesc, paramNames);
	}

	@Override
	public void fillWithValues(DoubleDataset data, CoordinatesIterator it) {
		it.reset();
		int bufferIndex = 0;
		double[] buffer = data.getData();
		
		while(it.hasNext()) {
			double x = it.getCoordinates()[0];
			double z = parameters[C].getValue();
			z += evaluateSingleLorentzian(x, parameters[X0].getValue(), parameters[A1].getValue(), parameters[G1].getValue());
			z += evaluateSingleLorentzian(x, parameters[X0].getValue(), parameters[A2].getValue(), parameters[G2].getValue());
			z += evaluateSingleLorentzian(x, parameters[X0].getValue(), parameters[A3].getValue(), parameters[G3].getValue());
			
			buffer[bufferIndex] = z;
			bufferIndex++;
		}

	}

	private double evaluateSingleLorentzian(double x, double x0, double a, double gamma) {
		return a/(1 + square((x-x0)/gamma));
	}

	private double square(double x) {
		return x*x;
	}
}

