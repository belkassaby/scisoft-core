package uk.ac.diamond.scisoft.analysis.powder.indexer.indexers;

import uk.ac.diamond.scisoft.analysis.PythonHelper;
import uk.ac.diamond.scisoft.analysis.PythonHelper.PythonRunInfo;
import uk.ac.diamond.scisoft.analysis.rpc.AnalysisRpcClient;
import uk.ac.diamond.scisoft.xpdf.views.CrystalSystem;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.rpc.AnalysisRpcException;
import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 *         GsasIIWrap call based of xmlrpc. Class acts as a client that
 *         commuincates with the python server that has access to gsasII
 *         indexing procedure. The server file {@link runGSASII.py} to run as
 *         the server.
 * 
 * @author Dean P. Ottewell
 */
public class GsasIIWrap extends AbstractPowderIndexer {

	private static final Logger logger = LoggerFactory.getLogger(GsasIIWrap.class);

	public static final String ID = "GsasII";

	protected URL urlGsasIIPyServer = getClass().getResource("runGSASII.py");

	PythonRunInfo server;

	AnalysisRpcClient analysisRpcClient;

	private static final int PORT = 8715;

	private static final String INDEXING = "INDEXING";

	/*
	 * GSASII parameter set 
	*/
	
	// A selection of bravais searches that are active requried for GSASII
	// 14 lattice searches being respectively
	// 'Tetragonal-I','Tetragonal-P','Orthorhombic-F','Orthorhombic-I','Orthorhombic-C',
	// 'Orthorhombic-P','Monoclinic-C','Monoclinic-P','Triclinic']
	private List<Boolean> activeBravais = Arrays.asList(true, true, true, false, false, false, false, false, false,
			false, false, false, false, false);

	// Controls UNKNOWN_UNUSED,zero=0,ncno = 4 ,volume=25, - these are deafult
	// values
	private List<Double> controls = Arrays.asList(0.0, 0.0, 4.0, 25.0);

	public List<Boolean> getActiveBravais() {
		return activeBravais;
	}

	public void setActiveBravais(List<Boolean> activeBravais) {
		this.activeBravais = activeBravais;
	}

	@Override
	public List<CellParameter> getPlausibleCells() {
		return plausibleCells;
	}

	/**
	 * Destroys the server background python file listening for input.
	 * 
	 * Gives time to ensure ports are freed.
	 * 
	 */
	public void terminatePyServer() {
		if (server != null) {
			if (!server.hasTerminated()) {
				server.terminate();
				try {
					// Give some breadth in letting the server shutdown
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// server.getStdout(true);
			}
		}
	}

	/**
	 * Intialise python server to run in background and listen for requests
	 * 
	 */
	private void setUpServer() {

		try {
			File f = new File(urlGsasIIPyServer.getPath());
			String absPath = f.getAbsolutePath().toString();
			server = PythonHelper.runPythonFileBackground(absPath);

			analysisRpcClient = new AnalysisRpcClient(PORT);

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		} catch (Exception e) {
			logger.debug("Was not able to start gsasII python file" + e);
			terminatePyServer();
			e.printStackTrace();
		}

	}

	private Boolean connectServerSuccesfully() {

		Boolean avaliblePyfile = false;
		try {
			avaliblePyfile = (boolean) analysisRpcClient.request("AVAILABLE", new Object[] {});
		} catch (AnalysisRpcException e) {
			logger.debug("Server failed to respond avaliability" + e);
			terminatePyServer();
			e.printStackTrace();
		}

		return avaliblePyfile;
	}

	private List<CellParameter> extractCellResults(String result) {
		List<CellParameter> pC = new ArrayList<CellParameter>();

		String[] cells = result.split(",");

		for (int val = 0; val < cells.length; ++val) {
			// Create plausible cells
			CellParameter cell = new CellParameter();
			cell.setCrystalSystem(new CrystalSystem());

			Double merit = Double.valueOf(cells[val++]);
			Double a = Double.valueOf(cells[val++]);
			Double b = Double.valueOf(cells[val++]);
			Double c = Double.valueOf(cells[val++]);

			Double alp = Double.valueOf(cells[val++]);
			Double bet = Double.valueOf(cells[val++]);
			Double gam = Double.valueOf(cells[val]);

			cell.setFigureMerit(merit);
			cell.setUnitCellLengths(a, b, c);
			cell.setUnitCellAngles(alp, bet, gam);

			pC.add(cell);
		}

		return pC;
	}

	@Override
	public void configureIndexer() {
		// Call setup and start running the python file in the bg
		setUpServer();
		// Check success of server
		connectServerSuccesfully();
	}

	@Override
	public void runIndexer() {
		try {
			String rawCellResult = (String) analysisRpcClient.request(INDEXING,
					new Object[] { peakData, controls, activeBravais });

			if (rawCellResult.length() > 0)
				plausibleCells = extractCellResults(rawCellResult);

		} catch (AnalysisRpcException e) {
			logger.debug("Unable to request indexing results");
			terminatePyServer();
			e.printStackTrace();
		}

		// TODO: might want a rerun so no need to terminate here
		terminatePyServer();
	}

	@Override
	public void stopIndexer() {
		terminatePyServer();
	}

	@Override
	public String getStatus() {
		String status = null;

		if (!server.hasTerminated())
			status = "Indexing Active";

		return status;
	}

	@Override
	public boolean isPeakDataValid(IDataset peakData) {
		boolean isValid = true;

		if (peakData == null)
			isValid = false;

		return isValid;
	}

}
