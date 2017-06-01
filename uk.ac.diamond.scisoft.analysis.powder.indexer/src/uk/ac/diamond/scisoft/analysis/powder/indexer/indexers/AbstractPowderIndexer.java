package uk.ac.diamond.scisoft.analysis.powder.indexer.indexers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.eclipse.january.dataset.IDataset;

import uk.ac.diamond.scisoft.analysis.powder.indexer.IPowderIndexerParam;

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
public abstract class AbstractPowderIndexer implements IPowderIndexer {

	protected IDataset peakData;

	protected String fullIndexerPath;

	protected String outFileTitle;

	protected List<CellParameter> plausibleCells = new ArrayList<CellParameter>();

	protected Map<String, IPowderIndexerParam> parameters = new TreeMap<String, IPowderIndexerParam>();
	
	protected static String ID;

	public AbstractPowderIndexer() {
		this.parameters = this.getInitialParamaters();
		
		
		//Genric name generation?
		this.outFileTitle = "tmpSet";
	}


	//TODO: place active bravais searching in parameter set
	
	public IDataset getPeakData() {
		return peakData;
	}
 
	public void setPeakData(IDataset peakData) {
		this.peakData = peakData;
	}

	public List<CellParameter> getResultCells() {
		return plausibleCells;
	}

	public String getOutFileTitle() {
		return outFileTitle;
	}


	@Override
	public String getIndexerLocation(){
		return fullIndexerPath;
	}
	
	@Override
	public void setIndexerLocation(String fullpath) {
		this.fullIndexerPath = fullpath;
	}

	@Override
	public Boolean isIndexerAvaliable(String identifier) {
		String path = System.getenv(identifier);
		if(path != null){
			setIndexerLocation(path);
			return true;
		}
		return false;
	}

	
	//Standard parameter setup
	
	@Override
	public Map<String, IPowderIndexerParam> getParameters() {
		return this.parameters;
	}
	
	@Override
	public IPowderIndexerParam getParameter(String pName) throws Exception {
		return this.parameters.get(pName);
	}
	
	@Override
	public void setParameter(IPowderIndexerParam param, String paramName) throws Exception {
		this.parameters.put(paramName, param);
	}
	
	
}
