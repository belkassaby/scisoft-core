package org.dawnsci.surfacescatter;

import org.dawnsci.surfacescatter.AnalaysisMethodologies.FitPower;
import org.dawnsci.surfacescatter.AnalaysisMethodologies.Methodology;
import org.dawnsci.surfacescatter.TrackingMethodology.TrackerType1;

public class FittingParameters {

	private int pt0;
	private int pt1;
	private int len0;
	private int len1;
	private Methodology bgMethod;
	private TrackerType1 tracker;
	private FitPower fitPower;
	private int boundaryBox;
	private int[][] lenpt = new int[2][2];
	private int sliderPos;
	private String file;
	private double xValue;
	private boolean useNegativeQ;
	
	public int getPt0() {
		return pt0;
	}
	public void setPt0(int pt0) {
		this.pt0 = pt0;
		lenpt[1][0]=pt0;
	}
	public int getPt1() {
		return pt1;
		
	}
	public void setPt1(int pt1) {
		this.pt1 = pt1;
		lenpt[1][1]=pt1;
	}
	public int getLen0() {
		return len0;
		
	}
	public void setLen0(int len0) {
		this.len0 = len0;
		lenpt[0][0]=len0;
	}
	public int getLen1() {
		return len1;
	}
	public void setLen1(int len1) {
		this.len1 = len1;
		lenpt[0][1]=len1;
	}
	public Methodology getBgMethod() {
		return bgMethod;
	}
	public void setBgMethod(Methodology bgMethod) {
		this.bgMethod = bgMethod;
	}
	
	public TrackerType1 getTracker() {
		return tracker;
	}
	public void setTracker(TrackerType1 tracker) {
		this.tracker = tracker;
	}
	public FitPower getFitPower() {
		return fitPower;
	}
	public void setFitPower(FitPower fitPower) {
		this.fitPower = fitPower;
	}
	public int getBoundaryBox() {
		return boundaryBox;
	}
	public void setBoundaryBox(int boundaryBox) {
		this.boundaryBox = boundaryBox;
	}
	public int[][] getLenpt() {
		return lenpt;
	}
	public void setLenpt(int[][] lenpt) {
		this.lenpt = lenpt;
	}
	public int getSliderPos() {
		return sliderPos;
	}
	public void setSliderPos(int sliderPos) {
		this.sliderPos = sliderPos;
	}
	public String getFile() {
		return file;
	}
	public void setFile(String file) {
		this.file = file;
	}
	public void setXValue(double xValue){
		this.xValue = xValue;
	}
	public double getXValue(){
		return xValue;
	}
	public boolean getUseNegativeQ() {
		return useNegativeQ;
	}
	public void setUseNegativeQ(boolean useNegativeQ) {
		this.useNegativeQ = useNegativeQ;
	}
	
}
