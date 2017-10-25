import java.util.ArrayList;
import java.util.Arrays;

import ij.IJ;

public class Branch extends ArrayList<Ring>{

	public Branch(Ring ring, Volume vol, Volume test, Volume workingVol, double step){
		this.add(ring);
		this.evolve(vol, ring, step, test, 0.7);
		//evolve in opposite direction
		this.evolve(vol, ring.flippedRing(), step, test, 0.7);
		this.regression(workingVol, test, step);
	}
	
	public void regression(Volume workingVol, Volume test, double width){
		//erase the whole branch
		for(Ring ring: this) {
			ring.eraseVol(workingVol, width);
		}
		workingVol.showTwoChannels("Working volume", test);
		
		Branch branchCopy = (Branch) this.clone();
		for(int i = branchCopy.size()-1; i >0 ; i--){
			Ring nextRing = branchCopy.get(i-1);
			double step = width;
			evolve(workingVol, nextRing, step, test, 0.9);
			evolve(workingVol, nextRing.flippedRing(), step, test, 0.9);
		}
	}
	
	public void evolve(Volume vol, Ring initial, double step, Volume test, double breakValue) {

		Ring current = initial.duplicate();
		int iter = 0;
		double prevMax = -Double.MAX_VALUE;
		
		MAINLOOP:
			do {
				ArrayList<Ring> candidates = proposeCandidates(current, step, vol);
				//keep x% best
				candidates = keepBestCandidates(candidates, 40);
				ArrayList<Ring[]> candidatesTriple = new ArrayList<Ring[]>();
				for ( Ring cand : candidates){
					ArrayList<Ring> candidates2 = proposeCandidates(cand, step, vol);
					//keep x% best
					candidates2 = keepBestCandidates(candidates2, 20);
					for (Ring cand2 : candidates2){
						ArrayList<Ring> candidates3 = proposeCandidates(cand2, step, vol);
						candidates3 = keepBestCandidates(candidates3, 20);
						for (Ring cand3 : candidates3){
							candidatesTriple.add(new Ring[]{cand,cand2, cand3});
						}	
					}
				}
				
				//calculating the best contrast out of those three rings
				Ring best = null; //first ring of couple
				double max = -Double.MAX_VALUE;
				for(Ring[] cC : candidatesTriple) {
					double c = cC[0].contrast + cC[1].contrast + cC[2].contrast;
					//IJ.log(" c: " + c);
					if (c > max) {
						max = c;
						best = cC[0];	
					}
				}
				
				if(max<prevMax*breakValue) break MAINLOOP;
				
				//adjust the first ring with more subtle parameter change
				
				Ring current1 = best.duplicate();
				ArrayList<Ring> candidatesRefine = proposeCandidates(current1, step, vol, "refine");
				for(Ring cand : candidatesRefine) {
					double c = cand.contrast;
					//IJ.log(" c: " + c);
					if (c > max) {
						max = c;
						best = cand;	
					}
				}
							
				best.drawMeasureArea(test, step);
				//drawCenterLine(vol, best);
				this.add(best);
				
				current = best.duplicate();
				IJ.log(" after iter"  + iter + " " + candidatesTriple.size() );
				prevMax=max;
				iter++;
			}
			while (true);
	}
	
	private ArrayList<Ring> keepBestCandidates(ArrayList<Ring> rings, double percent) {
		//keeps percent of best candidates
		percent = 100 - percent;
		ArrayList<Ring> bestCands = new ArrayList<Ring>();	
		double[]contrasts = new double[rings.size()];
		int i=0;
		for(Ring ring : rings){
			contrasts[i] = ring.contrast;
			i++;
		}
		Arrays.sort(contrasts);
		//IJ.log(Arrays.toString(contrasts));
		int cutOffPos = (int) Math.round(percent*rings.size() /100);
		double cutOffContr = contrasts[cutOffPos];
		//IJ.log("cutoff: "+cutOffContr);
		for(Ring ring : rings){
			if(ring.contrast>=cutOffContr)
			bestCands.add(ring);
		}
		//IJ.log("cutoff: " + cutOffContr);
		return bestCands;

	}
	
	private ArrayList<Ring> proposeCandidates(Ring ring, double step, Volume volume) {
		ArrayList<Ring> cands = new ArrayList<Ring>();	
		double angleStep = Math.PI/10;
		int angleRange = 1;

		double initRadius = ring.radius;
		double maxRadius = 1.40;
		double maxMeasurmentArea = 2;
		double width = step;
		step = step/2;


		for(double dt = -angleRange*angleStep; dt<=angleRange*angleStep; dt+=angleStep) {
			for(double dp = -angleRange*angleStep; dp<=angleRange*angleStep; dp+=angleStep) {	
				//return the MeasurmentVolume
				Ring maxRing = ring.duplicate();
				maxRing.radius = initRadius*maxRadius*maxMeasurmentArea;
				double polar[] = maxRing.getAnglesFromDirection();
				maxRing.c = maxRing.getPositionFromSphericalAngles(step, polar[0] + dt, polar[1] + dp);
				maxRing.dir = new Point3D((maxRing.c.x-ring.c.x)/step, (maxRing.c.y-ring.c.y)/step, (maxRing.c.z-ring.c.z)/step);
				MeasurmentVolume mv = new MeasurmentVolume(volume, maxRing, width);
				//IJ.log("radius: " + maxRing.radius);
				//IJ.log(mv.toString());

				for(double r = initRadius*0.80; r<initRadius*maxRadius; r+=0.20*initRadius) {
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

	private ArrayList<Ring> proposeCandidates(Ring ring, double step, Volume volume, String refine) {
		ArrayList<Ring> cands = new ArrayList<Ring>();	
		double angleStep = Math.PI/20;
		int angleRange = 3;

		double initRadius = ring.radius;
		double maxRadius = 1.05;
		double maxMeasurmentArea = 2;
		double width = step;
		step = 0;


		for(double dt = -angleRange*angleStep; dt<=angleRange*angleStep; dt+=angleStep) {
			for(double dp = -angleRange*angleStep; dp<=angleRange*angleStep; dp+=angleStep) {	
				//return the MeasurmentVolume
				Ring maxRing = ring.duplicate();
				maxRing.radius = initRadius*maxRadius*maxMeasurmentArea;
				double polar[] = maxRing.getAnglesFromDirection();
				maxRing.c = maxRing.getPositionFromSphericalAngles(step, polar[0] + dt, polar[1] + dp);
				maxRing.dir = new Point3D((maxRing.c.x-ring.c.x)/step, (maxRing.c.y-ring.c.y)/step, (maxRing.c.z-ring.c.z)/step);
				MeasurmentVolume mv = new MeasurmentVolume(volume, maxRing, width);
				//IJ.log("radius: " + maxRing.radius);
				//IJ.log(mv.toString());

				for(double r = initRadius*0.95; r<initRadius*maxRadius; r+=0.05*initRadius) {
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

}
