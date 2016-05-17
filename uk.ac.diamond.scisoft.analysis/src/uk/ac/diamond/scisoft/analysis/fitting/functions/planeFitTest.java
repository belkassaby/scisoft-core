/*-
 * Copyright 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.fitting.functions;

import static org.junit.Assert.*;

import org.junit.Test;

public class planeFitTest {

	@Test
	public void testPaneFit(int[][]coords, double[] values, double[] a, int degree) {
		assertEquals(0.5, a[0]);
	}

}
