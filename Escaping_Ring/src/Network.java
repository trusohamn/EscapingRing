import java.util.ArrayList;
import javax.swing.DefaultListModel;

public class Network extends ArrayList<Branch> {
	private int totalNumberRings = 0;
	private double totalContrast = 0;
	private double meanContrast = -Double.MAX_VALUE;
	DefaultListModel<Branch> branchList;

	public Network(DefaultListModel<Branch> branchList){
		this.branchList = branchList;
	}

	public void save(String filename) {

	}

	public void load(String filename) {

	}

	public void recalculateContrast(double c) {
		++totalNumberRings;
		totalContrast += c;
		this.meanContrast = totalContrast/totalNumberRings;
	}

	public double getMeanContrast() {
		return this.meanContrast;
	}
	
	public void resetContrast(){
		this.meanContrast = -Double.MAX_VALUE;
		this.totalContrast = 0;
		this.totalNumberRings = 0;
	}

	public void lowerMeanContrast(double percent) {
		this.meanContrast = meanContrast*percent;
	}
	
	public void generateSkeleton(Volume vol) {
		for(Branch branch : this) {
			for(int n = 0; n<branch.size()-1; n++) {
				double[] angles = new double[2];
				Point3D first = branch.get(n).c;
				Point3D second = branch.get(n+1).c;
				Point3D dir = first.middlePointDir(second);
				angles[0] = Math.acos(dir.z);
				angles[1] = Math.atan2(dir.y, dir.x);
				double sint = Math.sin(angles[0]);
				double cost = Math.cos(angles[0]);
				double sinp = Math.sin(angles[1]);
				double cosp = Math.cos(angles[1]);
				double R[][] = 
					{{cosp*cost, -sinp, cosp*sint},
							{sinp*cost, cosp, sinp*sint},
							{-sint, 0, cost}};
				int i = 0;
				int j = 0;
				int maxK = (int)first.distance(second);
				for(int k=0; k<=10; k++) {
					double dx = i*R[0][0] + j*R[0][1] + k*R[0][2];
					double dy = i*R[1][0] + j*R[1][1] + k*R[1][2];
					double dz = i*R[2][0]  + k*R[2][2];
					vol.setValue(first, dx, dy, dz, 1000);
				}
			}
		}
		
	}

	@Override public boolean add(Branch branch) {
		branchList.addElement(branch);
		return super.add(branch);
	}


}
