/*-
 * Copyright 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.processing.sxrdrods;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;

public class BoxSlicerRodScans2D {
	
	
	public static Dataset regionOfRegard (IDataset input , IMonitor monitor, 
			int[] len, int[] pt, int boundaryBox) throws OperationException{
		   
	
		   	SliceND slice0 = new SliceND(input.getShape());
			slice0.setSlice(1, pt[0]-boundaryBox, pt[0]+len[0]+boundaryBox, 1);
			slice0.setSlice(0, pt[1]-boundaryBox, pt[1] + len[1] + boundaryBox, 1);
			IDataset small0 = input.getSlice(slice0);
			Dataset small0d = DatasetUtils.cast(small0, Dataset.FLOAT64);
			
		
		
		return small0d;
	}
	
	public static List<Dataset> coordMesh(IDataset input , IMonitor monitor, 
			int[] len, int[] pt, int boundaryBox) throws OperationException{
		
		Dataset regionOfRegard = regionOfRegard(input, monitor, len, pt, boundaryBox);
		
		Dataset x = DatasetFactory.createRange(regionOfRegard.getShape()[0], Dataset.FLOAT64);
		Dataset y = DatasetFactory.createRange(regionOfRegard.getShape()[1], Dataset.FLOAT64);;
		
		List<Dataset> meshGrid = DatasetUtils.meshGrid(x,y);
		
		return meshGrid;
	}
	

	public static Dataset[] LeftRightTopBottomBoxes (IDataset input , IMonitor monitor, 
			int[] len, int[] pt, int boundaryBox) throws OperationException{
		   
		long startTime = System.currentTimeMillis();
		
		Dataset regionOfRegard = regionOfRegard(input, monitor, len, pt, boundaryBox);
		
		int noOfPoints = (len[1] + 2*boundaryBox)*(len[0] +2*boundaryBox) - len[1]*len[0];
		
		
		DoubleDataset xset = new DoubleDataset(noOfPoints);
		DoubleDataset yset = new DoubleDataset(noOfPoints);
		DoubleDataset zset = new DoubleDataset(noOfPoints);
		
		int l =0;
				
		for (int i =0; i<len[1]+2*boundaryBox; i++){
			if (i<boundaryBox || i>boundaryBox+len[0]){
				for (int j = 0; j<len[0] + 2*boundaryBox;j++){
					if ((j<boundaryBox || j>boundaryBox+len[1])){
						xset.set(i, l);
						yset.set(j, l);
						//System.out.println("########## i:" + i+ "   j: "+j+"  l: " + l + "  #########");
						zset.set(regionOfRegard.getDouble(i, j), l);
						l++;
//						if (monitor != null && monitor.isCancelled()){
//							return null;
					}
				}
			}	
		}	
		
		Dataset[] output = new Dataset[3];
		
		output[0] = xset;
		output[1] = yset;
		output[2] = zset;
		
		long endTime = System.currentTimeMillis();
		
		long elapsedTime = endTime-startTime;
		
		System.out.println("LeftRightTopBottomBoxes method took: " + elapsedTime);
	
	return output;
	}	
	
	public static Dataset[] subRange (IDataset input , IMonitor monitor, 
			int[] len, int[] pt, int boundaryBox) throws OperationException{
		   
		long startTime = System.currentTimeMillis();
		
		Dataset[] ranges = LeftRightTopBottomBoxes(input, monitor, len, pt, boundaryBox);
		
		int subRangeNo = (int) 0.01*ranges[0].getShape()[0];
		
		if (subRangeNo<100){
			subRangeNo = 100;
		}
		
		DoubleDataset xSub = new DoubleDataset(subRangeNo);
		DoubleDataset ySub = new DoubleDataset(subRangeNo);
		DoubleDataset zSub = new DoubleDataset(subRangeNo);
		
		Random rng = new Random(); 
		
		Set<Integer> subSet = new HashSet<Integer>();
		
		while (subSet.size() < subRangeNo){
		    Integer next = rng.nextInt(subRangeNo) + 1;
		    // As we're adding to a set, this will automatically do a containment check
		    subSet.add(next);
		}
		
		java.util.Iterator<Integer> itr = subSet.iterator();
		
		while(itr.hasNext()){
			int p = itr.next();
			xSub.set(ranges[0].getObject(itr.next()),p);
			ySub.set(ranges[1].getObject(itr.next()),p);
			zSub.set(ranges[2].getObject(itr.next()),p);
		}
		
		Dataset[] output = new Dataset[3];
		
		output[0] = xSub;
		output[1] = ySub;
		output[2] = zSub;
		
		
		long endTime = System.currentTimeMillis();
		
		long elapsedTime = endTime-startTime;
		
		System.out.println("subRange method took: " + elapsedTime);
	
	return output;
	}	
	
	public static Dataset[] subRange (IDataset input , IMonitor monitor, 
			int[] len, int[] pt, int boundaryBox, int size) throws OperationException{
		   
		long startTime = System.currentTimeMillis();
		
		Dataset[] ranges = LeftRightTopBottomBoxes(input, monitor, len, pt, boundaryBox);
		
		int subRangeNo = (int) ((int) size*0.01*ranges[0].getShape()[0]);
		
		if (subRangeNo<(size*100)){
			subRangeNo = size*100;
		}
		
		DoubleDataset xSub = new DoubleDataset(subRangeNo);
		DoubleDataset ySub = new DoubleDataset(subRangeNo);
		DoubleDataset zSub = new DoubleDataset(subRangeNo);
		
		Random rng = new Random(); 
		
		Set<Integer> subSet = new HashSet<Integer>();
		
		while (subSet.size() < subRangeNo){
		    Integer next = rng.nextInt(subRangeNo) + 1;
		    // As we're adding to a set, this will automatically do a containment check
		    subSet.add(next);
		}
		
		java.util.Iterator<Integer> itr = subSet.iterator();
		
		while(itr.hasNext()){
			int p = itr.next();
			xSub.set(ranges[0].getObject(itr.next()),p);
			ySub.set(ranges[1].getObject(itr.next()),p);
			zSub.set(ranges[2].getObject(itr.next()),p);
		}
		
		Dataset[] output = new Dataset[3];
		
		output[0] = xSub;
		output[1] = ySub;
		output[2] = zSub;
		
		
		long endTime = System.currentTimeMillis();
		
		long elapsedTime = endTime-startTime;
		
		System.out.println("subRange method took: " + elapsedTime);
	
	return output;
	}
	
	public static Dataset[] subRangeDownSample (IDataset input , IMonitor monitor, 
			int[] len, int[] pt, int boundaryBox, int size, String enc) throws OperationException{
		   
		long startTime = System.currentTimeMillis();
		
		Dataset[] ranges = LeftRightTopBottomBoxes(input, monitor, len, pt, boundaryBox);
		
		int subRangeNo = (int) ((int) size*0.01*ranges[0].getShape()[0]);
		
		if (subRangeNo<(size*100)){
			subRangeNo = size*100;
		}
		
		DoubleDataset xSub = new DoubleDataset(subRangeNo);
		DoubleDataset ySub = new DoubleDataset(subRangeNo);
		DoubleDataset zSub = new DoubleDataset(subRangeNo);
		
		Random rng = new Random(); 
		
		Set<Integer> subSet = new HashSet<Integer>();
		
		while (subSet.size() < subRangeNo){
		    Integer next = rng.nextInt(subRangeNo) + 1;
		    // As we're adding to a set, this will automatically do a containment check
		    subSet.add(next);
		}
		
		java.util.Iterator<Integer> itr = subSet.iterator();
		
		while(itr.hasNext()){
			int p = itr.next();
			xSub.set(ranges[0].getObject(itr.next()),p);
			ySub.set(ranges[1].getObject(itr.next()),p);
			zSub.set(ranges[2].getObject(itr.next()),p);
		}
		
		Dataset[] output = new Dataset[3];
		
		output[0] = xSub;
		output[1] = ySub;
		output[2] = zSub;
		
		
		long endTime = System.currentTimeMillis();
		
		long elapsedTime = endTime-startTime;
		
		System.out.println("subRange Downsample method took: " + elapsedTime);
	
	return output;
	}
	

	
	public static DoubleDataset weightingMask (IDataset input , IMonitor monitor, 
			int[] len, int[] pt, int boundaryBox) throws OperationException{
		   
	
			Dataset regionOfRegard = regionOfRegard(input, monitor, len, pt, boundaryBox);
			
			DoubleDataset mask = new DoubleDataset(new int[] {regionOfRegard.getShape()[0], regionOfRegard.getShape()[1]});
					

			for (int i =0; i<len[1]+2*boundaryBox; i++){
				for (int j = 0; j<len[0] + 2*boundaryBox;j++){
					
					if ((i<boundaryBox || i>=boundaryBox+len[1]) && (j<boundaryBox || j>=boundaryBox+len[0])){
						mask.set(i,j, 1);
//						if (monitor != null && monitor.isCancelled()){
//							return null;
//						}
						
					}
				}	
			}
			
		return mask;
	}	
	
	
}

