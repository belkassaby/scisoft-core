package uk.ac.diamond.scisoft.xrmc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import uk.ac.diamond.scisoft.xpdf.xrmc.XRMCSpectrum;

public class SpectrumTest {
	
	private XRMCSpectrum initTestSpectrum() {
		String[] lines = ("Newdevice spectrum		; Device type\n" + 
				"Spectrum		; Device name\n" + 
				"PolarizedFlag 1		; unpolarized/polarized beam (0/1)\n" + 
				"LoopFlag 1		; 0: extract random energies on the whole spectrum\n" + 
				"			; 1: loop on all lines and sampling points\n" + 
				"ContinuousPhotonNum 1	; Multiplicity of events for each interval in spectrum \n" + 
				"LinePhotonNum 10		; Multiplicity of events for each line in the spectrum\n" + 
				"RandomEneFlag 1		; enable random energy on each interval (0/1)\n" + 
				"Lines	      		; discrete energy lines of the spectrum\n" + 
				"1			; Number of lines in the spectrum\n" + 
				";			Energy Lines :\n" + 
				"76.6 0.1 1e10 1e7	; Energy (keV) , sigma (keV) , intensity (photons/sec)\n" + 
				"\n" + 
				"End\n" + 
				"").split("\n");

		return new XRMCSpectrum(lines);
	}
	
	@Test
	public void testIsSpectrumFile() {
		assertTrue(initTestSpectrum().isSpectrumFile());
	}

	@Test
	public void testGetName() {
		assertEquals("Name does not match", "Spectrum", initTestSpectrum().getName());
	}

	@Test
	public void testGetPolarizedFlag() {
		assertTrue("Polarization incorrect", initTestSpectrum().getPolarizedFlag());
	}

	@Test
	public void testGetLoopFlag() {
		assertTrue("Loop flag incorrect", initTestSpectrum().getLoopFlag());
	}

	@Test
	public void testGetContinuousPhotonNum() {
		assertEquals("ContinuousPhotonNum incorrect", 1, initTestSpectrum().getContinuousPhotonNum());
	}

	@Test
	public void testGetLinePhotonNum() {
		assertEquals("Line Photon Number incorrect", 10, initTestSpectrum().getLinePhotonNum());
	}

	@Test
	public void testGetRandomEneFlag() {
		assertTrue("RandomEneFlag incorrect", initTestSpectrum().getRandomEneFlag());
	}

	@Test
	public void testGetSpectrum() {
		List<XRMCSpectrum.SpectrumComponent> components = initTestSpectrum().getSpectrum();
		assertEquals("Incorrect spectrum length", 1, components.size());
	}

}
