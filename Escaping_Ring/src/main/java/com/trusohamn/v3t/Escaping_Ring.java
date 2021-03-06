package com.trusohamn.v3t;


import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.gui.StackWindow;
import ij.process.ImageProcessor;
import net.imagej.ops.OpService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;

import org.scijava.ItemIO;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.thread.ThreadService;
import org.scijava.ui.UIService;

import com.trusohamn.v3t.io.Parameters;
import com.trusohamn.v3t.vascularObjects.Branch;
import com.trusohamn.v3t.vascularObjects.Network;
import com.trusohamn.v3t.vascularObjects.Ring;
import com.trusohamn.v3t.volumes.MyVolume;


@Plugin(type = Command.class, headless = true,menuPath = "Plugins>Vessel3DTracer")
public class Escaping_Ring implements Command {
	@Parameter
	OpService ops;

	@Parameter
	LogService log;

	@Parameter
	UIService ui;

	@Parameter
	CommandService cmd;

	@Parameter
	StatusService status;

	@Parameter
	ThreadService thread;
	
	@Parameter(type = ItemIO.OUTPUT)
	RandomAccessibleInterval<FloatType> myOutput;
	
	public static MyVolume vol; //raw image, not changable in processing
	public static MyVolume workingVol; //raw image, changed during processing
	public static ImageCanvas iC;
	public static ImagePlus imp; //display image
	public static StackWindow imgS;
	public static String imageName;
	public static double pixelWidth;
	public static double pixelHeight;
	public static double voxelDepth;
	private static MyGui dialog = null;

	@Override
	public void run() {

		vol = null;
		workingVol = null;
		iC = null;
		imp = null;
		imgS=null;


		SwingUtilities.invokeLater(() -> {
			if (dialog == null) {
				dialog = new  MyGui();
			}
			dialog.setVisible(true);


		});
	}

	public static void start(Network network, double step, double impInside, double impOutside, double threshold, double branchFacilitator,
			double firstLoop, double secondLoop, double thirdLoop,
			double maxIn, double widthMem, double minOut, double maxOut,
			double checkWorstRings) {

		imp = WindowManager.getCurrentImage();

		if (imp == null) {
			IJ.error("No open image.");
			return;
		}

		Roi roi = imp.getRoi();
		if (roi == null) {
			IJ.error("No selected ROI.");
			return;
		}

		if (roi.getType() != Roi.OVAL){
			IJ.error("No selected Oval ROI.");
			return;
		}

		OvalRoi oval = (OvalRoi)roi;
		Rectangle rect = oval.getBounds();
		int xc = rect.x + rect.width/2;
		int yc = rect.y + rect.height/2;
		double radius = (rect.width + rect.height) / 4;	
		int zc = imp.getSlice();

		Ring.setImpInside(impInside);
		Ring.setImpOutside(impOutside);
		Branch.setEvolveValue(threshold);
		Branch.setBranchFacilitator(branchFacilitator);
		Branch.setFirstLoopElimination(firstLoop);
		Branch.setSecondLoopElimination(secondLoop);
		Branch.setThirdLoopElimination(thirdLoop);
		Branch.stopAll(false);	
		Ring.setParameters(maxIn, widthMem, minOut, maxOut);
		Branch.setCheckWorstRings(checkWorstRings);


		Ring initial = new Ring(xc, yc, zc, 0, 0, 0, radius, step*2);

		if(vol == null) {
			vol = new MyVolume(imp);
			imageName = imp.getTitle();
			MyGui.updateSaveAsWithCurrentImage();
			pixelWidth = imp.getCalibration().pixelWidth;
			pixelHeight = imp.getCalibration().pixelWidth;
			voxelDepth = imp.getCalibration().pixelDepth;
			MyGui.updateLoadedImage();
		}
		if(workingVol == null) workingVol = new MyVolume(imp); 

		Parameters params = new Parameters(imageName, xc, yc, zc, radius,  step,  impInside,
				impOutside, threshold, branchFacilitator, firstLoop,  secondLoop,
				thirdLoop, maxIn, widthMem, minOut, maxOut);
		MyGui.getUsedParameters().add(params);

		generateView(true);

		Ring adjInitial = initial.adjustFirstRing(workingVol);
		IJ.log(" Initial Ring " + adjInitial.getContrast());
		network.recalculateContrast(adjInitial.getContrast());


		new Branch(adjInitial, step);
	}

	public static void start(Network network, Parameters param) {
		imp = WindowManager.getCurrentImage();
		if (imp == null) {
			IJ.error("No open image.");
			return;
		}

		int xc = param.getXc();
		int yc = param.getYc();
		double radius = param.getRadius();	
		int zc = param.getZc();

		Ring.setImpInside(param.getImpInside());
		Ring.setImpOutside(param.getImpOutside());
		Branch.setEvolveValue(param.getThreshold());
		Branch.setBranchFacilitator(param.getBranchFacilitator());
		Branch.setFirstLoopElimination(param.getFirstLoop());
		Branch.setSecondLoopElimination(param.getSecondLoop());
		Branch.setThirdLoopElimination(param.getThirdLoop());
		Branch.stopAll(false);	
		Ring.setParameters(param.getMaxIn(), param.getWidthMem(), param.getMinOut(), param.getMaxOut());


		Ring initial = new Ring(xc, yc, zc, 0, 0, 0, radius, param.getStep()*2);

		if(vol == null) {
			vol = new MyVolume(imp);
			imageName = imp.getTitle();
			MyGui.updateLoadedImage();
		}
		if(workingVol == null) workingVol = new MyVolume(imp); 
		param.setImageName(imageName);
		MyGui.getUsedParameters().add(param);

		generateView(true);

		Ring adjInitial = initial.adjustFirstRing(workingVol);
		IJ.log(" Initial Ring " + adjInitial.getContrast());
		network.recalculateContrast(adjInitial.getContrast());


		new Branch(adjInitial, param.getStep());

	}

	public static Ring trySeedRing(Network network, double step, double impInside, double impOutside, double threshold, double branchFacilitator,
			double firstLoop, double secondLoop, double thirdLoop,
			double maxIn, double widthMem, double minOut, double maxOut,
			double checkWorstRings) {

		imp = WindowManager.getCurrentImage();
		Ring adjInitial = null;
		if (imp == null) {
			IJ.error("No open image.");
			return adjInitial;
		}

		Roi roi = imp.getRoi();
		if (roi == null) {
			IJ.error("No selected ROI.");
			return adjInitial;
		}

		if (roi.getType() != Roi.OVAL){
			IJ.error("No selected Oval ROI.");
			return adjInitial;
		}

		OvalRoi oval = (OvalRoi)roi;
		Rectangle rect = oval.getBounds();
		int xc = rect.x + rect.width/2;
		int yc = rect.y + rect.height/2;
		double radius = (rect.width + rect.height) / 4;	
		int zc = imp.getSlice();

		Ring.setImpInside(impInside);
		Ring.setImpOutside(impOutside);	
		Ring.setParameters(maxIn, widthMem, minOut, maxOut);


		Ring initial = new Ring(xc, yc, zc, 0, 0, 0, radius, step*2);

		if(vol == null) {
			vol = new MyVolume(imp);
			imageName = imp.getTitle();
			MyGui.updateLoadedImage();
		}
		if(workingVol == null) workingVol = new MyVolume(imp); 



		adjInitial = initial.adjustFirstRing(workingVol);

		generateView(true);
		showRings(Arrays.asList(adjInitial));
		Escaping_Ring.iC.repaint();
		
		return adjInitial;
	}

	public static void drawNetwork(Network network){

		for(Branch branch : network) {
			for(Ring ring : branch) {
				ring.drawMeasureArea(iC.getImage(), java.awt.Color.BLUE);
			}
		}
	}

	public static void drawNetworkBranchEndPoints(Network network){
		java.awt.Color normal = java.awt.Color.BLUE;
		java.awt.Color notCorrect = java.awt.Color.ORANGE;
		java.awt.Color branchpoint = java.awt.Color.MAGENTA;
		java.awt.Color endpoint = java.awt.Color.CYAN;


		for(Branch branch : network) {
			Ring ring;

			for(int i = 1; i< branch.size()-1; i++) {
				//other rings
				ring = branch.get(i);
				ring.drawMeasureArea(iC.getImage(), normal);
			}
			//first and last ring
			for(int j : new int[]{0, (branch.size()-1)}){
				ring = branch.get(j);
				if(ring.getBranches().size()>2) ring.drawMeasureArea(iC.getImage(), branchpoint);
				else if(ring.getBranches().size()>1) ring.drawMeasureArea(iC.getImage(), notCorrect);
				else ring.drawMeasureArea(iC.getImage(), endpoint);
			}
		}
	}

	public static void drawBranchBranchEndPoints(Branch branch){
		java.awt.Color normal = java.awt.Color.BLUE;
		java.awt.Color notCorrect = java.awt.Color.ORANGE;
		java.awt.Color branchpoint = java.awt.Color.MAGENTA;
		java.awt.Color endpoint = java.awt.Color.CYAN;

		Ring ring;
		for(int i = 1; i< branch.size()-1; i++) {
			//other rings
			ring = branch.get(i);
			ring.drawMeasureArea(iC.getImage(), normal);
		}
		//first and last ring
		for(int j : new int[]{0, (branch.size()-1)}){
			ring = branch.get(j);
			if(ring.getBranches().size()>2) ring.drawMeasureArea(iC.getImage(), branchpoint);
			else if(ring.getBranches().size()>1) ring.drawMeasureArea(iC.getImage(), notCorrect);
			else ring.drawMeasureArea(iC.getImage(), endpoint);

		}
	}
	public static void drawRingBranchEndPoints(Ring ring){
		java.awt.Color normal = java.awt.Color.BLUE;
		java.awt.Color notCorrect = java.awt.Color.ORANGE;
		java.awt.Color branchpoint = java.awt.Color.MAGENTA;
		java.awt.Color endpoint = java.awt.Color.CYAN;

		if(ring.getBranches().size()==1) {
			int index = ring.getBranches().get(0).indexOf(ring);
			if( index == 0 || index == ring.getBranches().get(0).size()-1) ring.drawMeasureArea(iC.getImage(), endpoint);
			else ring.drawMeasureArea(iC.getImage(), normal);

		}
		else if(ring.getBranches().size()>2) ring.drawMeasureArea(iC.getImage(), branchpoint);
		else if(ring.getBranches().size()>1) ring.drawMeasureArea(iC.getImage(), notCorrect);

	}

	public static void showResult(DefaultListModel<Branch> branchList){
		for(int i=0; i< branchList.getSize(); i++){
			Branch branch = branchList.getElementAt(i);
			for(Ring ring : branch) {
				ring.drawMeasureArea(iC.getImage(), java.awt.Color.RED);
			}
		}
	}
	
	public static void showResult(List<Branch> branchList){
		for(int i=0; i< branchList.size(); i++){
			Branch branch = branchList.get(i);
			for(Ring ring : branch) {
				ring.drawMeasureArea(iC.getImage(), java.awt.Color.RED);
			}
		}
	}

	public static void showRings(DefaultListModel<Ring> ringList){
		for(int i=0; i< ringList.getSize(); i++){
			Ring ring = ringList.getElementAt(i);
			ring.drawMeasureArea(iC.getImage(), java.awt.Color.GREEN);
		}
	}

	public static void showRings(List<Ring> ringList){
		for(int i=0; i< ringList.size(); i++){
			Ring ring = ringList.get(i);
			ring.drawMeasureArea(iC.getImage(), java.awt.Color.GREEN);
		}
	}

	/*
	private double[] unit(double[] u) {
		double norm = 0.0;
		for(int i=0; i<u.length; i++)
			norm += u[i]*u[i];
		norm = Math.sqrt(norm);
		return new double[] {u[0]/norm, u[1]/norm, u[2]/norm};	
	}
	 */

	public void drawCenterLine(MyVolume myVolume, Ring ring) {

		double angles[] = ring.getAnglesFromDirection();
		double sint = Math.sin(angles[0]);
		double cost = Math.cos(angles[0]);
		double sinp = Math.sin(angles[1]);
		double cosp = Math.cos(angles[1]);
		double R[][] = 
			{{cosp*cost, -sinp, cosp*sint},
					{sinp*cost, cosp, sinp*sint},
					{-sint, 0, cost}};
		int i = 0;
		int j = 0;
		for(int k=0; k<=10; k++) {
			double dx = i*R[0][0] + j*R[0][1] + k*R[0][2];
			double dy = i*R[1][0] + j*R[1][1] + k*R[1][2];
			double dz = i*R[2][0]  + k*R[2][2];
			myVolume.setValue(ring.getC(), dx, dy, dz, 1000);
		}
	}

	public static void generateView(boolean setVisible){
		if(iC==null || Escaping_Ring.imgS.isVisible() == false){
			imp = new ImagePlus("Vessel3DTracer", vol.createImageStackFrom3DArray());
			imp.setDisplayMode(IJ.COLOR);
			iC = new ImageCanvas(imp);
			imgS = new StackWindow (imp, iC);			
		}
		iC.setVisible(true);

	}

	public static void updateImgWithVol(ImagePlus img) {
		for(int z=0; z<vol.nz; z++) {
			ImageProcessor ip = img.getStack().getProcessor(z+1);

			for(int x=0; x<vol.nx; x++){
				for(int y=0; y<vol.ny; y++) {			
					//ip.setValue((double)this.data[x][y][z]);
					//ip.drawPixel(x, y );
					ip.putPixelValue(x, y, vol.data[x][y][z]);
				}
			}

		}
	}

}
