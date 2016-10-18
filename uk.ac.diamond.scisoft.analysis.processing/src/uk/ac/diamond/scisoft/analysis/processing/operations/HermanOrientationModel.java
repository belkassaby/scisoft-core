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

import uk.ac.diamond.scisoft.analysis.processing.operations.externaldata.ExternalDataModel;

public class HermanOrientationModel extends ExternalDataModel {

//	@OperationModelField Things for the UI

	@OperationModelField(label = "Dataset Name", hint = "The name of the dataset or the node path of the data if a .NXS file")
	private String dataName = "";
	@OperationModelField(label = "Start Angle", hint = "A value between zero and 180 degrees, where zero is north and increasing angle is clockwise")
	private double integrationStartAngle = 25;
	@OperationModelField(label = "Integration Range", hint = "Integrate over half the ring (Pi) or the whole ring (Two Pi)")
	private double integrationRange = 25;

	public double getIntegrationStartAngle() {
		return integrationStartAngle;
	}
	public void setIntegrationStartAngle(double integrationStartAngle) {
		firePropertyChange("IntegrationStartAngle", this.integrationStartAngle, this.integrationStartAngle = integrationStartAngle);
	}
	public double getIntegrationRange() {
		return integrationRange;
	}
	public void setIntegrationRange(double integrationRange) {
		firePropertyChange("IntegrationRange", this.integrationRange, this.integrationRange = integrationRange);
	}
	public String getDataName() {
		return dataName;
	}
	public void setDataname(String dataName) {
		firePropertyChange("DataName", this.dataName, this.dataName = dataName);
	}

}
