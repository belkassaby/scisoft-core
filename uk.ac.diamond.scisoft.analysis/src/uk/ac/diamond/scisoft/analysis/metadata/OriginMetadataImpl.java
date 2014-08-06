/*-
 * Copyright 2014 Diamond Light Source Ltd.
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

package uk.ac.diamond.scisoft.analysis.metadata;

import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Slice;

public class OriginMetadataImpl implements OriginMetadata {
	
	private ILazyDataset parent;
	private Slice[] slice;
	private int[] dataDims;

	public OriginMetadataImpl(ILazyDataset parent, Slice[] slice, int[] dataDims) {
		this.parent = parent;
		this.slice = slice;
		this.dataDims = dataDims;
	}
	
	@Override
	public int[] getDataDimensions() {
		return dataDims;
	}

	@Override
	public ILazyDataset getParent() {
		return parent;
	}

	@Override
	public Slice[] getSlice() {
		return slice;
	}
	
	@Override
	public OriginMetadata clone() {
		return new OriginMetadataImpl(parent, slice.clone(), dataDims.clone());
	}

}
