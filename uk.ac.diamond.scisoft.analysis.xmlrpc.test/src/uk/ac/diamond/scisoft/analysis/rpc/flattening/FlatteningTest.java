/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.rpc.flattening;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;

public class FlatteningTest extends ExplicitFlatteningTestAbstract {

	@Override
	protected Object doAdditionalWorkOnFlattendForm(Object flat) {
		// Nothing else to do for basic flattening test
		return flat;
	}


	@Test
	public void testGuiBeanReallyGaveMeAGuiBean() {
		// Make sure that we really get a GuiBean out, not just some Map that looks similar to a GuiBean
		Assert.assertTrue(flattener.unflatten(flattenAndCheck(new GuiBean())) instanceof GuiBean);
	}

	// Gui Beans allow some special operations on unflattening, PLOTMODE and PLOTID can be strings and will
	// automatically be converted to the correct type.
	@Test
	public void testGuiBean_PLOTIDAsString() {
		GuiBean bean = new GuiBean();
		bean.put(GuiParameters.PLOTID, UUID.fromString("93dfd804-85ba-4074-afce-d621f7f2aac6"));

		Map<String, Object> flatBean = new HashMap<String, Object>();
		flatBean.put(IFlattener.TYPE_KEY, GuiBean.class.getCanonicalName());
		flatBean.put(GuiParameters.PLOTID.toString(), "93dfd804-85ba-4074-afce-d621f7f2aac6");
		Object unflatten = flattener.unflatten(flatBean);

		assertFlattenEquals(bean, unflatten);
	}

	@Test
	public void testGuiBean_PLOTMODEAsString() {
		GuiBean bean = new GuiBean();
		bean.put(GuiParameters.PLOTMODE, GuiPlotMode.MULTI2D);

		Map<String, Object> flatBean = new HashMap<String, Object>();
		flatBean.put(IFlattener.TYPE_KEY, GuiBean.class.getCanonicalName());
		flatBean.put(GuiParameters.PLOTMODE.toString(), GuiPlotMode.MULTI2D.toString());
		Object unflatten = flattener.unflatten(flatBean);

		assertFlattenEquals(bean, unflatten);
	}

	@Test
	public void testGuiBean_keysAsString() {
		GuiBean bean = new GuiBean();
		bean.put(GuiParameters.PLOTMODE, GuiPlotMode.MULTI2D);
		bean.put(GuiParameters.TITLE, "My amazing plot");

		Map<String, Object> flatBean = new HashMap<String, Object>();
		flatBean.put(IFlattener.TYPE_KEY, GuiBean.class.getCanonicalName());
		flatBean.put(GuiParameters.PLOTMODE.toString(), GuiPlotMode.MULTI2D.toString());
		flatBean.put("Title", "My amazing plot");
		Object unflatten = flattener.unflatten(flatBean);

		assertFlattenEquals(bean, unflatten);
	}

	@Test
	public void testCheckFlattanableFalse() {
		Assert.assertFalse(flattener.canFlatten(new Object()));
	}

	@Test
	public void testCheckUnFlattanableFalse() {
		Assert.assertFalse(flattener.canUnFlatten(new Object()));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testFlattanableUnsupported() {
		flattener.flatten(new Object());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testUnFlattanableUnsupported() {
		flattener.unflatten(new Object());
	}
}
