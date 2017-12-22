import java.io.Serializable;
import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

public class Ring  implements Serializable {

	private static final long serialVersionUID = 1L;
	private Point3D c;
	private Point3D dir = new Point3D(0, 0, 1);

	private double radius;
	private double thickness = 3; //??
	private double length;
	private double contrast;
	
	private ArrayList<Branch> branches;
	
	static private double impInside = -0.25;
	static private double impOutside = -0.25;
	
	static private double maxIn, minMem, maxMem, minOut, maxOut;
	static private double plusErase = 2;
	


	public Ring() {	
		branches = new ArrayList<Branch>();
		contrast = 0;
	}
	
	public Ring(double x, double y, double z, double radius, double length) {
		this();
		c = new Point3D(x, y, z);
		this.radius = radius;
		this.length = length;
	}
	
	public Ring(double x, double y, double z, double dx, double dy, double dz, double radius, double length) {
		this(x,y,z,radius,length);
		dir = new Point3D(dx, dy, dz);

	}

	public String toString() {
		String out = "" + contrast;
		if(branches.size()>0){
			for(Branch b: branches){
				out += " from: " + b;
			}
		}
		return out;
	}
	
	public Ring duplicate() {
		Ring r = new Ring();
		r.c = new Point3D(c.getX(), c.getY(), c.getZ());
		r.dir = new Point3D(dir.getX(), dir.getY(), dir.getZ());
		r.radius = radius;
		r.thickness = thickness;
		r.length = length;
		//r.contrast = contrast;
		//what with branches?
		return r;
	}

	// 0 RADIUS
	// 1 theta Z
	// 2 phi XY
	public Point3D getPositionFromSphericalAngles(double step, double theta, double phi) {	
		double st = Math.sin(theta);
		double xp = c.getX() + step * st * Math.cos(phi);
		double yp = c.getY() + step * st * Math.sin(phi);
		double zp = c.getZ() + step * Math.cos(theta);	
		return new Point3D(xp, yp, zp);
	}
	
	public Point3D getDirectionFromSphericalAngles(double theta, double phi) {	
		double st = Math.sin(theta);
		double xp = st * Math.cos(phi);
		double yp = st * Math.sin(phi);
		double zp = Math.cos(theta);	
		return new Point3D(xp, yp, zp);
	}
	
	public double[] getAnglesFromDirection() {
		double[] polar = new double[2];
		polar[0] = Math.acos(dir.getZ());
		polar[1] = Math.atan2(dir.getY(), dir.getX());
		return polar;
	}
	
	public void calculateContrast(MeasurmentVolume mv){
		int n = (int) Math.ceil(this.radius*2/mv.bin);
		double meanInner = 0;
		double countInner = 0;
		double meanMembrane = 0;
		double countMembrane = 0;
		double meanOuter = 0;
		double countOuter = 0;
		//IJ.log("radius: " + radius);
		
		for(int i=0; i<n; i++){
			double curRadius = i*mv.bin;
			//IJ.log("current radius: " + curRadius);
			if (curRadius  < maxIn *radius) {
				meanInner =+ mv.sumIntensity[i];
				countInner =+ mv.count[i];
			}

			if (curRadius  >= minMem *radius && curRadius <= maxMem*radius) {
				meanMembrane =+ mv.sumIntensity[i];
				countMembrane =+ mv.count[i];
			}

			if (curRadius  >= minOut *radius && curRadius <= maxOut*radius) {
				meanOuter =+ mv.sumIntensity[i];
				countOuter =+ mv.count[i];
			}
		}	
		this.contrast = (meanMembrane/countMembrane) +  impInside*(meanInner/countInner) + impOutside*(meanOuter/countOuter) ;	
	}
	public void drawMeasureArea(Volume volume) {
		int radius = (int)Math.ceil(this.radius);

		double angles[] = this.getAnglesFromDirection();
		double sint = Math.sin(angles[0]);
		double cost = Math.cos(angles[0]);
		double sinp = Math.sin(angles[1]);
		double cosp = Math.cos(angles[1]);
		double R[][] = 
			{{cosp*cost, -sinp, cosp*sint},
					{sinp*cost, cosp, sinp*sint},
					{-sint, 0, cost}};

		for(int k=-(int)this.length/2; k<=(int)this.length/2; k++) {
			for(int j=-radius*2; j<=radius*2; j++) {
				for(int i=-radius*2; i<=radius*2; i++) {

					double dx = i*R[0][0] + j*R[0][1] + k*R[0][2];
					double dy = i*R[1][0] + j*R[1][1] + k*R[1][2];
					double dz = i*R[2][0]  + k*R[2][2];

					double d = Math.sqrt(i*i+j*j);


					if (d >= 0.8*radius && d <=1.2*radius) {
						volume.setValue(this.c, dx, dy, dz, 150);
					}
				}	
			}
		}
	}
	
	public void drawMeasureArea(ImagePlus img, java.awt.Color color) {
		int radius = (int)Math.ceil(this.radius);

		double angles[] = this.getAnglesFromDirection();
		double sint = Math.sin(angles[0]);
		double cost = Math.cos(angles[0]);
		double sinp = Math.sin(angles[1]);
		double cosp = Math.cos(angles[1]);
		double R[][] = 
			{{cosp*cost, -sinp, cosp*sint},
					{sinp*cost, cosp, sinp*sint},
					{-sint, 0, cost}};

		for(int k=-(int)this.length/2; k<=(int)this.length/2; k++) {
			for(int j=-radius*2; j<=radius*2; j++) {
				for(int i=-radius*2; i<=radius*2; i++) {

					int dx = (int) Math.round(this.c.getX() +  i*R[0][0] + j*R[0][1] + k*R[0][2]);
					int dy = (int) Math.round(this.c.getY() + i*R[1][0] + j*R[1][1] + k*R[1][2]);
					int dz = (int) Math.round(this.c.getZ() + i*R[2][0]  + k*R[2][2]);

					double d = Math.sqrt(i*i+j*j);
					if(dx>=0 && dy>=0 && dz>=0 && dx<img.getWidth() && dy<img.getHeight() && dz< img.getImageStackSize()){
						ImageProcessor ip = img.getStack().getProcessor(dz+1);
						ip.setColor(color);
						if (d >= 0.8*radius && d <=1.2*radius) {
							ip.drawPixel(dx, dy);
						}
					}
				}	
			}
		}
	}
	
	public Ring flippedRing() {
		Ring newRing = this.duplicate();
		newRing.getDir().setX( -this.dir.getX());
		newRing.getDir().setY( -this.dir.getY());
		newRing.getDir().setZ( -this.dir.getZ());
		newRing.setContrast( this.contrast);
		return newRing;
	}
	
	public void eraseVol(Volume workingVol){
		int radius = (int)Math.ceil(this.radius*plusErase);
		
		
		double angles[] = this.getAnglesFromDirection();
		double sint = Math.sin(angles[0]);
		double cost = Math.cos(angles[0]);
		double sinp = Math.sin(angles[1]);
		double cosp = Math.cos(angles[1]);
		double R[][] = 
			{{cosp*cost, -sinp, cosp*sint},
					{sinp*cost, cosp, sinp*sint},
					{-sint, 0, cost}};

		for(int k=-(int)length/2; k<=(int)length/2; k++) {
			for(int j=-radius; j<=radius; j++) {
				for(int i=-radius; i<=radius; i++) {

					double dx = i*R[0][0] + j*R[0][1] + k*R[0][2];
					double dy = i*R[1][0] + j*R[1][1] + k*R[1][2];
					double dz = i*R[2][0]  + k*R[2][2];

					double d = Math.sqrt(i*i+j*j);	
					if (d <=radius) {
						workingVol.setValue(this.c, dx, dy, dz, 0 );
					}
				}	
			}
		}
	}
	
	public void restoreVol(Volume workingVol, Volume vol){
		int radius = (int)Math.ceil(this.radius*plusErase);
		
		
		double angles[] = this.getAnglesFromDirection();
		double sint = Math.sin(angles[0]);
		double cost = Math.cos(angles[0]);
		double sinp = Math.sin(angles[1]);
		double cosp = Math.cos(angles[1]);
		double R[][] = 
			{{cosp*cost, -sinp, cosp*sint},
					{sinp*cost, cosp, sinp*sint},
					{-sint, 0, cost}};

		for(int k=-(int)length/2; k<=(int)length/2; k++) {
			for(int j=-radius; j<=radius; j++) {
				for(int i=-radius; i<=radius; i++) {

					double dx = i*R[0][0] + j*R[0][1] + k*R[0][2];
					double dy = i*R[1][0] + j*R[1][1] + k*R[1][2];
					double dz = i*R[2][0]  + k*R[2][2];

					double d = Math.sqrt(i*i+j*j);	
					if (d <=radius) {
						int restoredPixel = (int) vol.getValue(this.c, dx, dy, dz);
						workingVol.setValue(this.c, dx, dy, dz, restoredPixel );
					}
				}	
			}
		}
	}
	
	public Ring adjustFirstRing( Volume vol) {
		Ring bestCand = null;	
		double maxContrast = -Double.MAX_VALUE;
		double angleStep = Math.PI/12;

		double initRadius = this.radius;	
		double maxRadius = 1.25;
		double maxMeasurmentArea = 2;

		for(double dt = -Math.PI; dt<=Math.PI; dt+=angleStep) {
			for(double dp = -Math.PI/2; dp<=Math.PI/2; dp+=angleStep) {
				//return the MeasurmentVolume
				Ring maxRing = this.duplicate();
				maxRing.setRadius(initRadius*maxRadius*maxMeasurmentArea);
				maxRing.setDir(maxRing.getDirectionFromSphericalAngles( dt,  dp));
				MeasurmentVolume mv = new MeasurmentVolume(vol, maxRing);
				//IJ.log(mv.toString());
				for(double r = initRadius*0.90; r<initRadius*maxRadius; r+=0.05*initRadius) {
					Ring cand = maxRing.duplicate();
					cand.setRadius(r);
					cand.calculateContrast(mv);
					double contrast = cand.contrast;
					//IJ.log(""+ contrast + " ( " + cand.dir.x + " , " +cand.dir.y + ", " + cand.dir.z );
					if(contrast > maxContrast) {
						IJ.log("better >>>>>"+ contrast + " ( " + cand.getDir().getX() + " , " +cand.getDir().getY() + ", " + cand.getDir().getZ() );
						bestCand=cand;
						maxContrast=contrast;
					}
				}
			}
		}	
		IJ.log("best candidate: "+ maxContrast + " rad: " + bestCand.radius);
		return bestCand;
	}
	
	public Ring getClosestRing(){
		Point3D target = this.c;

		double minDistance = Double.MAX_VALUE;
		Ring closestRing = null;
		for(Branch branch : Gui.network){
			for(Ring ring : branch){
				double thisDistance=target.distance(ring.getC());
				if(thisDistance<minDistance){
					minDistance = thisDistance;
					closestRing = ring;
				}
			}
		}
		return closestRing;
	}
	/*GETTERS SETTERS*/
	
	public ArrayList<Branch> getBranches() {
		return branches;
	}
	public void setBranches(ArrayList<Branch> branches) {
		this.branches = branches;
	}
	public void addBranch(Branch branch){
		this.branches.add(branch);
	}
	
	public void removeBranch(Branch branch){
		this.branches.remove(branch);
	}
	
	public double getContrast() {
		return contrast;
	}
	
	public void setContrast(double contrast) {
		this.contrast = contrast;
	}
	
	public double getLength(){
		return length;
	}
	
	public static double getImpInside() {
		return impInside;
	}

	public static void setImpInside(double impInside) {
		Ring.impInside = impInside;
	}

	public static double getImpOutside() {
		return impOutside;
	}

	public static void setImpOutside(double impOutside) {
		Ring.impOutside = impOutside;
	}
	
	public static void setParameters(double maxIn, double minMem, double maxMem, double minOut, double maxOut){
		Ring.maxIn = maxIn;
		Ring.minMem = minMem;
		Ring.maxMem = maxMem;
		Ring.minOut = minOut;
		Ring.maxOut = maxOut;
		
	}
	public double getRadius() {
		return radius;
	}
	public Point3D getC() {
		return c;
	}

	public void setC(Point3D c) {
		this.c = c;
	}

	public Point3D getDir() {
		return dir;
	}

	public void setDir(Point3D dir) {
		this.dir = dir;
	}

	public double getThickness() {
		return thickness;
	}

	public void setThickness(double thickness) {
		this.thickness = thickness;
	}

	public static double getMaxIn() {
		return maxIn;
	}

	public static void setMaxIn(double maxIn) {
		Ring.maxIn = maxIn;
	}

	public static double getMinMem() {
		return minMem;
	}

	public static void setMinMem(double minMem) {
		Ring.minMem = minMem;
	}

	public static double getMaxMem() {
		return maxMem;
	}

	public static void setMaxMem(double maxMem) {
		Ring.maxMem = maxMem;
	}

	public static double getMinOut() {
		return minOut;
	}

	public static void setMinOut(double minOut) {
		Ring.minOut = minOut;
	}

	public static double getMaxOut() {
		return maxOut;
	}

	public static void setMaxOut(double maxOut) {
		Ring.maxOut = maxOut;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public void setLength(double length) {
		this.length = length;
	}

}
