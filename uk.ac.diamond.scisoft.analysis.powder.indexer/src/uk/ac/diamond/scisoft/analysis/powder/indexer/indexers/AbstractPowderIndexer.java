package uk.ac.diamond.scisoft.analysis.powder.indexer.indexers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.eclipse.january.dataset.IDataset;

import uk.ac.diamond.scisoft.analysis.powder.indexer.IPowderIndexerParam;
import uk.ac.diamond.scisoft.analysis.powder.indexer.IPowderIndexerPowderParams;

/**
 * 
 *         Abstract class used to create an indexing wrapper for powder
 *         analysis.
 * 
 *         Base requirements for several auto indexing methods.
 * 
 *         Use peakdata points from diffraction pattern to run automatic powder indexing procedure
 * 
 *         Results can be acquired in {@link CellParameter} format.
 *         
 *         TODO: would like a way to force a extension of abstract powder indexer

 * @author Dean P. Ottewell
 */
public abstract class AbstractPowderIndexer implements IPowderIndexer, IPowderIndexerPowderParams {

	protected IDataset peakData;

	protected String indexerOutFilepath;

	protected String outFileTitle;

	protected List<CellParameter> plausibleCells = new ArrayList<CellParameter>();

	protected Map<String, IPowderIndexerParam> parameters = new TreeMap<String, IPowderIndexerParam>();
	
	// A selection of bravais searches that are active required for GSASII
	// 14 lattice searches being respectively
	// 'Tetragonal-I','Tetragonal-P','Orthorhombic-F','Orthorhombic-I','Orthorhombic-C',
	// 'Orthorhombic-P','Monoclinic-C','Monoclinic-P','Triclinic']
	private List<Boolean> activeBravais = Arrays.asList(true, true, true, false, false, false, false, false, false,
			false, false, false, false, false);
	
	//TODO: place above in paramater set
	
	protected static String ID;
	
	public IDataset getPeakData() {
		return peakData;
	}
 
	public void setPeakData(IDataset peakData) {
		this.peakData = peakData;
	}

	public List<CellParameter> getPlausibleCells() {
		return plausibleCells;
	}

	public String getOutFileTitle() {
		return outFileTitle;
	}

	public void setOutFileTitle(String outTitle) {
		this.outFileTitle = outTitle;
	};

	@Override
	public Map<String, IPowderIndexerParam> getParameters() {
		return this.parameters;
	}

	@Override
	public IPowderIndexerParam getParameter(String pName) throws Exception {
		return this.parameters.get(pName);
	}

	@Override
	public void setParameter(IPowderIndexerParam param) throws Exception {
		this.parameters.put(param.getName(), param);
	}
	
	public AbstractPowderIndexer() {
		this.parameters = this.initialParamaters();
	}

}
