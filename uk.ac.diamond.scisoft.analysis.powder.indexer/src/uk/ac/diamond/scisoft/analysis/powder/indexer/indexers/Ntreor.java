package uk.ac.diamond.scisoft.analysis.powder.indexer.indexers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.powder.indexer.IPowderIndexerParam;
import uk.ac.diamond.scisoft.analysis.powder.indexer.PowderIndexerParam;
import uk.ac.diamond.scisoft.analysis.powder.indexer.indexers.Dicvol.DicvolParam;
import uk.ac.diamond.scisoft.xpdf.views.CrystalSystem;

/**
 *         Utilises Ntreor indexer
 *
 *         Produce indexed file extensions are *.short. *.imp, *.con Plausible
 *         cells are extracted from *.short
 *
 *         Summary of input line used to Ntreor:
 *
 *         Maximum Volume (A^3): 4000 -VOL is key, default to 4000 Maximum
 *         a,b,c: 35000 -KS ~ Max impurity lines: 1 -believe this result of
 *         limit Min figure-of-merit: -MERIT is key, default 10
 *
 *         Molecular weight: MOLW - depends on DENS use Measured density: DENS =
 *         0 (No density used) Error on density: EDENS - depends on DENS use
 *
 *         See Ntreor documentation for further input file and output file
 *         information.
`1
 * @author Dean P. Ottewell
 */
public class Ntreor extends AbstractPowderIndexerProcess {

	private final Logger logger = LoggerFactory.getLogger(Ntreor.class);

	public static final String ID = "Ntreor";

	private static String CELLFILEINDETIFIER = "The following cell has been selected for refinement by PIRUM:";

	private static String BINNAME = "ntreor_new";

	public Ntreor() {
		binName = BINNAME;
		resultsExtension = ".short";
	}

	public boolean isPeakDataValid(IDataset peakData) {
		boolean isValid = true;

		if (peakData.getSize() < 10) {
			logger.debug("to few data lines to index on");
			isValid = false;
		}

		return isValid;
	}

	public void generateIndexFile(String fullPathFile) {
		try {
			PrintWriter writer = new PrintWriter(fullPathFile, "UTF-8");

			writer.println(outFileTitle);

			// write in the peak data
			for (int i = 0; i < peakData.getSize(); ++i) {
				double d = peakData.getDouble(i);
				writer.println(String.valueOf(d));
			}

			writer.println();

			for (Entry<String, IPowderIndexerParam> entry : parameters.entrySet()) {
				NtreorParam param = (NtreorParam) entry.getValue();
				writer.println(param.formatParam());
			}
			
			// finish file
			writer.println("END*");
			writer.println("0.00");
			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<CellParameter> getResults(String resultFilePath) {

		List<String> rawIndexes = extractRawCellInfo(resultFilePath, CELLFILEINDETIFIER, 3);

		for (String i : rawIndexes) {
			CellParameter cell = new CellParameter();

			String id = "Crystal system: ";
			String crystalSys = i.substring(id.length() + 1, i.length()); // Until
																			// reach
																			// space

			Map<String, Double> raw = extractDataKeyVal(i);

			CrystalSystem system = new CrystalSystem();

			// Extract crystal system indexing found
			cell.setCrystalSystem(system); //TODO: Shouldn't really be having to set this

			//// the cell A, B, C, Alpha, Beta, Gamma
			cell.setUnitCellLengths(raw.get("A"), raw.get("B"), raw.get("C"));
			cell.setUnitCellAngles(raw.get("Alpha"), raw.get("Beta"), raw.get("Gamma"));

			// Extract & set figure of merit
			//double merit = (Double) raw.get("M(20)"));
			cell.setFigureMerit(20);

			plausibleCells.add(cell);
		}

		return this.plausibleCells;
	}

	private Map<String, Double> extractDataKeyVal(String rawCellData) {

		Map<String, Double> cellParam = new HashMap<String, Double>();
		// Cell unit data + merit
		String[] out = rawCellData.split("\\s+");
		for (int i = 0; i < out.length; ++i) {
			if (out[i].equals("=")) {
				cellParam.put(out[i - 1], Double.parseDouble(out[i + 1]));
				++i;
			}
		}
		return cellParam;
	}

	@Override
	public void commsSpecificIndexer(BufferedWriter bw, String path) {
		// Communication need when executing process
		try {
			// General information ?[Y / N, (default=N) ]
			bw.write("N\n");
			// Input file......:
			bw.write(path + "/" + outFileTitle + ".dat" + "\n");
			// Output file.....:
			bw.write(path + "/" + outFileTitle + ".imp" + "\n");
			// Condensed output file.....:
			bw.write(path + "/" + outFileTitle + ".con" + "\n");
			// Short output file ........:
			bw.write(path + "/" + outFileTitle + ".short" + "\n");
			// Theta-shift (irrelevant if LIMIT=1) :
			bw.write("10\n");
			// Do you want to have the possibility to stop the iterated N-TREOR
			// run ?
			// Y makes it possible to avoid the triclinic tests.
			// [Y / N, (default=N) ]
			bw.write("N\n");
			bw.flush();

		} catch (IOException e) {
			logger.debug("Logger was unable to ");
			e.printStackTrace();
		}
	}

	@Override
	public String getResultsDataPath() {
		return filepath + outFileTitle + ".short";
	}

	@Override
	public Map<String, IPowderIndexerParam> initialParamaters() {
		Map<String, IPowderIndexerParam> intialParams = new TreeMap<String, IPowderIndexerParam>();
		intialParams.put("wavelength", new NtreorParam("WAVE", new Double(0.826033)));
		//intialParams.put("volume", new NtreorParam("VOL", new Double(4000)));
		//intialParams.put("limit", new NtreorParam("LIMIT", new Double(1)));
		intialParams.put("merit", new NtreorParam("MERIT", new Double(10)));
		intialParams.put("choice", new NtreorParam("CHOICE", new Double(3)));
		return intialParams;
	}
	
	class NtreorParam extends PowderIndexerParam {

		public NtreorParam(String name, Number value) {
			super(name, value);
		}

		@Override
		public String formatParam() {
			String key = this.getName();
			String value = this.getValue().toString();
			String formated = key + "=" + value + ",";
			return formated; 
		}
		
	}

}
