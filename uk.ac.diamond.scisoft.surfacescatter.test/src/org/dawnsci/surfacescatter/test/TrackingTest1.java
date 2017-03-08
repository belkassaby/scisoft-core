/**
 * 
 */
package org.dawnsci.surfacescatter.test;

import static org.junit.Assert.*;

import org.dawnsci.boofcv.BoofCVImageTrackerServiceCreator;
import org.eclipse.dawnsci.analysis.api.image.IImageTracker;
import org.eclipse.dawnsci.analysis.api.image.IImageTracker.TrackerType;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.junit.Test;

/**
 * @author awq68923
 *
 */
public class TrackingTest1 {

	private IImageTracker tracker;

//	private String dataname = "image-01";
	private IDataset data;
	private IDataset data2;

	
	
	public void dataGenerator(){
		
		data = DatasetFactory.zeros(new int[] {1000, 1000});
		data2 = DatasetFactory.zeros(new int[] {1000, 1000});
		
		for(int i = 100; i<200; i++){
			for(int j = 100; j<200; j++){
				data.set(10, i,j);
			}
		}
		
		for(int i1 = 110; i1<210; i1++){
			for(int j1 = 110; j1<210; j1++){
				data2.set(10, i1,j1);
			}
		}
		
	}
	

//	public void before() throws Exception {
//		if (tracker == null)
//			tracker = BoofCVImageTrackerServiceCreator.createImageTrackerService();
////		data = LoaderFactory.getData("/dls/i07/data/2015/si10262-1/pilatus3/p3Image1024107.tif", null).getDataset(dataname);
////		data2 = LoaderFactory.getData("/dls/i07/data/2015/si10262-1/pilatus3/p3Image1024108.tif", null).getDataset(dataname);
//	}
	
	
	@Test
	public void testImageTrackingTLD() throws Exception {
		
		dataGenerator();
		
		double[] originalLocation = new double[] { 90, 90, 210, 90, 90, 210, 210, 210 };
		// initialize tracker
		
		tracker = BoofCVImageTrackerServiceCreator.createImageTrackerService();
		tracker.initialize(data, originalLocation, TrackerType.TLD);
		// run tracker against second image
		double[] location = tracker.track(data2);
		System.out.println("location: " + location[0] + location[1] + location[2] + location[3] + location[4] + location[5] + location[6] + location[7]);
		double[] locationExpected = new double[] { 100, 100, 220,
				100, 100, 220, 220, 220};
		
		assertTrue(location.equals(locationExpected));

	}
	
	
//	@Test
//	public void test() {
//		fail("Not yet implemented");
//	}

}