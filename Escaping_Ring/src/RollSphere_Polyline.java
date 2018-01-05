import java.util.ArrayList;
import ij.plugin.PlugIn;

public class RollSphere_Polyline implements PlugIn {

	public static void main(String arg[]) {
		new RollSphere_Polyline().run("");
	}
	
	@Override
	public void run(String arg0) {
		double sampling = 2.0; // 1/2 pixel
		int nx = 300;
		int ny = 300;
		int nz = 300;
		Volume vol = new Volume(nx, ny, nz);

		ArrayList<Ring> polyline = new ArrayList<Ring>();
		// The ring structure is only used to store the 3D center points and the radius
		polyline.add(new Ring(30, 30, 30, 20, 0));
		polyline.add(new Ring(100, 40, 40, 10, 0));
		polyline.add(new Ring(200, 200, 50, 25, 0));
		
		for(int p=0; p<polyline.size()-1; p++) {
			Point3D p1 = polyline.get(p).getC();
			Point3D p2 = polyline.get(p+1).getC();
			double dist = Math.sqrt((p1.x-p2.x)*(p1.x-p2.x) + (p1.y-p2.y)*(p1.y-p2.y) + (p1.z-p2.z)*(p1.z-p2.z));
			int ns = (int)(sampling*Math.round(dist));
			double r1[] = {p1.x, p1.y, p1.z, polyline.get(p).getRadius()};
			double r2[] = {p2.x, p2.y, p2.z, polyline.get(p+1).getRadius()};
			double dr[] = {p2.x-p1.x, p2.y-p1.y, p2.z-p1.z, r2[3]-r1[3]};
			double sr[] = {dr[0]/ns, dr[1]/ns, dr[2]/ns, dr[3]/ns};
			Point3D zero = new Point3D(0, 0, 0);
			System.out.println("Segment " + p1);
			for(int s=0; s<=ns; s++) {
				Point3D pt = new Point3D(r1[0] + s*sr[0], r1[1] + s*sr[1], r1[2] + s*sr[2]);
				double radius = r1[3] + s*sr[3];
				int x = (int)(Math.round(pt.x));
				int y = (int)(Math.round(pt.y));
				int z = (int)(Math.round(pt.z));
				int r = (int)(Math.ceil(radius)+2);
				for(int i=x-r; i<=x+r; i++)
				for(int j=y-r; j<=y+r; j++)
				for(int k=z-r; k<=z+r; k++) {
					double d = Math.sqrt((pt.x-i)*(pt.x-i) + (pt.y-j)*(pt.y-j) + (pt.z-k)*(pt.z-k));
					if (d < radius) {
						vol.setValue(zero, i, j, k, 100);
					}
				}
				System.out.println("" + pt + " " + radius + " " + r);
				
			}
		}
		vol.showFloat("Rolling ball");	

		Volume s = smooth(vol);
		s.showFloat("Smooth");	

		Volume g = gradient(s);
		g.showFloat("Gradient");	


	}
	
	private Volume gradient(Volume in) {
		int nx = in.nx;
		int ny = in.ny;
		int nz = in.nz;
		Volume out = new Volume(nx, ny, nz);
		Point3D zero = new Point3D(0, 0, 0);
		for(int i=1; i<nx-1; i++)
		for(int j=1; j<ny-1; j++)
		for(int k=1; k<nz-1; k++) {
			float gx = in.data[i-1][j][k] - in.data[i+1][j][k];
			float gy = in.data[i][j-1][k] - in.data[i][j+1][k];
			float gz = in.data[i][j][k-1] - in.data[i][j][k+1];
			float u = (float)Math.sqrt(gx*gx + gy*gy + gz*gz);
			out.setValue(zero, i, j, k, u);
		}
		return out;	
	}
	
	private Volume smooth(Volume in) {
		int nx = in.nx;
		int ny = in.ny;
		int nz = in.nz;
		Volume out = new Volume(nx, ny, nz);
		Point3D zero = new Point3D(0, 0, 0);
		for(int i=1; i<nx-1; i++)
		for(int j=1; j<ny-1; j++)
		for(int k=1; k<nz-1; k++) {
			float gx = in.data[i-1][j][k] + in.data[i+1][j][k];
			float gy = in.data[i][j-1][k] + in.data[i][j+1][k];
			float gz = in.data[i][j][k-1] + in.data[i][j][k+1];
			float u = (in.data[i-1][j][k] + gx + gy + gz) / 5;
			out.setValue(zero, i, j, k, u);
		}
		return out;	
		
	}

}
