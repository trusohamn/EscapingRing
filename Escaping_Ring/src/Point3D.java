
public class Point3D {

	public double x;
	public double y;
	public double z;
	
	public Point3D(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
		
	public double distance(Point3D pt) {
		return Math.sqrt( (x-pt.x)*(x-pt.x) + (y-pt.y)*(y-pt.y) + (z-pt.z)*(z-pt.z));
	}
	
	public Point3D minus(Point3D a) {
		return new Point3D(x-a.x, y-a.y, z-a.z);
	}
	
	public Point3D plus(Point3D a) {
		return new Point3D(x+a.x, y+a.y, z+a.z);
	}
	
	public Point3D divide(double k) {
		return new Point3D(x/k, y/k, z/k);
	}

	public Point3D duplicate() {
		return new Point3D(x, y, z);
	}
	
	public String toString() {
		return " " + x + " " + y  + " " + z;
	}

}
