package tests;

public class Matrices {
	
	
	public static void main(String[] args) {
		
		double[] angles = new double[] {0.5,0.5};
		double sint = Math.sin(angles[0]);
		double cost = Math.cos(angles[0]);
		double sinp = Math.sin(angles[1]);
		double cosp = Math.cos(angles[1]);
		
		
		double Ry[][] = {{cost, 0, sint}, {0, 1, 0}, {-sint, 0, cost}};
		double Rz[][] = {{cosp, -sinp, 0}, {sinp, cosp, 0}, {0, 0, 1}};
		// Rz*(Ry*v) = (Rz*Ry)*v
		//multiplication RzxRy
		double R[][] = 
				{{cosp*cost, -sinp, cosp*sint},
				{sinp*cost, cosp, sinp*sint},
				{-sint, 0, cost}};
		
		int i=2;
		int j=1;
		int k=2;
				
		double ii = i*Ry[0][0] + j*Ry[0][1] + k*Ry[0][2];
		double jj = i*Ry[1][0] + j*Ry[1][1] + k*Ry[1][2];
		double kk = i*Ry[2][0] + j*Ry[2][1] + k*Ry[2][2];	

		double dx = ii*Rz[0][0] + jj*Rz[0][1] + kk*Rz[0][2];
		double dy = ii*Rz[1][0] + jj*Rz[1][1] + kk*Rz[1][2];
		double dz = ii*Rz[2][0] + jj*Rz[2][1] + kk*Rz[2][2];
		
		double dx2 = i*R[0][0] + j*R[0][1] + k*R[0][2];
		double dy2 = i*R[1][0] + j*R[1][1] + k*R[1][2];
		double dz2 = i*R[2][0] + j*R[2][1] + k*R[2][2];
		
		
		
		System.out.println(dx + "    " + dx2);
		System.out.println(dy + "    " + dy2);
		System.out.println(dz + "    " + dz2);
		
	}

}
