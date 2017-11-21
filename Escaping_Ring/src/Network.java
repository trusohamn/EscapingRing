import java.util.ArrayList;
import javax.swing.DefaultListModel;

public class Network extends ArrayList<Branch> {
	private int totalNumberRings = 0;
	private double totalContrast = 0;
	private double meanContrast = -Double.MAX_VALUE;
	DefaultListModel<String> branchList;

	public Network(DefaultListModel<String> branchList){
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

	public void lowerMeanContrast(double percent) {
		this.meanContrast = meanContrast*percent;
	}

	@Override public boolean add(Branch branch) {
		branchList.addElement(branch.toString());
		return super.add(branch);
	}

}
