package uk.ac.diamond.scisoft.xpdf.xrmc;

import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.metadata.IMetadata;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.io.TIFFImageSaver;
import uk.ac.diamond.scisoft.analysis.optimize.ApacheOptimizer;
import uk.ac.diamond.scisoft.analysis.optimize.ApacheOptimizer.Optimizer;
import uk.ac.diamond.scisoft.xpdf.XPDFDetector;
import uk.ac.diamond.scisoft.xpdf.XPDFSubstance;

public class XRMCEnergyIntegratorExample {

	public static void main(String[] args) {
		if (args.length < 3) 
			usageError();
		
		String inputFileName = args[0];
		String xrmcFilePath = args[1];
		String outputFileName = args[2];

		if (!hasExtension(inputFileName, "xrmc"))
			inputFileName += ".xrmc";
		if (!hasExtension(outputFileName, "h5") && !hasExtension(outputFileName, "hdf5"))
			outputFileName += ".h5";
		
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
		NexusFile nFile = factorio.newNexusFile(outputFileName);
		try {
			nFile.openToWrite(true);
		} catch (NexusException nE) {
			System.err.println("Failed to set write (or create) file \"" + nFile.getFilePath() + "\": " + nE.toString() + ". No output will be generated.");
			nFile = null;
		}
		
		Dataset planeData = integrateData(inputFileName, nFile, det, xdet);
		
		XRMCBackgroundFunction fit = fitData(planeData, det, xdet, outputFileName);
		
		if (nFile != null) {
			int nexusCode = 0;
			try {
				nexusCode = nFile.flush();
			} catch (NexusException nE) {
				System.err.println("Could not flush data to file \"" + nFile.getFilePath() + "\": " + nE.toString());
			}
			if (nexusCode == -1) {
				System.err.println("Could not flush data to file \"" + nFile.getFilePath() + "\".");
			}
			
			try {
				nFile.close();
			} catch (NexusException nE) {
				System.err.println("Could not close file \"" + nFile.getFilePath() + "\": " + nE.toString());
			}
		}
		
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

	private static XRMCBackgroundFunction fitData(Dataset planeData, XPDFDetector det, XRMCDetector xdet, String outputFileName) {
		
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
		xfit.setParameterValues(new double[] {xbg, 0.5*(nx-1), xamp, nx* 0.125,	2*xamp/3, nx*0.25, xamp/3, 3*nx*0.125});
		yfit.setParameterValues(new double[] {ybg, 0.5*(ny-1), yamp, ny* 0.125,	2*yamp/3, ny*0.25, yamp/3, 3*ny*0.125});
		
		// optimize the 1D backgrounds along each axis.
		try {
			new ApacheOptimizer(Optimizer.SIMPLEX_NM).optimize(new IDataset[] {DatasetFactory.createRange(nx)}, xAxisData, xfit);
			new ApacheOptimizer(Optimizer.SIMPLEX_NM).optimize(new IDataset[] {DatasetFactory.createRange(ny)}, yAxisData, yfit);
		} catch (Exception e) {
			System.err.println("Fitting of xaxis data failed: " + e.toString());
		}
		
		return new XRMCBackgroundFunction(xfit, yfit);
	}
	
	private static void usageError() {
		System.err.println("usage: xrmcintegrator input.xrmc path/to/input/files.dat output.tiff");
		System.exit(1);
	}
	
	private static boolean hasExtension(String fileName, String desiredExtension) {
		String[] parts = fileName.split("\\.");
		return parts[parts.length-1].equals(desiredExtension);
	}
	
	
	
}
