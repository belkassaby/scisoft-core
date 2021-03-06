/*-
 * Copyright 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.xpdf.operations;

import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.dawnsci.analysis.api.processing.model.EmptyModel;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperation;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDataset;

/**
 * Abstract class containing the common matters between sample and container 
 * metadata insertion Operations
 * @author Timothy Spain timothy.spain@diamond.ac.uk
 * @since 2015-09-14
 *
 */
abstract public class XPDFInsertComponentMetadataOperation extends AbstractOperation<EmptyModel, OperationData> {

	
//	abstract public 
	
	protected OperationData process(IDataset input, IMonitor monitor) throws OperationException {
		return new OperationData(input);
	}
	@Override
	public String getId() {
		return "uk.ac.diamond.scisoft.xpdf.operations.XPDFInsertComponentMetadataOperation";
	}

	@Override
	public OperationRank getInputRank() {
		return OperationRank.ANY;
	}

	@Override
	public OperationRank getOutputRank() {
		return OperationRank.SAME;
	}

}
