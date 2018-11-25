package com.trusohamn.v3t;


import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
		lastBranchNo = 0;
	}

	public Network(DefaultListModel<Branch> branchList){
		this.branchList = branchList;
		lastBranchNo = branchList.size()-1;
	}

	public Network(ArrayList<Branch> branchList){
		this.addAll(branchList);
		lastBranchNo = branchList.size()-1;
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

	public void eraseNetworkVolume(MyVolume workingVol){
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

	public void generateSkeleton(MyVolume vol) {
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
			for(int e : new int[] {0, branch.size()-1}) {
				//checking if it is a endpoint
				Ring firstR = branch.get(e);
				if(firstR.getBranches().size() == 1) {
					//we cannot say in which direction the endpoint ring is directed
					//I will prolong the skeleton by half of width of the ring 
					//in the direction opposed to the direction connecting it with next one
					Ring secondR = e==0? branch.get(1): branch.get(branch.size()-2);
					Point3D dir = firstR.getC().middlePointDir(secondR.getC());
					Point3D first = firstR.getC();

					dir = dir.flipp();
					double[] angles = new double[2];

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
					int maxK = (int)firstR.getLength()/2;
					for(int k=0; k<=maxK; k++) {
						double dx = i*R[0][0] + j*R[0][1] + k*R[0][2];
						double dy = i*R[1][0] + j*R[1][1] + k*R[1][2];
						double dz = i*R[2][0]  + k*R[2][2];
						vol.setValue(first, dx, dy, dz, 1000);
					}
				}
			}
		}
	}

	public void generateBinary(MyVolume vol) {
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
		FileWriter writer = new FileWriter(csvFile + "_Output.csv");

		double totalLength = 0; //of branches in the network
		int numberBranchPoints = 0;
		int numberEndPoints = 0;
		double totalIntWidth = 0;
		double totalIntStraightness = 0;

		MyGui.updateRingsUsed();
		for(Ring r: MyGui.ringsUsed) {
			if (r.isBranchPoint) ++ numberBranchPoints;
			if (r.isEndPoint) ++numberEndPoints;
		}

		for(Branch branch : this) {
			IJ.log("BranchNO: " + branch.getBranchNo());
			if(branch.size()>1) {
				int BranchNo = branch.getBranchNo();
				double branchLength = 0;
				double totalBranchWidth = 0;
				ArrayList<Ring> points = new ArrayList<Ring>();
				points.addAll(branch);

				for(int n = 1; n<branch.size(); n++) {
					double thisBranchLength = branch.get(n-1).getC().distance(branch.get(n).getC());
					branchLength += thisBranchLength ;
					totalBranchWidth += branch.get(n).getRadius()*thisBranchLength;			
				}
				IJ.log("branchLength without ext: " + branchLength);

				//adding halfring to the endpoint
				for(int e : new int[] {0, branch.size()-1}) {
					Ring firstR = branch.get(e);
					if(firstR.getBranches().size() == 1) { //endpoint
						Ring secondR = e==0? branch.get(1): branch.get(branch.size()-2);
						Point3D dir = firstR.getC().middlePointDir(secondR.getC());
						dir = dir.flipp();

						Ring newRing = firstR.duplicate();
						newRing.setDir(dir);
						double polar[] = newRing.getAnglesFromDirection();
						newRing.setC(newRing.getPositionFromSphericalAngles(firstR.getLength()/2, polar[0], polar[1]));				
						double halfRingDistance = firstR.getC().distance(newRing.getC());

						branchLength += halfRingDistance;
						totalBranchWidth += branch.get(e).getRadius()*halfRingDistance;

						if(e==0) points.add(0, newRing);
						if(e==branch.size()-1)points.add(newRing);
					}
				}
				IJ.log("branchLength with ext: " + branchLength);
				
				totalLength += branchLength;
				double branchWidth = totalBranchWidth/branchLength;
				double branchStraightness = points.get(0).getC().distance(points.get(branch.size()-1).getC()) / branchLength;	
				totalIntWidth += totalBranchWidth;
				totalIntStraightness += branchStraightness*branchLength;
				;
				List<String> row = Arrays.asList( String.valueOf(BranchNo), String.valueOf(branchLength), String.valueOf(branchWidth), String.valueOf(branchStraightness));
				data.add(row);
			}
		}
		CSVUtils.writeLine(writer, header);
		for(List<String> row : data){
			CSVUtils.writeLine(writer, row);
		}
		writer.flush();
		writer.close();

		//General stats//
		writer = new FileWriter(csvFile+"_GenStats.csv");

		header = Arrays.asList("TotalLength", "TotalWidth", "TotalStraightness", "BranchPointsNo", "EndpointsNo",
				"PixelWidth", "PixelHeight", "VoxelDepth");



		CSVUtils.writeLine(writer, header);			
		CSVUtils.writeLine(writer, Arrays.asList(String.valueOf(totalLength),String.valueOf(totalIntWidth/totalLength), String.valueOf(totalIntStraightness/totalLength), 
				String.valueOf(numberBranchPoints), String.valueOf(numberEndPoints ), 
				String.valueOf(Escaping_Ring.pixelWidth), String.valueOf(Escaping_Ring.pixelHeight), String.valueOf(Escaping_Ring.voxelDepth)));

		writer.flush();
		writer.close();

	}

	public void createMask(MyVolume in, double sampling, boolean mask) {
		MyVolume vol = new MyVolume(in.nx, in.ny, in.nz);
		Point3D zero = new Point3D(0, 0, 0);


		for(Branch b: this) {
			ArrayList<Ring> polyline = new ArrayList<Ring>();

			Ring firstR = b.get(0);
			if(firstR.getBranches().size() == 1) { //endpoint
				Ring secondR =  b.get(1);
				Point3D dir = firstR.getC().middlePointDir(secondR.getC());
				dir = dir.flipp();

				Ring newRing = firstR.duplicate();
				newRing.setDir(dir);
				double polar[] = newRing.getAnglesFromDirection();
				newRing.setC(newRing.getPositionFromSphericalAngles(firstR.getLength()/2, polar[0], polar[1]));				
				polyline.add(newRing);
			}

			for(Ring r:b) {
				polyline.add(r);			
			}

			firstR = b.get(b.size()-1);
			if(firstR.getBranches().size() == 1) { //endpoint
				Ring secondR = b.get(b.size()-2);
				Point3D dir = firstR.getC().middlePointDir(secondR.getC());
				dir = dir.flipp();

				Ring newRing = firstR.duplicate();
				newRing.setDir(dir);
				double polar[] = newRing.getAnglesFromDirection();
				newRing.setC(newRing.getPositionFromSphericalAngles(firstR.getLength()/2, polar[0], polar[1]));				
				polyline.add(newRing);
			}	

			for(int p=0; p<polyline.size()-1; p++) {
				Point3D p1 = polyline.get(p).getC();
				Point3D p2 = polyline.get(p+1).getC();
				double dist = Math.sqrt((p1.x-p2.x)*(p1.x-p2.x) + (p1.y-p2.y)*(p1.y-p2.y) + (p1.z-p2.z)*(p1.z-p2.z));
				int ns = (int)(sampling*Math.round(dist));
				double r1[] = {p1.x, p1.y, p1.z, polyline.get(p).getRadius()};
				double r2[] = {p2.x, p2.y, p2.z, polyline.get(p+1).getRadius()};
				double dr[] = {p2.x-p1.x, p2.y-p1.y, p2.z-p1.z, r2[3]-r1[3]};
				double sr[] = {dr[0]/ns, dr[1]/ns, dr[2]/ns, dr[3]/ns};

				for(int s=0; s<=ns; s++) {
					Point3D pt = new Point3D(r1[0] + s*sr[0], r1[1] + s*sr[1], r1[2] + s*sr[2]);
					double radius = r1[3] + s*sr[3];
					int x = (int)(Math.round(pt.x));
					int y = (int)(Math.round(pt.y));
					int z = (int)(Math.round(pt.z));
					int r = (int)(Math.ceil(radius)+2);
					for(int i=x-r; i<=x+r; i++)
						for(int j=y-r; j<=y+r; j++)
							for(int k=z-r; k<=z+r; k++) {
								double d = Math.sqrt((pt.x-i)*(pt.x-i) + (pt.y-j)*(pt.y-j) + (pt.z-k)*(pt.z-k));
								if (d < radius) {
									vol.setValue(zero, i, j, k, 100);
								}
							}

				}
			}
		}


		//vol.showFloat("Rolling ball");
		MyVolume s = vol.smooth(vol);
		if(mask) {		
			s.showFloat("Mask");
		}
		else {
			MyVolume g = s.gradient(s);
			g.showFloat("Outline");
		}

			

	}	

	public void orderBranchPoints(){
		MyGui.updateRingsUsed();

		for(int m = 0; m < MyGui.ringsUsed.size(); m++){
			Ring r = MyGui.ringsUsed.get(m);
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

							for(Ring ri: motherBranches.get(j)){
								if(ri.getBranches().contains(motherBranches.get(j))) {
									ri.removeBranch(motherBranches.get(j));
								}
							}
							remove(motherBranches.get(j));

							for(Ring ri: newBranch1) {
								if(!ri.getBranches().contains(newBranch1)) ri.addBranch(newBranch1);
							}
							for(Ring ri: newBranch2) {
								if(!ri.getBranches().contains(newBranch2))ri.addBranch(newBranch2);
							}

							add(newBranch1);
							add(newBranch2);
							IJ.log("Cut into: " + newBranch1.toString() + " and " + newBranch2.toString());
						}
					}
				}	
			}
		}

		MyGui.updateRingsUsed();
	}

	@Override public boolean add(Branch branch) {
		branchList.addElement(branch);
		for(Ring r : branch){
			if(!MyGui.ringsUsed.contains(r)) MyGui.ringsUsed.add(r);
		}
		++this.lastBranchNo;
		branch.setBranchNo(lastBranchNo+1);
		
		//adds branch to the display
		Escaping_Ring.generateView(true);
		Escaping_Ring.drawBranchBranchEndPoints(branch);
		Escaping_Ring.iC.repaint();
		
		return super.add(branch);
	}

	@Override
	public boolean remove(Object branch) {
		branchList.removeElement(branch);
		MyGui.extraBranchList.removeElement(branch);
		for(Ring r : (Branch) branch){
			if(MyGui.ringsUsed.contains(r)) MyGui.ringsUsed.remove(r);
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
