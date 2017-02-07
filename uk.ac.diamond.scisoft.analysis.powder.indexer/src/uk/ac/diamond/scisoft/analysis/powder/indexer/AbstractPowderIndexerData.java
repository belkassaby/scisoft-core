package uk.ac.diamond.scisoft.analysis.powder.indexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.dawnsci.analysis.api.peakfinding.IPeakFinderParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO: in many ways this is abstract containing basic essientials of a powderindexerdata
 * ...wavelength only essentials
 * 
 * TODO: order of list matters...
 * 
 * @author Dean P. Ottewell
 *
 */
public abstract class AbstractPowderIndexerData implements IPowderIndexerData {

	/* Add logging facility */
	protected static transient final Logger logger = LoggerFactory.getLogger(AbstractPowderIndexerData.class);
	
	protected String indexerName;
	private double wavelength;
	private List<IPowderIndexerParam> indexerParams = new ArrayList<IPowderIndexerParam>();
	
	
	protected AbstractPowderIndexerData() {
		//TODO: wait to pass themselves...
		indexerParams.add(new PowderIndexerParam("Wavelength", this.wavelength));
	}
	
	protected abstract void setName();

	protected abstract String getFormatedIndexerParam(IPowderIndexerParam param);
	
	protected void setWavelength(double wavelength){
		this.wavelength = wavelength;
	}
	
	protected double getWavelength(){
		return this.wavelength;
	}
	
	@Override
	public String getIndexerName() {
		return this.indexerName;
	}

	@Override
	public List<IPowderIndexerParam> getParameters() {
		return indexerParams;
	}

	@Override
	public IPowderIndexerParam getParameter(String pName) throws Exception {
		//TODO: find param... maybe
		return null;
	}

	@Override
	public void setParameter(IPowderIndexerParam param) throws Exception {
		indexerParams.add(param);
	}

}
