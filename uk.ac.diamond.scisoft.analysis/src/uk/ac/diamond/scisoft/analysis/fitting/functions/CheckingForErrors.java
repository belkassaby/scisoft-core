/*-
 * Copyright 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package uk.ac.diamond.scisoft.analysis.fitting.functions;

import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;

public class CheckingForErrors {

	
	public DoubleDataset getOutputValuesOverlapping (double[] d,
													int[] len, 
													int[] boundaryBox, 
													int fitPower ) {

		DoubleDataset output1 = DatasetFactory.zeros(new int[] {len[1], len[0]});//new DoubleDataset(len[1], len[0]);
		
		for (int k=boundaryBox[1]; k<((boundaryBox[1]))+len[1]; k++){
			for (int l=(boundaryBox[0]); l<((boundaryBox[0]))+len[0]; l++){
			
				double temp = 0;
				double x = k;
				double y = l;
				
				for (int j = 0; j < (fitPower+1); j++) {
					for (int i = 0; i < (fitPower+1); i++) {
						
						try{
							double v = d[(j*(fitPower+1)+i)]*Math.pow(x, i)*Math.pow(y, j);
							temp += v;
						}
						catch (ArrayIndexOutOfBoundsException exc){
				
						}
					}
				}
				
				output1.set(temp, k-boundaryBox[1], l-boundaryBox[0]);
			}
		}
		
		return output1;
	}
		
}
