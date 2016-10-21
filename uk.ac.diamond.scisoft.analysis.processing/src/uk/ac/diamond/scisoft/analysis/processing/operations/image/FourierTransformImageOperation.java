package uk.ac.diamond.scisoft.analysis.processing.operations.image;

import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.FFT;
import org.eclipse.january.dataset.FloatDataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;

public class FourierTransformImageOperation extends AbstractSimpleImageOperation<FourierTransformImageModel> {

	
	// Let's give this process an ID tag
	@Override
	public String getId() {
		return "uk.ac.diamond.scisoft.analysis.processing.operations.FourierTransformImageOperation";
	}


	// Before we start, let's make sure we know how many dimensions of data are going in...
	@Override
	public OperationRank getInputRank() {
		return OperationRank.TWO;
	}


	// ...and out
	@Override
	public OperationRank getOutputRank() {
		return OperationRank.TWO;
	}
	
		
	// Now let's define the processImage method
	@Override
	public IDataset processImage(IDataset iDataset, IMonitor monitor) throws OperationException {

		// First we need to create a placeholder for typecasting the data as the FFT algorithm can't take integers (needs floats)
		Dataset floatDataset;
		// Now, let's typecast the data
		floatDataset = DatasetUtils.cast(FloatDataset.class, iDataset);
		
		// Now we need a container for the transformed data
		IDataset transformedData;
		// Now we can do the FFT itself
		transformedData = FFT.fft2(floatDataset, null, null);

		// Now we can make the dataset, ready to return it to DAWN
		OperationData toReturn = new OperationData(transformedData);
		
		// And then return it!
		return toReturn;
		
	}

	
	// Now let's define the process method
	@Override
	protected OperationData process(IDataset iDataset, IMonitor monitor) throws OperationException {

		// First we need to create a placeholder for typecasting the data as the FFT algorithm can't take integers (needs floats)
		Dataset floatDataset;
		// Now, let's typecast the data
		floatDataset = DatasetUtils.cast(FloatDataset.class, iDataset);
		
		// Now we need a container for the transformed data
		IDataset transformedData;
		// Now we can do the FFT itself
		transformedData = FFT.fft2(floatDataset, null, null);

		// Now we can make the dataset, ready to return it to DAWN
		OperationData toReturn = new OperationData(transformedData);
		
		// And then return it!
		return toReturn;
	}
	
}
