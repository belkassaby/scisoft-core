package uk.ac.diamond.scisoft.xrmc;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.scisoft.xpdf.xrmc.XRMCDatReader;

public class XRMCDatReaderTest {

	String pathPrefix = "/home/rkl37156/xrmc/experiments/DSK_ceria/";
	
	@Before
	public void setUp() {
		
	}
	
	@Test
	public void testIndicesOf() {
		XRMCDatReader inputReader = new XRMCDatReader(pathPrefix + "input.dat");
		
		int[] loadLines = inputReader.indicesOf("Load");
		assertArrayEquals("Load lines not where expected", new int[] {0,1,2,3,4,5,6}, loadLines);
	}

	@Test
	public void testHasKey() {
		XRMCDatReader inputReader = new XRMCDatReader(pathPrefix + "input.dat");

		assertTrue("Could not find \"Run\" key", inputReader.hasKey("Run"));
		assertFalse("Found spurious \"Jam\" key", inputReader.hasKey("Jam"));
	}

	@Test
	public void testGetValue() {
		XRMCDatReader inputReader = new XRMCDatReader(pathPrefix + "input.dat");
		
		assertEquals("Could not find first file", "source.dat", inputReader.getValue("Load"));
	}
	
	@Test
	public void testGetValues() {
		XRMCDatReader quadricReader = new XRMCDatReader(pathPrefix + "quadric.dat");
		
		int nExtraLines = 1;
		
		String[] cylinderParams = quadricReader.getValues("CylinderZ", nExtraLines);
		assertEquals("Cylinder name incorrect", "CZ1", cylinderParams[0]);
		assertEquals("Cylinder parameters incorrect", "0 0 0.05 0.05", cylinderParams[1]);
	}
	
	@Test
	public void testGetLine() {
		XRMCDatReader detectorReader = new XRMCDatReader(pathPrefix + "detector.dat");
		
		assertEquals("Incorrect detector name", "DetectorArray", detectorReader.getLine(1));
	}
	
	@Test
	public void testGetLines() {
		XRMCDatReader detectorReader = new XRMCDatReader(pathPrefix + "detector.dat");
		
		int firstSeedLine = detectorReader.firstIndexOf("Seeds")+1;
		int nSeeds = Integer.parseInt(detectorReader.getValue("Seeds"));
		String[] seedStrings = detectorReader.getLines(firstSeedLine, nSeeds);
		
		assertArrayEquals("Seeds incorrect", new String[] {"113450", "113451", "113452", "113453", "113454", "113455", "113456", "113457", "113458", "113459"}, seedStrings);
	}
	
	@Test
	public void testFirstIndexOfAfter() {
		XRMCDatReader quadricReader = new XRMCDatReader(pathPrefix + "quadric.dat");
		assertEquals("Second \"Plane\" mislocated", 6, quadricReader.firstIndexOfAfter("Plane", quadricReader.firstIndexOf("Plane")));
	}
}
