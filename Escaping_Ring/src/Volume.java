import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.RGBStackMerge;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;


public class Volume {

	public int[][][] data;
	public int nx;
	public int ny;
	public int nz;
	
	public Volume(ImagePlus imp) {
		nx = imp.getWidth();
		ny = imp.getHeight();
		nz = imp.getNSlices();
		data = new int[nx][ny][nz];
		for(int z=0; z<nz; z++) {
			ImageProcessor ip = imp.getStack().getProcessor(z+1);
			for(int x=0; x<nx; x++)
				for(int y=0; y<ny; y++)
					data[x][y][z] =(int)ip.getPixelValue(x, y);
		}
		IJ.log("Create byte volume " + nx + " " + ny + " " + nz);
	}
	
	
	public Volume(int nx, int ny, int nz) {
		this.nx = nx;
		this.ny = ny;
		this.nz = nz;
		data = new int[nx][ny][nz];
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
		
		data[x][y][z] = (int)value;	
	}

	
	public void showFloat(String title) {
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
	public void show(String title) {
		new ImagePlus(title, createImageStackFrom3DArray(this)).show();
	}
	
	public void showTwoChannels(String title, Volume vol2) {
		ImagePlus first = new ImagePlus(title, createImageStackFrom3DArray(this));
		ImagePlus second = new ImagePlus(title, createImageStackFrom3DArray(vol2));
		RGBStackMerge.mergeChannels(new ImagePlus[] {first, second}, false).show();	
	}
	
	public ImagePlus generateThreeChannels(String title, Volume vol2, Volume vol3) {
		ImagePlus first = new ImagePlus("Raw", createImageStackFrom3DArray(this));
		ImagePlus second = new ImagePlus("Segmented", createImageStackFrom3DArray(vol2));
		ImagePlus third = new ImagePlus("Selected", createImageStackFrom3DArray(vol3));
		return RGBStackMerge.mergeChannels(new ImagePlus[] {first, second, third}, false);	
	}
	
	public ImageStack createImageStackFrom3DArray(Volume vol) {
		ImageStack stack = new ImageStack(nx, ny);
		for(int z=0; z<nz; z++) {
			ByteProcessor fp = new ByteProcessor(nx, ny);
			for(int x=0; x<nx; x++)
				for(int y=0; y<ny; y++)
					fp.putPixelValue(x, y, vol.data[x][y][z]);
			stack.addSlice("", fp);
		}
		return stack;
	}
		
}
