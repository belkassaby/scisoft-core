
package uk.ac.diamond.scisoft.analysis.powder.indexer;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.peakfinding.IPeakFinderParameter;

public interface IPowderIndexerData {
	
	public String getIndexerName();
	
	public List<IPowderIndexerParam> getParameters();
	
	public IPowderIndexerParam  getParameter(String pName) throws Exception;
	
	public void setParameter(IPowderIndexerParam param) throws Exception;
}
