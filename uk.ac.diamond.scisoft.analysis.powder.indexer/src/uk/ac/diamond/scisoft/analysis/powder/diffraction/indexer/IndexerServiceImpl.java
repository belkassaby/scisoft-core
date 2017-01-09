/*-
 * Copyright 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.powder.diffraction.indexer;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dawnsci.analysis.api.peakfinding.IPeakFinder;
import org.eclipse.dawnsci.analysis.api.peakfinding.IPeakFinderParameter;
import org.eclipse.january.dataset.IDataset;

import uk.ac.diamond.scisoft.analysis.utils.ClassUtils;

public class IndexerServiceImpl implements IndexerService {
	
	private final Map<String, PeakFinderInfo> PEAKFINDERS = new HashMap<String, PeakFinderInfo>();
	
	public IndexerServiceImpl() {
		//Intentionally left blank (OSGi).
	}
	
	/**
	 * Checks whether the PEAKFINDERS is populated and if not tries to fill it.
	 */
	private void checkForPeakFinders() {
		if (!PEAKFINDERS.isEmpty()) return;
		addPeakFindersByExtension();
	}
	
	private void checkForPFID(String pfID) {
		if (PEAKFINDERS.containsKey(pfID)) return;
		throw new IllegalArgumentException(pfID+" is not registered with the peak finding service");
	}
	
	@Override
	public void addPeakFindersByClass(ClassLoader cl, String pakage) 
			throws ClassNotFoundException,IllegalAccessException,InstantiationException {
		final List<Class<?>> clazzes = ClassUtils.getClassesForPackage(cl, pakage);
		for (Class<?> clazz : clazzes) {
			if (Modifier.isAbstract(clazz.getModifiers())) continue;
			if (IPeakFinder.class.isAssignableFrom(clazz)) {
				IPeakFinder pf = (IPeakFinder) clazz.newInstance();
				
				registerPeakFinder(null, pf.getName(), null, pf);
			}
		}
	}
	
	@Override
	public void addPeakFindersByExtension() {
		IConfigurationElement[] elems = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.dawnsci.analysis.api.peakfinder");
		for (IConfigurationElement el: elems) {
			if (el.getName().equals("peakFinder")) {
				final String pfID = el.getAttribute("id");
				final String pfNm = el.getAttribute("name");
				final String pfDesc = el.getAttribute("description");
				IPeakFinder pf = null;
				try {
					pf = (IPeakFinder)el.createExecutableExtension("class");
				} catch (Exception ex) {
					ex.printStackTrace();
					continue;
				}
				
				registerPeakFinder(pfID, pfNm, pfDesc, pf);
			}
		}
	}
	
	private void registerPeakFinder(String pfID, String pfNm, String pfDesc, IPeakFinder pf) {
		//In case we're not working from extension points.
		if (pfID == null) {
			pfID = pf.getClass().getName();
		}
		
		PEAKFINDERS.put(pfID, new PeakFinderInfo(pfNm, pfDesc, pf));
	}
	
	@Override
	public String getPeakFinderName(String pfID) {
		checkForPeakFinders();
		checkForPFID(pfID);
		return PEAKFINDERS.get(pfID).getName();
	}

	@Override
	public Collection<String> getRegisteredPeakFinders() {
		checkForPeakFinders();
		return PEAKFINDERS.keySet();
	}

	@Override
	public Map<String, IPeakFinderParameter> getPeakFinderParameters(String pfID) {
		checkForPeakFinders();
		checkForPFID(pfID);
		IPeakFinder selectedPeakFinder = PEAKFINDERS.get(pfID).getPeakFinder();
		return selectedPeakFinder.getParameters();
	}

	@Override
	public String getPeakFinderDescription(String pfID) {
		checkForPeakFinders();
		checkForPFID(pfID);
		return PEAKFINDERS.get(pfID).getDescription();
	}
	
	private class PeakFinderInfo {
		
		private String name;
		private String description;
		private IPeakFinder peakFinder;
		
		public PeakFinderInfo(String nm, String desc, IPeakFinder pf) {
			this.name = nm;
			this.description = desc;
			this.peakFinder = pf;
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}

		public IPeakFinder getPeakFinder() {
			return peakFinder;
		}
	}
}
