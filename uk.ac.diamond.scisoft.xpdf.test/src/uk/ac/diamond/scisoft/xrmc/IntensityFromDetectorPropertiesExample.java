package uk.ac.diamond.scisoft.xrmc;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.diffraction.DetectorProperties;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.ServiceHolder;
import org.eclipse.dawnsci.nexus.builder.NexusFileBuilder;
import org.eclipse.dawnsci.nexus.builder.impl.DefaultNexusFileBuilder;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.io.NexusDiffractionCalibrationReader;
import uk.ac.diamond.scisoft.xpdf.xrmc.IntensityFromDetectorProperties;

public class IntensityFromDetectorPropertiesExample {

	private enum Parameter {DETFILE, XRMCFILE, OUTFILE};
	
	private static Map<Parameter, String> flagStrings;
	
	public static void main(String[] args) {

		new ServiceHolder().setNexusFileFactory(new NexusFileFactoryHDF5());

		flagStrings = new EnumMap<>(Parameter.class);
		flagStrings.put(Parameter.DETFILE, "-d");
		flagStrings.put(Parameter.XRMCFILE, "-i");
		flagStrings.put(Parameter.OUTFILE, "-o");
		
		Map<Parameter, String> params = parseArguments(args);
		
		String detectorFileName = params.get(Parameter.DETFILE);
		String intensityFileName = params.get(Parameter.XRMCFILE);
		String outputFileName = params.get(Parameter.OUTFILE);
		
		runFromFileNames(detectorFileName, intensityFileName, outputFileName);
		
		System.out.println("Basically done, here");
	}
	
	public static void runFromFileNames(String detectorFileName, String intensityFileName, String outputFileName) {

		// Get the DiffractionMetadata
		IDiffractionMetadata dmeta = null;
		try {
			dmeta = NexusDiffractionCalibrationReader.getDiffractionMetadataFromNexus(detectorFileName, null);
		} catch (DatasetException dSE) {
			System.err.println("Could not get diffraction metadata from " + detectorFileName + ": " + dSE.toString());
		}
		
		IDataHolder iDH = null;
		Dataset intensity = null; 
		Dataset gammaAxis = null;
		Dataset deltaAxis = null;
		try {
			iDH = LoaderFactory.getData(intensityFileName);
			intensity = DatasetUtils.convertToDataset(iDH.getLazyDataset("/entry1/data/data").getSlice());
			gammaAxis = DatasetUtils.convertToDataset(iDH.getLazyDataset("/entry1/data/gamma").getSlice());
			deltaAxis = DatasetUtils.convertToDataset(iDH.getLazyDataset("/entry1/data/delta").getSlice());
		} catch (Exception e) {
			System.err.println("Could not load data from " + intensityFileName + ": " + e.toString());
			System.exit(3); // FIXME: Probably shouldn't sysexit, except from main
		}
		
		Dataset intensityOnDet = runFromDatasets(dmeta.getDetector2DProperties(), intensity, gammaAxis, deltaAxis);

		NexusFileBuilder bob = new DefaultNexusFileBuilder(outputFileName);
		NXentry nxentry;
		try {
			nxentry = bob.newEntry("entry1").getNXentry();
		} catch (NexusException nE) {
			System.err.println("Failed to create new entry with NeXus file builder: " + nE.toString());
			return; //, having written no files
		}
		NXdata outdata = NexusNodeFactory.createNXdata();

		outdata.setAttribute(null, "signal", "data");
		outdata.setData(intensityOnDet);
		nxentry.addGroupNode("data", outdata);
		
		try {
			bob.createFile(false).close();
		} catch (NexusException nE) {
			System.err.println("Error writing NeXus file: " + nE.toString());
		}
	}
	
	public static Dataset runFromDatasets(DetectorProperties dProp, Dataset intensity, Dataset gammaAxis, Dataset deltaAxis) {
		return IntensityFromDetectorProperties.calculate(dProp, intensity, gammaAxis, deltaAxis);
	}
	
	private static Map<Parameter, String> parseArguments(String[] args) {
		
		Map<Parameter, String> namedParameters = new EnumMap<>(Parameter.class);
		List<String> argList = new LinkedList<>(Arrays.asList(args));
		
		// usage: xrmcintegrator input.xrmc -p path/to/input/files/ -d debug.h5 -o output.nxs
		// path to XRMC files
		
		for (Parameter p : Parameter.values())
			putAnArgument(namedParameters, p, argList);
		
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

	private static void errUsageError(String errorString) {
		System.err.println(errorString);
		usageError();
	}
	private static void usageError() {
		System.err.println("usage: intensityxrmcinterpolator -d detector.nxs -i input.nxs -o output.nxs");
		System.exit(1);
	}
}
