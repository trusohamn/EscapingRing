import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.gui.StackWindow;
import ij.plugin.PlugIn;

import java.awt.Rectangle;

import javax.swing.DefaultListModel;



public class Espacing_Ring implements PlugIn {
	static Volume vol; //raw image, not changable in processing
	static Volume workingVol; //raw image, changed during processing
	static ImageCanvas iC;
	static ImagePlus imp; //display image
	static StackWindow imgS;
	static String imageName;

	@Override
	public void run(String arg0) {

		vol = null;
		workingVol = null;
		iC = null;
		imp = null;
		imgS=null;
		
		Gui dialog = new Gui();
		dialog.setVisible(true);

		//start();

	}

	public static void start(Network network, double step, double impInside, double impOutside, double threshold, double branchFacilitator,
			double firstLoop, double secondLoop, double thirdLoop,
			double maxIn, double minMem, double maxMem, double minOut, double maxOut,
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
		Ring.setParameters(maxIn, minMem, maxMem, minOut, maxOut);
		Branch.setCheckWorstRings(checkWorstRings);


		Ring initial = new Ring(xc, yc, zc, 0, 0, 0, radius, step*2);

		if(vol == null) {
			vol = new Volume(imp);
			imageName = imp.getTitle();
			Gui.updateLoadedImage();
		}
		if(workingVol == null) workingVol = new Volume(imp); 

		Parameters params = new Parameters(imageName, xc, yc, zc, radius,  step,  impInside,
				 impOutside, threshold, branchFacilitator, firstLoop,  secondLoop,
				thirdLoop, maxIn, minMem,  maxMem, minOut, maxOut);
		Gui.usedParameters.add(params);
		
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
		Ring.setParameters(param.getMaxIn(), param.getMinMem(), param.getMaxMem(), param.getMinOut(), param.getMaxOut());


		Ring initial = new Ring(xc, yc, zc, 0, 0, 0, radius, param.getStep()*2);

		if(vol == null) {
			vol = new Volume(imp);
			imageName = imp.getTitle();
			Gui.updateLoadedImage();
		}
		if(workingVol == null) workingVol = new Volume(imp); 
		param.setImageName(imageName);
		Gui.usedParameters.add(param);
		
		generateView(true);

		Ring adjInitial = initial.adjustFirstRing(workingVol);
		IJ.log(" Initial Ring " + adjInitial.getContrast());
		network.recalculateContrast(adjInitial.getContrast());


		new Branch(adjInitial, param.getStep());

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
		java.awt.Color branchpoint = java.awt.Color.MAGENTA;
		java.awt.Color endpoint = java.awt.Color.CYAN;

		for(Branch branch : network) {
			//first ring
			Ring ring = branch.get(0);
			if(ring.getBranches().size()>1) ring.drawMeasureArea(iC.getImage(), branchpoint);
			else ring.drawMeasureArea(iC.getImage(), endpoint);
			
			for(int i = 1; i< branch.size()-1; i++) {
				//other rings
				ring = branch.get(i);
				ring.drawMeasureArea(iC.getImage(), normal);
			}
			//last ring
			ring = branch.get(branch.size()-1);
			if(ring.getBranches().size()>1) ring.drawMeasureArea(iC.getImage(), branchpoint);
			else ring.drawMeasureArea(iC.getImage(), endpoint);
		}
	}

	public static void showResult(DefaultListModel<Branch> branchList){
		for(int i=0; i< branchList.getSize(); i++){
			Branch branch = branchList.getElementAt(i);
			for(Ring ring : branch) {
				ring.drawMeasureArea(iC.getImage(), java.awt.Color.RED);
			}
		}
	}

	public static void showRings(DefaultListModel<Ring> ringList){
		for(int i=0; i< ringList.getSize(); i++){
			Ring ring = ringList.getElementAt(i);
			ring.drawMeasureArea(iC.getImage(), java.awt.Color.YELLOW);
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

	public void drawCenterLine(Volume volume, Ring ring) {

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
			volume.setValue(ring.getC(), dx, dy, dz, 1000);
		}
	}

	public static void generateView(boolean setVisible){
		if(iC==null || Espacing_Ring.imgS.isVisible() == false){
			imp = new ImagePlus("VascRing3D", vol.createImageStackFrom3DArray());
			imp.setDisplayMode(IJ.COLOR);
			iC = new ImageCanvas(imp);
			imgS = new StackWindow (imp, iC);			
		}
		iC.setVisible(true);

	}
}
