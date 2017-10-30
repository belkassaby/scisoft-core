package uk.ac.diamond.scisoft.xpdf.operations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationDataForDisplay;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperation;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.metadata.MaskMetadata;

import uk.ac.diamond.scisoft.xpdf.XPDFAbsorptionMaps;
import uk.ac.diamond.scisoft.xpdf.XPDFCoordinates;
import uk.ac.diamond.scisoft.xpdf.XPDFElectronCrossSections;
import uk.ac.diamond.scisoft.xpdf.XPDFQSquaredIntegrator;
import uk.ac.diamond.scisoft.xpdf.XPDFTargetComponent;
import uk.ac.diamond.scisoft.xpdf.metadata.XPDFMetadata;

public class XPDFIncoherentScatteringIterationOperation
		extends AbstractOperation<XPDFIncoherentScatteringIterationModel, OperationDataForDisplay> {

	@Override
	public String getId() {
		return "uk.ac.diamond.scisoft.xpdf.operations.XPDFIncoherentScatteringIterationOperation";
	}

	@Override
	public OperationRank getInputRank() {
		return OperationRank.ANY;
	}

	@Override
	public OperationRank getOutputRank() {
		return OperationRank.SAME;
	}

	@Override
	protected OperationData process(IDataset input, IMonitor monitor) {
		
		// Check for the necessary auxiliary data
		XPDFOperationChecker.checkXPDFMetadata(this, input, true, true, true);
		if (!XPDFOperationChecker.isAllIncoherentScaterPresent(input))
			throw new OperationException(this, "Incoherent scattering data not found. Please use XPDFIterateCalibrationCOnstant instead.");

		XPDFMetadata theXPDFMetadata = input.getFirstMetadata(XPDFMetadata.class);
		if (theXPDFMetadata == null) throw new OperationException(this, "XPDFMetadata not found.");

		// Get the background subtracted data
		List<Dataset> backgroundSubtracted = new ArrayList<>();
		// The 0th element is the sample
		backgroundSubtracted.add(DatasetUtils.convertToDataset(input));
		// Add the containers in order, innermost to outermost
		for (XPDFTargetComponent container : theXPDFMetadata.getContainers()) {
			backgroundSubtracted.add(theXPDFMetadata.getContainerTrace(container).getTrace());
		}

		// Extract the data coordinates
		XPDFCoordinates coordinates = new XPDFCoordinates(DatasetUtils.convertToDataset(input));
		// Detector transmission correction
		Dataset transmissionCorrection = theXPDFMetadata.getDetector().getTransmissionCorrection(coordinates.getTwoTheta(), theXPDFMetadata.getBeam().getBeamEnergy());
		// Provide the Q² integrator and Krogh-Moe sum
		XPDFQSquaredIntegrator qSquaredIntegrator = (input.getFirstMetadata(MaskMetadata.class) != null) ?
				new XPDFQSquaredIntegrator(coordinates, DatasetUtils.convertToDataset(input.getFirstMetadata(MaskMetadata.class).getMask())):
					new XPDFQSquaredIntegrator(coordinates);

		// The Krogh-Moe sum integration is constant
		XPDFTargetComponent sample = theXPDFMetadata.getSample();
		double selfScatteringNumerator = sample.getKroghMoeSum();

				
		double lastGain = 0;
		double currentGain = 1;
		double gainThreshold = model.getGainThreshold();
		Dataset sampleData = null;
		
		// Begin the gain iteration
		while (Math.abs(lastGain/currentGain - 1) > gainThreshold) {
			List<Dataset> workingData = new ArrayList<>();
			// Scale the data by the gain, and copy into the working array
			for (Dataset componentData : backgroundSubtracted) {
				workingData.add(Maths.divide(componentData, currentGain));
			}
		
			// Subtract the XRMC data
			for (int i = 0; i < workingData.size(); i++) {
				workingData.get(i).isubtract(theXPDFMetadata.getIncoherentScattering(i));
			}
			
			// Subtract the scaled container data
			XPDFAbsorptionMaps absMaps = theXPDFMetadata.getAbsorptionMaps(coordinates.getGamma(), coordinates.getDelta());
			sampleData = scaleAndSubtractContainers(workingData, absMaps);
			
			// Correct for the quantum efficiency (detector transmission correction)
			sampleData.idivide(transmissionCorrection);
			
			// correct by the absorption of sample-scattered radiation by the sample and container
//			Dataset a_s_sc = 1 - (1-absMaps.getAbsorptionMap(0, 0))*(1-absMaps.getAbsorptionMap(0, 1));
			Dataset a_s_sc = Maths.subtract(1, Maths.subtract(1, absMaps.getAbsorptionMap(0, 0)).imultiply(Maths.subtract(1, absMaps.getAbsorptionMap(0, 1))));
			
			// normalize for number of atoms
			sampleData.idivide(theXPDFMetadata.getSampleIlluminatedAtoms());
			
			// divide by the Thomson electron cross-section and integrate wrt Q²
			double denominator = qSquaredIntegrator.ThomsonIntegral(sampleData);
			double gainCorrection = selfScatteringNumerator/denominator;
			lastGain = currentGain;
			currentGain *= gainCorrection;

		}
		
		
		return new OperationDataForDisplay(sampleData);
	}

	
	private Dataset scaleAndSubtractContainers(List<Dataset> data, XPDFAbsorptionMaps absMaps) {
		if (data.size() > 2)
			throw new IllegalArgumentException("XPDFIncoherentScatteringIterationOperation: more than 2 components not currently supoorted");
		else if (data.size() == 1)
			return data.get(0);
		
		// TODO: implement the general, n > 2 case
		// Ac,sc = 1 - [(1-Ac,s)(1-Ac,c)]
		Dataset absorptionRatio = Maths.subtract(1.0, absMaps.getAbsorptionMap(1, 0)).imultiply(Maths.subtract(1.0,  absMaps.getAbsorptionMap(1, 1))).imultiply(-1).iadd(1);
		absorptionRatio.idivide(absMaps.getAbsorptionMap(1, 1));
		
		return data.get(0).isubtract(absorptionRatio.imultiply(data.get(1)));
	}
}
