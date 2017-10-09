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
		//test.show("firstDir");
		evolve(vol, flippedRing(adjInitial), step, test);
		test.show("secDir");
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

		for(double r = initRadius*0.90; r<initRadius*1.25; r+=0.05*initRadius) {
			for(double dt = -Math.PI; dt<=Math.PI; dt+=angleStep) {
				for(double dp = -Math.PI/2; dp<=Math.PI/2; dp+=angleStep) {
					Ring cand = ring.duplicate();
					cand.radius = r;
					cand.dir = cand.getDirectionFromSphericalAngles( dt,  dp);
					//cand.dir = new Point3D((cand.c.x-ring.c.x)/step, (cand.c.y-ring.c.y)/step, (cand.c.z-ring.c.z)/step);
					double contrast = contrast(vol, cand, step);
					//IJ.log(""+ contrast + " ( " + cand.dir.x + " , " +cand.dir.y + ", " + cand.dir.z );
					if(contrast > maxContrast) {
						IJ.log("better >>>>>"+ contrast + " ( " + cand.dir.x + " , " +cand.dir.y + ", " + cand.dir.z );
						bestCand=cand;
						maxContrast=contrast;

					}
				}
			}
		}	
		IJ.log("best candidate"+ maxContrast + "rad: " + bestCand.radius + "init" +initRadius);
		return bestCand;

	}


	private void evolve(Volume vol, Ring initial, double step, Volume test) {

		Ring current = initial.duplicate();
		int iter = 0;
		double prevMax = -Double.MAX_VALUE;
		MAINLOOP:
			do {
				ArrayList<Ring> candidates = proposeCandidates(current, step);
				Ring best = null;
				double max = -Double.MAX_VALUE;
				for(int i=0; i<candidates.size(); i++) {
					double c = contrast(vol, candidates.get(i), step);
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

	private ArrayList<Ring> proposeCandidates(Ring ring, double step) {
		ArrayList<Ring> cands = new ArrayList<Ring>();	
		double angleStep = Math.PI/12;
		int angleRange = 2;

		double initRadius = ring.radius;	
		step=step/2;

		for(double r = initRadius*0.95; r<initRadius*1.25; r+=0.05*initRadius) {
			for(double dt = -angleRange*angleStep; dt<=angleRange*angleStep; dt+=angleStep) {
				for(double dp = -angleRange*angleStep; dp<=angleRange*angleStep; dp+=angleStep) {
					Ring cand = ring.duplicate();
					cand.radius = r;
					double polar[] = cand.getAnglesFromDirection();
					cand.c = cand.getPositionFromSphericalAngles(step, polar[0] + dt, polar[1] + dp);
					cand.dir = new Point3D((cand.c.x-ring.c.x)/step, (cand.c.y-ring.c.y)/step, (cand.c.z-ring.c.z)/step);
					cands.add(cand);
				}
			}
		}	
		return cands;
	}

	private double contrast(Volume volume, Ring ring, double step) {
		int radius = (int)Math.ceil(ring.radius);
		double angles[] = ring.getAnglesFromDirection();
		double sint = Math.sin(angles[0]);
		double cost = Math.cos(angles[0]);
		double sinp = Math.sin(angles[1]);
		double cosp = Math.cos(angles[1]);
		/*double Ry[][] = {{cost, 0, sint}, {0, 1, 0}, {-sint, 0, cost}};
		double Rz[][] = {{cosp, -sinp, 0}, {sinp, cosp, 0}, {0, 0, 1}};*/
		// Rz*(Ry*v) = (Rz*Ry)*v
		//multiplication RzxRy
		double R[][] = 
			{{cosp*cost, -sinp, cosp*sint},
					{sinp*cost, cosp, sinp*sint},
					{-sint, 0, cost}};

		double meanMembrane = 0.0;
		int countMembrane = 0;
		double meanInner = 0.0;
		int countInner = 0;
		double meanOuter = 0.0;
		int countOuter = 0;


		for(int k=-(int)step/2; k<=(int)step/2; k++) {
			for(int j=-radius*2; j<=radius*2; j++) {
				for(int i=-radius*2; i<=radius*2; i++) {

					double dx = i*R[0][0] + j*R[0][1] + k*R[0][2];
					double dy = i*R[1][0] + j*R[1][1] + k*R[1][2];
					double dz = i*R[2][0]  + k*R[2][2];

					double d = Math.sqrt(i*i+j*j);

					if (d < 0.7*radius) {
						meanInner += volume.getValue(ring.c, dx, dy, dz);
						countInner++;
					}

					if (d >= 0.8*radius && d <=1.2*radius) {
						meanMembrane += volume.getValue(ring.c, dx, dy, dz);
						countMembrane++;
					}

					if (d >= 1.3*radius && d < 2*radius) {
						countOuter += volume.getValue(ring.c, dx, dy, dz);
						countInner++;
					}
				}	
			}
		}
		double constrast = meanMembrane/countMembrane - (meanInner/countInner)/2 - (meanOuter/countOuter);		
		return constrast;

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
