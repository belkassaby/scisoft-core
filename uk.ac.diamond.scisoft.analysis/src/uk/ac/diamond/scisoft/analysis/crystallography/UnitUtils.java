/*-
 * Copyright 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.crystallography;

import javax.measure.spi.ServiceProvider;

import tec.uom.se.spi.DefaultServiceProvider;

public class UnitUtils {

	public static ServiceProvider getUOMServiceProvider() {
		ServiceProvider serviceProvider = null;
		try {
			serviceProvider = ServiceProvider.current();
		} catch (IllegalStateException e) {
			serviceProvider = new DefaultServiceProvider();
		}
		return serviceProvider;
	}
}
