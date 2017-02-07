package uk.ac.diamond.scisoft.analysis.powder.indexer.indexers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.january.dataset.IDataset;

/**
 * 
 *         Abstract class used to create an indexing wrapper for powder
 *         analysis.
 * 
 *         Base requirements for several auto indexing methods.
 * 
 *         Use peakdata points from difrraction pattern to run auto indexing
 *         proecdure.
 * 
 *         Results can be acquired in {@link CellParameter} format.

 * @author Dean P. Ottewell
 */
public abstract class AbstractPowderIndexer implements IPowderIndexer {

	protected IDataset peakData;

	protected String indexerOutFilepath;

	protected String outFileTitle;

	protected List<CellParameter> plausibleCells = new ArrayList<CellParameter>();

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
}
