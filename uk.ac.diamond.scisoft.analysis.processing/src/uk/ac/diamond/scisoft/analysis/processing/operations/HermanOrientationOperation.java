/*-
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.analysis.processing.operations;

import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperation;
//import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDataset;

//import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
//import uk.ac.diamond.scisoft.analysis.processing.operations.utils.OperationServiceLoader;

public class HermanOrientationOperation extends AbstractOperation<HermanOrientationModel, OperationData> {


	@Override
	public String getId() {
		return "uk.ac.diamond.scisoft.analysis.processing.operations.HermanOrientationOperation";
	}

	@Override
	public OperationData process(IDataset dataset, IMonitor monitor) {
		// get series metadata (will not be null), to check we are from the same
		// parent and whether we have hit the final image
		//SliceFromSeriesMetadata ssm = getSliceSeriesMetadata(dataset);
		OperationData toReturn = new OperationData();
		return toReturn;	
	}

	@Override
	public OperationRank getInputRank() {
		return OperationRank.TWO;
	}

	@Override
	public OperationRank getOutputRank() {
		return OperationRank.TWO;
	}

}
