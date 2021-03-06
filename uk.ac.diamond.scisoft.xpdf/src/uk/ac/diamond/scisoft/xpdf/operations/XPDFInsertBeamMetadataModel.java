/*-
 * Copyright 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.xpdf.operations;

import org.eclipse.dawnsci.analysis.api.processing.model.AbstractOperationModel;
import org.eclipse.dawnsci.analysis.api.processing.model.FileType;
import org.eclipse.dawnsci.analysis.api.processing.model.OperationModelField;

public class XPDFInsertBeamMetadataModel extends AbstractOperationModel {

	@OperationModelField(hint="Enter the path to the file", file = FileType.EXISTING_FILE, label = "Data File")
	private String filePath = "/scratch/dawn_diamond_ws/runtime-uk.ac.diamond.dawn.product/data/ceriaXPDF/Empty_I15.xy";
	@OperationModelField(hint="Name of the dataset", dataset = "filePath", label = "Dataset")
	private String dataset = "";
	@OperationModelField(hint="Enter the path to the file containing the error values", file = FileType.EXISTING_FILE, label = "Errors File")
	private String errorFilePath = "";
	@OperationModelField(hint="Name of the dataset containing the errors", dataset = "errorFilePath", label = "Errors Dataset")
	private String errorDataset = "";
	@OperationModelField(hint="Enter the beam energy", label = "Beam Energy", unit = "keV")
	private double beamEnergy = 76.6;
	@OperationModelField(hint="Enter the beam height", label = "Beam Height", unit = "mm")
	private double beamHeight = 0.07;
	@OperationModelField(hint="Enter the beam width", label = "Beam Width", unit = "mm")
	private double beamWidth = 0.07;
	@OperationModelField(hint="Enter the experiment counting time", label = "Counting Time", unit = "s")
	private double countingTime = 60.0;
	@OperationModelField(hint="Enter the relative flux", label = "Monitor Relative Flux")
	private double monitorRelativeFlux = 1.0;
	
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		firePropertyChange("filePath", this.filePath, this.filePath = filePath);
	}

	public String getDataset() {
		return dataset;
	}
	
	public void setDataset(String dataset) {
		firePropertyChange("errorDataset", this.dataset, this.dataset = dataset);
	}
	
	public String getErrorFilePath() {
		return errorFilePath;
	}

	public void setErrorFilePath(String errorFilePath) {
		firePropertyChange("errorFilePath", this.errorFilePath, this.errorFilePath = errorFilePath);
	}

	public String getErrorDataset() {
		return errorDataset;
	}
	
	public void setErrorDataset(String errorDataset) {
		firePropertyChange("errorDataset", this.errorDataset, this.errorDataset = errorDataset);
	}
	
	public double getBeamEnergy() {
		return beamEnergy;
	}

	public void setBeamEnergy(double beamEnergy) {
		firePropertyChange("beamEnergy", this.beamEnergy, this.beamEnergy = beamEnergy);
	}

	public double getBeamHeight() {
		return beamHeight;
	}

	public void setBeamHeight(double beamHeight) {
		firePropertyChange("beamHeight", this.beamHeight, this.beamHeight = beamHeight);
	}

	public double getBeamWidth() {
		return beamWidth;
	}

	public void setBeamWidth(double beamWidth) {
		firePropertyChange("beamWidth", this.beamWidth, this.beamWidth = beamWidth);
	}

	public double getCountingTime() {
		return countingTime;
	}

	public void setCountingTime(double countingTime) {
		firePropertyChange("countingTime", this.countingTime, this.countingTime = countingTime);
	}

	public double getMonitorRelativeFlux() {
		return monitorRelativeFlux;
	}

	public void setMonitorRelativeFlux(double monitorRelativeFlux) {
		firePropertyChange("monitorRelativeFlux", this.monitorRelativeFlux, this.monitorRelativeFlux = monitorRelativeFlux);
	}


}
