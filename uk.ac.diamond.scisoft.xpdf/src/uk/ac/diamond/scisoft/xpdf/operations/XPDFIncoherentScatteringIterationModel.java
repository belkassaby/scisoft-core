package uk.ac.diamond.scisoft.xpdf.operations;

import org.eclipse.dawnsci.analysis.api.processing.model.AbstractOperationModel;
import org.eclipse.dawnsci.analysis.api.processing.model.OperationModelField;

public class XPDFIncoherentScatteringIterationModel extends AbstractOperationModel {

	@OperationModelField(hint = "Target gain precision theshold", label = "Gain threshold")
	double gainThreshold = 1e-2;
	
	public void setGainThreshold(double gainThreshold) {
		firePropertyChange("gainThreshold", this.gainThreshold, this.gainThreshold = gainThreshold);
	}
	
	public double getGainThreshold() {
		return this.gainThreshold;
	}
}
