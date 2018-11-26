package com.trusohamn.v3t.io;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.trusohamn.v3t.MyGui;

import ij.IJ;

public class Parameters {
	String imageName;
	int xc, yc, zc;
	double radius;
	double step,  impInside,  impOutside,  threshold, branchFacilitator;
	double firstLoop,  secondLoop,  thirdLoop;
	double maxIn, widthMem,  minOut,  maxOut;

	public Parameters(String imageName, double step, double impInside,
			double impOutside, double threshold, double branchFacilitator, double firstLoop, double secondLoop,
			double thirdLoop, double maxIn, double widthMem, double minOut, double maxOut) {
		this.step = step;
		this.impInside = impInside;
		this.impOutside = impOutside;
		this.threshold = threshold;
		this.branchFacilitator = branchFacilitator;
		this.firstLoop = firstLoop;
		this.secondLoop = secondLoop;
		this.thirdLoop = thirdLoop;
		this.maxIn = maxIn;
		this.widthMem = widthMem;
		this.minOut = minOut;
		this.maxOut = maxOut;
		this.imageName = imageName;
	}

	public Parameters(String imageName, int xc, int yc, int zc, double radius, double step, double impInside,
			double impOutside, double threshold, double branchFacilitator, double firstLoop, double secondLoop,
			double thirdLoop, double maxIn, double widthMem, double minOut, double maxOut) {
		this(imageName,  step,  impInside,
				impOutside, threshold, branchFacilitator, firstLoop,  secondLoop,
				thirdLoop, maxIn, widthMem, minOut, maxOut);
		this.xc = xc;
		this.yc = yc;
		this.zc = zc;
		this.radius = radius;
	}

	public Parameters(String imgName, double[] params){
		if(params.length == 17){
			this.imageName = imgName;
			this.xc = (int) params[0];
			this.yc = (int) params[1];
			this.zc = (int) params[2];
			this.radius = params[3];
			this.step = params[4];
			this.impInside = params[5];
			this.impOutside = params[6];
			this.threshold = params[7];
			this.branchFacilitator = params[8];
			this.firstLoop = params[9];
			this.secondLoop = params[10];
			this.thirdLoop = params[11];
			this.maxIn = params[12];
			this.widthMem = params[13];
			this.minOut = params[14];
			this.maxOut = params[15];
		}
	}

	public List<String>  listParams(){
		List<String> row = Arrays.asList(String.valueOf(imageName), String.valueOf(xc), String.valueOf(yc), String.valueOf(zc), String.valueOf(radius), String.valueOf(step),
				String.valueOf(impInside), String.valueOf(impOutside), String.valueOf(threshold), String.valueOf(branchFacilitator), String.valueOf(firstLoop),
				String.valueOf(secondLoop), String.valueOf(thirdLoop), String.valueOf(maxIn), String.valueOf(widthMem), String.valueOf(minOut), String.valueOf(maxOut));


		return row;
	}
	
	public static void exportParams(String csvFile) throws IOException{
		List<String> header = Arrays.asList("imageName", "xc", "yc", "zc", "radius", "step",  "impInside", 
				"impOutside",  "threshold", "branchFacilitator","firstLoop",  "secondLoop",  "thirdLoop",
				"maxIn", "widthMem",  "minOut",  "maxOut");
		List<List<String>> data = new ArrayList<List<String>>();

		FileWriter writer = new FileWriter(csvFile);

		for(Parameters p : MyGui.getUsedParameters()) {
			List<String> row = p.listParams();
			IJ.log(row.toString());
			data.add(row);
		}
		CSVUtils.writeLine(writer, header);
		for(List<String> row : data){
			CSVUtils.writeLine(writer, row);
		}
		writer.flush();
		writer.close();
	}

	public static ArrayList<Parameters> importParams(String csvFile) {
		ArrayList<Parameters> output = new ArrayList<Parameters>();
		String line = "";
		String cvsSplitBy = String.valueOf(CSVUtils.getDEFAULT_SEPARATOR());

		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
			line = br.readLine(); //header
			IJ.log(line);
			while ((line = br.readLine()) != null) {
				IJ.log(line);
				// use comma as separator
				String[] params = line.split(cvsSplitBy);
				String imgName = params[0];
				double[] paramsD = new double[params.length-1];
				for(int i = 1; i<params.length; i++){
					paramsD[i-1]= Double.parseDouble(params[i]);
				}
				Parameters p = new Parameters(imgName, paramsD);
				output.add(p);       
			}

		} catch (IOException e) {
			e.printStackTrace();
			IJ.log(e.toString());
		}

		return output;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public int getXc() {
		return xc;
	}

	public void setXc(int xc) {
		this.xc = xc;
	}

	public int getYc() {
		return yc;
	}

	public void setYc(int yc) {
		this.yc = yc;
	}

	public int getZc() {
		return zc;
	}

	public void setZc(int zc) {
		this.zc = zc;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getStep() {
		return step;
	}

	public void setStep(double step) {
		this.step = step;
	}

	public double getImpInside() {
		return impInside;
	}

	public void setImpInside(double impInside) {
		this.impInside = impInside;
	}

	public double getImpOutside() {
		return impOutside;
	}

	public void setImpOutside(double impOutside) {
		this.impOutside = impOutside;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public double getBranchFacilitator() {
		return branchFacilitator;
	}

	public void setBranchFacilitator(double branchFacilitator) {
		this.branchFacilitator = branchFacilitator;
	}

	public double getFirstLoop() {
		return firstLoop;
	}

	public void setFirstLoop(double firstLoop) {
		this.firstLoop = firstLoop;
	}

	public double getSecondLoop() {
		return secondLoop;
	}

	public void setSecondLoop(double secondLoop) {
		this.secondLoop = secondLoop;
	}

	public double getThirdLoop() {
		return thirdLoop;
	}

	public void setThirdLoop(double thirdLoop) {
		this.thirdLoop = thirdLoop;
	}

	public double getMaxIn() {
		return maxIn;
	}

	public void setMaxIn(double maxIn) {
		this.maxIn = maxIn;
	}

	public double getWidthMem() {
		return widthMem;
	}

	public void setWidthMem(double widthMem) {
		this.widthMem = widthMem;
	}

	public double getMinOut() {
		return minOut;
	}

	public void setMinOut(double minOut) {
		this.minOut = minOut;
	}

	public double getMaxOut() {
		return maxOut;
	}

	public void setMaxOut(double maxOut) {
		this.maxOut = maxOut;
	}

	@Override
	public String toString() {
		return "Parameters [imageName=" + imageName + ", xc=" + xc + ", yc=" + yc + ", zc=" + zc + ", radius=" + radius
				+ ", step=" + step + ", impInside=" + impInside + ", impOutside=" + impOutside + ", threshold="
				+ threshold + ", branchFacilitator=" + branchFacilitator + ", firstLoop=" + firstLoop + ", secondLoop="
				+ secondLoop + ", thirdLoop=" + thirdLoop + ", maxIn=" + maxIn + ", widthMem=" + widthMem + 
				", minOut=" + minOut + ", maxOut=" + maxOut + "]";
	}

}
