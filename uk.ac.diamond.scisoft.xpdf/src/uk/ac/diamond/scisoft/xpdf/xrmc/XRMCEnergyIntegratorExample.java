package uk.ac.diamond.scisoft.xpdf.xrmc;

import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.metadata.IMetadata;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.io.TIFFImageSaver;
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
		
//		XRMCBackgroundFunction fit = fitData(planeData, det, xdet, outputFileName);
		
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
			
			//			TIFFImageSaver tiffis = new TIFFImageSaver(out, 32);
//			try {
//				tiffis.saveFile(iDH);
//			} catch (ScanFileHolderException scfe) {
//				System.err.println("Could not save file " + out + ": " + scfe.toString());
//			}
		}
		
		return counts;
		
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
