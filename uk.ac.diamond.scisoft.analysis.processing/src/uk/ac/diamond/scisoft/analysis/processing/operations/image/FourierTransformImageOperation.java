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
	public IDataset processImage(IDataset dataset, IMonitor monitor) throws OperationException {

		// So that DAWN doesn't crash whilst testing/developing
		return dataset;
		
	}

	
	// Now let's define the process method
	@Override
	protected OperationData process(IDataset iDataset, IMonitor monitor) throws OperationException {
		// Let's find out how big the image is datasetShape[height, width]
		int[] datasetShape = iDataset.getShape();

		IDataset floatDataset;
		
		//floatDataset = DatasetFactory.zeros(new int[]{datasetShape[0] + 1, datasetShape[1] + 1}, dataset.FLOAT64);

		floatDataset = DatasetUtils.cast(FloatDataset.class, iDataset);
		
		// We need to typecast the int dataset to a float dataset, manually.
//		for (int yIndex = 0; yIndex < datasetShape[0]; yIndex++) {
//			for (int xIndex = 0; xIndex < datasetShape[0]; xIndex++) {
//				floatDataset.set(dataset.getObject(yIndex, xIndex), yIndex, xIndex);
//			}
//		}

		// Somewhere to put the transformed data
		Dataset transformedData;
		// The FFT itself!
		transformedData = FFT.fft2(floatDataset, null, null);
		
//		double valueToAssign = 0.00;
//		int currentIndex = 0;
//		
////		for (int yWave = 0; yWave < 50; yWave++) {
//	//		for (int xWave = 0; xWave < 50; xWave++) {
//				for (int ySpace = 0; ySpace < datasetShape[0]; ySpace++) {
//					for (int xSpace = 0; xSpace < datasetShape[1]; xSpace++) {
//						currentIndex = ySpace * xSpace;
//						valueToAssign = transformedData.getDouble(ySpace, xSpace);
//						valueToAssign += (dataset.getDouble(ySpace, xSpace) * Math.cos(2.0 * Math.PI * ((1.0 * xWave * xSpace / datasetShape[1]) + (1.0 * yWave * ySpace / datasetShape[0])))) / Math.sqrt(datasetShape[0] * datasetShape[1]);
//						transformedData.set(valueToAssign, ySpace, xSpace);
//						valueToAssign = 0.00;
//					}
//				}
//
//		//	}
//		//}
//		
		// So that DAWN doesn't crash whilst testing/developing
		//OperationData toReturn = (OperationData) dataset;
		OperationData toReturn = new OperationData(transformedData);
		
		return toReturn;
	}
	
}
