package uk.ac.diamond.scisoft.analysis.powder.indexer;

import org.eclipse.january.dataset.IDataset;

public class NtreorPowderIndexerData  {
//
//	@Override
//	protected void setName() {
//		this.indexerName = "ntreor";
//	}
//
//	@Override
//	protected String getFormatedIndexerParam(IPowderIndexerParam param) {
//		return param.getName() + "=" + param.getValue() + ",";
//	}
//	
//	public String formatPeakData(IDataset peaks){
//		//TODO: validate
//		String peaksFormatted = null;
//		
//		for (int i = 0; i < peaks.getSize(); ++i) {
//			double d = peaks.getDouble(i);
//			peaksFormatted += String.valueOf(d) + "\n";
//		}
//		
//		return peaksFormatted;
//	}
	
	//The old func
	//	public void generateIndexFile(String fullPathFile) {
//		try {
//			PrintWriter writer = new PrintWriter(fullPathFile, "UTF-8");
//
//			writer.println(outFileTitle);
//
//			// write in the peak data
//			for (int i = 0; i < peakData.getSize(); ++i) {
//				double d = peakData.getDouble(i);
//				writer.println(String.valueOf(d));
//			}
//
//			writer.println();
//
//			for (Entry<String, String> entry : stdKeyVals.entrySet()) {
//				String key = entry.getKey();
//				Object value = entry.getValue();
//				writer.println(key + "=" + value + ",");
//			}
//
//			// finish file
//			writer.println("END*");
//			writer.println("0.00");
//			writer.close();
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//

}
