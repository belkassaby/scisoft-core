package uk.ac.diamond.scisoft.xpdf.xrmc;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IParameter;
import org.eclipse.january.dataset.DoubleDataset;

import uk.ac.diamond.scisoft.analysis.fitting.functions.AFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CoordinatesIterator;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Parameter;

public class XRMCBackgroundFunction extends AFunction {

	// Named indices for the parameters
	public static final int C = 0, A1 = 1, GX1 = 2, GY1 = 3, A2 = 4, GX2 = 5, GY2 = 6, A3 = 7, GX3 = 8, GY3 = 9, NPARAMS = 10;
	private double x0;
	private double y0;
	
	/**
	 * 20171120
	 */
	private static final long serialVersionUID = 6510111306118833450L;

	public XRMCBackgroundFunction() {
		super(NPARAMS);
		parameters[C] = new Parameter(0.0, -Double.MAX_VALUE, Double.MAX_VALUE);
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
	
	public XRMCBackgroundFunction(XRMCBackgroundFunction source) {
		this();
		for (int i : new int[] {C, A1, GX1, GY1, A2, GX2, GY2, A3, GX3, GY3}) {
			parameters[i].setValue(source.parameters[i].getValue());
		}
		x0 = source.x0;
		y0 = source.y0;
	}
	
	public XRMCBackgroundFunction(XRMCBackground1D xfit, XRMCBackground1D yfit) {
		this();

		// Now set the derive the parameter values from the x and y xfits
		parameters[C].setValue( 0.5*(xfit.getParameterValue(XRMCBackground1D.C) + yfit.getParameterValue(XRMCBackground1D.C)));

		this.x0 = xfit.getX0();
		this.y0 = yfit.getX0();

		// get the fitted peaks by order of amplitude, lowest to highest
		int[] xOrder = sortedAmplitudeIndices(xfit);
		int[] yOrder = sortedAmplitudeIndices(yfit);
		
		int[] orderedA = new int[] {XRMCBackground1D.A1, XRMCBackground1D.A2, XRMCBackground1D.A3};
		int[] orderedGamma = new int[] {XRMCBackground1D.G1, XRMCBackground1D.G2, XRMCBackground1D.G3};
		
		parameters[A1].setValue( 0.5 * (
				xfit.getParameterValue(orderedA[xOrder[0]]) +
				yfit.getParameterValue(orderedA[yOrder[0]])));
		parameters[GX1].setValue(xfit.getParameterValue(orderedGamma[xOrder[0]]));
		parameters[GY1].setValue(yfit.getParameterValue(orderedGamma[yOrder[0]]));
		
		parameters[A2].setValue( 0.5 * (
				xfit.getParameterValue(orderedA[xOrder[1]]) +
				yfit.getParameterValue(orderedA[yOrder[1]])));
		parameters[GX2].setValue(xfit.getParameterValue(orderedGamma[xOrder[1]]));
		parameters[GY2].setValue(yfit.getParameterValue(orderedGamma[yOrder[1]]));
		
		parameters[A3].setValue( 0.5 * (
				xfit.getParameterValue(orderedA[xOrder[2]]) +
				yfit.getParameterValue(orderedA[yOrder[2]])));
		parameters[GX3].setValue(xfit.getParameterValue(orderedGamma[xOrder[2]]));
		parameters[GY3].setValue(yfit.getParameterValue(orderedGamma[yOrder[2]]));
		
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
		
		double l1 = evaluateSingleLorentzian2D(x, y, x0, y0, parameters[A1].getValue(), parameters[GX1].getValue(), parameters[GY1].getValue());
		double l2 = evaluateSingleLorentzian2D(x, y, x0, y0, parameters[A2].getValue(), parameters[GX2].getValue(), parameters[GY2].getValue());
		double l3 = evaluateSingleLorentzian2D(x, y, x0, y0, parameters[A3].getValue(), parameters[GX3].getValue(), parameters[GY3].getValue());
		
		
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
			z += evaluateSingleLorentzian2D(x, y, x0, y0, parameters[A1].getValue(), parameters[GX1].getValue(), parameters[GY1].getValue());
			z += evaluateSingleLorentzian2D(x, y, x0, y0, parameters[A2].getValue(), parameters[GX2].getValue(), parameters[GY2].getValue());
			z += evaluateSingleLorentzian2D(x, y, x0, y0, parameters[A3].getValue(), parameters[GX3].getValue(), parameters[GY3].getValue());
			
			buffer[bufferIndex] = z;
			bufferIndex++;
		}
		
	}
	
	/**
	 * Normalizes a pixel-based fit to one centred on the beam, with
	 * FWHM (γ, ϝ) values in units of metres.
	 * @param pixelSizeX
	 * 					pixel size in the x direction, in units of metres
	 * @param pixelSizeY
	 * 					pixel size in the y direction, in units of metres
	 * @return
	 * 		A fit equivalent to this one, centred on the beam (x0 = y0 = 0),
	 * 		with FWHM values in metres.
	 */
	public XRMCBackgroundFunction normalizedFit(double pixelSizeX, double pixelSizeY) {
		
		XRMCBackgroundFunction norman = new XRMCBackgroundFunction(this);
		
		// Now fiddle with the relevant parameters
		// Centre on the beam
		norman.x0 = 0.0;
		norman.y0 = 0.0;
		
		// Convert the FWHM to metres
		for (int i : new int[] {GX1, GX2, GX3}) {
			norman.parameters[i].setValue(norman.parameters[i].getValue()*pixelSizeX);
		}
		for (int i : new int[] {GY1, GY2, GY3}) {
			norman.parameters[i].setValue(norman.parameters[i].getValue()*pixelSizeY);
		}
		
		return norman;
	}

	// For reasons of normalization, the 2D Lorentzian is the product
	// of two 1D Lorentzians.
	private double evaluateSingleLorentzian2D(double x, double y, double x0, double y0, double a, double gamma, double digamma) {
		return a/(1 + square((x-x0)/gamma) + square((y-y0)/digamma));
	}
	
	private double square(double x) {
		return x*x;
	}
	
	private int[] sortedIndices3(double[] values) {
		int[] sortedIndices = new int[3];
		DoubleSummaryStatistics stat = Arrays.stream(values).summaryStatistics();
		double min = stat.getMin();
		double max = stat.getMax();
		sortedIndices[0] = ArrayUtils.indexOf(values, min);
		sortedIndices[2] = ArrayUtils.indexOf(values, max);
		sortedIndices[1] = 3 - sortedIndices[0] - sortedIndices[2];
		return sortedIndices;
	}

	private int[] sortedAmplitudeIndices(XRMCBackground1D fit) {
		return sortedIndices3(new double[] {
				fit.getParameter(XRMCBackground1D.A1).getValue(),
				fit.getParameter(XRMCBackground1D.A2).getValue(),
				fit.getParameter(XRMCBackground1D.A3).getValue()
		});
	}
	
}
