import javax.swing.DefaultListModel;
import javax.swing.ListModel;

import ij.IJ;
import ij.plugin.PlugIn;

public class Test_Mask implements PlugIn {

	public static void main(String arg[]) {
		new Test_Mask().run("");
	}
	
	@Override
	public void run(String arg0) {
		int nx = 300;
		int ny = 300;
		int nz = 300;
		Volume vol = new Volume(nx, ny, nz);

		DefaultListModel<Branch> branchList = new DefaultListModel<Branch>();
		Network network = new Network(branchList);
		Branch branch1 = new Branch();
		Branch branch2 = new Branch();
		Branch branch3 = new Branch();
		Branch branch4 = new Branch();
		int n1 = 60;
		for(int k=0; k<n1; k++) {
			double u = k*2.0*Math.PI/n1;
			double x = 60 * Math.cos(u);
			double y = 60 * Math.sin(u);
			double dx = -Math.sin(u);
			double dy =  Math.cos(u);
			branch1.add(new Ring(100+x, 90+y, 90, dx, dy, 0, 8, 8));
			branch2.add(new Ring(160+x, 90+y, 90, dx, dy, 0, 8, 8));
			branch3.add(new Ring(150, 90+x, 90+y, 0, dx, dy, 8, 8));
			branch4.add(new Ring(100, 150+x, 90+y, 0, dx, dy, 8, 8));
		}
		network.add(branch1);
		network.add(branch2);
		network.add(branch3);
		network.add(branch4);

		Volume v = network.createMask(vol, 0.5);
		v.showFloat("Mask");	

		Volume s = smooth(v);
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
		Volume out = new Volume (nx, ny, nz);
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
