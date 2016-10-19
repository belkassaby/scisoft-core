/*-
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.processing.operations;

import org.eclipse.dawnsci.analysis.api.processing.model.OperationModelField;
import uk.ac.diamond.scisoft.analysis.processing.operations.IntegrationModel;
import org.eclipse.dawnsci.analysis.api.processing.model.FileType;

// Might not need these later on, let's see...
// import org.eclipse.dawnsci.analysis.api.processing.model.FileType;
// import uk.ac.diamond.scisoft.analysis.processing.operations.HermanOrientationModel.NumberOfPis;


// The model for a DAWN process to perform a Herman Orientation calculation on a given image
public class HermanOrientationModel extends IntegrationModel {


	// Let's give the user a fixed choice on the integration range so they don't go too nuts...
	enum NumberOfPis {
		HALF_PI(1),
		WHOLE_PI(2);
		
		private final int pis;
		
		NumberOfPis(int pis) {
			this.pis = pis;
		}
		
		public int getNumberOfPis() {
			return pis;
		}
		
		@Override
		public String toString() {
			return String.format("%d", this.pis);
		}
	}
	
	
	//@OperationModelField annotations for the UI setup
	// First the start angle of the integration N.B. 0 = North, going clockwise.
	@OperationModelField(label = "Start Angle", hint = "A value between zero and 180 degrees, where zero is north and increasing angle is clockwise")
	private double integrationStartAngle = 0.00;

	// Now the getters and setters
	public double getIntegrationStartAngle() {
		return integrationStartAngle;
	}

	public void setIntegrationStartAngle(double integrationStartAngle) {
		firePropertyChange("IntegrationStartAngle", this.integrationStartAngle, this.integrationStartAngle = integrationStartAngle);
	}


	// Should we be integrating over a half or one Pi radians?
	@OperationModelField(label = "Integration Range", hint = "Integrate over half the ring (Pi) or the whole ring (Two Pi)")
	private NumberOfPis integrationRange = NumberOfPis.WHOLE_PI;

	// Now the getters and setters
	public NumberOfPis getIntegrationRange() {
		return integrationRange;
	}

	public void setIntegrationRange(NumberOfPis integrationRange) {
		firePropertyChange("IntegrationRange", this.integrationRange, this.integrationRange = integrationRange);
	}


	// For the ROI, we can certainly use a file, but can the user do any drawing? How is an ROI file formatted?
	@OperationModelField(hint="The path to the a NeXus file containing a ROI.\nYou can click and drag a file into this field.", file = FileType.EXISTING_FILE, label = "Region of Interest File")
	private String filePath = "";

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		firePropertyChange("filePath", this.filePath, this.filePath = filePath);
	}

}