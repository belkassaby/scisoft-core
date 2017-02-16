package uk.ac.diamond.scisoft.analysis.peakfinding.peakfinders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.X509ExtendedTrustManager;
import javax.xml.crypto.Data;

import org.eclipse.dawnsci.analysis.dataset.impl.Signal;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.dataset.Slice;
import org.junit.Assert;
import org.junit.Test;


import org.apache.commons.math3.analysis.integration.*;

import uk.ac.diamond.scisoft.analysis.fitting.functions.AFunction;

/**
 * TODO: Wavelet function integrates to zero; 
 * 
 * @author Dean P. Ottewell
 *
 */
public class MexicanHatWaveletTest {

	private final double ABS_TOL = 1e-7;
	private final double WIDTH = 4.0;
	private final double SIZE = 100;

	@Test
	public void parameterTest() {
		AFunction f = new MexicanHatWavelet(SIZE,WIDTH);		
		
		//Test Parameters
		Assert.assertEquals(2, f.getNoOfParameters());
		Assert.assertArrayEquals(new double[] {SIZE/2,WIDTH}, f.getParameterValues(), ABS_TOL);
	}
	
	@Test
	public void centerValueAmplitudeTest(){
		AFunction f = new MexicanHatWavelet(SIZE,WIDTH);		
		
		//Check center value
		double peakVal = 2 / (Math.sqrt(3 * WIDTH) * (Math.pow(Math.PI, 0.25)));
		
		Assert.assertEquals(peakVal, f.val(0), ABS_TOL); //center should be equal to highest value 
	}
	
	@Test
	public void shapePeaksTrough(){
		AFunction f = new MexicanHatWavelet(SIZE,WIDTH);		
		//Test is a mexican hat at key points
		Dataset testPoints = DatasetFactory.createLinearSpace(1, 100, 100, Dataset.FLOAT64);
		Dataset dx;
		dx = f.calculateValues(testPoints);
				
		double maxVal = Math.abs(dx.getDouble(dx.maxPos()));
		int count = 0;
		for (int i = 0; i < dx.getSize(); ++i){
			if(Math.abs(dx.getDouble(i)) == maxVal){
				count++;
			}
		}
		assertEquals(1, count);
		
		double minVal = Math.abs(dx.getDouble(dx.minPos()));
		count = 0;
		for (int i = 0; i < dx.getSize(); ++i){
			if(Math.abs(dx.getDouble(i)) == minVal){
				count++;
			}
		}
		assertEquals(2, count);
	}
	
	@Test
	public void firstDerivativeChecks(){
		//First derivative should result in change that is 
		//Maths.derivative(x, y, n)
		
		AFunction f = new MexicanHatWavelet(SIZE,WIDTH);		
		Dataset testPoints = DatasetFactory.createLinearSpace(1, 100, 100, Dataset.FLOAT64);
		Dataset dx;
		dx = f.calculateValues(testPoints);
		
		//Maths.derivative(x, y, 0);
	}	
	
//	@Test
//	public void orthagonalTest(){
//		AFunction f = new MexicanHatWavelet(SIZE,WIDTH);		
//		Dataset testPoints = DatasetFactory.createLinearSpace(1, 100, 100, Dataset.FLOAT64);
//		Dataset dx  = f.calculateValues(testPoints);
//
//		Dataset xRange = DatasetFactory.createRange(dx.getSize(), Dataset.FLOAT64);
//			
//		Dataset xy =DatasetUtils.createCompoundDataset(dx,xRange); //= DatasetFactory.createFromList();
//		xy = DatasetUtils.resize(xy, xy.getShape()[0]/2,2);
//		
//		Double test = xy.getDouble(0);
//		Double testing = xy.getDouble(2);
//		
//		//Because those tests helped
//		double[] dd = {0., 1., 1., 2., 2., 3.};
//		Dataset straightLine = DatasetFactory.createFromObject(dd,3,2);
//
//		//Integral should be 0
//		//Integrate2D row = new Integrate2D();
//		//List<? extends Dataset> dsets = row.value(straightLine);
//		
//		assertTrue(dsets.size() == 0);	
//	}
//	

	
	
}
