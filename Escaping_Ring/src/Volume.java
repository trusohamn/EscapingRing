import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;


public class Volume {

	public float[][][] data;
	public int nx;
	public int ny;
	public int nz;
	
	public Volume(ImagePlus imp) {
		nx = imp.getWidth();
		ny = imp.getHeight();
		nz = imp.getNSlices();
		data = new float[nx][ny][nz];
		for(int z=0; z<nz; z++) {
			ImageProcessor ip = imp.getStack().getProcessor(z+1);
			for(int x=0; x<nx; x++)
				for(int y=0; y<ny; y++)
					data[x][y][z] = ip.getPixelValue(x, y);
		}
		IJ.log("Create byte volume " + nx + " " + ny + " " + nz);
	}
	
	public Volume(int nx, int ny, int nz) {
		this.nx = nx;
		this.ny = ny;
		this.nz = nz;
		data = new float[nx][ny][nz];
		IJ.log("Create byte volume " + nx + " " + ny + " " + nz);
	}

	
	public double getValue(Point3D center, double dx, double dy, double dz) {
		int x = (int)Math.round(center.x + dx);
		if (x < 0)
			return 0.0;
		if (x >= nx)
			return 0.0;
	
		
		int y = (int)Math.round(center.y + dy);
		if (y < 0)
			return 0.0;
		if (y >= ny)
			return 0.0;
		
		int z = (int)Math.round(center.z + dz);
		if (z < 0)
			return 0.0;
		if (z >= nz)
			return 0.0;		
		return data[x][y][z];
	}
	
	public void setValue(Point3D center, double dx, double dy, double dz, float value) {
		int x = (int)Math.round(center.x + dx);
		if (x < 0)
			return;
		if (x >= nx)
			return;
	
		
		int y = (int)Math.round(center.y + dy);
		if (y < 0)
			return;
		if (y >= ny)
			return;
		
		int z = (int)Math.round(center.z + dz);
		if (z < 0)
			return;
		if (z >= nz)
			return;
		
		data[x][y][z] = value;
		
	//	IJ.log(" // " + x + " " + y + " " + z + " " + data[x][y][z]);
		
	
	}

	
	public void show(String title) {
		ImageStack stack = new ImageStack(nx, ny);
		for(int z=0; z<nz; z++) {
			FloatProcessor fp = new FloatProcessor(nx, ny);
			for(int x=0; x<nx; x++)
				for(int y=0; y<ny; y++)
					fp.putPixelValue(x, y, data[x][y][z]);
			stack.addSlice("", fp);
		}
		new ImagePlus(title, stack).show();
	}
		
}
