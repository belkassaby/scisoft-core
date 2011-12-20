/*
 * Copyright © 2011 Diamond Light Source Ltd.
 * Contact :  ScientificSoftware@diamond.ac.uk
 * 
 * This is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 * 
 * This software is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this software. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.scisoft.analysis.rpc.flattening;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * One of three tests that demonstrate creating custom flatteners and unflatteners.
 * <ul>
 * <li> {@link AddHelperSimpleWithInterfaceFlatteningTest} - Flattener for a simple class.
 * <li> {@link AddHelperSpecializedMapFlatteningTest} - Flattener for a class that is a specialisation of a class that
 * flattening is already supported for, in this example a specialised {@link Map}.
 * <li> {@link AddHelperSimpleWithInterfaceFlatteningTest} - Flattener for a simple class. Demonstrates use of
 * {@link IFlattens}.
 * </ul>
 */
public class AddHelperSimpleWithInterfaceFlatteningTest {

	/**
	 * Dummy class that doesn't do anything, only exists to verify flattening and unflattening.
	 * <p>
	 * This class can flatten itself, therefore it implements {@link IFlattens}
	 */
	private static class AddHelperMockClass implements IFlattens {
		public AddHelperMockClass(int value) {
			this.value = value;
		}

		private int value;

		public int getValue() {
			return value;
		}

		@Override
		public Object flatten(IRootFlattener rootFlattener) {
			Map<String, Object> hashMap = new HashMap<String, Object>();
			hashMap.put(IFlattener.TYPE_KEY, AddHelperMockClass.class.getCanonicalName());
			hashMap.put(AddHelperIFlattener.VALUE, getValue());
			return hashMap;
		}
	}

	/**
	 * As this is a helper for a class that self flattens, canFlatten should return false and flatten can throw an
	 * exception indicating a programming error as it will never be called if canFlatten returns false.
	 */
	private static class AddHelperIFlattener implements IFlattener<AddHelperMockClass> {

		private static final String VALUE = "value";

		@Override
		public Object flatten(Object obj, IRootFlattener root) {
			throw new AssertionError();
		}

		@Override
		public boolean canFlatten(Object obj) {
			return false;
		}

		@Override
		public AddHelperMockClass unflatten(Object obj, IRootFlattener root) {
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) obj;
			return new AddHelperMockClass((Integer) map.get(VALUE));
		}

		@Override
		public boolean canUnFlatten(Object obj) {
			if (obj instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, Object> map = (Map<String, Object>) obj;
				if (AddHelperMockClass.class.getCanonicalName().equals(map.get(IFlattener.TYPE_KEY))) {
					return true;
				}
			}
			return false;
		}

	}

	@Test
	public void testAddNewHelper() {
		IRootFlattener root = new RootFlattener();
		// make sure class can flatten because of IFlattens even before we have
		// registered the helper
		Assert.assertTrue(root.canFlatten(new AddHelperMockClass(23)));

		Object flat = root.flatten(new AddHelperMockClass(23));
		// make sure we can't already unflatten before the handler is added
		Assert.assertFalse(root.canUnFlatten(flat));
		
		// add the new helper
		root.addHelper(new AddHelperIFlattener());

		// make sure we can flatten it now
		Assert.assertTrue(root.canUnFlatten(flat));
		Object unflatten = root.unflatten(flat);
		Assert.assertTrue(unflatten instanceof AddHelperMockClass);
		Assert.assertEquals(23, ((AddHelperMockClass) unflatten).getValue());
	}
}
