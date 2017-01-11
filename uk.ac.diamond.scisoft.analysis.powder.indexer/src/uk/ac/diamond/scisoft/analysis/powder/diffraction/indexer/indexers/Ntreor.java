package uk.ac.diamond.scisoft.analysis.powder.diffraction.indexer.indexers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.xpdf.views.CrystalSystem;

/**
 *
 *         Utilises Ntreor indexer
 *
 *         Produce indexed file extensions are *.short. *.imp, *.con Plausible
 *         cells are extracted from *.short
 *
 *         Summary of input line used to Ntreor:
 *
 *         Maximum Volume (A^3): 4000 -VOL is key, default to 4000 Maximum
 *         a,b,c: 35000 -KS ~ Max impuirity lines: 1 -believe this result of
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
public class Ntreor extends AbstractAutoIndexerProcess {

	private final Logger logger = LoggerFactory.getLogger(Ntreor.class);

	public static final String ID = "Ntreor";

	private static String CELLFILEINDETIFIER = "has been reduced into the following one";

	private static String BINNAME = "ntreor_new";

	public Ntreor() {
		binName = BINNAME;
		resultsExtension = ".short";
	}

	public static Map<String, String> stdKeyVals = new HashMap<String, String>() {
		{
			put("WAVE", "0.41328");// put("WAVE","1.5405981");
			// put("VOL", "4000");
			// put("LIMIT", "1");
			// put("MERIT", "10");
			put("CHOICE", "3"); // Defualt??
		}
	};

	public static Map<String, String> getStdKeyVals() {
		return stdKeyVals;
	}

	public static void setStdKeyVals(Map<String, String> stdKeyVals) {
		Ntreor.stdKeyVals = stdKeyVals;
	}

	public boolean isPeakDataValid(IDataset peakData) {
		boolean isValid = true;

		if (peakData.getSize() < 10) {
			logger.debug("to few data lines to index on");
			isValid = false;
		}

		return isValid;
	}

	private void processCmds() {
		Map<String, String> cmdKeys = new HashMap<String, String>();

		// Extract data particular to ntreor
		String dataName = peakData.getName();

		/* Accommodate for ntreor command. place into own containers */
		if (dataName.contains("D_space")) {
			cmdKeys.put("CHOICE", "4");
		} else if (dataName.contains("Theta")) {
			cmdKeys.put("CHOICE", "2");
		} else if (dataName.contains("Two Theta")) {
			cmdKeys.put("CHOICE", "3");
		} else {
			// cmdKeys.put("CHOICE", "3");
		}
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

			for (Entry<String, String> entry : stdKeyVals.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				writer.println(key + "=" + value + ",");
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

		List<String> rawIndexes = extractRawCellInfo(resultFilePath, CELLFILEINDETIFIER, 9);

		for (String i : rawIndexes) {
			if (i == null) {
				String test = "this was null";
			}
			CellParameter cell = new CellParameter();

			String id = "Crystal system: ";
			String crystalSys = i.substring(id.length() + 1, i.length()); // Until
																			// reach
																			// space

			Map<String, Double> raw = extractKeyVal(i);

			CrystalSystem system = new CrystalSystem();

			// Extract crystal system indexing found
			cell.setCrystalSystem(system); // Shouldnt really be having to set
											// this

			//// the cell A, B, C, Alpha, Beta, Gamma
			cell.setUnitCellLengths(raw.get("A"), raw.get("B"), raw.get("C"));
			cell.setUnitCellAngles(raw.get("Alpha"), raw.get("Beta"), raw.get("Gamma"));

			// Extract & set figure of merit
			cell.setFigureMerit((Double) raw.get("M(20)"));

			plausibleCells.add(cell);
		}

		return this.plausibleCells;
	}

	private Map<String, Double> extractKeyVal(String rawCellData) {

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
			bw.write("0\n");
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

}
