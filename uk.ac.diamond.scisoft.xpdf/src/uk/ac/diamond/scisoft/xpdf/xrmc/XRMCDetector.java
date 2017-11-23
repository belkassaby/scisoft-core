package uk.ac.diamond.scisoft.xpdf.xrmc;

import java.util.Arrays;

import javax.vecmath.Matrix3d;
import javax.vecmath.Vector3d;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.dawnsci.analysis.api.diffraction.DetectorProperties;

import uk.ac.diamond.scisoft.xpdf.XPDFDetector;

public class XRMCDetector {

	XRMCDatReader reader;
	
	/**
	 * Creates a new Detector class, based on the given file
	 * @param fileName
	 * 				file path at which the source file can be found
	 */
	public XRMCDetector(String fileName) {
		reader = new XRMCDatReader(fileName);
	}
	
	/**
	 * Checks that what the XRMCDatReader has ingested is an XRMC detector file.
	 * @return true if the structure in the reader matches an XRMC detector file
	 */
	public boolean isDetectorFile() {
		return reader.hasKey("Newdevice") 
				&& "detectorarray".equals(reader.getValue("Newdevice"));
	}
	
	/**
	 * Returns the number of pixels in each dimension of the detector.
	 * @return the integral number of pixels in the detector array.
	 */
	public int[] getNPixels() {
		String pixelString = reader.getValue("NPixels");
		String[] tokens = pixelString.split("\\s+");
		int[] nxny = new int[2];
		nxny[0] = Integer.parseInt(tokens[0]);
		nxny[1] = Integer.parseInt(tokens[1]);

		return nxny;
	}
	
	/**
	 * Returns the size of the pixel in each dimension in μm.
	 * @return The size of the detector pixels in μm.
	 */
	public double[] getPixelSize() {
		// an extra multiplication by 10 000 to go from cm to μm
		return getParseAndScaleValues("PixelSize", 10_000);
	}
	
	/**
	 * Returns the centre of the detector, positioned in mm
	 * @return coordinates of the screen in mm, relative to the origin. 
	 */
	public double[] getDetectorPosition() {
		return getParseAndScaleValues("X", 10);
	}

	public double[] getDetectorNormal() {
		return getAndParseValues("uk");
	}
	
	public double[] getDetectorUpVector() {
		return getAndParseValues("ui");
	}
	
	public int getNBins() {
		// Assume there are not more than 2^53 energies
		return (int) getAndParseValues("NBins")[0];
	}
	
	public double getEmin() {
		return Double.parseDouble(reader.getValue("Emin"));
	}
	
	public double getEmax() {
		return Double.parseDouble(reader.getValue("Emax"));
	}

	private double[] parseToDoubleArray(String[] array) {
		return ArrayUtils.toPrimitive(Arrays.stream(array).map((String s) -> Double.parseDouble(s)).toArray(Double[]::new));
	}
	
	private double[] getAndParseValues(String key) {
		return parseToDoubleArray(reader.getValue(key).split("\\s+"));
	}
	
	private double[] getParseAndScaleValues(String key, double scale) {
		return Arrays.stream(getAndParseValues(key)).map(d -> d*scale).toArray();
	}
	
	/**
	 * Returns the solid angle subtended by the detector.
	 * @return the solid angle subtended by the detector (steradians).
	 */
	public double getSolidAngle() {
		// Generate the corner vectors for the detector
		//   Generate the basis vectors for the screen in the lab coordinate system
		Maths3d xScreen = new Maths3d(this.getDetectorUpVector());
		Maths3d zScreen = new Maths3d(this.getDetectorNormal());
		Maths3d yScreen = zScreen.cross(xScreen);
		
		// screen centre in mm
		Maths3d screenCentre = new Maths3d(getDetectorPosition());
		// in metres
		screenCentre = screenCentre.times(0.001);

		// half lengths of the screen in metres
		double xHalfLength = this.getNPixels()[0] * this.getPixelSize()[0] / 1_000_000 / 2;
		double yHalfLength = this.getNPixels()[1] * this.getPixelSize()[1] / 1_000_000 / 2;
		
		Maths3d mm = screenCentre.minus(xScreen.times(xHalfLength)).minus(yScreen.times(yHalfLength));
		Maths3d pm = screenCentre.plus(xScreen.times(xHalfLength)).minus(yScreen.times(yHalfLength));
		Maths3d mp = screenCentre.minus(xScreen.times(xHalfLength)).plus(yScreen.times(yHalfLength));
		Maths3d pp = screenCentre.plus(xScreen.times(xHalfLength)).plus(yScreen.times(yHalfLength));
		
		return
				DetectorProperties.calculatePlaneTriangleSolidAngle(screenCentre.value, pp.value, mp.value) +
				DetectorProperties.calculatePlaneTriangleSolidAngle(screenCentre.value, mp.value, mm.value) +
				DetectorProperties.calculatePlaneTriangleSolidAngle(screenCentre.value, mm.value, pm.value) +
				DetectorProperties.calculatePlaneTriangleSolidAngle(screenCentre.value, pm.value, pp.value);
	}
	
	/**
	 * Returns the euler angles of the detector relative to the beam.
	 * @return Euler angles in radians ordered as pitch, yaw, roll.
	 */
	public double[] getEulerAngles() {
		Maths3d xScreen = new Maths3d(this.getDetectorUpVector());
		Maths3d zScreen = new Maths3d(this.getDetectorNormal());
		Maths3d yScreen = zScreen.cross(xScreen);

		// Use elements of the full rotation matrix to get the
		// trigonometric functions of the Euler angles.
		
		double sinYaw = zScreen.get().x;
		double yaw = Math.asin(sinYaw);
		
		double pitch = Math.atan2(-zScreen.get().y, zScreen.get().z);
		// When the screen is exactly face on, the pitch will here be 
		// π/2, because of the definitions of the screen and lab
		// coordinate systems.  
		pitch -= Math.PI/2;
		
		double roll = Math.atan2(-yScreen.get().x, xScreen.get().x);
		
		return new double[] {pitch, yaw, roll};
	}

	/**
	 * A class of non-mutating arithmetic for three-element double vectors
	 */
	private class Maths3d {
		Vector3d value;
		
		public Maths3d(Vector3d v) {
			value = v;
		}
		
		public Maths3d(Maths3d vobj) {
			this(vobj.value);
		}
		
		public Maths3d(double[] darr) {
			this(new Vector3d(Arrays.copyOf(darr, 3)));
		}
		
		public Vector3d get() {
			return value;
		}
		
		public Maths3d plus(Maths3d b) {
			return this.plus(b.value);
		}
		
		public Maths3d plus(Vector3d b) {
			Maths3d a = new Maths3d(new Vector3d(value));
			a.value.add(b);
			return a;
		}
		
		public Maths3d plus(MathsD b) {
			return this.plus(b.get());
		}

		public Maths3d plus(double b) {
			Maths3d a = new Maths3d(new Vector3d(value));
			a.value.add(new Vector3d(b, b, b));
			return a;
		}
		
		public Maths3d minus(Maths3d b) {
			return this.minus(b.value);
		}
		
		public Maths3d minus(Vector3d b) {
			Maths3d a = new Maths3d(new Vector3d(value));
			a.value.sub(b);
			return a;
		}
		
		public Maths3d minus(MathsD b) {
			return this.minus(b.get());
		}

		public Maths3d minus(double b) {
			Maths3d a = new Maths3d(new Vector3d(value));
			a.value.add(new Vector3d(-b, -b, -b));
			return a;
		}
		
		public MathsD dot(Maths3d b) {
			return this.dot(b.value);
		}
		
		public MathsD dot(Vector3d b) {
			return new MathsD(this.value.dot(b));
		}
		
		
		public Maths3d cross(Maths3d b) {
			return this.cross(b.value);
		}
		
		public Maths3d cross(Vector3d b) {
			Vector3d a = new Vector3d();
			a.cross(this.value, b);
			return new Maths3d(a);
		}
		
		public Maths3d times(MathsD b) {
			return this.times(b.get());
		}
		
		public Maths3d times(double b) {
			Maths3d a = new Maths3d(new Vector3d(value));
			a.value.scale(b);
			return a;
		}
		
		
	}
	
	private class MathsD {
		double value;
		
		public MathsD(double v) {
			value = v;
		}
		
		public MathsD(MathsD b) {
			this.value = b.value;
		}
		
		public double get() {
			return value;
		}

		public Maths3d plus(Maths3d b) {
			return b.plus(this);
		}
		
		public Maths3d plus(Vector3d b) {
			Maths3d bobj = new Maths3d(new Vector3d(b));
			return bobj.plus(this);
		}
		
		public MathsD plus(MathsD b) {
			return this.plus(b.get());
		}

		public MathsD plus(double b) {
			MathsD a = new MathsD(this);
			a.value += b;
			return a;
		}
		
		public Maths3d minus(Maths3d b) {
			Maths3d negB = new Maths3d(new Vector3d(b.value));
			negB.get().negate(); // no copying, should be okay
			return this.plus(negB);
		}
		
		public Maths3d minus(Vector3d b) {
			Maths3d bobj = new Maths3d(new Vector3d(b));
			return this.minus(bobj);
		}
		
		public MathsD minus(MathsD b) {
			return this.minus(b.value);
		}

		public MathsD minus(double b) {
			return new MathsD(this.value - b);
		}
		
		public Maths3d times(Maths3d b) {
			return b.times(this);
		}
		
		public Maths3d times(Vector3d b) {
			return new Maths3d(b).times(this);
		}
		
		public MathsD times(MathsD b) {
			return this.times(b.value);
		}
		
		public MathsD times(double b) {
			return new MathsD(this.value * b);
		}
	}
}
