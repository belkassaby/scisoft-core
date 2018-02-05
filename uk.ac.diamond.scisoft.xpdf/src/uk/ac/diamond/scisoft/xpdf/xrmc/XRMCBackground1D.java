package uk.ac.diamond.scisoft.xpdf.xrmc;

import org.eclipse.january.dataset.DoubleDataset;

import uk.ac.diamond.scisoft.analysis.fitting.functions.AFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CoordinatesIterator;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Parameter;

public class XRMCBackground1D extends AFunction {

	public static final int C = 0, A1 = 1, G1 = 2, A2 = 3, G2 = 4, A3 = 5, G3 = 6, NPARAMS = 7;
	private double x0;
	
	/**
	 * 20171127
	 */
	private static final long serialVersionUID = 5306569215395858155L;

	public XRMCBackground1D() {
		super(NPARAMS);
		parameters[C] = new Parameter(0.0, -Double.MAX_VALUE, Double.MAX_VALUE);
		parameters[A1] = new Parameter(0.0, -Double.MAX_VALUE, Double.MAX_VALUE);
		parameters[G1] = new Parameter(1.0, 0.125, Double.MAX_VALUE);
		parameters[A2] = new Parameter(0.0, -Double.MAX_VALUE, Double.MAX_VALUE);
		parameters[G2] = new Parameter(1.0, 0.125, Double.MAX_VALUE);
		parameters[A3] = new Parameter(0.0, -Double.MAX_VALUE, Double.MAX_VALUE);
		parameters[G3] = new Parameter(1.0, 0.125, Double.MAX_VALUE);
	}
	
	@Override
	public double val(double... values) {
		if (values.length < 1) throw new IllegalArgumentException("XRMCBackground1D is a one dimensional function.");		
		double x = values[0];
		
		double l1 = evaluateSingleLorentzian(x, x0, parameters[A1].getValue(), parameters[G1].getValue());
		double l2 = evaluateSingleLorentzian(x, x0, parameters[A2].getValue(), parameters[G2].getValue());
		double l3 = evaluateSingleLorentzian(x, x0, parameters[A3].getValue(), parameters[G3].getValue());
		
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
		int bufferIndex = data.get1DIndex(0);
		double[] buffer = data.getData();
		
		while(it.hasNext()) {
			double x = it.getCoordinates()[0];
			double z = parameters[C].getValue();
			z += evaluateSingleLorentzian(x, x0, parameters[A1].getValue(), parameters[G1].getValue());
			z += evaluateSingleLorentzian(x, x0, parameters[A2].getValue(), parameters[G2].getValue());
			z += evaluateSingleLorentzian(x, x0, parameters[A3].getValue(), parameters[G3].getValue());
			
			buffer[bufferIndex] = z;
			bufferIndex++;
		}

	}

	public void setX0(double x0) {
		this.x0 = x0;
	}
	
	public double getX0() {
		return this.x0;
	}
	
	private static double evaluateSingleLorentzian(double x, double x0, double a, double gamma) {
		return a/(1 + square((x-x0)/gamma));
	}

	private static double square(double x) {
		return x*x;
	}
}
