package org.dawnsci.surfacescatter;

import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperation;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.LinearAlgebra;
import org.eclipse.january.dataset.Maths;

import uk.ac.diamond.scisoft.analysis.fitting.functions.Polynomial2D;

public class OverlappingBgBoxUsingIOperation 
	extends AbstractOperation<SecondConstantROIBackgroundSubtractionModel, OperationData> {

	private static Polynomial2D g2;
		private static Dataset output;
		private DoubleDataset in1Background;
		private int[][] newOffsetLenPt;
		private static int DEBUG =1;
		
		@Override
		public String getId() {
			return "uk.ac.diamond.scisoft.surfacescatter.OverlappingBgBoxUsingIOperation";
		}

		@Override
		public OperationRank getInputRank() {
			return OperationRank.TWO ;
		}

		@Override
		public OperationRank getOutputRank() {
			return OperationRank.TWO ;
		}
		
		
		public OperationData process (IDataset input, IMonitor monitor) 
				throws OperationException {
			
			g2 =null;
			
			int[] len = model.getLenPt()[0];
			int[] pt = model.getLenPt()[1];
			
			debug("pt[0]: " + pt[0]);
			debug("pt[1]: " + pt[1]);
		
			if (g2 == null){
				g2 = new Polynomial2D(AnalaysisMethodologies.toInt(model.getFitPower()));
			}
			
			if ((int) Math.pow(AnalaysisMethodologies.toInt(model.getFitPower()) + 1, 2) != g2.getNoOfParameters()){
				g2 = new Polynomial2D(AnalaysisMethodologies.toInt(model.getFitPower()));
			}
			
			Dataset in1 = BoxSlicerRodScanUtilsForDialog.rOIBox(input,len, pt);
			
	        int[] backLen = model.getBackgroundLenPt()[0];
	        int[] backPt = model.getBackgroundLenPt()[1];
	        
	        if(model.getBoxOffsetLenPt() != null){
				
				int[] offsetLen = model.getBoxOffsetLenPt()[0];
				int[] offsetPt = model.getBoxOffsetLenPt()[1];
				
				int pt0 = pt[0] + offsetPt[0];
				int pt1 = pt[1] + offsetPt[1];
				backPt = new int[] {pt0, pt1}; 
				
				
				int len0 = len[0] + offsetLen[0];
				int len1 = len[1] + offsetLen[1];
				backLen = new int[] {len0, len1}; 
				
	        }
	        
	        debug("backPt[0]: " + backPt[0]);
			debug("backPt[1]: " + backPt[1]);
			
			debug("backLen[0]: " + backLen[0]);
			debug("backLen[1]: " + backLen[1]);
		
	        BackgroundRegionArrays br = new BackgroundRegionArrays();
	        
			for (int j = backPt[0]; j<backPt[0]+backLen[0]; j++){
				for(int i = backPt[1]; i<backPt[1]+backLen[1]; i++){
					
					if((j<pt[0]||j>=(pt[0]+len[0]))||(i<pt[1]||i>=(pt[1]+len[1]))){
						br.xArrayAdd(j);
						br.yArrayAdd(i);
						br.zArrayAdd(input.getDouble(i,j));
					}
					else{

					}
				}
			}
			
			Dataset xBackgroundDat = DatasetFactory.createFromObject(br.getXArray());
			Dataset yBackgroundDat = DatasetFactory.createFromObject(br.getYArray());
			Dataset zBackgroundDat = DatasetFactory.createFromObject(br.getZArray());
			
			Dataset matrix = LinearLeastSquaresServicesForDialog.polynomial2DLinearLeastSquaresMatrixGenerator(
					AnalaysisMethodologies.toInt(model.getFitPower()), xBackgroundDat, yBackgroundDat);
			double[] location = null;
	              	   
	        DoubleDataset test = (DoubleDataset)LinearAlgebra.solveSVD(matrix, zBackgroundDat);
			double[] params = test.getData();

			g2.setParameterValues(params);
			
			double[] d = params;
			int[] len1 = len;
			int[][] boxOffsetLenPt = model.getBoxOffsetLenPt();

			in1Background = g2.getOutputValuesOverlapping(params, len, pt,
					AnalaysisMethodologies.toInt(model.getFitPower()));
					
			Dataset pBackgroundSubtracted = DatasetFactory.zeros(new int[] {2}, Dataset.ARRAYFLOAT64);
			
			try{
				pBackgroundSubtracted = Maths.subtract(in1, in1Background, null);
			}
			catch(Exception e){
				debug("error in second overlapping background subtraction IOperation");
			}
			
				
			output = DatasetUtils.cast(pBackgroundSubtracted, Dataset.FLOAT64);
				
			output.setName("Region of Interest, constant background removed");

			return new OperationData(output, 
									 location, 
									 null,
									 in1Background,
									 newOffsetLenPt);
			
		}
	
		private void debug (String output) {
			if (DEBUG == 1) {
				System.out.println(output);
			}
		}
		
}
