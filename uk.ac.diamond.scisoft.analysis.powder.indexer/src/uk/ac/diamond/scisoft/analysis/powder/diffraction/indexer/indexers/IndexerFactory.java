package uk.ac.diamond.scisoft.analysis.powder.diffraction.indexer.indexers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 *         Basic factory made for swapping out different indexers by on the
 *         indexers ID
 * 
 * @author Dean P. Ottewell
 */
public class IndexerFactory {

	private static final Logger logger = LoggerFactory.getLogger(IndexerFactory.class);

	/**
	 * Creates a instance of corresponding indexer based on the unqiue ID for each idexer
	 * 
	 * @param ID
	 * @return auto-indexer 
	 */
	public static AbstractAutoIndexer createIndexer(String ID) {

		if (ID.equals(Dicvol.ID))
			return new Dicvol();
		if (ID.equals(Ntreor.ID))
			return new Ntreor();
		if (ID.equals(GsasIIWrap.ID))
			return new GsasIIWrap();
		else
			logger.debug("INVALID ID");
		return null;
	}

}
