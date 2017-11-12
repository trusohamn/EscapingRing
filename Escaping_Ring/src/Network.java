import java.util.ArrayList;

public class Network extends ArrayList<Branch> {
	private int totalNumberRings = 0;
	private double totalContrast = 0;
	private double meanContrast = -Double.MAX_VALUE;

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
	
	public void lowerMeanContrast(double percent) {
		this.meanContrast = meanContrast*percent;
	}
	
}
