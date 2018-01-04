import java.util.Arrays;


public class MeasurmentVolume {
	double[] sumIntensity; //in the cilinder with wall with bin and length of the step/width
	int[] count;
	double bin;

	public MeasurmentVolume(Volume volume, Ring ring){
		//Ring with maximum radius is passed

		double angles[] =  ring.getAnglesFromDirection();
		double width = ring.getLength();
		float sint = (float) Math.sin(angles[0]);
		float cost = (float) Math.cos(angles[0]);
		float sinp = (float) Math.sin(angles[1]);
		float cosp = (float) Math.cos(angles[1]);
		/*double Ry[][] = {{cost, 0, sint}, {0, 1, 0}, {-sint, 0, cost}};
		double Rz[][] = {{cosp, -sinp, 0}, {sinp, cosp, 0}, {0, 0, 1}};*/
		// Rz*(Ry*v) = (Rz*Ry)*v
		//multiplication RzxRy
		float R[][] = 
			{{cosp*cost, -sinp, cosp*sint},
					{sinp*cost, cosp, sinp*sint},
					{-sint, 0, cost}};

		int maxR = (int)Math.ceil(ring.getRadius()); // maximal needed radius - the maximum radius and more
		bin =  1; //with bin 1 it properly aligns first ring
		int noBins = (int) Math.ceil(maxR/bin)+1;
		

		sumIntensity = new double[noBins];
		count = new int[noBins];
		
		for(int i=-maxR; i<=maxR; i++){
			for(int j=-maxR; j<=maxR; j++){
				double d = Math.sqrt(i*i+j*j);
				if(d<=maxR){
					int binIndex = (int) Math.round(d/bin); 
					for(int k=-(int)Math.round(width/2); k<=(int)Math.round(width/2); k++) {
						float dx = i*R[0][0] + j*R[0][1] + k*R[0][2];
						float dy = i*R[1][0] + j*R[1][1] + k*R[1][2];
						float dz = i*R[2][0] + k*R[2][2];
						sumIntensity[binIndex] += volume.getValue(ring.getC(), dx, dy, dz);
						++count[binIndex];
					}
				}
			}
		}
/*
		for(int k=-(int)Math.round(width/2); k<=(int)Math.round(width/2); k++) {
			for(int i=-maxR; i<=maxR; i++){
				for(int j=-maxR; j<=maxR; j++){
					float dx = i*R[0][0] + j*R[0][1] + k*R[0][2];
					float dy = i*R[1][0] + j*R[1][1] + k*R[1][2];
					float dz = i*R[2][0] + k*R[2][2];

					double d = Math.sqrt(i*i+j*j);
					if(d<=maxR){
						int binIndex = (int) Math.round(d/bin); 
						sumIntensity[binIndex] += volume.getValue(ring.getC(), dx, dy, dz);
						++count[binIndex];
					}
				}
			}
		}*/
	}

	@Override
	public String toString() {
		return "MeasurmentVolume [sumIntensity=" + Arrays.toString(sumIntensity) + ", count=" + Arrays.toString(count)
				+ "]";
	}
	
	
}
