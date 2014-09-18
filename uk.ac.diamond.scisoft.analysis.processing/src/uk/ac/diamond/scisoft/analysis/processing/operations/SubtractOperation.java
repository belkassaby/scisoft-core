package uk.ac.diamond.scisoft.analysis.processing.operations;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;

/**
 * Subtracts either one dataset from another or a scalar value from all values of a dataset.
 * @author fcp94556
 *
 */
public class SubtractOperation extends AbstractMathsOperation {

	protected IDataset operation(IDataset a, Object value) {
		return ((Dataset)a).isubtract(value);
	}

	@Override
	public String getId() {
		return "uk.ac.diamond.scisoft.analysis.processing.subtractOperation";
	}
	@Override
    public String getName() {
		return "Subtract datasets";
	}
}