package uk.ac.diamond.scisoft.analysis.processing.operations;

import uk.ac.diamond.scisoft.analysis.processing.model.AbstractOperationModel;
import uk.ac.diamond.scisoft.analysis.roi.IRectangularROI;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

public class RotatedCartesianBoxModel extends AbstractOperationModel {

	private IRectangularROI roi;
	
	public RotatedCartesianBoxModel() {
		roi = new RectangularROI(0,0,10,10,0);
	}

	public IRectangularROI getRoi() {
		return roi;
	}

	public void setRoi(IRectangularROI roi) {
		this.roi = roi;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((roi == null) ? 0 : roi.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RotatedCartesianBoxModel other = (RotatedCartesianBoxModel) obj;
		if (roi == null) {
			if (other.roi != null)
				return false;
		} else if (!roi.equals(other.roi))
			return false;
		return true;
	}
}
