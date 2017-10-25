import ij.IJ;

public class Ring {

	public Point3D c;
	public Point3D dir = new Point3D(0, 0, 1);

	public double radius;
	public double thickness = 3; //??
	public double length = 10; //??
	public double contrast;
	
	public Ring() {	
	}
	
	public Ring(double x, double y, double z, double radius) {
		c = new Point3D(x, y, z);
		this.radius = radius;
	}
	
	public Ring(double x, double y, double z, double dx, double dy, double dz, double radius) {
		c = new Point3D(x, y, z);
		dir = new Point3D(dx, dy, dz);
		this.radius = radius;
	}

	public String toString() {
		return "" + c.x + " " + c.y + " " + c.z;
	}
	
	public Ring duplicate() {
		Ring r = new Ring();
		r.c = new Point3D(c.x, c.y, c.z);
		r.dir = new Point3D(dir.x, dir.y, dir.z);
		r.radius = radius;
		r.thickness = thickness;
		r.length = length;
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
		polar[0] = Math.acos(dir.z); //(-pi/2,pi/2)
		polar[1] = Math.atan2(dir.y, dir.x);//(-pi, pi)
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
			if (curRadius  < 0.7*radius) {
				meanInner =+ mv.sumIntensity[i];
				countInner =+ mv.count[i];
			}

			if (curRadius  >= 0.8*radius && curRadius <=1.2*radius) {
				meanMembrane =+ mv.sumIntensity[i];
				countMembrane =+ mv.count[i];
			}

			if (curRadius  >= 1.3*radius && curRadius < 2*radius) {
				meanOuter =+ mv.sumIntensity[i];
				countOuter =+ mv.count[i];
			}
		}	
		this.contrast = meanMembrane/countMembrane - (meanInner/countInner)/2 - (meanOuter/countOuter)/4;	
		//IJ.log("meanM: " + meanMembrane + " countM: " + countMembrane+
		//		" meanI: " + meanInner + " countI: " + countInner+
		//		" meanO: " + meanOuter + " countO: " + countOuter);
	}
	public void drawMeasureArea(Volume volume, double step) {
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

		for(int k=-(int)step/2; k<=(int)step/2; k++) {
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
	
	public Ring flippedRing() {
		Ring newRing = this.duplicate();
		newRing.dir.x = -this.dir.x;
		newRing.dir.y = -this.dir.y;
		newRing.dir.z = -this.dir.z;

		return newRing;
	}
	
	public void eraseVol(Volume workingVol, double width){
		double plusErase = 1.2;
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

		for(int k=-(int)width/2; k<=(int)width/2; k++) {
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
	
}
