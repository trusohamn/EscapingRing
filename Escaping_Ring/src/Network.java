import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import javax.swing.DefaultListModel;

import ij.IJ;


public class Network extends ArrayList<Branch> implements Serializable{

	private static final long serialVersionUID = 1L;
	private int totalNumberRings = 0;
	private double totalContrast = 0;
	private double meanContrast = -Double.MAX_VALUE;
	protected DefaultListModel<Branch> branchList = null;

	private int lastBranchNo = 0;

	public Network(){	
	}

	public Network(DefaultListModel<Branch> branchList){
		this.branchList = branchList;
		lastBranchNo = branchList.size();
	}

	public Network(ArrayList<Branch> branchList){
		this.addAll(branchList);
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

	public void recalcualteContrast(){		
		resetContrast();
		totalContrast = 0; //can give problems if values of contrast <0
		for (Branch b: this){
			for (Ring r: b){
				//IJ.log("recalculating contrast" + r.getContrast());
				recalculateContrast(r.getContrast());
			}
		}
	}

	public void assignBranchesToRing(){
		for (Branch b: this){
			for (Ring r: b){
				r.setBranches(new ArrayList<Branch>());
				r.addBranch(b);

			}
		}
	}

	public void eraseNetworkVolume(Volume workingVol){
		for (Branch b: this){
			for (Ring r: b){
				r.eraseVol(workingVol);
			}
		}
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
				Point3D first = branch.get(n).getC();
				Point3D second = branch.get(n+1).getC();
				Point3D dir = first.middlePointDir(second);
				angles[0] = Math.acos(dir.getZ());
				angles[1] = Math.atan2(dir.getY(), dir.getX());
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

	public void generateBinary(Volume vol) {
		for(Branch branch : this) {
			for(int n = 0; n<branch.size()-1; n++) {
				double[] angles = new double[2];
				Point3D first = branch.get(n).getC();
				Point3D second = branch.get(n+1).getC();
				Point3D dir = first.middlePointDir(second);
				angles[0] = Math.acos(dir.getZ());
				angles[1] = Math.atan2(dir.getY(), dir.getX());
				double sint = Math.sin(angles[0]);
				double cost = Math.cos(angles[0]);
				double sinp = Math.sin(angles[1]);
				double cosp = Math.cos(angles[1]);
				double R[][] = 
					{{cosp*cost, -sinp, cosp*sint},
							{sinp*cost, cosp, sinp*sint},
							{-sint, 0, cost}};

				int maxK = (int)Math.ceil(first.distance(second))+1; //without this +1 there is a gap in the binary
				for(int ki=0; ki<=2*maxK; ki++) {
					double k = ki/2.0;
					double radius =  branch.get(n).getRadius();
					int rad = (int) radius;
					for(int ji=-4*rad; ji<=4*rad; ji++) {
						for(int ii=-4*rad; ii<=4*rad; ii++) {
							double j = ji/2.0;
							double i = ii/2.0;

							double dx = i*R[0][0] + j*R[0][1] + k*R[0][2];
							double dy = i*R[1][0] + j*R[1][1] + k*R[1][2];
							double dz = i*R[2][0]  + k*R[2][2];

							double d = Math.sqrt(i*i+j*j);
							if(d<=radius) vol.setValue(first, dx, dy, dz, 1000);
						}
					}
				}
			}
		}
	}

	public void exportData(String csvFile) throws IOException{
		//List of branches//
		List<String> header = Arrays.asList("BranchNo", "Length", "Width", "Straightness");
		List<List<String>> data = new ArrayList<List<String>>();
		FileWriter writer = new FileWriter(csvFile + "VascRing3_Output.csv");

		double totalLength = 0;
		int numberBranchPoints = 0;
		int numberEndPoints = 0;
		double totalIntWidth = 0;
		double totalIntStraightness = 0;

		Gui.updateRingsUsed();
		for(Ring r: Gui.ringsUsed) {
			if (r.isBranchPoint) ++ numberBranchPoints;
			if (r.isEndPoint) ++numberEndPoints;

		}
		for(Branch branch : this) {
			int BranchNo = branch.getBranchNo();
			double branchLength = 0;
			double totalBranchWidth = 0;
			int ringNumber = 0;


			for(int n = 0; n<branch.size(); n++) {
				++ringNumber;
				if(n>0){
					branchLength += branch.get(n-1).getC().distance(branch.get(n).getC());
				}
				totalBranchWidth += branch.get(n).getRadius();

			}

			totalLength += branchLength;
			double branchWidth = totalBranchWidth/ringNumber;
			double branchStraightness = branch.get(0).getC().distance(branch.get(branch.size()-1).getC()) / branchLength;	
			totalIntWidth += branchWidth*branchLength;
			totalIntStraightness += branchStraightness*branchLength;
			;
			List<String> row = Arrays.asList( String.valueOf(BranchNo), String.valueOf(branchLength), String.valueOf(branchWidth), String.valueOf(branchStraightness));
			data.add(row);

		}
		CSVUtils.writeLine(writer, header);
		for(List<String> row : data){
			CSVUtils.writeLine(writer, row);
		}
		writer.flush();
		writer.close();

		//General stats//
		writer = new FileWriter(csvFile+"VascRing3_GenStats.csv");

		header = Arrays.asList("TotalLength", "TotalWidth", "TotalStraightness", "BranchPointsNo", "EndpointsNo");


		CSVUtils.writeLine(writer, header);			
		CSVUtils.writeLine(writer, Arrays.asList(String.valueOf(totalLength),String.valueOf(totalIntWidth/totalLength), String.valueOf(totalIntStraightness/totalLength), 
				String.valueOf(numberBranchPoints), String.valueOf(numberEndPoints )));

		writer.flush();
		writer.close();

	}
	
	public Volume createMask(Volume in, double sampling) {
		Volume vol = new Volume(in.nx, in.ny, in.nz);
		IJ.log(">>>>>>> Network");
		int k = 1;
		for (Branch b: this) {
			k++;
			for (Ring r: b) {
				r.draw(vol, 200, sampling);
			}
		}
		return vol;
	}	

	public void orderBranchPoints(){
		Gui.updateRingsUsed();

			for(int m = 0; m < Gui.ringsUsed.size(); m++){
				Ring r = Gui.ringsUsed.get(m);
				ArrayList<Branch> motherBranches = new ArrayList<Branch>();
				motherBranches.addAll(r.getBranches());
				if(motherBranches.size()>1){
					ArrayList<Integer> indexes = new ArrayList<Integer>();
					ArrayList<Boolean> isLastFirst = new ArrayList<Boolean>();
					for(Branch motherBranch : motherBranches){
						indexes.add( motherBranch.indexOf(r));
						boolean isLF = (motherBranch.indexOf(r) == motherBranch.size()-1 || motherBranch.indexOf(r) == 0)? true : false;
						isLastFirst.add(isLF);
					}

					if(isLastFirst.size()==2 && isLastFirst.get(0) && isLastFirst.get(1)){
						//branches are connected by their last/first rings ---> to join
						Branch newBranch = new Branch() ;
						IJ.log("Trying to join: " + motherBranches.get(0).toString() + " with " + motherBranches.get(1).toString());
						ArrayList<Ring> clone = new ArrayList<Ring>();
						ArrayList<Ring> clone0 = new ArrayList<Ring>();
						ArrayList<Ring> clone1 = new ArrayList<Ring>();
						if(indexes.get(0) == motherBranches.get(0).size()-1){
							newBranch.addAll(motherBranches.get(0));
							clone.addAll(motherBranches.get(1));
							if(indexes.get(1) == motherBranches.get(1).size()-1){						
								Collections.reverse(clone);
							}
							newBranch.addAll(clone);
						}
						else if(indexes.get(1) == motherBranches.get(1).size()-1){
							newBranch.addAll(motherBranches.get(1));
							clone.addAll(motherBranches.get(0));
							newBranch.addAll(clone);
						}
						else{
							clone0.addAll(motherBranches.get(0));
							clone1.addAll( motherBranches.get(1));
							Collections.reverse(clone0);
							if(indexes.get(1) == motherBranches.get(1).size()-1){						
								Collections.reverse(clone1);
							}

							newBranch.addAll(clone0);
							newBranch.addAll(clone1);

						}	

						for (Ring ri:motherBranches.get(0)) {
							ri.removeBranch(motherBranches.get(0));
							if(!ri.getBranches().contains(newBranch)) ri.addBranch(newBranch);
						}
						for (Ring ri:motherBranches.get(1)) {					
							ri.removeBranch(motherBranches.get(1));
							if(!ri.getBranches().contains(newBranch)) ri.addBranch(newBranch);
						}
						remove(motherBranches.get(0));
						remove(motherBranches.get(1));
						add(newBranch);


						IJ.log("Joined into: " + newBranch.toString());
					}

					else{
						//one of branches finishes into another. cut another into two
						IJ.log("Trying to cut: " + motherBranches.get(0).toString() + " or " + motherBranches.get(1).toString());
						for(int j=0; j<isLastFirst.size(); j++){
							if(!isLastFirst.get(j)){
								IJ.log("Cutting: " + motherBranches.get(j).toString());
								Branch newBranch1 = motherBranches.get(j).duplicateCrop(0, indexes.get(j));
								Branch newBranch2 = motherBranches.get(j).duplicateCrop(indexes.get(j), motherBranches.get(j).size()-1);

								IJ.log("Removing: " + motherBranches.get(j).toString());
								remove(motherBranches.get(j));
								IJ.log("Removing: " + motherBranches.get(j).toString());
								for(Ring ri: motherBranches.get(j)){
									if(ri.getBranches().contains(motherBranches.get(j))) {
										ri.removeBranch(motherBranches.get(j));
									}

								}
								for(Ring ri: newBranch1) ri.addBranch(newBranch1);
								for(Ring ri: newBranch2) ri.addBranch(newBranch2);

								add(newBranch1);
								add(newBranch2);
								IJ.log("Cut into: " + newBranch1.toString() + " and " + newBranch2.toString());
							}
						}
					}	
				}
			}

		Gui.updateRingsUsed();
	}

	@Override public boolean add(Branch branch) {
		branchList.addElement(branch);
		for(Ring r : branch){
			if(!Gui.ringsUsed.contains(r)) Gui.ringsUsed.add(r);
		}
		++this.lastBranchNo;
		return super.add(branch);
	}



	@Override
	public boolean remove(Object branch) {
		branchList.removeElement(branch);
		Gui.extraBranchList.removeElement(branch);
		for(Ring r : (Branch) branch){
			if(Gui.ringsUsed.contains(r)) Gui.ringsUsed.remove(r);
		}
		return super.remove(branch);
	}

	public int getLastBranchNo() {
		return lastBranchNo;
	}
	public int getTotalNumberRings() {
		return totalNumberRings;
	}

	public double getTotalContrast() {
		return totalContrast;
	}

	public void setTotalNumberRings(int totalNumberRings) {
		this.totalNumberRings = totalNumberRings;
	}

	public void setTotalContrast(double totalContrast) {
		this.totalContrast = totalContrast;
	}

	public void setMeanContrast(double meanContrast) {
		this.meanContrast = meanContrast;
	}

	public void setLastBranchNo(int lastBranchNo) {
		this.lastBranchNo = lastBranchNo;
	}
	public DefaultListModel<Branch> getBranchList() {
		return branchList;
	}

	public void setBranchList(DefaultListModel<Branch> branchList) {
		this.branchList = branchList;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}


}
