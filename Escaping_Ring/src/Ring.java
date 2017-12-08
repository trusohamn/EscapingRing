import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

public class Ring {

	public Point3D c;
	public Point3D dir = new Point3D(0, 0, 1);

	public double radius;
	public double thickness = 3; //??
	private double length;
	private double contrast;
	private Branch branch = null;
	
	static private double impInside = -0.25;
	static private double impOutside = -0.25;
	
	static private double maxIn, minMem, maxMem, minOut, maxOut;
	


	public Ring() {	
	}
	
	public Ring(double x, double y, double z, double radius, double length) {
		c = new Point3D(x, y, z);
		this.radius = radius;
		this.length = length;
	}
	
	public Ring(double x, double y, double z, double dx, double dy, double dz, double radius, double length) {
		c = new Point3D(x, y, z);
		dir = new Point3D(dx, dy, dz);
		this.radius = radius;
		this.length = length;
	}

	public String toString() {
		String out = "" + dir.x + " " + dir.y + " " + dir.z;
		if(branch != null){
			out += " from: " + branch;
		}
		return out;
	}
	
	public Ring duplicate() {
		Ring r = new Ring();
		r.c = new Point3D(c.x, c.y, c.z);
		r.dir = new Point3D(dir.x, dir.y, dir.z);
		r.radius = radius;
		r.thickness = thickness;
		r.length = length;
		//r.contrast = contrast;
		return r;
	}

	// 0 RADIUS
	// 1 theta Z
	// 2 phi XY
	public Point3D getPositionFromSphericalAngles(double step, double theta, double phi) {	
		double st = Math.sin(theta);
		double xp = c.x + step * st * Math.cos(phi);
		double yp = c.y + step * st * Math.sin(phi);
		double zp = c.z + step * Math.cos(theta);	
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
		polar[0] = Math.acos(dir.z);
		polar[1] = Math.atan2(dir.y, dir.x);
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

			if (curRadius  >= minOut *radius && curRadius < maxOut*radius) {
				meanOuter =+ mv.sumIntensity[i];
				countOuter =+ mv.count[i];
			}
		}	
		this.contrast = (meanMembrane/countMembrane) +  impInside*(meanInner/countInner) + impOutside*(meanOuter/countOuter) ;	
		//IJ.log("meanM: " + meanMembrane + " countM: " + countMembrane+
		//		" meanI: " + meanInner + " countI: " + countInner+
		//		" meanO: " + meanOuter + " countO: " + countOuter);
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

					int dx = (int) Math.round(this.c.x +  i*R[0][0] + j*R[0][1] + k*R[0][2]);
					int dy = (int) Math.round(this.c.y + i*R[1][0] + j*R[1][1] + k*R[1][2]);
					int dz = (int) Math.round(this.c.z + i*R[2][0]  + k*R[2][2]);

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
		newRing.dir.x = -this.dir.x;
		newRing.dir.y = -this.dir.y;
		newRing.dir.z = -this.dir.z;
		newRing.contrast = this.contrast;
		return newRing;
	}
	
	public void eraseVol(Volume workingVol){
		double plusErase = 2;
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
		double plusErase = 2;
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
				maxRing.radius = initRadius*maxRadius*maxMeasurmentArea;
				maxRing.dir = maxRing.getDirectionFromSphericalAngles( dt,  dp);
				MeasurmentVolume mv = new MeasurmentVolume(vol, maxRing);
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
	/*GETTERS SETTERS*/
	
	public Branch getBranch() {
		return branch;
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

	public void setBranch(Branch branch) {
		this.branch = branch;
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

}
