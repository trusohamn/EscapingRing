import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import ij.IJ;

public class Branch extends ArrayList<Ring>  implements Serializable {
	private static final long serialVersionUID = 1L;
	private double step;
	private static double evolveValue = 0.4; //for finishing the branch threshold
	private static double branchFacilitator = 0.4;
	private static double firstLoopElimination = 30;
	private static double secondLoopElimination = 30;
	private static double thirdLoopElimination = 100;
	private static int minLengthBranch = 4;
	private static double checkWorstRings = 0.5; //max = 0


	private int branchNo;


	public Branch(){
		for(Ring r: this){
			r.addBranch(this);
		}
	}

	public Branch(Ring ring, double step){
		//first branch
		IJ.log("first branch");
		this.step = step;
		this.add(ring);
		this.addAll(evolve( ring));
		Collections.reverse(this);
		this.addAll(evolve( ring.flippedRing()));
		Gui.network.add(this);
		this.branchNo = Gui.network.getLastBranchNo();
		this.regression();
		for(Ring r: this){
			r.addBranch(this);
		}
	}

	public Branch(ArrayList<Ring> branch, double step){
		this.step = step;
		this.addAll(branch);
		Gui.network.add(this);
		this.branchNo = Gui.network.getLastBranchNo();
		this.regression();
		for(Ring r: this){
			r.addBranch(this);
		}
	}

	public void regression(){
		class OneShotTask implements Runnable{
			Ring nextRing;
			OneShotTask(Ring nextRing) { 
				this.nextRing = nextRing;
			}			
			public void run() {
				Gui.ringsRunning.add(nextRing);
				Gui.updateRunning();
				try {
					ArrayList<Ring> ringsAround = sparseCandidates(nextRing);
					//sparseCandidate is not added to the branch, init is
					for(Ring r : ringsAround) {

						if(Gui.stopAll) break;
						ArrayList<Ring> branchCand = new ArrayList<Ring>();
						branchCand.add(nextRing);
						branchCand.addAll(evolve( r));

						if(branchCand.size()>=minLengthBranch) {
							new Branch(branchCand, step);
						}	
					}
				}
				catch(Exception e) {
					IJ.log(e.toString());
				}
				finally {
					Gui.ringsRunning.remove(nextRing);
					Gui.updateRunning();
				}
			}
		}


		for(Ring ring: this) {
			ring.eraseVol(Espacing_Ring.workingVol);
		}
		ArrayList<Ring> sortedBranchCopy = this.sortLowestContrastFirst();
		ArrayList<Thread> listOfThreads = new ArrayList<Thread>();
		for(int i = 0; i < (int) sortedBranchCopy.size()*checkWorstRings; i++){
			if(Gui.stopAll) break;
			IJ.log("checking ring: " + i);
			Ring nextRing = sortedBranchCopy.get(i);
			Thread t = new Thread(new OneShotTask(nextRing));
			t.start();
			listOfThreads.add(t);
		}
		if(Gui.synch){
			//IJ.log("Synching: " + listOfThreads.size() + " threads" );
			for(Thread t: listOfThreads){
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public ArrayList<Ring> evolve( Ring initial) {
		Ring current = initial.duplicate();
		int iter = 0;
		double prevMax = Gui.network.getMeanContrast() == -Double.MAX_VALUE ? initial.getContrast()*2 : Gui.network.getMeanContrast()*2; //later the contrast value is a sum of three rings
		prevMax = prevMax*branchFacilitator; //to lower the threshold of starting the new branch
		ArrayList<Ring> newBranch = new ArrayList<Ring>();
		//newBranch.add(current);
		double maxRadius = 1.40;
		double minRadius = 0.60;
		MAINLOOP:
			do {
				ArrayList<Ring> candidates = proposeCandidates(current, step,maxRadius, minRadius);

				if(newBranch.size()>0)candidates = keepRingsWhichDontOverlapWithOthers(candidates, 0.95, newBranch.get(newBranch.size()-1));
				else candidates = keepRingsWhichDontOverlapWithOthers(candidates, 0.2, null);//first ring
				if(candidates.size()==0) break MAINLOOP;

				//keep x% best
				candidates = keepBestCandidates(candidates, firstLoopElimination);
				if(candidates.size()==0) break MAINLOOP;	

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

				double currentContrast = best.getContrast();

				ArrayList<Ring> candidatesRefine = refineCandidate(best);
				for(Ring cand4 : candidatesRefine) {
					if (cand4.getContrast() > currentContrast) {
						currentContrast = cand4.getContrast();
						best = cand4;
					}
				}

				max = currentContrast + rest; 

				if(max<prevMax*evolveValue || max==0) {
					//check if there is a branching point, always break
					Ring closestRing = current.getClosestRing();
					//IJ.log("Closest:" + closestRing);
					if(closestRing!= null && current.getC().distance(closestRing.getC())<step*4){
						newBranch.add(closestRing);
					}
					break MAINLOOP;
				}

				newBranch.add(best);

				current = best.duplicate();


				//erase ring 2 places backwards
				if(newBranch.size()>=2  && newBranch.size()>= minLengthBranch-1) {
					Gui.network.recalculateContrast(best.getContrast());
					IJ.log("------>" +Gui.ringsUsed.size());
					if(!Gui.ringsUsed.contains(newBranch.get(newBranch.size()-2))) Gui.ringsUsed.add(newBranch.get(newBranch.size()-2));
					if(!Gui.ringsUsed.contains(best)) Gui.ringsUsed.add(best);
					newBranch.get(newBranch.size()-2).eraseVol(Espacing_Ring.workingVol);
				}
				prevMax = Gui.network.getMeanContrast()*2;

				IJ.log(" after iter"  + iter + " current contrast:  " +currentContrast + " mean: " + Gui.network.getMeanContrast() + " nrRings: " + Gui.network.getTotalNumberRings() + " totalContrast: " + Gui.network.getTotalContrast());
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
		int cutOffPos = (int) Math.round(percent*rings.size() /100);
		double cutOffContr = contrasts[cutOffPos];
		for(Ring ring : rings){
			if(ring.getContrast()>=cutOffContr)
				bestCands.add(ring);
		}
		return bestCands;
	}

	private ArrayList<Ring> keepRingsWhichDontOverlapWithOthers(ArrayList<Ring> rings, double rate, Ring previous){
		//keeps percent of best candidates
		ArrayList<Ring> keepCands = new ArrayList<Ring>();	
		keepCands.addAll(rings);
		for(Ring cand: rings) {
			for(Ring r: Gui.ringsUsed){
				if(previous != null && r.equals(previous)==false){
					if( cand.getC().distance(r.getC()) < (cand.getRadius()+r.getRadius())*rate){
						if(keepCands.contains(cand)) keepCands.remove(cand);
						break;
					}
				}
			}
		}
		return keepCands;
	}

	private ArrayList<Ring> proposeCandidates(Ring ring, double step, double maxRadius, double minRadius) {
		ArrayList<Ring> cands = new ArrayList<Ring>();	
		double angleStep = Math.PI/10;
		int angleRange = 1;

		double initRadius = ring.getRadius();
		//double maxRadius = 1.40;
		double maxMeasurmentArea = 2;

		for(double dt = -angleRange*angleStep; dt<=angleRange*angleStep; dt+=angleStep) {
			for(double dp = -angleRange*angleStep; dp<=angleRange*angleStep; dp+=angleStep) {	
				//return the MeasurmentVolume
				Ring maxRing = ring.duplicate();
				maxRing.setRadius(initRadius*maxRadius*maxMeasurmentArea);
				double polar[] = maxRing.getAnglesFromDirection();
				maxRing.setC(maxRing.getPositionFromSphericalAngles(step, polar[0] + dt, polar[1] + dp));
				maxRing.setDir(new Point3D((maxRing.getC().getX() - ring.getC().getX()) / step, (maxRing.getC().getY()-ring.getC().getY())/step, (maxRing.getC().getZ()-ring.getC().getZ())/step));
				MeasurmentVolume mv = new MeasurmentVolume(Espacing_Ring.workingVol, maxRing);
				//IJ.log("radius: " + maxRing.radius);
				//IJ.log(mv.toString());

				for(double r = initRadius*minRadius; r<initRadius*maxRadius; r+=0.20*initRadius) {
					Ring cand = maxRing.duplicate();
					cand.setRadius(r);
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

		double initRadius = ring.getRadius();
		double maxRadius = 1.01;
		double maxMeasurmentArea = 2;


		for(double dt = -angleRange*angleStep; dt<=angleRange*angleStep; dt+=angleStep) {
			for(double dp = -angleRange*angleStep; dp<=angleRange*angleStep; dp+=angleStep) {	
				//return the MeasurmentVolume
				Ring maxRing = ring.duplicate();
				double polar[] = maxRing.getAnglesFromDirection();
				maxRing.setRadius(initRadius*maxRadius*maxMeasurmentArea);
				maxRing.setDir(maxRing.getDirectionFromSphericalAngles(polar[0] + dt, polar[1] + dp));
				MeasurmentVolume mv = new MeasurmentVolume(Espacing_Ring.workingVol, maxRing);
				//IJ.log("radius: " + maxRing.radius);
				//IJ.log(mv.toString());

				for(double r = initRadius*0.99; r<initRadius*maxRadius; r+=0.01*initRadius) {
					Ring cand = maxRing.duplicate();
					cand.setRadius(r);
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

		double initRadius = ring.getRadius();
		double maxRadius = 1.75;
		double maxMeasurmentArea = 2;


		for(double dt = -angleRange*angleStep; dt<=angleRange*angleStep; dt+= angleStep) {
			for(double dp = -angleRange*angleStep; dp<=angleRange*angleStep; dp+= angleStep) {	
				Ring maxRing = ring.duplicate();
				double polar[] = maxRing.getAnglesFromDirection();
				maxRing.setRadius (initRadius*maxRadius*maxMeasurmentArea);
				maxRing.setDir (maxRing.getDirectionFromSphericalAngles(polar[0] + dt, polar[1] + dp));
				double r = initRadius;
				//for(double r = initRadius*0.25; r<=initRadius*maxRadius; r+=0.75*initRadius) {
				Ring cand = maxRing.duplicate();
				cand.setRadius(r);
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
		newB.step = step;
		//newB.evolveValue = evolveValue ; //for finishing the branch threshold
		newB.branchNo = branchNo;
		return newB;
	}
	public static Branch createBranchBetweenTwoRings(Ring start, Ring end, double width){
		//creates a single long ring between centers of two rings
		Point3D startPoint = start.getC();
		Point3D endPoint = end.getC();
		double avgRadius = width;
		double distanceBetween = startPoint.distance(endPoint);
		Point3D newMiddle = startPoint.middlePoint(endPoint);
		Ring newRing =  new Ring(newMiddle.getX(), newMiddle.getY(), newMiddle.getZ(), avgRadius, distanceBetween);
		newRing.setDir(startPoint.middlePointDir(endPoint));

		Branch newBranch = new Branch();
		newBranch.add(start);
		newBranch.add(newRing);
		newBranch.add(end);
		newBranch.eraseBranch();
		Gui.network.add(newBranch);


		return newBranch;
	}

	public static Branch createBranchBetweenRingAndPoint(Ring start, Point3D endPoint, double width){
		Point3D startPoint = start.getC();
		double avgRadius = width;
		double distanceBetween = startPoint.distance(endPoint);
		Point3D newMiddle = startPoint.middlePoint(endPoint);
		Ring endRing =  new Ring(endPoint.getX(), endPoint.getY(), endPoint.getZ(), avgRadius, start.getLength());
		Ring newRing =  new Ring(newMiddle.getX(), newMiddle.getY(), newMiddle.getZ(), avgRadius, distanceBetween);
		newRing.setDir(startPoint.middlePointDir(endPoint));	
		endRing.setDir(startPoint.middlePointDir(endPoint));

		Branch newBranch = new Branch();
		newBranch.add(start);
		newBranch.add(newRing);
		newBranch.add(endRing);
		newBranch.eraseBranch();
		Gui.network.add(newBranch);
		return newBranch;

	}

	public void restoreBranch(){
		for(Ring ring : this){
			ring.restoreVol(Espacing_Ring.workingVol, Espacing_Ring.vol);
			if(ring.getBranches().size()==1 && Gui.ringsUsed.contains(ring)) {
				Gui.ringsUsed.remove(ring);
			}			
		}
	}

	public void eraseBranch(){
		for(Ring ring: this) {
			ring.eraseVol(Espacing_Ring.workingVol);
		}
	}

	public ArrayList<Ring> sortLowestContrastFirst(){
		ArrayList<Ring> output = new ArrayList<Ring>();
		ArrayList<Ring> input = new ArrayList<Ring>();
		input.addAll(this);
		Collections.shuffle(input);
		while(input.size()>0){
			double minContrast = Double.MAX_VALUE;
			Ring minRing = null;
			for(Ring r : input){
				if(r.getContrast()<minContrast){
					minContrast = r.getContrast();
					minRing = r;
				}
			}
			output.add(minRing);
			input.remove(minRing);
		}
		return output;

	}


	/*GETTERS AND SETTERS*/
	public static void stopAll(boolean stop) {
		Gui.stopAll = stop;
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
	public double getStep() {
		return step;
	}
	public void setStep(double step) {
		this.step = step;
	}
	public static double getEvolveValue() {
		return evolveValue;
	}
	public static double getCheckWorstRings() {
		return checkWorstRings;
	}

	public static void setCheckWorstRings(double checkWorstRings) {
		Branch.checkWorstRings = checkWorstRings;
	}

	public static double getBranchFacilitator() {
		return branchFacilitator;
	}
	public static double getFirstLoopElimination() {
		return firstLoopElimination;
	}
	public static double getSecondLoopElimination() {
		return secondLoopElimination;
	}
	public static double getThirdLoopElimination() {
		return thirdLoopElimination;
	}
	public void setBranchNo(int branchNo) {
		this.branchNo = branchNo;
	}


	@Override
	public String toString() {
		return "BranchNo=" + branchNo  + " branchSize= " + this.size();
	}

}
