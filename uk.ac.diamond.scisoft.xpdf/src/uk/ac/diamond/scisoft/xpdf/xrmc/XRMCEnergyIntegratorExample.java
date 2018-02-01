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
import org.eclipse.dawnsci.nexus.ServiceHolder;
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
		
		// Initialize the NeXus file writer
		ServiceHolder.setNexusFileFactory(new NexusFileFactoryHDF5());
		
		Map<Parameter, String> parameters = parseArguments(args);
		
		String inputFileName = parameters.get(Parameter.INFILE);
		String xrmcFilePath = parameters.get(Parameter.XRMCDIR);
		String debugFileName = parameters.get(Parameter.DEBUGFILE);
		String nxFileName = parameters.get(Parameter.OUTFILE);

		if (inputFileName == null)
			errUsageError("No input file specified");
		if (xrmcFilePath == null)
			errUsageError("No XRMC file path specified");
		if (nxFileName == null)
			errUsageError("No NeXus file specified");
		
		if (!hasExtension(inputFileName, "xrmc"))
			inputFileName += ".xrmc";
		if (debugFileName != null && !hasExtension(debugFileName, "h5") && !hasExtension(debugFileName, "hdf5"))
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

		double delta0 = 0;
		// Limits of delta
		for (int i = 0; i < shape[1]; i++) {
			if (Math.abs(delta.getDouble(0, i)) >= Math.abs(gamma.getDouble(0, i)))
				delta0 = Math.abs(delta.getDouble(0, i));
		}
		double delta00 = -delta0;
		double deltaNx = delta0;
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
		
		if (dFile != null) {
			String nodeName = "/fit";
			try {
				GroupNode node = dFile.getGroup("/", true);
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
		
		writeOutputNexus(planeData, gammaRange, deltaRange, nxFileName);
		
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
		
		NXentry nxentry;
		try {
			nxentry = builder.newEntry("entry1").getNXentry();
		} catch (NexusException nE) {
			System.err.println("Failed to create new entry with NeXus file builder: " + nE.toString());
			// return, having written no files
			return;
		}
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
