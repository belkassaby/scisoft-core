package uk.ac.diamond.scisoft.xrmc;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.scisoft.xpdf.xrmc.XRMCDetector;

public class DetectorTest {

	XRMCDetector det;
	static final String pathPrefix = "/home/rkl37156/xrmc/experiments/DSK_ceria/";
	
	@Before
	public void setUp() {
		det = new XRMCDetector(pathPrefix+"detector.dat");
	}
	
	@Test
	public void testNPixels() {
		int[] npixels = det.getNPixels();
		assertArrayEquals("Incorrect detector dimensions", new int[] {40,  40}, npixels);
	}

	@Test
	public void testPixelSize() {
		double[] pixelSize = det.getPixelSize();
		assertArrayEquals("Incorrect pixel size", new double[] {10000,  10000},  pixelSize, 1e-6);
	}
	
	@Test
	public void testDetectorPosition() {
		double[] detectorPosition = det.getDetectorPosition();
		assertArrayEquals("Incorrect detector position", new double[] {0, 200., 0}, detectorPosition, 1e-6);
	}
	
	@Test
	public void testDetectorOrientation() {
		double[] detectorN = det.getDetectorNormal();
		double[] detectorK = det.getDetectorUpVector();
		
		assertArrayEquals("Incorrect detector normal", new double[] {0, -1, 0}, detectorN, 1e-6);
		assertArrayEquals("Incorrect detector up vector", new double[] {1, 0, 0}, detectorK, 1e-6);
	}
	
	@Test
	public void testSolidAngle() {
		double omega = det.getSolidAngle();
		assertEquals("Incorrect solid angle", 2.094, omega, 2e-3);
	}
	
	@Test
	public void testNBins() {
		assertEquals("Incorrect NBins", 1000, det.getNBins());
	}
	
	@Test
	public void testEnergyLimits() {
		assertEquals("Incorrect Emin", 0, det.getEmin(), 1e-6);
		assertEquals("Incorrect Emax", 80.0, det.getEmax(), 1e-6);
	}
}
