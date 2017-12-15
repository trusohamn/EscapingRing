import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import ij.IJ;

public class Branch extends ArrayList<Ring> {
	Network network;
	double step;
	private static double evolveValue = 0.4; //for finishing the branch threshold
	private static double branchFacilitator = 0.4;
	private static double firstLoopElimination = 30;
	private static double secondLoopElimination = 30;
	private static double thirdLoopElimination = 100;
	//private static boolean lookForward = true;


	private int branchNo;
	static ArrayList<Ring> ringsRunning = new ArrayList<Ring>();
	private static boolean stopAll;

	public Branch(){

	}
	public Branch(Network network, Ring ring, double step){
		//first branch
		this.add(ring);
		this.network = network;
		this.addAll(evolve( ring, step, evolveValue));
		Collections.reverse(this);
		this.addAll(evolve( ring.flippedRing(), step , evolveValue));
		network.add(this);
		this.branchNo = network.getLastBranchNo();
		this.regression( step);
	}

	public Branch(Network network, ArrayList<Ring> branch, double step){
		this.addAll(branch);
		this.network = network;
		network.add(this);
		this.branchNo = network.getLastBranchNo();
		//this.drawBranch(test, step);
		//workingVol.showTwoChannels("Second round", test);
		this.regression( step);
	}

	public void regression( double step){
		class OneShotTask implements Runnable{
			Ring nextRing;
			OneShotTask(Ring nextRing) { 
				this.nextRing = nextRing;
			}			
			public void run() {
				ringsRunning.add(nextRing);
				Gui.updateRunning();

				ArrayList<Ring> ringsAround = sparseCandidates(nextRing);
				for(Ring r : ringsAround) {
					if(stopAll) break;
					//IJ.log("checking next from " + ringsAround.size());
					ArrayList<Ring> branchCand = evolve( r, step, evolveValue);
					if(branchCand.size()>3) {
						new Branch(network, branchCand, step);
					}	
				}

				ringsRunning.remove(nextRing);
				Gui.updateRunning();
			}
		}


		for(Ring ring: this) {
			ring.eraseVol(Espacing_Ring.workingVol);
		}

		Branch branchCopy = (Branch) this.clone();

		for(int i = branchCopy.size()-1; i >=0 ; i--){
			if(stopAll) break;
			IJ.log("checking ring: " + i);
			Ring nextRing = branchCopy.get(i);
			Thread t = new Thread(new OneShotTask(nextRing));
			t.start();
		}



	}
	/*
	public ArrayList<Ring> evolve(Volume vol, Ring initial, double step, double breakValue) {

		Ring current = initial.duplicate();
		int iter = 0;
		double prevMax = network.getMeanContrast() == -Double.MAX_VALUE ? initial.getContrast()*branchFacilitator : network.getMeanContrast()*branchFacilitator; 
		ArrayList<Ring> newBranch = new ArrayList<Ring>();
		newBranch.add(current);
		double maxRadius = 1.40;
		double minRadius = 0.80;

		 DO ONCE 
		ArrayList<Ring> candidates = proposeCandidates(current, step, vol, maxRadius, minRadius);
		//keep x% best
		candidates = keepBestCandidates(candidates, firstLoopElimination);
		ArrayList<Ring[]> candidatesTriple = new ArrayList<Ring[]>();
		for ( Ring cand : candidates){
			ArrayList<Ring> candidates2 = proposeCandidates(cand, step, vol, maxRadius, minRadius);
			//keep x% best
			candidates2 = keepBestCandidates(candidates2, secondLoopElimination);
			for (Ring cand2 : candidates2){
				ArrayList<Ring> candidates3 = proposeCandidates(cand2, step, vol, maxRadius, minRadius);
				candidates3 = keepBestCandidates(candidates3, thirdLoopElimination);
				for (Ring cand3 : candidates3){
					candidatesTriple.add(new Ring[]{cand,cand2, cand3});
				}	
			}
		}
		//calculating the best contrast out of those [three rings]
		Ring best = null; //first ring of triple
		double max = -Double.MAX_VALUE; //total contrast of three rings
		double rest = -Double.MAX_VALUE; //sum of contrast of second and third ring
		for(Ring[] cC : candidatesTriple) {
			double c = cC[0].getContrast() + cC[1].getContrast() + cC[2].getContrast();
			//IJ.log(" c: " + c);
			if (c > max) {
				max = c;
				best = cC[0];	
				rest = cC[1].getContrast() + cC[2].getContrast();
			}
		}
		//if the is no candidate, break
		if(best == null) return newBranch;

		//adjust the first ring with more subtle parameter change

		double currentContrast = best.getContrast();

		ArrayList<Ring> candidatesRefine = refineCandidate(best, vol);
		for(Ring cand4 : candidatesRefine) {
			if (cand4.getContrast() > currentContrast) {
				currentContrast = cand4.getContrast();
				best = cand4;
				//IJ.log("refined: " + currentContrast);
			}
		}

		max = currentContrast + rest; 

		//IJ.log("TotalContrast: " + max + "max: " + currentContrast + "rest: " + rest );


		if(max<prevMax*breakValue*3 || max==0) return newBranch;

		newBranch.add(best);
		current = best.duplicate();


		maxRadius = 1.80;
		minRadius = 0.60;
		MAINLOOP:
			do {
				candidates = proposeCandidates(current, step, vol, maxRadius, minRadius);

				//calculating the best contrast out of those [three rings]
				best = null; //first ring of triple
				max = -Double.MAX_VALUE; //total contrast of three rings
				for(Ring cC : candidates) {
					double c = cC.getContrast();
					//IJ.log(" c: " + c);
					if (c > max) {
						max = c;
						best = cC;	
					}
				}
				//if the is no candidate, break
				if(best == null) break MAINLOOP;

				//adjust the first ring with more subtle parameter change

				currentContrast = best.getContrast();

				candidatesRefine = refineCandidate(best, vol);
				for(Ring cand4 : candidatesRefine) {
					if (cand4.getContrast() > currentContrast) {
						currentContrast = cand4.getContrast();
						best = cand4;
						//IJ.log("refined: " + currentContrast);
					}
				}

				max = currentContrast; 

				//IJ.log("TotalContrast: " + max + "max: " + currentContrast + "rest: " + rest );


				if(max<prevMax*breakValue || max==0) break MAINLOOP;

				newBranch.add(best);
				//best.drawMeasureArea(test, step);
				//drawCenterLine(vol, best);
				//this.add(best); //what is it doing here? - for the first branch...

				current = best.duplicate();


				//erase ring 2 places backwards
				if(newBranch.size()>3) {
					network.recalculateContrast(best.getContrast());
					newBranch.get(newBranch.size()-2).eraseVol(workingVol);
				}
				prevMax = network.getMeanContrast();

				IJ.log(" after iter"  + iter + " current contrast:  " +currentContrast + " mean: " + network.getMeanContrast() + " nrRings: " + network.totalNumberRings + " totalContrast: " + network.totalContrast);
				Gui.updateMeanContrast();
				iter++;
			}
			while (true);
		return newBranch;
	}
	 */

	public ArrayList<Ring> evolve( Ring initial, double step, double breakValue) {

		Ring current = initial.duplicate();
		int iter = 0;
		double prevMax = network.getMeanContrast() == -Double.MAX_VALUE ? initial.getContrast()*2 : network.getMeanContrast()*2; //later the contrast value is a sum of three rings
		prevMax = prevMax*branchFacilitator; //to lower the threshold of starting the new branch
		ArrayList<Ring> newBranch = new ArrayList<Ring>();
		newBranch.add(current);
		double maxRadius = 1.40;
		double minRadius = 0.60;
		MAINLOOP:
			do {
				ArrayList<Ring> candidates = proposeCandidates(current, step,maxRadius, minRadius);
				//keep x% best
				candidates = keepBestCandidates(candidates, firstLoopElimination);
				ArrayList<Ring[]> candidatesTriple = new ArrayList<Ring[]>();
				for ( Ring cand : candidates){
					ArrayList<Ring> candidates2 = proposeCandidates(cand, step, maxRadius, minRadius);
					//keep x% best
					candidates2 = keepBestCandidates(candidates2, secondLoopElimination);
					for (Ring cand2 : candidates2){
						ArrayList<Ring> candidates3 = proposeCandidates(cand2, step, maxRadius, minRadius);
						candidates3 = keepBestCandidates(candidates3, thirdLoopElimination);
						for (Ring cand3 : candidates3){
							candidatesTriple.add(new Ring[]{cand,cand2, cand3});
						}	
					}
				}

				//calculating the best contrast out of those [three rings]
				Ring best = null; //first ring of triple
				double max = -Double.MAX_VALUE; //total contrast of three rings
				double rest = -Double.MAX_VALUE; //sum of contrast of second and third ring
				for(Ring[] cC : candidatesTriple) {
					double c = cC[0].getContrast() + ( cC[1].getContrast() + cC[2].getContrast())/2;
					//IJ.log(" c: " + c);
					if (c > max) {
						max = c;
						best = cC[0];	
						rest = (cC[1].getContrast() + cC[2].getContrast())/2;
					}
				}
				//if the is no candidate, break
				if(best == null) break MAINLOOP;


				//adjust the first ring with more subtle parameter change


				//IJ.log("best: " + max);
				double currentContrast = best.getContrast();

				ArrayList<Ring> candidatesRefine = refineCandidate(best);
				for(Ring cand4 : candidatesRefine) {
					if (cand4.getContrast() > currentContrast) {
						currentContrast = cand4.getContrast();
						best = cand4;
						//IJ.log("refined: " + currentContrast);
					}
				}

				max = currentContrast + rest; 

				//IJ.log("TotalContrast: " + max + "max: " + currentContrast + "rest: " + rest );


				if(max<prevMax*breakValue || max==0) break MAINLOOP;

				newBranch.add(best);
				//best.drawMeasureArea(test, step);
				//drawCenterLine(vol, best);
				//this.add(best); //what is it doing here? - for the first branch...

				current = best.duplicate();


				//erase ring 2 places backwards
				if(newBranch.size()>3) {
					network.recalculateContrast(best.getContrast());
					newBranch.get(newBranch.size()-2).eraseVol(Espacing_Ring.workingVol);
				}
				prevMax = network.getMeanContrast()*2;

				IJ.log(" after iter"  + iter + " current contrast:  " +currentContrast + " mean: " + network.getMeanContrast() + " nrRings: " + network.getTotalNumberRings() + " totalContrast: " + network.getTotalContrast());
				Gui.updateMeanContrast();
				iter++;
			}
			while (true);
		return newBranch;
	}

	private ArrayList<Ring> keepBestCandidates(ArrayList<Ring> rings, double percent) {
		//keeps percent of best candidates
		percent = 100 - percent;
		ArrayList<Ring> bestCands = new ArrayList<Ring>();	
		double[]contrasts = new double[rings.size()];
		int i=0;
		for(Ring ring : rings){
			contrasts[i] = ring.getContrast();
			i++;
		}
		Arrays.sort(contrasts);
		//IJ.log(Arrays.toString(contrasts));
		int cutOffPos = (int) Math.round(percent*rings.size() /100);
		double cutOffContr = contrasts[cutOffPos];
		//IJ.log("cutoff: "+cutOffContr);
		for(Ring ring : rings){
			if(ring.getContrast()>=cutOffContr)
				bestCands.add(ring);
		}
		//IJ.log("cutoff: " + cutOffContr);
		return bestCands;

	}

	private ArrayList<Ring> proposeCandidates(Ring ring, double step, double maxRadius, double minRadius) {
		ArrayList<Ring> cands = new ArrayList<Ring>();	
		double angleStep = Math.PI/10;
		int angleRange = 1;

		double initRadius = ring.radius;
		//double maxRadius = 1.40;
		double maxMeasurmentArea = 2;

		for(double dt = -angleRange*angleStep; dt<=angleRange*angleStep; dt+=angleStep) {
			for(double dp = -angleRange*angleStep; dp<=angleRange*angleStep; dp+=angleStep) {	
				//return the MeasurmentVolume
				Ring maxRing = ring.duplicate();
				maxRing.radius = initRadius*maxRadius*maxMeasurmentArea;
				double polar[] = maxRing.getAnglesFromDirection();
				maxRing.c = maxRing.getPositionFromSphericalAngles(step, polar[0] + dt, polar[1] + dp);
				maxRing.dir = new Point3D((maxRing.c.x-ring.c.x)/step, (maxRing.c.y-ring.c.y)/step, (maxRing.c.z-ring.c.z)/step);
				MeasurmentVolume mv = new MeasurmentVolume(Espacing_Ring.workingVol, maxRing);
				//IJ.log("radius: " + maxRing.radius);
				//IJ.log(mv.toString());

				for(double r = initRadius*minRadius; r<initRadius*maxRadius; r+=0.20*initRadius) {
					Ring cand = maxRing.duplicate();
					cand.radius = r;
					cand.calculateContrast(mv);
					//IJ.log("contrast: " + cand.contrast);
					cands.add(cand);
				}
			}
		}	
		return cands;
	}


	private ArrayList<Ring> refineCandidate(Ring ring) {
		ArrayList<Ring> cands = new ArrayList<Ring>();	
		double angleStep = Math.PI/40;
		int angleRange = 1;

		double initRadius = ring.radius;
		double maxRadius = 1.01;
		double maxMeasurmentArea = 2;


		for(double dt = -angleRange*angleStep; dt<=angleRange*angleStep; dt+=angleStep) {
			for(double dp = -angleRange*angleStep; dp<=angleRange*angleStep; dp+=angleStep) {	
				//return the MeasurmentVolume
				Ring maxRing = ring.duplicate();
				double polar[] = maxRing.getAnglesFromDirection();
				maxRing.radius = initRadius*maxRadius*maxMeasurmentArea;
				maxRing.dir = maxRing.getDirectionFromSphericalAngles(polar[0] + dt, polar[1] + dp);
				MeasurmentVolume mv = new MeasurmentVolume(Espacing_Ring.workingVol, maxRing);
				//IJ.log("radius: " + maxRing.radius);
				//IJ.log(mv.toString());

				for(double r = initRadius*0.99; r<initRadius*maxRadius; r+=0.01*initRadius) {
					Ring cand = maxRing.duplicate();
					cand.radius = r;
					cand.calculateContrast(mv);
					//IJ.log("contrast: " + cand.contrast);
					cands.add(cand);
				}				
			}
		}	
		return cands;
	}

	private ArrayList<Ring> sparseCandidates(Ring ring) {
		//returns sparse rings in 3d space, which keep the initial contrast
		double keepContrast = ring.getContrast();
		ArrayList<Ring> cands = new ArrayList<Ring>();	
		double angleStep = Math.PI/2;
		int angleRange = 1;

		double initRadius = ring.radius;
		double maxRadius = 1.75;
		double maxMeasurmentArea = 2;


		for(double dt = -angleRange*angleStep; dt<=angleRange*angleStep; dt+= angleStep) {
			for(double dp = -angleRange*angleStep; dp<=angleRange*angleStep; dp+= angleStep) {	
				Ring maxRing = ring.duplicate();
				double polar[] = maxRing.getAnglesFromDirection();
				maxRing.radius = initRadius*maxRadius*maxMeasurmentArea;
				maxRing.dir = maxRing.getDirectionFromSphericalAngles(polar[0] + dt, polar[1] + dp);
				double r = initRadius;
				//for(double r = initRadius*0.25; r<=initRadius*maxRadius; r+=0.75*initRadius) {
				Ring cand = maxRing.duplicate();
				cand.radius = r;
				cand.setContrast(keepContrast) ;
				//IJ.log("contrast: " + cand.contrast);
				cands.add(cand);
				//}				
			}
		}	
		return cands;
	}
	public void drawBranch(Volume volume) {
		for(Ring r:this) {
			r.drawMeasureArea(volume);
		}
	}

	public Branch duplicateCrop(int start, int stop) {
		ArrayList<Ring> n = new ArrayList<Ring>();
		for(int i=start; i<=stop; i++){
			n.add(this.get(i));
		}
		Branch newB = new Branch();
		newB.addAll(n);
		newB.network = network;
		newB.step = step;
		//newB.evolveValue = evolveValue ; //for finishing the branch threshold
		newB.branchNo = branchNo;
		return newB;
	}
	public Branch createBranchBetweenTwoRings(Ring start, Ring end, double width){
		//creates a single long ring between centers of two rings
		Point3D startPoint = start.c;
		Point3D endPoint = end.c;
		double avgRadius = width;
		double distanceBetween = startPoint.distance(endPoint);
		Point3D newMiddle = startPoint.middlePoint(endPoint);
		Ring newRing =  new Ring(newMiddle.x, newMiddle.y, newMiddle.z, avgRadius, distanceBetween);
		newRing.dir = startPoint.middlePointDir(endPoint);

		Branch newBranch = this.duplicateCrop(0, 0);
		newBranch.remove(0);
		newBranch.add(start);
		newBranch.add(newRing);
		newBranch.add(end);
		newBranch.eraseBranch();
		network.add(newBranch);

		return newBranch;
	}

	public Branch createBranchBetweenRingAndPoint(Ring start, Point3D endPoint, double width){
		Point3D startPoint = start.c;
		double avgRadius = width;
		double distanceBetween = startPoint.distance(endPoint);
		Point3D newMiddle = startPoint.middlePoint(endPoint);
		Ring newRing =  new Ring(newMiddle.x, newMiddle.y, newMiddle.z, avgRadius, distanceBetween);
		newRing.dir = startPoint.middlePointDir(endPoint);	

		Branch newBranch = this.duplicateCrop(0, 0);
		newBranch.remove(0);
		newBranch.add(start);
		newBranch.add(newRing);
		newBranch.eraseBranch();
		network.add(newBranch);
		return newBranch;

	}

	public void restoreBranch(){
		for(Ring ring : this){
			ring.restoreVol(Espacing_Ring.workingVol, Espacing_Ring.vol);
		}
	}

	public void eraseBranch(){
		for(Ring ring: this) {
			ring.eraseVol(Espacing_Ring.workingVol);
		}
	}


	/*GETTERS AND SETTERS*/
	public static void stopAll(boolean stop) {
		stopAll = stop;
	}

	public int getBranchNo() {
		return branchNo;
	}

	public static void setEvolveValue(double value) {
		evolveValue = value;
	}

	public static void setBranchFacilitator(double value) {
		branchFacilitator = value;
	}

	public static void setFirstLoopElimination(double value){
		firstLoopElimination = value;
	}

	public static void setSecondLoopElimination(double value){
		secondLoopElimination = value;
	}

	public static void setThirdLoopElimination(double value){
		thirdLoopElimination = value;
	}


	@Override
	public String toString() {
		return "BranchNo=" + branchNo  + " branchSize= " + this.size();
	}

}
