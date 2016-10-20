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
import org.eclipse.dawnsci.analysis.dataset.roi.RingROI;
import org.eclipse.dawnsci.analysis.api.roi.IROI;


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
	@OperationModelField(label = "Start Angle", hint = "A value between zero and 180 degrees, where zero is north and increasing angle is clockwise", fieldPosition = 1)
	private double integrationStartAngle = 135.00;

	// Now the getters and setters
	public double getIntegrationStartAngle() {
		return integrationStartAngle;
	}

	public void setIntegrationStartAngle(double integrationStartAngle) {
		firePropertyChange("IntegrationStartAngle", this.integrationStartAngle, this.integrationStartAngle = integrationStartAngle);
	}


	// Should we be integrating over a half or one Pi radians?
	@OperationModelField(label = "Integration Range", hint = "Integrate over half the ring (Pi) or the whole ring (Two Pi)", fieldPosition = 2)
	private NumberOfPis integrationRange = NumberOfPis.HALF_PI;

	// Now the getters and setters
	public NumberOfPis getIntegrationRange() {
		return integrationRange;
	}

	public void setIntegrationRange(NumberOfPis integrationRange) {
		firePropertyChange("IntegrationRange", this.integrationRange, this.integrationRange = integrationRange);
	}


	// Now let's get the user to tell us where the centre of the beam is and which ring they're interested in evaluating
	public HermanOrientationModel() {
		super();
		setRegion(new RingROI(742, 79, 20, 50));
	}

	public HermanOrientationModel(IROI sector) {
		super();
		setRegion(sector);
	}
	
	
	// Finally, see if the user wants to use a non-standard C value for the calculation
	@OperationModelField(label = "Herman C Value", hint = "Leave this set to 1, unless you have a good reason", fieldPosition = 4)
	private double hermanCValue = 1.00;

	// Now the getters and setters
	public double getHermanCValue() {
		return integrationStartAngle;
	}

	public void setHermanCValue(double hermanCValue) {
		firePropertyChange("IntegrationStartAngle", this.hermanCValue, this.hermanCValue = hermanCValue);
	}

}