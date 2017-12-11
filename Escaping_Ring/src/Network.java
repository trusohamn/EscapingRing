import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultListModel;


public class Network extends ArrayList<Branch> {
	int totalNumberRings = 0;
	double totalContrast = 0;
	private double meanContrast = -Double.MAX_VALUE;
	DefaultListModel<Branch> branchList;
	private int lastBranchNo = 0;

	public Network(DefaultListModel<Branch> branchList){
		this.branchList = branchList;
		lastBranchNo = branchList.size();
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
				for(int k=0; k<=maxK; k++) {
					double dx = i*R[0][0] + j*R[0][1] + k*R[0][2];
					double dy = i*R[1][0] + j*R[1][1] + k*R[1][2];
					double dz = i*R[2][0]  + k*R[2][2];
					vol.setValue(first, dx, dy, dz, 1000);
				}
			}
		}

	}

	public void exportData(String csvFile) throws IOException{
		List<String> header = Arrays.asList("BranchNo", "Length", "Width");
		List<List<String>> data = new ArrayList<List<String>>();

		FileWriter writer = new FileWriter(csvFile);

		for(Branch branch : this) {
			int BranchNo = branch.getBranchNo();
			double branchLength = 0;
			double totalBranchWidth = 0;
			int ringNumber = 0;
			for(int n = 0; n<branch.size()-1; n++) {
				++ringNumber;
				if(n>0){
					branchLength += branch.get(n-1).c.distance(branch.get(n).c);
				}
				totalBranchWidth += branch.get(n).getRadius();

			}
			double branchWidth = totalBranchWidth/ringNumber;
			List<String> row = Arrays.asList( String.valueOf(BranchNo), String.valueOf(branchLength), String.valueOf(branchWidth));
			data.add(row);

		}
		CSVUtils.writeLine(writer, header);
		for(List<String> row : data){
			CSVUtils.writeLine(writer, row);
		}
		writer.flush();
		writer.close();

	}


	@Override public boolean add(Branch branch) {
		branchList.addElement(branch);
		++this.lastBranchNo;
		return super.add(branch);
	}

	public int getLastBranchNo() {
		return lastBranchNo;
	}


}
