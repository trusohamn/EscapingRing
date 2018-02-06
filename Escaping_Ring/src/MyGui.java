
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.StackWindow;
import ij.measure.Calibration;
import ij.process.ImageProcessor;

public class MyGui extends JDialog {

	private static final long serialVersionUID = 1L;
	static DefaultListModel<Branch> branchList = new DefaultListModel<Branch>();
	static DefaultListModel<Branch> extraBranchList = new DefaultListModel<Branch>();
	DefaultListModel<Ring> ringList = new DefaultListModel<Ring>();
	static ArrayList<Ring> ringsUsed = new ArrayList<Ring>();
	JList<Branch> list;

	static Network network = new Network(branchList);

	double step;
	double impInside;
	double impOutside;
	double threshold;
	double firstLoop, secondLoop, thirdLoop;
	double maxIn, widthMem, minOut, maxOut;
	double branchFacilitator, checkWorstRings; 

	static JLabel runningLabel;
	static JLabel meanContrastLabel;
	static JLabel loadedImageLabel;
	static JButton cancelButton = null;
	static JPanel downPanel;

	JFormattedTextField sepField = null;
	static JFormattedTextField nameField = null;
	JFormattedTextField firstField = null;
	JFormattedTextField secondField = null;
	JFormattedTextField thirdField = null;
	JFormattedTextField maxInField = null;
	JFormattedTextField widthMemField = null;
	JFormattedTextField minOutField = null;
	JFormattedTextField maxOutField = null; 
	JFormattedTextField checkWorstRingsField = null;
	Point3D end;

	static ArrayList<Parameters> usedParameters = new ArrayList<Parameters>();
	static ArrayList<Parameters> toUseParameters = new ArrayList<Parameters>();
	static ArrayList<Ring> ringsRunning = new ArrayList<Ring>();
	static boolean stopAll;
	static boolean roiRunning = false; //not needed for now
	static boolean synch = false;
	ArrayList<MouseListener> activatedListeners = new ArrayList<MouseListener>();

	

	public MyGui() {
		JPanel tab1;
		JPanel tab2;
		JPanel tab3;
		JPanel tab4;
		JPanel tab5;
		JPanel tab6;
		final JButton showButton = new JButton("Show");
		final JButton resetButton = new JButton("Reset");
		setBounds(100, 100, 750, 300);
		setTitle("Vessel3DTracer");


		IJ.log("Starting GUI");
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				IJ.log("Terminating processing");
				Branch.stopAll(true);
				dispose();
			}
		});


		/*****TAB1*****/	 
		tab1 = new JPanel();
		tab1.setLayout(new BorderLayout());

		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		tab1.add(leftPanel, BorderLayout.CENTER);

		JPanel downPanel = new JPanel();
		downPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		tab1.add(downPanel, BorderLayout.SOUTH);


		JLabel stepLabel = new JLabel("Step size");
		JFormattedTextField stepField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		stepField.setColumns(5);
		stepField.setText("5");
		stepLabel.setLabelFor(stepField);
		leftPanel.add(stepLabel);
		leftPanel.add(stepField);

		JLabel thresholdLabel = new JLabel("Threshold");
		JFormattedTextField thresholdField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		thresholdField.setColumns(5);
		thresholdField.setText("0.4");
		leftPanel.add(thresholdLabel);
		leftPanel.add(thresholdField);

		JLabel branchLabel = new JLabel("Branching");
		JFormattedTextField branchField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		branchField.setColumns(5);
		branchField.setText("1");
		leftPanel.add(branchLabel);
		leftPanel.add(branchField);


		JLabel impInsideLabel = new JLabel("Importance inside");
		JFormattedTextField impInsideField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		impInsideField.setColumns(5);
		impInsideField.setText("-0.5");
		impInsideLabel.setLabelFor(impInsideField);
		downPanel.add(impInsideLabel);
		downPanel.add(impInsideField);

		JLabel impOutsideLabel = new JLabel("outside");
		JFormattedTextField impOutsideField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		impOutsideField.setColumns(5);
		impOutsideField.setText("-0.5");
		impOutsideLabel.setLabelFor(impOutsideField);
		downPanel.add(impOutsideLabel);
		downPanel.add(impOutsideField);


		final JButton btn1 = new JButton("Start");
		btn1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				try {
					step= Double.parseDouble(stepField.getText());
					impInside = Double.parseDouble(impInsideField.getText());
					impOutside = Double.parseDouble(impOutsideField.getText());
					threshold = Double.parseDouble(thresholdField.getText());
					branchFacilitator = Double.parseDouble(branchField.getText());
					firstLoop = Double.parseDouble(firstField.getText());
					secondLoop = Double.parseDouble(secondField.getText());
					thirdLoop = Double.parseDouble(thirdField.getText());
					maxIn = Double.parseDouble(maxInField.getText());
					widthMem = Double.parseDouble(widthMemField.getText());
					minOut = Double.parseDouble(minOutField.getText());
					maxOut = Double.parseDouble(maxOutField.getText());
					checkWorstRings = Double.parseDouble(checkWorstRingsField.getText());
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				Espacing_Ring.start(network, step, impInside, impOutside, threshold, branchFacilitator, firstLoop, secondLoop, thirdLoop,
						maxIn, widthMem, minOut, maxOut, checkWorstRings);

			}
		}); 
		leftPanel.add(btn1);

		final JButton btnContrast = new JButton("ResetContrast");
		btnContrast.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				network.resetContrast();
				updateMeanContrast();
			}
		}); 
		downPanel.add(btnContrast);

		final JButton btnTrySeed = new JButton("Try seed");
		btnTrySeed.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				try {
					step= Double.parseDouble(stepField.getText());
					impInside = Double.parseDouble(impInsideField.getText());
					impOutside = Double.parseDouble(impOutsideField.getText());
					threshold = Double.parseDouble(thresholdField.getText());
					branchFacilitator = Double.parseDouble(branchField.getText());
					firstLoop = Double.parseDouble(firstField.getText());
					secondLoop = Double.parseDouble(secondField.getText());
					thirdLoop = Double.parseDouble(thirdField.getText());
					maxIn = Double.parseDouble(maxInField.getText());
					widthMem = Double.parseDouble(widthMemField.getText());
					minOut = Double.parseDouble(minOutField.getText());
					maxOut = Double.parseDouble(maxOutField.getText());
					checkWorstRings = Double.parseDouble(checkWorstRingsField.getText());
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				Ring r = Espacing_Ring.trySeedRing(network, step, impInside, impOutside, threshold, branchFacilitator, firstLoop, 
						secondLoop, thirdLoop, maxIn, widthMem, minOut, maxOut, checkWorstRings);
				String message = "Initial ring radius: " + String.format(Locale.US, "%.2f", r.getRadius()) + "\ncontrast: " + String.format(Locale.US, "%.2f",r.getContrast());
				JOptionPane.showMessageDialog(downPanel, message);


			}
		}); 
		leftPanel.add(btnTrySeed);

		/*CHANGE PARAMETERS BETWEEN FILLED AND EMPTY VESSELS*/

		JRadioButton emptyButton = new JRadioButton("hollow tube");
		emptyButton.setSelected(true);
		emptyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				impInsideField.setText("-0.5");
				impOutsideField.setText("-0.5");
				widthMemField.setText("0.1");
				maxInField.setText("0.8");
				minOutField.setText("1.2");
			}
		}); 
		downPanel.add(emptyButton);

		JRadioButton filledButton = new JRadioButton("filled tube");
		filledButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				impInsideField.setText("1");
				impOutsideField.setText("-1");
				widthMemField.setText("0");
				maxInField.setText("1");
				minOutField.setText("1");
			}
		}); 
		downPanel.add(filledButton);

		JRadioButton otherButton = new JRadioButton("other");
		otherButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {

			}
		}); 
		downPanel.add(otherButton);

		ButtonGroup group = new ButtonGroup();
		group.add(emptyButton);
		group.add(filledButton);
		group.add(otherButton);


		/*****TAB2 Branches*****/
		tab2 = new JPanel();
		tab2.setLayout(new GridLayout(1, 2, 5, 5));
		JPanel tab2Left = new JPanel();
		JPanel tab2Right = new JPanel();
		tab2Left.setLayout(new BorderLayout());
		tab2Right.setLayout(new BorderLayout());
		tab2.add(tab2Left);
		tab2.add(tab2Right);

		JPanel tab2Down= new JPanel();
		tab2Down.setLayout(new FlowLayout(FlowLayout.LEFT));
		JPanel tab2UpLeft= new JPanel();
		tab2UpLeft.setLayout(new FlowLayout(FlowLayout.LEFT));

		tab2.add(tab2Left);
		tab2.add(tab2Right);
		tab2Left.add(tab2Down,BorderLayout.SOUTH);
		tab2Left.add(tab2UpLeft,BorderLayout.NORTH);

		list = new JList<Branch>(branchList);
		JPanel listPanel = new JPanel();
		listPanel.add(list, BorderLayout.CENTER);
		JScrollPane scrol = new JScrollPane(list);
		tab2Left.add(scrol,BorderLayout.WEST);
		scrol.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		tab2Left.add(listPanel);


		JLabel alistLabel = new JLabel("All branches: ");
		tab2UpLeft.add(alistLabel);

		JList<Branch> list2 = new JList<Branch>(extraBranchList);
		JPanel listPanel2 = new JPanel();
		listPanel2.add(list2, BorderLayout.CENTER);
		JScrollPane scrol2 = new JScrollPane(list2);
		scrol2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		tab2Right.add(scrol2,BorderLayout.EAST);
		tab2Right.add(listPanel2);

		JLabel listLabel = new JLabel("Chosen branches: ");
		JPanel listLabelPanel = new JPanel();
		listLabelPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		listLabelPanel.add(listLabel);
		tab2Right.add(listLabelPanel, BorderLayout.NORTH);

		JPanel buttonListPanel = new JPanel();
		buttonListPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		tab2Right.add(buttonListPanel, BorderLayout.AFTER_LAST_LINE);

		final JButton btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				ArrayList<Branch> toRemove = new ArrayList<Branch>();
				for(int i=0; i< extraBranchList.getSize(); i++){
					toRemove.add(extraBranchList.getElementAt(i));
				}
				Espacing_Ring.generateView(true);
				
				for(Branch b: toRemove){
					network.remove(b);
					b.restoreBranch();
					b.redrawRawBranch(Espacing_Ring.iC.getImage());
				}
				updateRingsUsed();
				for(Branch b: toRemove){
					for(Ring r: b) {
						if(ringsUsed.contains(r)) Espacing_Ring.drawRingBranchEndPoints(r);
					}
				}
				Espacing_Ring.iC.repaint();
			}
		}); 
		buttonListPanel.add(btnDelete);

		final JButton btnCleanBranches = new JButton("Clean");
		btnCleanBranches.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				ArrayList<Branch> toRemove = new ArrayList<Branch>();
				for(int i=0; i< extraBranchList.getSize(); i++){
					toRemove.add(extraBranchList.getElementAt(i));
				}
				Espacing_Ring.generateView(true);
				for(Branch b: toRemove){
					extraBranchList.removeElement(b);
					Espacing_Ring.drawBranchBranchEndPoints(b);
				}
								
				Espacing_Ring.iC.repaint();
			}
		}); 
		buttonListPanel.add(btnCleanBranches);


		final JButton clickBranches = new JButton("Click branches");
		clickBranches.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				removeImageListeners();
				MouseListener mouseListenerImage = new MouseAdapter() {
					public void mouseClicked(MouseEvent mouseEvent) {				
						activatedListeners.add(this);
						Point location = Espacing_Ring.iC.getCursorLoc();
						int x = location.x;
						int y = location.y;
						int z = Espacing_Ring.iC.getImage().getSlice();
						Point3D target = new Point3D(x, y, z);
						IJ.log("click! " + z+y+z);
						double minDistance = Double.MAX_VALUE;
						Branch closestBranch = null;
						for(Branch branch : network){
							for(Ring ring : branch){
								double thisDistance=target.distance(ring.getC());
								if(thisDistance<minDistance){
									minDistance = thisDistance;
									closestBranch = branch;
								}
							}
						}

						if(closestBranch.isEmpty()==false){
							if(extraBranchList.contains(closestBranch)==false){
								extraBranchList.addElement(closestBranch);
								Espacing_Ring.generateView(true);
								Espacing_Ring.showResult(extraBranchList);
								Espacing_Ring.iC.repaint();
							}
						}

					}
				};

				Espacing_Ring.iC.addMouseListener(mouseListenerImage);
			}
		}); 
		listLabelPanel.add(clickBranches); 

		MouseListener mouseListener = new MouseAdapter() {
			//adds branch to the extra list
			public void mouseClicked(MouseEvent mouseEvent) {
				JList<Branch> theList = (JList<Branch>) mouseEvent.getSource();
				if (mouseEvent.getClickCount() == 2) {
					int index = theList.locationToIndex(mouseEvent.getPoint());
					if (index >= 0) {
						Branch o = theList.getModel().getElementAt(index);
						if(extraBranchList.contains(o)==false) {
							extraBranchList.addElement(o);
							Espacing_Ring.generateView(true);
							Espacing_Ring.showResult(extraBranchList);
							Espacing_Ring.iC.repaint();
						}
					}
				}
			}
		};
		list.addMouseListener(mouseListener);

		MouseListener mouseListener2 = new MouseAdapter() {
			//removes branch from the extra list
			public void mouseClicked(MouseEvent mouseEvent) {
				JList<Branch> theList = (JList<Branch>) mouseEvent.getSource();
				if (mouseEvent.getClickCount() == 2) {
					int index = theList.locationToIndex(mouseEvent.getPoint());
					if (index >= 0) {
						Branch o = theList.getModel().getElementAt(index);
						extraBranchList.removeElement(o);
						Espacing_Ring.generateView(true);
						Espacing_Ring.drawBranchBranchEndPoints(o);
						Espacing_Ring.showResult(extraBranchList);
						Espacing_Ring.iC.repaint();
					}
				}
			}
		};
		list2.addMouseListener(mouseListener2);


		/*FILTER BRANCHES*/
		JLabel filterLabel = new JLabel("Filter size");
		JFormattedTextField filterField = new JFormattedTextField();
		filterField.setColumns(3);
		filterField.setText("");
		tab2Down.add(filterLabel);
		tab2Down.add(filterField);

		JLabel filterLabel2 = new JLabel("Branch no");
		JFormattedTextField filterField2 = new JFormattedTextField();
		filterField2.setColumns(3);
		filterField2.setText("");
		tab2Down.add(filterLabel2);
		tab2Down.add(filterField2);

		final JButton filterButton = new JButton("Filter");
		filterButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {


				ArrayList<Branch> filteredBranches = new ArrayList<Branch>();
				boolean filtered = false;
				for(int i=0; i< branchList.getSize(); i++){
					filteredBranches.add(branchList.getElementAt(i));				
				}

				try {
					int filterSize= Integer.parseInt(filterField.getText());
					if(filterSize>0) {
						ArrayList<Branch> temp = new ArrayList<Branch>();
						temp.addAll(filteredBranches);
						for(int j=0; j<temp.size();j++) {
							if(temp.get(j).size()> filterSize) {
								filteredBranches.remove(temp.get(j));
							}
						}
						filtered = true;
					}

				} catch (Exception e) {
				}
				try {
					ArrayList<Branch> temp = new ArrayList<Branch>();
					temp.addAll(filteredBranches);
					int branchNo= Integer.parseInt(filterField2.getText());
					if(branchNo>0) {
						for(int j=0; j<temp.size();j++) {
							if(temp.get(j).getBranchNo()< branchNo) {
								filteredBranches.remove(temp.get(j));
							}
						}
						filtered = true;
					}

				} catch (Exception e) {
				}

				if(filtered) {
					for(Branch b : filteredBranches){
						if(!extraBranchList.contains(b)) extraBranchList.addElement(b);
					}
					Espacing_Ring.generateView(true);
					Espacing_Ring.showResult(extraBranchList);
					Espacing_Ring.iC.repaint();
				}
			}
		}); 
		tab2Down.add(filterButton);

		final JButton btnOrderNetwork = new JButton("Order network");
		btnOrderNetwork.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				network.orderBranchPoints();
				Espacing_Ring.generateView(true);
				Espacing_Ring.drawNetworkBranchEndPoints(network);
				Espacing_Ring.iC.repaint();

			}}); 
		tab2UpLeft.add(btnOrderNetwork);


		/*****TAB3 Rings*****/	 
		tab3 = new JPanel();
		tab3.setLayout(new GridLayout(1, 2, 5, 5));
		JPanel tab3Left = new JPanel();
		JPanel tab3Right = new JPanel();
		tab3Right.setLayout(new BorderLayout());
		tab3.add(tab3Left);
		tab3.add(tab3Right);

		tab3Left.setLayout(new GridLayout(6,1));

		JPanel firstRow = new JPanel();
		firstRow.setLayout(new FlowLayout(FlowLayout.LEFT));
		JPanel secondRow = new JPanel();
		secondRow.setLayout(new FlowLayout(FlowLayout.LEFT));
		JPanel thirdRow = new JPanel();
		thirdRow.setLayout(new FlowLayout(FlowLayout.LEFT));
		JPanel fourthRow = new JPanel();
		fourthRow.setLayout(new FlowLayout(FlowLayout.LEFT));
		tab3Left.add(firstRow);
		tab3Left.add(secondRow);
		tab3Left.add(thirdRow);
		tab3Left.add(fourthRow);

		JPanel selectRingPanelT = new JPanel();
		selectRingPanelT.setLayout(new FlowLayout(FlowLayout.RIGHT));
		tab3Right.add(selectRingPanelT, BorderLayout.NORTH);

		JList<Ring>listRing = new JList<Ring>(ringList);
		JPanel ringPanel = new JPanel();
		ringPanel.add(listRing);
		JScrollPane scrolRing = new JScrollPane(listRing);
		scrolRing.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);		
		tab3Right.add(scrolRing, BorderLayout.EAST);
		tab3Right.add(ringPanel);

		JPanel selectRingPanel = new JPanel();
		selectRingPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		tab3Right.add(selectRingPanel, BorderLayout.SOUTH);

		MouseListener mouseListener3 = new MouseAdapter() {
			//removes ring from the list
			public void mouseClicked(MouseEvent mouseEvent) {
				JList<Ring> theList = (JList<Ring>) mouseEvent.getSource();
				if (mouseEvent.getClickCount() == 2) {
					int index = theList.locationToIndex(mouseEvent.getPoint());
					if (index >= 0) {
						Ring o = theList.getModel().getElementAt(index);
						ringList.removeElement(o);
						showButton.doClick();
					}
				}
			}
		};
		listRing.addMouseListener(mouseListener3);

		final JButton clickRings = new JButton("Click rings");
		clickRings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				removeImageListeners();
				Espacing_Ring.iC.setVisible(true);
				MouseListener mouseListenerImage = new MouseAdapter() {
					public void mouseClicked(MouseEvent mouseEvent) {

						activatedListeners.add(this);
						Point location = Espacing_Ring.iC.getCursorLoc();
						int x = location.x;
						int y = location.y;
						int z = Espacing_Ring.iC.getImage().getSlice();
						Point3D target = new Point3D(x, y, z);

						double minDistance = Double.MAX_VALUE;
						Ring closestRing = null;
						for(Branch branch : network){
							for(Ring ring : branch){
								double thisDistance=target.distance(ring.getC());
								if(thisDistance<minDistance){
									minDistance = thisDistance;
									closestRing = ring;
								}
							}
						}

						if(closestRing!=null){
							if(ringList.contains(closestRing)==false){
								ringList.addElement(closestRing);
								Espacing_Ring.generateView(true);
								Espacing_Ring.showRings(ringList);
								Espacing_Ring.iC.repaint();
							}
						}
					}
				};
				Espacing_Ring.iC.addMouseListener(mouseListenerImage);
			}
		}); 
		selectRingPanelT.add(clickRings);


		final JButton btnDeleteRing = new JButton("Delete rings");
		btnDeleteRing.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				IJ.log(""+ringList.getSize());
				Espacing_Ring.generateView(true);
				ArrayList<Ring> ringsToRemove = new ArrayList<Ring>();
				for(int i=0; i< ringList.getSize(); i++){
					ringsToRemove.add(ringList.getElementAt(i));
				}
				for(int j=0; j< ringsToRemove.size(); j++){	
					Ring toRemove = ringsToRemove.get(j);
					ArrayList<Branch> motherBranches = toRemove.getBranches();
					for(Branch motherBranch : motherBranches){
						int indexOfRingToRemove = motherBranch.indexOf(toRemove);
						if(indexOfRingToRemove > 0 && indexOfRingToRemove < motherBranch.size()-1){
							//divide the branch if the ring from the middle is removed
							Branch newBranch1 = motherBranch.duplicateCrop(0, indexOfRingToRemove-1);
							Branch newBranch2 = motherBranch.duplicateCrop(indexOfRingToRemove+1, motherBranch.size()-1);
							network.remove(motherBranch);
							network.add(newBranch1);
							network.add(newBranch2);
							branchList.removeElement(motherBranch);
							extraBranchList.removeElement(motherBranch);
							
							toRemove.redrawRaw(Espacing_Ring.iC.getImage());
							Espacing_Ring.drawBranchBranchEndPoints(newBranch1);
							Espacing_Ring.drawBranchBranchEndPoints(newBranch2);
						}
						else{
							//the ring is last of first of the branch
							motherBranch.remove(toRemove);
							toRemove.redrawRaw(Espacing_Ring.iC.getImage());
							Espacing_Ring.drawBranchBranchEndPoints(motherBranch);
						}}	
					ringList.removeElement(toRemove);
					updateRingsUsed();
					
				}
				
				Espacing_Ring.iC.repaint();
			}
		}); 
		selectRingPanel.add(btnDeleteRing);

		final JButton btnCleanRing = new JButton("Clean");
		btnCleanRing.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				ArrayList<Ring> ringsToRemove = new ArrayList<Ring>();
				for(int i=0; i< ringList.getSize(); i++){
					ringsToRemove.add(ringList.getElementAt(i));
				}
				Espacing_Ring.generateView(true);
				for(Ring toRemove: ringsToRemove){	
					ringList.removeElement(toRemove);
					Espacing_Ring.drawRingBranchEndPoints(toRemove);
				}				
				Espacing_Ring.iC.repaint();
			}
		}); 
		selectRingPanel.add(btnCleanRing);


		JLabel widthLabel = new JLabel("Width of new branch");
		JFormattedTextField widthField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		widthField.setColumns(5);
		widthField.setText("0");
		firstRow.add(widthLabel);
		firstRow.add(widthField);

		JLabel brLabel = new JLabel("Branch to detach");
		JFormattedTextField brField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		brField.setColumns(4);
		brField.setText("");
		thirdRow.add(brLabel);
		thirdRow.add(brField);

		final JButton btnJoinRings = new JButton("Join two rings");
		btnJoinRings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				if(ringList.getSize()==2){
					try {
						double width= Double.parseDouble(widthField.getText());
						Ring start = ringList.getElementAt(0);
						Ring end = ringList.getElementAt(1);
						Branch newBranch = Branch.createBranchBetweenTwoRings(start, end, width);
						Espacing_Ring.generateView(true);
						Espacing_Ring.drawBranchBranchEndPoints(newBranch);
						Espacing_Ring.iC.repaint();

					} catch (NumberFormatException e) {
						e.printStackTrace();
					}				
				}
				else{
					IJ.log("Select only two rings");
				}
			}
		}); 
		secondRow.add(btnJoinRings);

		final JButton btnFreeBranch = new JButton("Join ring and a point");
		btnFreeBranch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				if(ringList.getSize()==1){
					Ring start = ringList.getElementAt(0);
					JOptionPane.showMessageDialog(downPanel, "Select the end point in the image");
					removeImageListeners();
					MouseListener mouseListenerPoint = new MouseAdapter() {
						public void mouseClicked(MouseEvent mouseEvent) {

							activatedListeners.add(this);
							Point location = Espacing_Ring.iC.getCursorLoc();
							int x = location.x;
							int y = location.y;
							int z = Espacing_Ring.iC.getImage().getSlice();
							IJ.log("Endpoint: " + x + y + z);
							end = new Point3D(x, y, z);
							try {
								double width= Double.parseDouble(widthField.getText());
								Branch newBranch = Branch.createBranchBetweenRingAndPoint(start, end, width);
								Espacing_Ring.iC.removeMouseListener(this);
								Espacing_Ring.generateView(true);
								Espacing_Ring.drawBranchBranchEndPoints(newBranch);
								Espacing_Ring.iC.repaint();
							} catch (Exception e) {
								e.printStackTrace();
								IJ.log(e.toString());
							}
						}
					};
					Espacing_Ring.iC.addMouseListener(mouseListenerPoint);
				}
				else{
					JOptionPane.showMessageDialog(downPanel, "Select only one ring");
				}
			}
		}); 
		secondRow.add(btnFreeBranch);

		final JButton btnRemoveBranchFromRing = new JButton("Disconnect Branch from Branching Point");
		btnRemoveBranchFromRing.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				if(ringList.getSize()==1){
					try {
						int branchNo = Integer.parseInt(brField.getText());
						Ring r = ringList.getElementAt(0);
						if(r.getBranches().size()>1) {
							Branch toDis = null;
							for(Branch b : r.getBranches()) {
								if(b.getBranchNo()== branchNo) {
									toDis = b;
								}
							}
							if(toDis == null)JOptionPane.showMessageDialog(downPanel, "Ring doesnt belong to this Branch!");
							else {
								r.removeBranch(toDis);
								toDis.remove(r);
								Espacing_Ring.generateView(true);
								Espacing_Ring.drawRingBranchEndPoints(r);
								Espacing_Ring.iC.repaint();
							}
						}
						else JOptionPane.showMessageDialog(downPanel, "Select a branch point!");					

					} catch (NumberFormatException e) {
						e.printStackTrace();
					}				
				}
				else{
					JOptionPane.showMessageDialog(downPanel, "Select only one ring");
				}
			}
		}); 
		fourthRow.add(btnRemoveBranchFromRing);


		/*****TAB4 Export Import *****/	
		Border raisedetched = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);

		tab4 = new JPanel();
		tab4.setLayout(new GridLayout(1, 3, 8, 8));
		JPanel tab4row1 = new JPanel();
		tab4row1.setLayout(new GridLayout(6, 1, 8, 8));
		JPanel tab4row2 = new JPanel();
		tab4row2.setLayout(new GridLayout(6, 1, 8, 8));
		JPanel tab4row3 = new JPanel();
		tab4row3.setLayout(new GridLayout(6, 1, 8, 8));
		tab4row1.setBorder(raisedetched);
		tab4row2.setBorder(raisedetched);
		tab4row3.setBorder(raisedetched);
		tab4.add(tab4row1);
		tab4.add(tab4row2);
		tab4.add(tab4row3);

		JPanel sepPanel = new JPanel();
		sepPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

		JLabel septLabel = new JLabel("Default separator");
		sepField = new JFormattedTextField();
		sepField.setColumns(1);
		sepField.setText(",");
		sepPanel.add(septLabel);
		sepPanel.add(sepField);
		tab4row1.add(sepPanel);

		JPanel namePanel = new JPanel();
		namePanel.setLayout(new FlowLayout(FlowLayout.CENTER));

		JLabel nameLabel = new JLabel("Save as:");
		nameField = new JFormattedTextField();
		nameField.setColumns(15);
		nameField.setText("");
		namePanel.add(nameLabel);
		namePanel.add(nameField);
		tab4row3.add(namePanel);

		final JButton btnSkeleton = new JButton("Generate skeleton");
		btnSkeleton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				MyVolume skeleton = new MyVolume(Espacing_Ring.vol.nx, Espacing_Ring.vol.ny, Espacing_Ring.vol.nz);
				network.generateSkeleton(skeleton);
				skeleton.show("Skeleton");

			}
		}); 
		tab4row2.add(btnSkeleton);

		final JButton btnBinary = new JButton("Generate mask");
		btnBinary.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				MyVolume binary = new MyVolume(Espacing_Ring.vol.nx, Espacing_Ring.vol.ny, Espacing_Ring.vol.nz);
				network.createMask(binary, 0.5, true);

			}
		}); 
		tab4row2.add(btnBinary);
		
		final JButton btnOutline = new JButton("Generate outline");
		btnOutline.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				MyVolume binary = new MyVolume(Espacing_Ring.vol.nx, Espacing_Ring.vol.ny, Espacing_Ring.vol.nz);
				network.createMask(binary, 0.5, false);
				//binary.show("Binary");

			}
		}); 
		tab4row2.add(btnOutline);


		final JButton btnCSV = new JButton("Export measurments as csv");
		btnCSV.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {

				try {
					JFileChooser chooser = new JFileChooser(); 
					chooser.setCurrentDirectory(new java.io.File("."));
					chooser.setDialogTitle("Choose directory to save");
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					chooser.setAcceptAllFileFilterUsed(false);
					//    
					if (chooser.showOpenDialog(tab4) == JFileChooser.APPROVE_OPTION) {
						CSVUtils.setDEFAULT_SEPARATOR(sepField.getText().charAt(0));

						network.exportData(chooser.getSelectedFile().getPath()+ File.separator+nameField.getText() );
					}
					else {
						System.out.println("No Selection ");
					}

				} catch (IOException e) {
					IJ.log("failed to generate csv");
					e.printStackTrace();
				}		
			}
		}); 
		tab4row1.add(btnCSV);

		final JButton btnExportXML = new JButton("Export network");
		btnExportXML.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				/*
				try {

					if (chooser.showOpenDialog(tab4) == JFileChooser.APPROVE_OPTION) { 
						encoder=new XMLEncoder(new BufferedOutputStream(new FileOutputStream(objectName)));
						encoder.writeObject(network);
						encoder.close();
					}
				}
				catch (IOException e) {			
				}	
				 */
				try{
					JFileChooser chooser = new JFileChooser(); 
					chooser.setCurrentDirectory(new java.io.File("."));
					chooser.setDialogTitle("Choose directory to save");
					chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					chooser.setAcceptAllFileFilterUsed(false);
					if (chooser.showOpenDialog(tab4) == JFileChooser.APPROVE_OPTION){
						String objectName = chooser.getSelectedFile().getPath() + File.separator + nameField.getText() + ".ser";
						IJ.log("saving as: " + objectName);
						FileOutputStream fileOut =new FileOutputStream(objectName);
						ObjectOutputStream out = new ObjectOutputStream(fileOut);
						out.writeObject(network);
						out.close();
						fileOut.close();
						System.out.printf("Serialized data is saved ");}
				} catch (Exception e) {
					IJ.log(e.toString());
					e.printStackTrace();
				}}
		}); 
		tab4row3.add(btnExportXML);


		final JButton btnImportXML = new JButton("Import network");
		btnImportXML.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				/*try {
						XMLDecoder decoder=new XMLDecoder(new BufferedInputStream(new FileInputStream(objectName)));
						try{
							Network imported =(Network) decoder.readObject();
							decoder.close();
				}
				catch (Exception e){
					IJ.log("other exception");
					IJ.log(e.toString());
					e.printStackTrace();
				}
				 */
				JFileChooser chooser = new JFileChooser(); 
				chooser.setCurrentDirectory(new java.io.File("."));
				chooser.setDialogTitle("Choose .ser file");
				if (chooser.showOpenDialog(tab4) == JFileChooser.APPROVE_OPTION) { 
					String objectName = chooser.getSelectedFile().getPath();
					FileInputStream fileIn = null;
					try {
						CSVUtils.setDEFAULT_SEPARATOR(sepField.getText().charAt(0));
						fileIn = new FileInputStream(objectName);

						ObjectInputStream in = new ObjectInputStream(fileIn);
						Network n = (Network) in.readObject();
						IJ.log(n.toString());
						in.close();
						fileIn.close();
						
						Espacing_Ring.imp = WindowManager.getCurrentImage();
						if(Espacing_Ring.vol == null) {
							Espacing_Ring.vol = new MyVolume(Espacing_Ring.imp);
							Espacing_Ring.imageName = Espacing_Ring.imp.getTitle();
							MyGui.updateLoadedImage();
						}
						if(Espacing_Ring.workingVol == null) Espacing_Ring.workingVol = new MyVolume(Espacing_Ring.imp); 
						
						for(Branch b : n){
							network.add(b);
						}
						network.recalcualteContrast();
						updateMeanContrast();
						network.assignBranchesToRing();
						showButton.doClick();
						network.eraseNetworkVolume(Espacing_Ring.workingVol);
					}
					catch (Exception e) {
						JOptionPane.showMessageDialog(downPanel, "Make sure that the proper image is opened before loading the network");
						IJ.log(e.toString());
						e.printStackTrace();

					}
				}
			}
		}); 
		tab4row3.add(btnImportXML );

		final JButton btnExportParams = new JButton("Export parameters");
		btnExportParams .addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser(); 
				chooser.setCurrentDirectory(new java.io.File("."));
				chooser.setDialogTitle("Choose directory to save");
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);

				if (chooser.showOpenDialog(tab4) == JFileChooser.APPROVE_OPTION) {

					try {
						CSVUtils.setDEFAULT_SEPARATOR(sepField.getText().charAt(0));
						Parameters.exportParams(chooser.getSelectedFile().getPath()+File.separator + nameField.getText() +  "_Params.csv");
					} catch (IOException e) {

						e.printStackTrace();
					}
				}
				else {
					System.out.println("No Selection ");
				}
			}
		}); 

		tab4row1.add(btnExportParams);

		final JButton btnImportParams = new JButton("Import parameters");
		btnImportParams .addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser(); 
				chooser.setCurrentDirectory(new java.io.File("."));
				chooser.setDialogTitle("Choose .csv file with parameters");
				if (chooser.showOpenDialog(tab4) == JFileChooser.APPROVE_OPTION) { 
					CSVUtils.setDEFAULT_SEPARATOR(sepField.getText().charAt(0));
					String objectName = chooser.getSelectedFile().getPath();
					MyGui.toUseParameters = Parameters.importParams(objectName);
					for (Parameters p : MyGui.toUseParameters){
						IJ.log(p.toString());
					}
				}
			}
		}); 
		tab4row1.add(btnImportParams);

		final JButton btnStartParams = new JButton("Start parameters");
		btnStartParams.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				MyGui.synch = true;
				for(Parameters param: MyGui.toUseParameters){
					Espacing_Ring.start(network, param);
					IJ.log("Left: " + MyGui.ringsRunning.size());

				}
				MyGui.synch = false;
			}
		}); 
		tab4row1.add(btnStartParams);

		final JButton btnStartParamsSave = new JButton("Start parameters one by one");
		btnStartParamsSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				int n=0;
				JFileChooser chooser = new JFileChooser(); 
				chooser.setCurrentDirectory(new java.io.File("."));
				chooser.setDialogTitle("Choose directory to save");
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setAcceptAllFileFilterUsed(false);

				if (chooser.showOpenDialog(tab4) == JFileChooser.APPROVE_OPTION){
					MyGui.synch = true;
					CSVUtils.setDEFAULT_SEPARATOR(sepField.getText().charAt(0));
					for(Parameters param: MyGui.toUseParameters){
						Espacing_Ring.start(network, param);

						String objectName = chooser.getSelectedFile().getPath() + File.separator + nameField.getText() + n + ".ser";
						FileOutputStream fileOut;
						try {
							fileOut = new FileOutputStream(objectName);
							ObjectOutputStream out = new ObjectOutputStream(fileOut);
							out.writeObject(network);
							out.close();
							fileOut.close();
							System.out.printf("Serialized data is saved ");
						} 
						catch (Exception e) {

							e.printStackTrace();
						}

						btnContrast.doClick();
						resetButton.doClick();
						++n;
					}
					MyGui.synch = false;
				}
			}}); 
		tab4row1.add(btnStartParamsSave);


		/***TAB5 Advanced Settings *****/
		tab5 = new JPanel();
		tab5.setLayout(new GridLayout(1, 3, 8, 8));

		JPanel tab5Upper = new JPanel();
		tab5Upper.setLayout(new GridLayout(6, 1, 8, 8));
		tab5.add(tab5Upper);
		tab5Upper.setBorder(raisedetched);

		JPanel tab5Center = new JPanel();
		tab5Center.setLayout(new GridLayout(6, 1, 8, 8));
		tab5.add(tab5Center);
		tab5Center.setBorder(raisedetched);

		JPanel empty = new JPanel();
		tab5.add(empty);

		JPanel Cell1 = new JPanel();
		Cell1.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel firstLabel = new JLabel("Keep after 1st generation");
		firstField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		firstField.setColumns(3);
		firstField.setText("100");
		Cell1.add(firstLabel);
		Cell1.add(firstField);
		tab5Upper.add(Cell1);

		JPanel Cell2 = new JPanel();
		Cell2.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel secondLabel = new JLabel("Keep after 2nd generation");
		secondField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		secondField.setColumns(3);
		secondField.setText("5");
		Cell2.add(secondLabel);
		Cell2.add(secondField);
		tab5Upper.add(Cell2);

		JPanel Cell3 = new JPanel();
		Cell3.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel thirdLabel = new JLabel("Keep after 3rd generation");
		thirdField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		thirdField.setColumns(3);
		thirdField.setText("10");
		Cell3.add(thirdLabel);
		Cell3.add(thirdField);
		tab5Upper.add(Cell3);

		JPanel Cell4 = new JPanel();
		Cell4.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel checkWorstRingsLabel = new JLabel("Check weak Rings");
		checkWorstRingsField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		checkWorstRingsField.setColumns(4);
		checkWorstRingsField.setText("100");
		Cell4.add(checkWorstRingsLabel);
		Cell4.add(checkWorstRingsField);
		tab5Upper.add(Cell4);

		JPanel Cell5 = new JPanel();
		Cell5.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel maxInLabel = new JLabel("Max inside");
		maxInField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		maxInField.setColumns(4);
		maxInField.setText("0.9");
		Cell5.add(maxInLabel);
		Cell5.add(maxInField);
		tab5Center.add(Cell5);

		JPanel Cell6 = new JPanel();
		Cell6.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel widthMemLabel = new JLabel("Width membrane");
		widthMemField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		widthMemField.setColumns(4);
		widthMemField.setText("0.1");
		Cell6.add(widthMemLabel);
		Cell6.add(widthMemField);
		tab5Center.add(Cell6);


		JPanel Cell8 = new JPanel();
		Cell8.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel minOutLabel = new JLabel("Min outside");
		minOutField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		minOutField.setColumns(4);
		minOutField.setText("1.1");
		Cell8.add(minOutLabel);
		Cell8.add(minOutField);
		tab5Center.add(Cell8);

		JPanel Cell9 = new JPanel();
		Cell9.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel maxOutLabel = new JLabel("Max outside");
		maxOutField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		maxOutField.setColumns(4);
		maxOutField.setText("2");
		Cell9.add(maxOutLabel);
		Cell9.add(maxOutField);
		tab5Center.add(Cell9);

		/***TAB6 Preprocessing***/
		tab6 = new JPanel();
		tab6.setLayout(new GridLayout(1, 3, 8, 8));
		JPanel tab6row1 = new JPanel();
		tab4row1.setLayout(new GridLayout(6, 1, 8, 8));
		JPanel tab6row2 = new JPanel();
		tab4row2.setLayout(new GridLayout(6, 1, 8, 8));
		JPanel tab6row3 = new JPanel();
		tab6row3.setLayout(new GridLayout(6, 1, 8, 8));
		//tab6row1.setBorder(raisedetched);
		//tab6row2.setBorder(raisedetched);
		tab6row3.setBorder(raisedetched);
		tab6.add(tab6row1);
		tab6.add(tab6row2);
		tab6.add(tab6row3);

		final JButton btnShowMeta = new JButton("Show metadata");
		btnShowMeta.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				showMeta();
			}
		}); 
		tab6row1.add(btnShowMeta);

		final JButton btnMakeIso = new JButton("Make iso");
		btnMakeIso.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				ImagePlus imp = makeIso();
				imp.show();
			}
		}); 
		tab6row1.add(btnMakeIso);


		/*TABS*/

		JTabbedPane tabPane = new JTabbedPane();
		tabPane.addTab( "Start", tab1);
		tabPane.addTab( "Branches", tab2);
		tabPane.addTab( "Rings", tab3);
		tabPane.addTab( "I/O", tab4);
		tabPane.addTab( "Advanced Settings", tab5);
		tabPane.addTab( "Preprocessing", tab6);
		getContentPane().add(tabPane);


		/* LOWER PANEL */

		final JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.LEFT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		loadedImageLabel = new JLabel("Loaded image: " + Espacing_Ring.imageName);
		buttonPane.add(loadedImageLabel);

		runningLabel = new JLabel("Running: " + MyGui.ringsRunning.size());
		buttonPane.add(runningLabel,  FlowLayout.LEFT);

		double meanContrast = network.getMeanContrast();
		if(meanContrast== -Double.MAX_VALUE) meanContrastLabel = new JLabel( "Mean: None");
		else meanContrastLabel = new JLabel( "Mean: " + String.format(Locale.US, "%.2f", meanContrast)  );
		buttonPane.add(meanContrastLabel, FlowLayout.LEFT);

		cancelButton = new JButton("StopAll");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				IJ.log("Terminating processing");
				Branch.stopAll(true);	
			}
		}); 
		buttonPane.add(cancelButton, FlowLayout.LEFT);


		showButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				MyGui.updateRingsUsed();
				if(Espacing_Ring.vol == null){
					IJ.log("Saving the image as volume");
					Espacing_Ring.imp = WindowManager.getCurrentImage();
					Espacing_Ring.vol = new MyVolume(Espacing_Ring.imp );
					Espacing_Ring.imageName = Espacing_Ring.imp.getTitle();
					MyGui.updateSaveAsWithCurrentImage();
					Espacing_Ring.pixelWidth = Espacing_Ring.imp.getCalibration().pixelWidth;
					Espacing_Ring.pixelHeight = Espacing_Ring.imp.getCalibration().pixelWidth;
					Espacing_Ring.voxelDepth = Espacing_Ring.imp.getCalibration().pixelDepth;
					MyGui.updateLoadedImage();
					Espacing_Ring.workingVol = new MyVolume(Espacing_Ring.imp );
					Espacing_Ring.imp  = new ImagePlus("Vessel3DTracer", Espacing_Ring.vol.createImageStackFrom3DArray());
					Espacing_Ring.imp.setDisplayMode(IJ.COLOR);
					Espacing_Ring.iC = new ImageCanvas(Espacing_Ring.imp);
					Espacing_Ring.imgS = new StackWindow (Espacing_Ring.imp, Espacing_Ring.iC);
					Espacing_Ring.iC.setVisible(true);
				}
				if(Espacing_Ring.imgS.isVisible() == false) {
					IJ.log("Restoring image");
					Espacing_Ring.generateView(true);
				}

				Espacing_Ring.drawNetworkBranchEndPoints(network);
				Espacing_Ring.showResult(extraBranchList);
				Espacing_Ring.showRings(ringList);	
				Espacing_Ring.iC.repaint();
			}
		}); 
		buttonPane.add(showButton, FlowLayout.LEFT);

		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				branchList.clear();
				extraBranchList.clear();
				ringList.clear();
				network.clear();	
				network.resetContrast();
				updateMeanContrast();
				network.setLastBranchNo(0);

				Espacing_Ring.vol = null;
				Espacing_Ring.workingVol = null;
				Espacing_Ring.imp = null;
				Espacing_Ring.imageName = null;
				//Espacing_Ring.iC = null;
				//Espacing_Ring.imgS = null;
				MyGui.ringsUsed = new ArrayList<Ring>();
				MyGui.updateLoadedImage();
			}
		}); 
		buttonPane.add(resetButton, FlowLayout.LEFT);

	}

	public static void updateRingsUsed() {
		ringsUsed = new ArrayList<Ring>();

		for(Branch b: network) {
			for(Ring r: b) {
				if(!MyGui.ringsUsed.contains(r)) {
					r.setBranches(new ArrayList<Branch>());
					MyGui.ringsUsed.add(r);
				}
			}
		}

		for(Branch b: network) {
			for(Ring r: b) {
				if(!r.getBranches().contains(b)) r.addBranch(b);
			}
		}


		Ring r;
		for(Branch b: network) {

			for(int n : new int[] {0, b.size()-1}) {
				r = b.get(n);
				if(r.getBranches().size()>1) r.isBranchPoint = true;
				else r.isEndPoint = true;

			}

			for(int n = 1; n< b.size()-1; n++) {
				r = b.get(n);
				r.isBranchPoint = false;
				r.isEndPoint = false;
				if(!MyGui.ringsUsed.contains(r)) MyGui.ringsUsed.add(r);
			}
		}
	}

	public static void updateRunning() {
		runningLabel.setText("Running: " + MyGui.ringsRunning.size());
	}

	public static void updateMeanContrast() {
		double meanContrast = network.getMeanContrast();

		if(meanContrast== -Double.MAX_VALUE) {		
			meanContrastLabel.setText( "Mean: None");
		}
		else {
			meanContrastLabel.setText( "Mean: " + String.format(Locale.US, "%.2f", meanContrast) );
		}
	}

	public static void updateLoadedImage(){
		loadedImageLabel.setText("Loaded image: " + Espacing_Ring.imageName);
	}

	public static String getNameWithoutExt() {
		String name = Espacing_Ring.imageName;
		int ind = name.lastIndexOf(".");
		if (ind > -1)  name = name.substring(0, ind);	
		return name;
	}

	public static void updateSaveAsWithCurrentImage() {		
		nameField.setText(getNameWithoutExt());
	}

	public void removeImageListeners(){
		for (MouseListener ml : activatedListeners){
			Espacing_Ring.iC.removeMouseListener(ml);
		}
		activatedListeners = new ArrayList<MouseListener>();
	}

	public void showMeta() {
		IJ.log("Metadata");
		ImagePlus imp = IJ.getImage();
		Double pixelWidth = imp.getCalibration().pixelWidth;
		Double pixelHeight = imp.getCalibration().pixelHeight;
		Double voxelDepth = imp.getCalibration().pixelDepth;
		int sliceNumber = imp.getStackSize();
		IJ.log("width: " + pixelWidth + " heighth: " + pixelHeight + " depth: " + voxelDepth + " nSlice: " + sliceNumber);

		String message = "Image: " + imp.getTitle() + "\npixel width: " + pixelWidth +
				"\npixel height: " + pixelHeight + "\nvoxel depth: " + voxelDepth +
				"\nnumber of slices: " + sliceNumber;
		JOptionPane.showMessageDialog(downPanel, message);
	}

	public ImagePlus makeIso() {
		int interpolationMethod = ImageProcessor.BICUBIC; 
		ImagePlus imp = IJ.getImage();
		Double pixelWidth = imp.getCalibration().pixelWidth;
		Double pixelHeight = imp.getCalibration().pixelHeight;
		Double voxelDepth = imp.getCalibration().pixelDepth;
		int sliceNumber = imp.getStackSize();
		int newSliceNumber = (int) Math.round(voxelDepth/pixelWidth*sliceNumber);

		ImageStack stack1 = imp.getStack();
		int width = stack1.getWidth();
		int height = stack1.getHeight();
		int depth = stack1.getSize();
		int bitDepth = imp.getBitDepth();

		ImagePlus imp2 = IJ.createImage(imp.getTitle(), bitDepth+"-bit", width, height, newSliceNumber);
		if (imp2==null) return null;
		ImageStack stack2 = imp2.getStack();
		ImageProcessor ip = imp.getProcessor();
		ImageProcessor xzPlane1 = ip.createProcessor(width, depth);
		xzPlane1.setInterpolationMethod(interpolationMethod);
		ImageProcessor xzPlane2;        
		Object xzpixels1 = xzPlane1.getPixels();
		for (int y=0; y<height; y++) {
			for (int z=0; z<depth; z++) { // get xz plane at y
				Object pixels1 = stack1.getPixels(z+1);
				System.arraycopy(pixels1, y*width, xzpixels1, z*width, width);
			}
			xzPlane2 = xzPlane1.resize(width, newSliceNumber, true);
			Object xypixels2 = xzPlane2.getPixels();
			for (int z=0; z<newSliceNumber; z++) {
				Object pixels2 = stack2.getPixels(z+1);
				System.arraycopy(xypixels2, z*width, pixels2, y*width, width);
			}
		}
		Calibration cal = imp2.getCalibration();

		cal.setUnit(imp.getCalibration().getUnit()) ;
		cal.pixelWidth = pixelWidth;
		cal.pixelHeight = pixelHeight;
		cal.pixelDepth = ((voxelDepth/pixelWidth*sliceNumber)/newSliceNumber)*pixelWidth;
		return imp2;		
	}
	

	public static void main(final String[] args) {
		try {
			final MyGui dialog = new MyGui();
			//dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
			IJ.log("Starting GUI");
			dialog.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					IJ.log("Terminating processing");
					Branch.stopAll(true);
					System.exit(0);
				}
			});

		}
		catch (final Exception e) {
			e.printStackTrace();
		}
	}

}

