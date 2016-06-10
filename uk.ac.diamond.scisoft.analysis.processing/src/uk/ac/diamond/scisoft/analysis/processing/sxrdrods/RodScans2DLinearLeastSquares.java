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
import org.eclipse.dawnsci.analysis.api.image.IImageFilterService;
import org.eclipse.dawnsci.analysis.api.monitor.IMonitor;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.IndexIterator;
import org.eclipse.dawnsci.analysis.dataset.impl.Maths;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperation;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;

import uk.ac.diamond.scisoft.analysis.fitting.functions.Polynomial2D;
import uk.ac.diamond.scisoft.analysis.optimize.AbstractOptimizer;
import uk.ac.diamond.scisoft.analysis.optimize.ApacheOptimizer;
import uk.ac.diamond.scisoft.analysis.optimize.ApacheOptimizer.Optimizer;
import uk.ac.diamond.scisoft.analysis.optimize.IOptimizer;
import uk.ac.diamond.scisoft.analysis.optimize.LinearLeastSquares;
import uk.ac.diamond.scisoft.analysis.processing.operations.utils.OperationServiceLoader;

public class RodScans2DLinearLeastSquares extends AbstractOperation<RodScanPolynomial2DModel, OperationData> {

	private Polynomial2D g2;
	
	@Override
	public String getId() {
		return "uk.ac.diamond.scisoft.analysis.processing.sxrdrods.RodScans2DLinearLeastSquares";
	}
		
	@Override
	public OperationRank getInputRank() {
		return OperationRank.TWO ;
	}

	@Override
	public OperationRank getOutputRank() {
		return OperationRank.TWO ;
	}
		
	@Override
	protected OperationData process(IDataset input, IMonitor monitor) {
			
		System.out.println("~~~~~~~~~~~restart~~~~~~~~~~~~~~~");
		
		RectangularROI box = model.getBox();
		
		Dataset in1 = BoxSlicerRodScans.rOIBox(input, monitor, box.getIntLengths(), box.getIntPoint());
				
		IOptimizer optimizer = new ApacheOptimizer(Optimizer.LEVENBERG_MARQUARDT);
		
		//LevenbergMarquardtOptimizer optimizer1 = new LevenbergMarquardtOptimizer();
		
		AbstractOptimizer optimizer2 = new ApacheOptimizer(Optimizer.LEVENBERG_MARQUARDT);
		
		//double[] initialSolution = {1, 1, 1};
		
		//optimizer2.setParameterValues(parameters);
		
		//optimizer1.
		
		Polynomial2D g1 = new Polynomial2D(1);
		if (g2 == null ) g2 = new Polynomial2D(model.getFitPower());
		if ((int) Math.pow(model.getFitPower()+1, 2) != g2.getNoOfParameters()) g2 = new Polynomial2D(model.getFitPower());
		
		Dataset[] fittingBackground = BoxSlicerRodScans2D.LeftRightTopBottomBoxes(input, monitor,
				box.getIntLengths(), box.getIntPoint(), model.getBoundaryBox());
		
		Dataset[] subSetBackground = BoxSlicerRodScans2D.subRange(input, monitor,
				box.getIntLengths(), box.getIntPoint(), model.getBoundaryBox(), 1);
		
		Dataset[] subSetBackground2 = BoxSlicerRodScans2D.subRange(input, monitor,
				box.getIntLengths(), box.getIntPoint(), model.getBoundaryBox(), 2);
		
		
		double[] d = new double[(int) Math.pow(2*model.getFitPower()+1,2)];
		
		long startTime = System.currentTimeMillis();

////////////////set up the first sub range fit /////////////		
		
		try {
			optimizer.optimize(new Dataset[]{subSetBackground[0], subSetBackground[1]}, 
					subSetBackground[2], g2);
		}
		catch (Exception e) {
				System.out.println("Fitting failed at 1");
		}	
		
		d = g2.outputParameters();
		
		//System.out.println("check sub range output@@@@@");
		//g2.checkFittingParameters();
		//System.out.println("d to string: @"+ d.toString());
		
		
		optimizer2.setNumberOfParameters((int) (Math.pow(1+model.getFitPower(),2)), d, g2);
		
		
		System.out.println("check sub range 1 output@@@@@");
		g2.checkFittingParameters();
		
		
		
////////////////set up the second sub range fit /////////////
		
		try {
			optimizer2.optimize(new Dataset[]{subSetBackground2[0], subSetBackground2[1]}, 
					subSetBackground2[2], g2);
		}
		catch (Exception e) {
				System.out.println("Fitting failed at 1");
		}
		
		d = g2.outputParameters();
		
		System.out.println("check sub range 2 output@@@@@");
		g2.checkFittingParameters();
		//System.out.println("d to string: @"+ d.toString());
			
		optimizer2.setNumberOfParameters((int) (Math.pow(1+model.getFitPower(),2)), d, g2);
		
		optimizer2.setParameterValues(d);
		
		long subRangeTime = System.currentTimeMillis();
		
		long subRangeElapsedTime = subRangeTime-startTime;
		System.out.println("@@@@@@@@@@@@optimizer subRangeTime:  " + subRangeElapsedTime+"  ########~~~~#####");
		
//////////////full range of data fit, limited to plane (params 0-3)/////////////		
		
		double[] dsub = new double[4];
		
		dsub[0]=d[0];
		dsub[1]=d[1];
		dsub[2]=d[2];
		dsub[3]=d[3];
		
		optimizer2.setNumberOfParameters(4, dsub, g1);
		
		try{	
			optimizer2.optimize(new Dataset[]{fittingBackground[0], fittingBackground[1]}, 
					fittingBackground[2], g1);
			}
		catch (Exception e) {
				System.out.println("Fitting failed");
			}	
		
		dsub = g1.outputParameters();
		
////////////////////all-up fit		
		
		double[] f = new double[d.length];

		f[0] = dsub[0];
		f[1] = dsub[1];
		f[2] = dsub[2];
		f[3] = dsub[3];
		
		for (int k = 4; k<d.length; k++){
			f[k] = d[k];
		}
		
		optimizer2.setNumberOfParameters((int) Math.pow(1+model.getFitPower(),2), f, g2);
		
		try{	
			optimizer2.optimize(new Dataset[]{fittingBackground[0], fittingBackground[1]}, 
					fittingBackground[2], g2);
			}
		catch (Exception e) {
				System.out.println("Fitting failed");
			}	
		
		System.out.println("check full range output@@@@@");
		g2.checkFittingParameters();
		
		long endTime = System.currentTimeMillis();
		
		long elapsedTime = endTime-startTime;
		System.out.println("@@@@@@@@@@@@optimizer elapsed time:  " + elapsedTime+"  ########~~~~#####");
		
		
////////////////////////////////////////////////////end of fitting flailing
		
		
////////////////////////////////////////////////////linear least squares test and comparison
		
		long linearLeastStartTime = System.currentTimeMillis();
		System.out.println("Started the Linear Least Squares Method");
		
		LinearLeastSquares tFit = new LinearLeastSquares(10000);
		double[] testOutput = new double[(int) Math.pow(model.getFitPower(), 2)];
		
		Dataset offset = DatasetFactory.ones(fittingBackground[2].getShape(), Dataset.FLOAT64);
		
		Dataset intermediateFitTest  = Maths.add(offset, fittingBackground[2]);
		
		Dataset matrix = LinearLeastSquaresServicesForSXRD.polynomial2DLinearLeastSquaresMatrixGenerator
				(model.getFitPower(), fittingBackground[0], fittingBackground[1]);
		
		Dataset zSigma = LinearLeastSquaresServicesForSXRD.polynomial2DLinearLeastSquaresSigmaGenerator
				(fittingBackground[2]);
		
		testOutput = tFit.solve(matrix, intermediateFitTest, zSigma);

		System.out.println("linear last squares output:");
		
		for (int i=0; i<((int) Math.pow(model.getFitPower(), 2)); i++){
			System.out.println("d [" + i + "]:  " + testOutput[i]);
		}
		
		long linearLeastEndTime  = System.currentTimeMillis();
		
		long linearLeastElapsedTime = linearLeastStartTime -linearLeastEndTime;
		System.out.println("@@@@@@@@@@@@linear least elapsed time:  " + linearLeastElapsedTime+"  ########~~~~#####");
		
		
///////////////////////////////////////////////////////////////		
		
		
		
		DoubleDataset in1Background = g2.getOutputValues(box.getIntLengths(), 
				model.getBoundaryBox(), model.getFitPower());
		
		IndexIterator it = in1Background.getIterator();
		
		while (it.hasNext()) {
			double v = in1Background.getElementDoubleAbs(it.index);
			if (v < 0) in1Background.setObjectAbs(it.index, 0);
		}

//		System.out.println("### in1.getShape()[0]: " + in1.getShape()[0]+ "  in1.getShape()[1]:  "+ in1.getShape()[1] + "  @@@@@@");
//		System.out.println("### in1Background.getShape()[0]: " + in1Background.getShape()[0]+ "  in1Background.getShape()[1]:  "+ in1Background.getShape()[1] + "  @@@@@@");
		
		
		Dataset pBackgroundSubtracted = Maths.subtract(in1, in1Background, null);

		pBackgroundSubtracted.setName("pBackgroundSubtracted");

		Dataset correction = Maths.multiply(RodScanCorrections.lorentz(input), RodScanCorrections.areacor(input
				, model.getBeamCor(), model.getSpecular(),  model.getSampleSize()
				, model.getOutPlaneSlits(), model.getInPlaneSlits(), model.getBeamInPlane()
				, model.getBeamOutPlane(), model.getScalingFactor()));

		correction = Maths.multiply(RodScanCorrections.polarisation(input, model.getInPlanePolarisation()
				, model.getOutPlanePolarisation()), correction);
		
		correction = Maths.multiply(model.getScalingFactor(), correction);
			
		pBackgroundSubtracted = Maths.multiply(pBackgroundSubtracted,correction);
		
		IndexIterator it1 = pBackgroundSubtracted.getIterator();
		
		while (it1.hasNext()) {
			double q = pBackgroundSubtracted.getElementDoubleAbs(it1.index);
			if (q < 0) pBackgroundSubtracted.setObjectAbs(it1.index, 0);
		}
		
		IImageFilterService service = OperationServiceLoader.getImageFilterService(); 
		
		IDataset otsuTest = service.globalOtsuThreshold(pBackgroundSubtracted, false, true);
		
		Dataset otsuTestOut = DatasetUtils.cast(otsuTest, Dataset.FLOAT64);
		
		Dataset fhkl = Maths.sqrt(pBackgroundSubtracted);
		
		Dataset in1Sum = DatasetFactory.createFromObject(in1.sum());
		Dataset in1BackgroundSum = DatasetFactory.createFromObject(in1Background.sum());
 		Dataset pBackgroundSubtractedSum = DatasetFactory.createFromObject(pBackgroundSubtracted.sum());
		Dataset fhklSum = DatasetFactory.createFromObject(fhkl.sum());
 	 		
		in1.setName("Region of Interest");
		in1Background.setName("Polynomial background");
		pBackgroundSubtracted.setName("Signal after polynomial background subtracted");
		fhkl.setName("fhkl");
		otsuTestOut.setName("Otsu test");
		
		
		in1Sum.setName("Region of Interest Summed") ;
		in1BackgroundSum.setName("Polynomial background summed");
		pBackgroundSubtractedSum.setName("Signal after polynomial background subtracted summed");
		fhklSum.setName("fhkl summed");
		
		return new OperationData(in1, in1Background, pBackgroundSubtracted, fhkl, in1Sum, in1BackgroundSum, pBackgroundSubtractedSum, fhklSum, otsuTestOut);
//, otsuTestOut
	}

}
