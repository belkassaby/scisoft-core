package uk.ac.diamond.scisoft.analysis.powder.indexer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dawnsci.analysis.api.peakfinding.IPeakFinder;

import uk.ac.diamond.scisoft.analysis.powder.indexer.indexers.IPowderIndexer;

public class PowderIndexerServiceImpl implements IPowderIndexerService {

	private final Map<String, PowderIndexerInfo> INDEXERSLOADED= new HashMap<String, PowderIndexerInfo>();
	
	@Override
	public String getIndexerName(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<String> getRegisteredIndexers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, IPowderIndexerParam> getIndexerParameters(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addIndexersByClass(ClassLoader cl, String pakage)
			throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addPeakFindersByExtension() {
		IConfigurationElement[] elems = Platform.getExtensionRegistry().getConfigurationElementsFor("uk.ac.diamond.scisoft.analysis.powder.indexer");
		for (IConfigurationElement el: elems) {
			if (el.getName().equals("PowderIndexer")) {
				final String indexerID = el.getAttribute("id");
				final String indexerDesc = el.getAttribute("description");
				IPowderIndexer indexer = null;
				try {
					indexer = (IPowderIndexer)el.createExecutableExtension("class");
				} catch (Exception ex) {
					ex.printStackTrace();
					continue;
				}
				
				registerIndexer(indexerID, indexerDesc, indexer);
			}
		}
	}
	
	private void registerIndexer(String pfID, String pfDesc, IPowderIndexer indexer) {
		//In case we're not working from extension points.
		if (pfID == null) {
			pfID = indexer.getClass().getName();
		}
		INDEXERSLOADED.put(pfID, new PowderIndexerInfo(pfDesc, indexer));
	}
	

	private class PowderIndexerInfo {
		
		private String name;
		private String description;
		private IPowderIndexer indexer;
		
		public PowderIndexerInfo(String desc, IPowderIndexer indexer) {
			this.description = desc;
			this.indexer = indexer;
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}

		public IPowderIndexer getPeakFinder() {
			return indexer;
		}
	}

	
}
