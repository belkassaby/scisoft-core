package uk.ac.diamond.scisoft.analysis.processing.operations.image;

import org.eclipse.dawnsci.analysis.api.image.IImageTransform;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.january.DatasetException;
import org.eclipse.january.IMonitor;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.dataset.SliceND;
import uk.ac.diamond.scisoft.analysis.processing.operations.utils.OperationServiceLoader;
import org.eclipse.dawnsci.analysis.dataset.slicer.SliceFromSeriesMetadata;

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
		datasetShape[0] -= 1;
		datasetShape[1] -= 1;
		Dataset dataset = (Dataset) iDataset;
		Dataset transformedData = DatasetFactory.zeros(new int[]{datasetShape[0], datasetShape[1]}, dataset.getDType());
//		SliceFromSeriesMetadata ssm = iDataset.getFirstMetadata(SliceFromSeriesMetadata.class);
//		Slice[] slice = ssm.getSliceInOutput();
//		SliceND plink = new SliceND(transformedData.getShape());
//		plink.setSlice(1, 0, 200, 1);
		
		
		
		double valueToAssign = 0.00;
		int currentIndex = 0;
		
		for (int yWave = 0; yWave < 50; yWave++) {
			for (int xWave = 0; xWave < 50; xWave++) {
				for (int ySpace = 0; ySpace < datasetShape[0]; ySpace++) {
					for (int xSpace = 0; xSpace < datasetShape[1]; xSpace++) {
						currentIndex = ySpace * xSpace;
						valueToAssign = transformedData.getDouble(ySpace, xSpace);
						valueToAssign += (dataset.getDouble(ySpace, xSpace) * Math.cos(2.0 * Math.PI * ((1.0 * xWave * xSpace / datasetShape[1]) + (1.0 * yWave * ySpace / datasetShape[0])))) / Math.sqrt(datasetShape[0] * datasetShape[1]);
						transformedData.set(valueToAssign, ySpace, xSpace);
						valueToAssign = 0.00;
					}
				}
				System.out.println(xWave);
			}
		}
		
		// So that DAWN doesn't crash whilst testing/developing
		//OperationData toReturn = (OperationData) dataset;
		OperationData toReturn = (OperationData) transformedData;
		
		return toReturn;
	}
	
}
