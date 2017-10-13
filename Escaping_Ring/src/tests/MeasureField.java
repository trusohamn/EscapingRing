package tests;

public class MeasureField {

	public static void main(String[] args) {
		int radius = 7;
		int step=20;
		double angles[] = {0.4, 0.4};
		double sint = Math.sin(angles[0]);
		double cost = Math.cos(angles[0]);
		double sinp = Math.sin(angles[1]);
		double cosp = Math.cos(angles[1]);
		/*double Ry[][] = {{cost, 0, sint}, {0, 1, 0}, {-sint, 0, cost}};
		double Rz[][] = {{cosp, -sinp, 0}, {sinp, cosp, 0}, {0, 0, 1}};*/
		// Rz*(Ry*v) = (Rz*Ry)*v
		//multiplication RzxRy
		double R[][] = 
			{{cosp*cost, -sinp, cosp*sint},
					{sinp*cost, cosp, sinp*sint},
					{-sint, 0, cost}};

		double meanMembrane = 0.0;
		int countMembrane = 0;
		double meanInner = 0.0;
		int countInner = 0;
		double meanOuter = 0.0;
		int countOuter = 0;


		//create a storageField
		int k=1;
		int maxR = 2; // maximal needed radius
		for(int i=-maxR*2; i<=maxR*2; i++){
			for(int j=-maxR*2; j<=maxR*2; j++){
				double dx = i*R[0][0] + j*R[0][1] + k*R[0][2];
				double dy = i*R[1][0] + j*R[1][1] + k*R[1][2];
				double dz = i*R[2][0]  + k*R[2][2];

				double d = Math.sqrt(i*i+j*j);



			}
		}
	}
}
