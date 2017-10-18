import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.plugin.PlugIn;

import java.awt.Rectangle;
import java.util.ArrayList;


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

		Ring adjInitial = adjustFirstRing(initial, vol, step);
		drawMeasureArea(test, adjInitial, step);
		evolve(vol, adjInitial, step, test);
		
		//evolve in opposite direction
		evolve(vol, flippedRing(adjInitial), step, test);
		vol.showTwoChannels("Result", test);

	}
	private Ring flippedRing(Ring ring) {
		Ring newRing = ring.duplicate();
		newRing.dir.x = -ring.dir.x;
		newRing.dir.y = -ring.dir.y;
		newRing.dir.z = -ring.dir.z;

		return newRing;
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


	private void evolve(Volume vol, Ring initial, double step, Volume test) {

		Ring current = initial.duplicate();
		int iter = 0;
		double prevMax = -Double.MAX_VALUE;
		MAINLOOP:
			do {
				ArrayList<Ring> candidates = proposeCandidates(current, step, vol);
				Ring best = null;
				double max = -Double.MAX_VALUE;
				for(int i=0; i<candidates.size(); i++) {
					double c = candidates.get(i).contrast;
					//IJ.log(" c: " + c);
					if (c > max) {
						max = c;
						best = candidates.get(i);	
					}
				}
				if(max<prevMax*0.7) break MAINLOOP;
				drawMeasureArea(test, best, step);
				//drawCenterLine(vol, best);
				current = best.duplicate();
				IJ.log(" after iter"  + iter + " " + max);
				prevMax=max;
				iter++;
			}
			while (true);
	}

	private ArrayList<Ring> proposeCandidates(Ring ring, double step, Volume volume) {
		ArrayList<Ring> cands = new ArrayList<Ring>();	
		double angleStep = Math.PI/12;
		int angleRange = 2;

		double initRadius = ring.radius;
		double maxRadius = 1.25;
		double maxMeasurmentArea = 2;
		double width = step;
		step = step/2;


		for(double dt = -angleRange*angleStep; dt<=angleRange*angleStep; dt+=angleStep) {
			for(double dp = -angleRange*angleStep; dp<=angleRange*angleStep; dp+=angleStep) {	
				//return the MeasurmentVolume
				Ring maxRing = ring.duplicate();
				maxRing.radius = initRadius*maxRadius*maxMeasurmentArea;
				double polar[] = maxRing.getAnglesFromDirection();
				maxRing.c = maxRing.getPositionFromSphericalAngles(step, polar[0] + dt, polar[1] + dp);
				maxRing.dir = new Point3D((maxRing.c.x-ring.c.x)/step, (maxRing.c.y-ring.c.y)/step, (maxRing.c.z-ring.c.z)/step);
				MeasurmentVolume mv = new MeasurmentVolume(volume, maxRing, width);
				//IJ.log("radius: " + maxRing.radius);
				//IJ.log(mv.toString());

				for(double r = initRadius*0.90; r<initRadius*maxRadius; r+=0.05*initRadius) {
					Ring cand = maxRing.duplicate();
					cand.radius = r;
					cand.calculateContrast(mv);
					//IJ.log("contrast: " + cand.contrast);
					cands.add(cand);
				}
			}
		}	
		return cands;
	}



	private double[] unit(double[] u) {
		double norm = 0.0;
		for(int i=0; i<u.length; i++)
			norm += u[i]*u[i];
		norm = Math.sqrt(norm);
		return new double[] {u[0]/norm, u[1]/norm, u[2]/norm};	
	}

	private void drawCenterLine(Volume volume, Ring ring) {

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

	private void drawMeasureArea(Volume volume, Ring ring, double step) {
		int radius = (int)Math.ceil(ring.radius);

		double angles[] = ring.getAnglesFromDirection();
		double sint = Math.sin(angles[0]);
		double cost = Math.cos(angles[0]);
		double sinp = Math.sin(angles[1]);
		double cosp = Math.cos(angles[1]);
		double R[][] = 
			{{cosp*cost, -sinp, cosp*sint},
					{sinp*cost, cosp, sinp*sint},
					{-sint, 0, cost}};

		for(int k=-(int)step/2; k<=(int)step/2; k++) {
			for(int j=-radius*2; j<=radius*2; j++) {
				for(int i=-radius*2; i<=radius*2; i++) {

					double dx = i*R[0][0] + j*R[0][1] + k*R[0][2];
					double dy = i*R[1][0] + j*R[1][1] + k*R[1][2];
					double dz = i*R[2][0]  + k*R[2][2];

					double d = Math.sqrt(i*i+j*j);


					if (d >= 0.8*radius && d <=1.2*radius) {
						volume.setValue(ring.c, dx, dy, dz, 150);
					}
				}	
			}
		}
	}
}
