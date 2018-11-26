package com.trusohamn.v3t.volumes;


import java.awt.Color;

import com.trusohamn.v3t.helperStructures.Point3D;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.RGBStackMerge;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;


public class MyVolume {

	public int[][][] data;
	public int nx;
	public int ny;
	public int nz;
	
	public MyVolume(ImagePlus imp) {
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
	
	
	public MyVolume(int nx, int ny, int nz) {
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
		new ImagePlus(title, createImageStackFrom3DArray()).show();
	}
	
	public void showTwoChannels(String title, MyVolume vol2) {
		ImagePlus first = new ImagePlus(title, this.createImageStackFrom3DArray());
		ImagePlus second = new ImagePlus(title, vol2.createImageStackFrom3DArray());
		RGBStackMerge.mergeChannels(new ImagePlus[] {first, second}, false).show();	
	}
	
	public ImagePlus generateThreeChannels(String title, MyVolume vol2, MyVolume vol3) {
		ImagePlus first = new ImagePlus("Raw", this.createImageStackFrom3DArray());
		ImagePlus second = new ImagePlus("Segmented", vol2.createImageStackFrom3DArray());
		ImagePlus third = new ImagePlus("Selected", vol3.createImageStackFrom3DArray());
		return RGBStackMerge.mergeChannels(new ImagePlus[] {first, second, third}, false);	
	}
	
	public ImageStack createImageStackFrom3DArray() {
		ImageStack stack = new ImageStack(nx, ny);
		for(int z=0; z<nz; z++) {
			ColorProcessor fp = new ColorProcessor(nx, ny);
			for(int x=0; x<nx; x++)
				for(int y=0; y<ny; y++)
					fp.putPixelValue(x, y, this.data[x][y][z]);
			stack.addSlice("", fp);
		}
		return stack;
	}
	

	public MyVolume gradient(MyVolume in) {
		int nx = in.nx;
		int ny = in.ny;
		int nz = in.nz;
		MyVolume out = new MyVolume(nx, ny, nz);
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
	
	public MyVolume smooth(MyVolume in) {
		int nx = in.nx;
		int ny = in.ny;
		int nz = in.nz;
		MyVolume out = new MyVolume (nx, ny, nz);
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
	
	public void copyOtherVolume() {
		for(int x = 0; x< nx; x++) {
			for(int y = 0; y<ny; y++) {
				for(int z = 0; z<nz; z++) {
					//Espacing_Ring.iC.getImage()[x][y]
					
					
				}
			}
		}
	}
		
}
