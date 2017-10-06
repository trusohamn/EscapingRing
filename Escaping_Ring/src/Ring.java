
public class Ring {

	public Point3D c;
	public Point3D dir = new Point3D(0, 0, 1);

	public double radius;
	public double thickness = 3;
	public double length = 10;
	
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
	/*
	// 0 RADIUS
	// 1 theta
	// 2 phi
	public void toCartesian(double polar[]) {
		double st = Math.sin(polar[1]);
		x = polar[0]* st * Math.cos(polar[2]);
		y = polar[0] * st * Math.sin(polar[2]);
		z = polar[0] * Math.cos(polar[1]);	
	}
	
	public double[] toPolar() {
		double[] polar = new double[3];
		polar[0] = Math.sqrt(x*x + y*y + z*z);
		polar[1] = Math.acos(z/polar[0]);
		polar[2] = Math.atan2(y, x);
		return polar;
	}
	*/
	

}
