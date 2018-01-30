package uk.ac.diamond.scisoft.xpdf.xrmc;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.builder.NexusFileBuilder;
import org.eclipse.dawnsci.nexus.builder.impl.DefaultNexusFileBuilder;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.metadata.IMetadata;

import uk.ac.diamond.scisoft.analysis.diffraction.powder.GammaDeltaPixelIntegrationCache;
import uk.ac.diamond.scisoft.analysis.diffraction.powder.PixelIntegration;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CoordinatesIterator;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.optimize.AbstractOptimizer;
import uk.ac.diamond.scisoft.analysis.optimize.ApacheOptimizer;
import uk.ac.diamond.scisoft.analysis.optimize.ApacheOptimizer.Optimizer;
import uk.ac.diamond.scisoft.xpdf.XPDFDetector;
import uk.ac.diamond.scisoft.xpdf.XPDFSubstance;

public class XRMCEnergyIntegratorExample {

	private enum Parameter {INFILE, XRMCDIR, DEBUGFILE, OUTFILE};
	
	private static Map<Parameter, String> flagStrings;
	
	public static void main(String[] args) {

		// initialize the parameter to flag mapping
		flagStrings = new EnumMap<>(Parameter.class);
		flagStrings.put(Parameter.INFILE, "-i");
		flagStrings.put(Parameter.XRMCDIR, "-p");
		flagStrings.put(Parameter.DEBUGFILE, "-d");
		flagStrings.put(Parameter.OUTFILE, "-o");
		
		
		Map<Parameter, String> parameters = parseArguments(args);
		
//		if (args.length < 3) 
//			usageError();
		
		String inputFileName = parameters.get(Parameter.INFILE);
		String xrmcFilePath = parameters.get(Parameter.XRMCDIR);
		String debugFileName = parameters.get(Parameter.DEBUGFILE);
		String nxFileName = parameters.get(Parameter.OUTFILE);

		if (!hasExtension(inputFileName, "xrmc"))
			inputFileName += ".xrmc";
		if (!hasExtension(debugFileName, "h5") && !hasExtension(debugFileName, "hdf5"))
			debugFileName += ".h5";
		if (!hasExtension(nxFileName, "nxs"))
			nxFileName += ".nxs";

		IDataHolder iDH = null;
		try {
			iDH = LoaderFactory.getData(inputFileName);
		} catch (Exception e) {
			System.err.println("Could not load data from " + inputFileName + ": " + e.toString());
			System.exit(2);
		}
		
		Dataset xrmcData = DatasetUtils.convertToDataset(iDH.getDataset(0));
		
		int[] dataShape = xrmcData.getShape();
		// Here, as in most cases, we only want the final scattering order
		xrmcData = xrmcData.getSliceView(new int[]{dataShape[0]-1, 0, 0, 0}, dataShape, new int[]{1, 1, 1, 1});
		xrmcData.squeeze();

		IMetadata meta = iDH.getMetadata();
		XRMCEnergyIntegrator integrator = new XRMCEnergyIntegrator();
		integrator.setXRMCData(xrmcData);
		
		XPDFDetector det = new XPDFDetector();
		// Assume substance and thickness; not specified by XRMC
		det.setSubstance(new XPDFSubstance("Caesium Iodide", "CsI", 4.51, 1.0));
		det.setThickness(0.5); // mm
		
		// currently assumes detector.dat
		// TODO: search for the actual file that defines 'detectorarray'
		XRMCDetector xdet = new XRMCDetector(xrmcFilePath + "detector.dat");
		det.setSolidAngle(xdet.getSolidAngle());
		det.setEulerAngles(xdet.getEulerAngles());

		// This is totally test code, so I can use this here
		NexusFileFactoryHDF5 factorio = new NexusFileFactoryHDF5();
		NexusFile dFile = factorio.newNexusFile(debugFileName);
		try {
			dFile.createAndOpenToWrite();
		} catch (NexusException nE) {
			System.err.println("Failed to set write (or create) file \"" + dFile.getFilePath() + "\": " + nE.toString() + ". No output will be generated.");
			dFile = null;
		}
		
		Dataset planeData = integrateData(inputFileName, dFile, det, xdet);
		Dataset xyData = planeData.clone();

		int[] shape = planeData.getShape();
		
		Dataset gamma = DatasetFactory.zeros(shape);
		Dataset delta = DatasetFactory.zeros(shape);
		Dataset x = DatasetFactory.zeros(shape);
		Dataset y = DatasetFactory.zeros(shape);
		Dataset z = DatasetFactory.zeros(shape);
		Dataset	phi = DatasetFactory.zeros(shape);
		Dataset tth = DatasetFactory.zeros(shape);
		
		// Get the angles
		for (int i = 0; i < shape[0]; i++) {
			for (int j = 0; j < shape[1]; j++) {
				Vector2d pxPos = new Vector2d(j+0.5, i+0.5);
				Vector2d angles = xdet.anglesFromPixel(pxPos);
				gamma.set(angles.x, i, j);
				delta.set(angles.y, i, j);
				Vector2d phtth = new Vector2d(xdet.polarAnglesFromPixel(pxPos));
				phi.set(phtth.x, i, j);
				tth.set(phtth.y, i, j);
				Vector3d position = xdet.labFromPixel(pxPos);
				x.set(position.x, i, j);
				y.set(position.y, i, j);
				z.set(position.z, i, j);
			}
		}
		
		// Interpolate to a (γ,δ) grid
		double gamma00 = gamma.getDouble(0, 0);
		double gammaNx = gamma.getDouble(0, shape[1]-1);
		double nGamma = shape[0];
		double gammaStep = (gammaNx - gamma00)/(nGamma-1);

		double delta00 = delta.getDouble(shape[0]-1, 0);
		double deltaNx = delta.getDouble(0, 0);
		double nDelta = shape[0];
		double deltaStep = (deltaNx - delta00)/(nDelta-1);

		// Make the axes isotropic at the origin
		double isoStep = Math.min(gammaStep, deltaStep);
		Dataset gammaRange;// = DatasetFactory.createRange(gamma00, gammaNx+isoStep, isoStep);
		Dataset deltaRange = DatasetFactory.createRange(delta00, deltaNx+isoStep, isoStep);
		gammaRange = deltaRange.clone();
		
		GammaDeltaPixelIntegrationCache gdpic = new GammaDeltaPixelIntegrationCache(gamma, delta, gammaRange, deltaRange);
		List<Dataset> piResults = PixelIntegration.integrate(planeData, null, gdpic);
		
		planeData = piResults.get(1);
		
		XRMCBackgroundFunction fit = fitData(planeData, /*det, xdet, */dFile);
		
		Dataset fittedData = expandFit(fit, planeData);
		
		Dataset residual = Maths.subtract(planeData, fittedData);
		Dataset percentage = Maths.divide(planeData, fittedData).isubtract(1.0).imultiply(100);
		
		if (dFile != null) {
			String nodeName = "/fit";
			try {
				GroupNode node = dFile.getGroup("/", true);
				dFile.createData(node, "fit", fittedData);
				dFile.createData(node, "residual", residual);
				dFile.createData(node, "pc.diff", percentage);
				dFile.createData(node, "gamma", gamma);
				dFile.createData(node, "delta", delta);
				dFile.createData(node, "phi", phi);
				dFile.createData(node, "2theta", tth);
				dFile.createData(node, "x", x);
				dFile.createData(node, "y", y);
				dFile.createData(node, "z", z);
				for (int i = 0; i < piResults.size(); i++) {
					dFile.createData(node, "results." + Integer.toString(i), piResults.get(i));
				}
				dFile.createData(node, "xydata", xyData);
			} catch (NexusException nE) {
				System.err.println("Failed to create data on node " + nodeName + ": " + nE.toString() + ". Sorry?");
			}
		}
		
		
		
		if (dFile != null) {
			int nexusCode = 0;
			try {
				nexusCode = dFile.flush();
			} catch (NexusException nE) {
				System.err.println("Could not flush data to file \"" + dFile.getFilePath() + "\": " + nE.toString());
			}
			if (nexusCode == -1) {
				System.err.println("Could not flush data to file \"" + dFile.getFilePath() + "\".");
			}
			
			try {
				dFile.close();
			} catch (NexusException nE) {
				System.err.println("Could not close file \"" + dFile.getFilePath() + "\": " + nE.toString());
			}
		}
		
		System.err.println(fit);
		
		String fitFilename = "/tmp/fitfile.dat";
		// Serialize and output background fit
		try (FileOutputStream outFile = new FileOutputStream(fitFilename)){
			ObjectOutputStream outStream = new ObjectOutputStream(outFile);
			outStream.writeObject(fit);
		} catch (IOException ioe) {
			System.err.println("Error writing fit data: " + ioe.toString());
		}
		
		writeOutputNexus(planeData, gamma, delta, nxFileName);
		
		System.exit(0);
	}
	
	private static Dataset integrateData(String in, NexusFile outFile, XPDFDetector tect, XRMCDetector xdet) {
		
		// Get the XRMC data
		IDataHolder iDH = null;
		try {
			iDH = LoaderFactory.getData(in);
		} catch (Exception e) {
			System.err.println("Error reading file " + in + ": " + e.toString());
			System.exit(2);
		}
		Dataset xrmcData = DatasetUtils.convertToDataset(iDH.getDataset(0));
		int[] dataShape = xrmcData.getShape();
		// Here, as in most cases, we only want the final scattering order
		xrmcData = xrmcData.getSliceView(new int[]{dataShape[0]-1, 0, 0, 0}, dataShape, new int[]{1, 1, 1, 1});
		xrmcData.squeeze();
		
		// Initialize the integrator
		XRMCEnergyIntegrator integrator = new XRMCEnergyIntegrator();
		integrator.setXRMCData(xrmcData);
		integrator.setDetector(tect);
		integrator.setXRMCDetector(xdet);

		Dataset counts = integrator.getDetectorCounts();
		// reuse the data holder to write out the data
		iDH = new DataHolder();
		iDH.addDataset("data", counts);
		// optional output. Output suppressed if the filename is null or the empty String
		if (outFile != null) {
			String rootNodeName = "/";
			GroupNode node = null;
			try {
				node = outFile.getGroup(rootNodeName, false);
			} catch (NexusException nE) {
				System.err.println("Failed to create node \"" + rootNodeName + "\" on file " + outFile.getFilePath() + ": " + nE.toString() + ".");
				node = null;
			}
			
			if (node != null) {
				try {
					outFile.createData(node, "data", counts);
				} catch (NexusException nE) {
					System.err.println("Failed to create data on node " + rootNodeName + ": " + nE.toString() + ". Sorry?");
				}
			}
			
		}
		
		return counts;
		
	}

	private static XRMCBackgroundFunction fitData(Dataset planeData, /*XPDFDetector det, XRMCDetector xdet, */NexusFile outputFile) {
		
		int nx, ny;
		nx = planeData.getShape()[0];
		ny = planeData.getShape()[1];
		// Get the axis aligned datasets: select the central points (1 point for n is odd, 2 for n is even), and average them to a 1D dataset 
		Dataset xAxisData = planeData.getSlice(new int[] {0,  (ny+1)/2-1}, new int[] {nx, ny/2 + 1}, new int[] {1, 1}).mean(1, true);
		Dataset yAxisData = planeData.getSlice(new int[] {(nx+1)/2-1, 0}, new int[] {nx/2 + 1, ny}, new int[] {1, 1}).mean(0, true);
		
		XRMCBackground1D xfit = new XRMCBackground1D();
		XRMCBackground1D yfit = new XRMCBackground1D();
		
		double xbg = (xAxisData.getElementDoubleAbs(0) + xAxisData.getElementDoubleAbs(nx-1))/2; 
		double xamp = 0.5*(xAxisData.getElementDoubleAbs((nx+1)/2) + xAxisData.getElementDoubleAbs(nx/2 + 1)) - xbg;

		double ybg = (yAxisData.getElementDoubleAbs(0) + yAxisData.getElementDoubleAbs(ny-1))/2; 
		double yamp = 0.5*(yAxisData.getElementDoubleAbs((ny+1)/2) + yAxisData.getElementDoubleAbs(ny/2 + 1)) - ybg;

		
		// initialize the 1 D fits
		xfit.setParameterValues(xbg, 0.5*xamp, nx* 0.125, xamp/3, nx*0.25, xamp/6, 3*nx*0.125);
		xfit.setX0(0.5*(nx-1));
		yfit.setParameterValues(ybg, 0.5*yamp, ny* 0.125, yamp/3, ny*0.25, yamp/6, 3*ny*0.125);
		yfit.setX0(0.5*(ny-1));
		
		if (outputFile != null) {
			String nodeName = "";
			try {
				GroupNode node = outputFile.getGroup("/", true);
				nodeName = "xdata";
				outputFile.createData(node, nodeName, xAxisData);
				nodeName = "ydata";
				outputFile.createData(node, nodeName, yAxisData);
			} catch (NexusException nE) {
				System.err.println("Failed to create data on node " + nodeName + ": " + nE.toString() + ". Sorry?");
			}
		}

		AbstractOptimizer nmOptim = new ApacheOptimizer(Optimizer.LEVENBERG_MARQUARDT); 
		
		Dataset xAxis = DatasetFactory.createRange(nx);
		Dataset yAxis = DatasetFactory.createRange(ny);
		Dataset xFitted = DatasetFactory.createRange(nx);
		Dataset yFitted = DatasetFactory.createRange(ny);
		CoordinatesIterator xIter = CoordinatesIterator.createIterator(new int[] {nx}, xAxis);
		CoordinatesIterator yIter = CoordinatesIterator.createIterator(new int[] {ny}, yAxis);
		
		// optimize the 1D backgrounds along each axis.
		try {
				nmOptim.optimize(new IDataset[] {DatasetFactory.createRange(nx)}, xAxisData, xfit);
				nmOptim.optimize(new IDataset[] {DatasetFactory.createRange(ny)}, yAxisData, yfit);

				xfit.fillWithValues((DoubleDataset) xFitted, xIter);
				yfit.fillWithValues((DoubleDataset) yFitted, yIter);
				
		} catch (Exception e) {
			System.err.println("Fitting of xaxis data failed: " + e.toString());
		}

		if (outputFile != null) {
			String nodeName = "";
			try {
				GroupNode node = outputFile.getGroup("/", true);
				nodeName = "xfits";
				outputFile.createData(node, nodeName, xFitted);
				nodeName = "yfits";
				outputFile.createData(node, nodeName, yFitted);
			} catch (NexusException nE) {
				System.err.println("Failed to create data on node " + nodeName + ": " + nE.toString() + ". Sorry?");
			}
		}
		
		XRMCBackgroundFunction fit2d = new XRMCBackgroundFunction(xfit, yfit);
		
		Dataset x2d = DatasetFactory.createRange(ny).getBroadcastView(ny, nx).transpose(1, 0);
		Dataset y2d = DatasetFactory.createRange(nx).getBroadcastView(nx, ny);
		
		try {
			nmOptim.optimize(new IDataset[] {x2d, y2d}, planeData, fit2d);
		} catch (Exception e) {
			System.err.println("Could not optimize 2d fit: " + e.toString());
		}
		return fit2d;
	}
	
	
	private static Dataset expandFit(XRMCBackgroundFunction fit, Dataset data) {
		DoubleDataset fitEvaluation = (DoubleDataset) DatasetFactory.zeros(data);
		
		CoordinatesIterator citer = CoordinatesIterator.createIterator(data.getShape(), DatasetFactory.createRange(data.getShape()[0]), DatasetFactory.createRange(data.getShape()[1]));

		fit.fillWithValues(fitEvaluation, citer);
	
		return fitEvaluation;
	}
	
	private static void errUsageError(String errorString) {
		System.err.println(errorString);
		usageError();
	}
	
	private static void usageError() {
		System.err.println("usage: xrmcintegrator -i input.xrmc -p path/to/input/files.dat -d debug.h5 -o output.nxs");
		System.exit(1);
	}
	
	private static boolean hasExtension(String fileName, String desiredExtension) {
		String[] parts = fileName.split("\\.");
		return parts[parts.length-1].equals(desiredExtension);
	}
	
	private static Map<Parameter, String> parseArguments(String[] args) {
		
		Map<Parameter, String> namedParameters = new EnumMap<>(Parameter.class);
		List<String> argList = new LinkedList<>(Arrays.asList(args));
		
		// usage: xrmcintegrator input.xrmc -p path/to/input/files/ -d debug.h5 -o output.nxs
		// path to XRMC files
		
		putAnArgument(namedParameters, Parameter.XRMCDIR, argList);
		putAnArgument(namedParameters, Parameter.DEBUGFILE, argList);
		putAnArgument(namedParameters, Parameter.OUTFILE, argList);
		putAnArgument(namedParameters, Parameter.INFILE, argList);
		
		
		return namedParameters;
	}
	
	private static void putAnArgument(Map<Parameter, String> parameters, Parameter param, List<String> args) {
		String flagString = flagStrings.get(param);
		int iFlag = args.indexOf(flagString);
		if (iFlag == ArrayUtils.INDEX_NOT_FOUND)
			errUsageError("Flag " + flagString + " parameter not found. Exiting.");
		
		int iValue= iFlag + 1;
		if (iValue >= args.size())
			errUsageError("Value for " + flagString + " parameter not found. Exiting");
		parameters.put(param, args.get(iValue));
		args.remove(iValue); // this order makes sure that the correct Strings
		args.remove(iFlag); // are at the correct addresses.

	}
	
	private static void writeOutputNexus(Dataset gammaDeltaCounts, Dataset gamma, Dataset delta, String fileName) {
		NexusFileBuilder builder = new DefaultNexusFileBuilder(fileName);
		NXroot nxroot = builder.getNXroot();
		
		NXentry nxentry = nxroot.getChildren(NXentry.class).values().toArray(new NXentry[1])[0];
		NXdata data = NexusNodeFactory.createNXdata();

		data.setAttribute(null, "signal", "data");
		data.setAttribute(null, "axes", DatasetFactory.createFromObject(new String[] {"gamma", "delta"}, 2));
		data.setAttribute(null, "gamma_indices", 0);
		data.setAttribute(null, "delta_indices", 1);
		data.setData(gammaDeltaCounts);
		data.setDataset("gamma", gamma);
		data.setDataset("delta", delta);
		nxentry.addGroupNode("data", data);
		
		try {
			builder.createFile(false).close();
		} catch (NexusException nE) {
			System.err.println("Error writing NeXus file: " + nE.toString());
		}
	}
	
}
