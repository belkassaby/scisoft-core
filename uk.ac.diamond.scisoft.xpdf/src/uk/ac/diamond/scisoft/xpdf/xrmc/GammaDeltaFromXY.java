/*-
 * Copyright 2018 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package uk.ac.diamond.scisoft.xpdf.xrmc;

import org.eclipse.january.dataset.Dataset;

public class GammaDeltaFromXY {

	// (γ,δ) values on the x,y grid
	Dataset gammaOnXY;
	Dataset deltaOnXY;
	
	public GammaDeltaFromXY() {
		gammaOnXY = null;
		deltaOnXY = null;
	}
	
	public void setGammaDelta(Dataset gamma, Dataset delta) {
		gammaOnXY = gamma;
		deltaOnXY = delta;
	}
	
	
}
