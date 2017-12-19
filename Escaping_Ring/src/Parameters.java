import java.util.Arrays;
import java.util.List;

public class Parameters {
	String imageName;
	int xc, yc, zc;
	int radius;
	double step,  impInside,  impOutside,  threshold, branchFacilitator;
	double firstLoop,  secondLoop,  thirdLoop;
	double maxIn, minMem,  maxMem,  minOut,  maxOut;

	public Parameters(String imageName, double step, double impInside,
			double impOutside, double threshold, double branchFacilitator, double firstLoop, double secondLoop,
			double thirdLoop, double maxIn, double minMem, double maxMem, double minOut, double maxOut) {
		this.step = step;
		this.impInside = impInside;
		this.impOutside = impOutside;
		this.threshold = threshold;
		this.branchFacilitator = branchFacilitator;
		this.firstLoop = firstLoop;
		this.secondLoop = secondLoop;
		this.thirdLoop = thirdLoop;
		this.maxIn = maxIn;
		this.minMem = minMem;
		this.maxMem = maxMem;
		this.minOut = minOut;
		this.maxOut = maxOut;
		this.imageName = imageName;
	}
	
	
	
	public Parameters(String imageName, int xc, int yc, int zc, int radius, double step, double impInside,
			double impOutside, double threshold, double branchFacilitator, double firstLoop, double secondLoop,
			double thirdLoop, double maxIn, double minMem, double maxMem, double minOut, double maxOut) {
		this(imageName,  step,  impInside,
				 impOutside, threshold, branchFacilitator, firstLoop,  secondLoop,
				thirdLoop, maxIn, minMem,  maxMem, minOut, maxOut);
		this.xc = xc;
		this.yc = yc;
		this.zc = zc;
		this.radius = radius;
	
	}

	public List<String>  listParams(){
		List<String> row = Arrays.asList(String.valueOf(imageName), String.valueOf(xc), String.valueOf(yc), String.valueOf(zc), String.valueOf(radius), String.valueOf(step),
				String.valueOf(impInside), String.valueOf(impOutside), String.valueOf(threshold), String.valueOf(branchFacilitator), String.valueOf(firstLoop),
				String.valueOf(secondLoop), String.valueOf(thirdLoop), String.valueOf(maxIn), String.valueOf(minMem),
				String.valueOf(maxMem), String.valueOf(minOut), String.valueOf(maxOut));
		
		
		return row;
	}
	
}
