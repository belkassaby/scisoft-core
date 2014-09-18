/*
 * Copyright 2011 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.plotserver;

import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;

/**
 *
 */
public class TestRMIPlotClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DoubleDataset yAxis = new DoubleDataset(new double[]{3,1,2,5,4,6,7,9,8,10}, 10);
		try {
			SDAPlotter.plot("Plot 1", "TEST", yAxis);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
