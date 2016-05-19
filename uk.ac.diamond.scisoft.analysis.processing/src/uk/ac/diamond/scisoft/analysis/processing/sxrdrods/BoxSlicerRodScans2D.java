/*-
 * Copyright 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.processing.sxrdrods;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.SliceND;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;

public class BoxSlicerRodScans2D {

	public static DoubleDataset[] LeftRightTopBottomBoxes (IDataset input , IMonitor monitor, 
			int[] len, int[] pt, int boundaryBox) throws OperationException{
		   
	
		   	SliceND slice0 = new SliceND(input.getShape());
			slice0.setSlice(1, pt[0]-boundaryBox, pt[0]+len[0]+boundaryBox, 1);
			slice0.setSlice(0, pt[1]-boundaryBox, pt[1] + len[1] + boundaryBox, 1);
			IDataset small0 = input.getSlice(slice0);
			Dataset small0d = DatasetUtils.cast(small0, Dataset.FLOAT64);
			
			//return small2;
			//Complete Box
			
			int noOfPoints = (len[1] + boundaryBox)*(len[0] +boundaryBox) - boundaryBox*boundaryBox;
			
			
			DoubleDataset xset = new DoubleDataset(noOfPoints);
			DoubleDataset yset = new DoubleDataset(noOfPoints);
			DoubleDataset zset = new DoubleDataset(noOfPoints);
			
			int l =0;
			
			for (int i =0; i<len[0]+boundaryBox; i++){
				for (int j = 0; j<len[1] + boundaryBox;j++){
					
					if ((i<boundaryBox || i>=boundaryBox+len[0]) && (j<boundaryBox || j>=boundaryBox+len[1])){
						xset.set(i, l);
						yset.set(j, l);
						zset.set(small0d.getDouble(i, j), l);
					}
				}	
			}
			
			DoubleDataset[] output = new DoubleDataset[3];
			
			output[0] = xset;
			output[1] = yset;
			output[2] = zset;
		
		
		return output;
	}	
	
	
}

