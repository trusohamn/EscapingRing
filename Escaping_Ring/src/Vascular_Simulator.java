
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;

public class Vascular_Simulator implements PlugIn {

	private int nx = 500;
	private int ny = 100;
	private int nz = 100;
	private float[][][] volume;

	public static void main(String arg[]) {
		new Vascular_Simulator().run("");
	}

	public void run(String arg) {
		
		Point3D A = new Point3D(0, 50, 50);
		Point3D B = new Point3D(200, 50, 50);
		
		Point3D C = new Point3D(400, 70, 50);
		Point3D D = new Point3D(600, 70, 100);
		Point3D E = new Point3D(400, 30, 50);
	
		volume = new float[nz][nx][nz];
		tube(A, B, 10);
		tube(B, C, 10);
		tube(C, D, 10);
		tube(B, E, 10);
	
		float[][][] g = gradient(volume);
		
		ImagePlus imp = create(g);
		imp.show();
		
		IJ.saveAsTiff(imp, "/Users/sage/Desktop/tubes.tif");
		/*
		volume = new float[nz][nx][nz];
		tube(A, B, 1);
		ImagePlus center = create(volume);
		center.show();	
		IJ.saveAsTiff(center, "/Users/sage/Desktop/center.tif");
		*/
	}
	
	private ImagePlus create(float[][][] vol) {
		ImageStack stack = new ImageStack(nx, ny);
		for(int z=0; z<nz; z++)
			stack.addSlice(new FloatProcessor(vol[z]));
		return new ImagePlus("out", stack);
	}
	
	private float[][][] gradient(float[][][] volume) {
		float[][][] g = new float[nz][nx][ny];
		for(int i=1; i<nx-1; i++)
			for(int j=1; j<ny-1; j++)
				for(int k=1; k<nz-1; k++) {
					float dx = volume[k][i-1][j] - volume[k][i+1][j]; 
					float dy = volume[k][i][j-1] - volume[k][i][j+1]; 
					float dz = volume[k-1][i][j] - volume[k+1][i][j];
					g[k][i][j] = dx*dx + dy*dy + dz*dz;
				}
		return g;	
	}

	private void tube(Point3D A, Point3D B, int radius) {
		double sampling = 1;
		double d = B.distance(A);
		int n = (int)(d / sampling);
		double step = d/n;
		Point3D D = B.minus(A).divide(n);
		Point3D P = A.duplicate();
		for(double k=0; k<=d; k+=step) {
			P = P.plus(D);
			fill(P, radius);
		}
	}
	
	private void fill(Point3D P, int r) {
		for(int i=-r; i<=r; i++) {
			int x = (int)(P.x + i + 0.5);
			if (x >= 0 && x < nx) {
				for(int j=-r; j<=r; j++) {
					int y = (int)(P.y + j + 0.5);
					if (y >= 0 && y < ny)  {
						for(int k=-r; k<=r; k++) {
							int z = (int)(P.z + k + 0.5);
							if (z >= 0 && z < nz) {
								double d = Math.sqrt(i*i + j*j + k*k);
								if (d < r)
									volume[z][x][y] = 255;
							}
						}
					}
				}
			}
		}
	}
}
