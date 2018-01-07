import java.io.Serializable;

public class Point3D  implements Serializable{

	private static final long serialVersionUID = 1L;
	protected double x;
	protected double y;
	protected double z;
	
	public Point3D(){		
	}
	
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
	
	public Point3D flipp() {
		return new Point3D(-x, -y, -z);
	}
	
	public Point3D middlePoint(Point3D point){
		double newx = (this.x + point.x)/2;
		double newy = (this.y + point.y)/2;
		double newz = (this.z + point.z)/2;
		return new Point3D(newx, newy, newz);
	}
	
	public Point3D middlePointDir(Point3D point){
		double distance = this.distance(point);
		double dirX = (point.x-this.x)/distance;
		double dirY = (point.y-this.y)/distance;
		double dirZ = (point.z-this.z)/distance;
			
		return new Point3D(dirX, dirY, dirZ);
	}
	
	public String toString() {
		return " " + x + " " + y  + " " + z;
	}
	
	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}


}
