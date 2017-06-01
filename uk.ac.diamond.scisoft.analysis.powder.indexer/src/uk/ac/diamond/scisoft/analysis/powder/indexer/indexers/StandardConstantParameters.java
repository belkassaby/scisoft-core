package uk.ac.diamond.scisoft.analysis.powder.indexer.indexers;

import java.util.Map;
import java.util.TreeMap;

import uk.ac.diamond.scisoft.analysis.powder.indexer.IPowderIndexerParam;
import uk.ac.diamond.scisoft.analysis.powder.indexer.PowderIndexerParam;
import uk.ac.diamond.scisoft.analysis.powder.indexer.Activator;

public class StandardConstantParameters {

	//TODO: tmp, realyl just need a collection of parameters to go through
	
	public Map<String, IPowderIndexerParam> getStandardParameters(){
		Map<String, IPowderIndexerParam> intialParams = new TreeMap<String, IPowderIndexerParam>();
		intialParams.put("Wavelength", new PowderIndexerParam("wave", 0));
		
		return null;
	}
	
	//Generic constants decided on for the ui..
	public static final String wavelength = "Wavelength";
	
	public static final String maxVolume = "MaxVol";
	
	public static final String maxABC = "MaxABC";
	
	public static final String minFigureMerit = "minFigureMerit";
	
	public static final String cubicSearch = "cubic";
	public static final String monoclinicSearch = "monoclinic";
	public static final String orthorhombicSearch = "orthorhombic";
	public static final String tetragonalSearch = "tetragonal";
	public static final String trigonalSearch = "trigonal";
	public static final String hexagonalSearch = "hexagonal";
	public static final String triclinicSearch = "triclinic";
	
}
