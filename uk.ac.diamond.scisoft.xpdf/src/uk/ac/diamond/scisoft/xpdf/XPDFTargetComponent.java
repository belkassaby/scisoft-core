/*-
 * Copyright 2015 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.xpdf;

import java.util.List;

import org.eclipse.dawnsci.nexus.NXsample;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.Maths;

/**
 * The properties of any component of the beam target, including both the
 * sample and it containers.
 * @author Timothy Spain timothy.spain@diamond.ac.uk
 * @since 2015-09-14
 */
public class XPDFTargetComponent {

	private String name;
	private XPDFComponentForm form;
	private boolean isSample;
	private double c3;
	
	/**
	 * Empty constructor.
	 */
	public XPDFTargetComponent() {
		this.name="";
		this.form=null;
		this.isSample=false;
		this.c3 = 1.0;
	}

	/**
	 * Copy constructor.
	 * @param inComp
	 * 				component to be copied.
	 */
	public XPDFTargetComponent(XPDFTargetComponent inComp) {
		this.name = inComp.name;
		this.form = inComp.form != null ? new XPDFComponentForm(inComp.form) : null;
		this.isSample = inComp.isSample;
		this.c3 = inComp.c3;
	}
	
	/**
	 * Constructor from NXsample.
	 * <p>
	 * Constructor for NeXus objects that do not have geometry information
	 * @param nxample
	 * 				object describing the contents of the NeXus file 
	 * @param geom
	 * 			geometry of the component
	 */
	public XPDFTargetComponent(NXsample nxample, XPDFComponentGeometry geom) {
		this.name = nxample.getNameScalar();
		this.form = new XPDFComponentForm(nxample, geom);
	}
	
	/**
	 * Setter for the name.
	 * @param name
	 * 			name to be given to the component.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Setter for the form of the component.
	 * @param form
	 * 			form to be given to the component.
	 */
	public void setForm(XPDFComponentForm form) {
		this.form = form;
	}

	/**
	 * Setter for the isSample boolean.
	 * @param isSample
	 * 				is this the sample?
	 */
	public void setSample(boolean isSample) {
		this.isSample = isSample;
	}

	/**
	 * Getter for the component name.
	 * @return the component name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Getter for the form.
	 * @return the form of the component.
	 */
	public XPDFComponentForm getForm() {
		return form;
	}

	/**
	 * Returns whether this component is the sample.
	 * @return a boolean as to whether this component is the sample.
	 */
	public boolean isSample() {
		return isSample;
	}

	/**
	 * Returns the Krogh-Moe sum for its material.
	 * @return the Krogh-Moe sum of this component.
	 */
	public double getKroghMoeSum() {
		return form.getKroghMoeSum();
	}

	/**
	 * Returns the self-scattering of the material.
	 * @param coordinates
	 * 					the beam energies and scattering angles to be considered.
	 * @return the self scattering attenuation.
	 */
	public Dataset getSelfScattering(XPDFCoordinates coordinates) {

		Dataset selfScattering = form.getSubstance().getComposition().getSelfScattering(coordinates);
		return selfScattering;
	}

	/**
	 * Returns the summed squared elastic scattering form factor.
	 * @param coords
	 * 				the beam energies and scattering angles to be considered.
	 * @return the summed squared elastic scattering form factor of the atoms
	 * 			in the material.
	 */
	public Dataset getFSquared(XPDFCoordinates coords) {
		return form.getSubstance().getComposition().getElasticScatteringFactorSquared(coords.getX());
	}

	/**
	 * Returns the number density of the material that makes up the component.
	 * @return the number density in 1/Å³
	 */
	public double getNumberDensity() {
		return form.getSubstance().getNumberDensity();
	}

	/**
	 * Returns the number density of the element with atomic number z.
	 * <p>
	 * Given an atomic number, <code>z</code>, this method returns the number
	 * density of that element within this component. If the component contains
	 * none of that element, then the number density therein is necessarily
	 * zero. Pass through to <code>XPDFSubstance.getNumberDensity(int)</code>.
	 * @param z
	 * 			the atomic number to query the density of.
	 * @return the number density in 1/Å³
	 */
	public double getNumberDensity(Integer z) {
		return form.getSubstance().getNumberDensity(z);
	}

	/**
	 * Returns the g0-1 factor for the material that makes up the component.
	 * @return g0-1
	 */
	public double getG0Minus1() {
		return form.getSubstance().getG0Minus1();
	}
	
	/**
	 * Returns the fluorescence data for the 5 strongest fluorescent lines.
	 * @param energy
	 * 				energy of the exciting beam in keV.
	 * @return the parameters of the fluorescences.
	 */
	public List<XPDFFluorescentLine> getFluorescences(double energy) {
		return getFluorescences(energy, 5);
	}

	/**
	 * Returns the fluorescence data for the 5 strongest fluorescent lines.
	 * @param energy
	 * 				energy of the exciting beam in keV.
	 * @param nLines
	 * 				the maximum number of fluorescent lines to return. 
	 * @return the parameters of the fluorescences.
	 */
	private List<XPDFFluorescentLine> getFluorescences(double energy, int nLines) {
		return form.getSubstance().getComposition().getFluorescences(energy, nLines);
	}
}
