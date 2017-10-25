import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.plugin.PlugIn;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;


public class Espacing_Ring implements PlugIn {

	Network network = new Network();

	@Override
	public void run(String arg0) {
		IJ.log("start");

		ImagePlus imp = WindowManager.getCurrentImage();

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
		int radius = (rect.width + rect.height) / 4;	
		int zc = imp.getSlice();



		GenericDialog dlg = new GenericDialog("Espacing Ring");
		dlg.addNumericField("Progression step (in pixels)", 20, 0);
		dlg.showDialog();
		if (dlg.wasCanceled())
			return;

		double step = dlg.getNextNumber();


		Ring initial = new Ring(xc, yc, zc, 0, 0, 0, radius);
		IJ.log(" Initial Ring " + initial);
		Volume test = new Volume(imp.getWidth(), imp.getHeight(), imp.getNSlices());
		//drawMeasureArea(test, initial, step);
		Volume vol = new Volume(imp);	
		Volume workingVol = new Volume(imp); //will be erased
		
		Ring adjInitial = adjustFirstRing(initial, vol, step);
		Branch firstBranch = new Branch(adjInitial, vol, test, workingVol, step);
		//drawMeasureArea(test, adjInitial, step);

		
		
		vol.showTwoChannels("Result", test);

	}


	private Ring adjustFirstRing(Ring ring, Volume vol, double step) {
		Ring bestCand = null;	
		double maxContrast = -Double.MAX_VALUE;
		double angleStep = Math.PI/12;

		double initRadius = ring.radius;	
		double maxRadius = 1.25;
		double maxMeasurmentArea = 2;

		for(double dt = -Math.PI; dt<=Math.PI; dt+=angleStep) {
			for(double dp = -Math.PI/2; dp<=Math.PI/2; dp+=angleStep) {
				//return the MeasurmentVolume
				Ring maxRing = ring.duplicate();
				maxRing.radius = initRadius*maxRadius*maxMeasurmentArea;
				maxRing.dir = maxRing.getDirectionFromSphericalAngles( dt,  dp);
				MeasurmentVolume mv = new MeasurmentVolume(vol, maxRing, step);
				//IJ.log(mv.toString());
				for(double r = initRadius*0.90; r<initRadius*maxRadius; r+=0.05*initRadius) {
					Ring cand = maxRing.duplicate();
					cand.radius = r;
					cand.calculateContrast(mv);
					double contrast = cand.contrast;
					//IJ.log(""+ contrast + " ( " + cand.dir.x + " , " +cand.dir.y + ", " + cand.dir.z );
					if(contrast > maxContrast) {
						IJ.log("better >>>>>"+ contrast + " ( " + cand.dir.x + " , " +cand.dir.y + ", " + cand.dir.z );
						bestCand=cand;
						maxContrast=contrast;
					}
				}
			}
		}	
		IJ.log("best candidate: "+ maxContrast + " rad: " + bestCand.radius);
		return bestCand;
	}




	private double[] unit(double[] u) {
		double norm = 0.0;
		for(int i=0; i<u.length; i++)
			norm += u[i]*u[i];
		norm = Math.sqrt(norm);
		return new double[] {u[0]/norm, u[1]/norm, u[2]/norm};	
	}

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
			volume.setValue(ring.c, dx, dy, dz, 1000);
		}
	}
}
