import java.awt.BorderLayout;
import java.awt.Dimension;
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
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.StackWindow;


public class Gui extends JDialog {

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
	double maxIn, minMem, maxMem, minOut, maxOut;
	double branchFacilitator, checkWorstRings; 

	static JLabel runningLabel;
	static JLabel meanContrastLabel;
	static JLabel loadedImageLabel;
	static JButton cancelButton = null;

	JFormattedTextField sepField = null;
	JFormattedTextField firstField = null;
	JFormattedTextField secondField = null;
	JFormattedTextField thirdField = null;
	JFormattedTextField maxInField = null;
	JFormattedTextField minMemField = null;
	JFormattedTextField maxMemField = null;
	JFormattedTextField minOutField = null;
	JFormattedTextField maxOutField = null; 
	JFormattedTextField checkWorstRingsField = null;
	Point3D end;

	static ArrayList<Parameters> usedParameters = new ArrayList<Parameters>();
	static ArrayList<Parameters> toUseParameters = new ArrayList<Parameters>();

	static ArrayList<Ring> ringsRunning = new ArrayList<Ring>();
	static boolean stopAll;
	static boolean roiRunning = false; //not needed for now

	String nameToSave = "name";
	static boolean synch = false;

	ArrayList<MouseListener> activatedListeners = new ArrayList<MouseListener>();


	public static void main(final String[] args) {
		try {
			final Gui dialog = new Gui();
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


	public Gui() {
		JPanel tab1;
		JPanel tab2;
		JPanel tab3;
		JPanel tab4;
		JPanel tab5;
		final JButton showButton = new JButton("Show");
		final JButton resetButton = new JButton("Reset");
		setBounds(100, 100, 750, 300);
		setTitle("VascRing3D");



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
		branchField.setText("0.6");
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
					minMem = Double.parseDouble(minMemField.getText());
					maxMem = Double.parseDouble(maxMemField.getText());
					minOut = Double.parseDouble(minOutField.getText());
					maxOut = Double.parseDouble(maxOutField.getText());
					checkWorstRings = Double.parseDouble(checkWorstRingsField.getText());
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				Espacing_Ring.start(network, step, impInside, impOutside, threshold, branchFacilitator, firstLoop, secondLoop, thirdLoop,
						maxIn, minMem, maxMem, minOut, maxOut, checkWorstRings);

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
					minMem = Double.parseDouble(minMemField.getText());
					maxMem = Double.parseDouble(maxMemField.getText());
					minOut = Double.parseDouble(minOutField.getText());
					maxOut = Double.parseDouble(maxOutField.getText());
					checkWorstRings = Double.parseDouble(checkWorstRingsField.getText());
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				Ring r = Espacing_Ring.trySeedRing(network, step, impInside, impOutside, threshold, branchFacilitator, firstLoop, 
						secondLoop, thirdLoop, maxIn, minMem, maxMem, minOut, maxOut, checkWorstRings);
				String message = "Initial ring radius: " + String.format(Locale.US, "%.2f", r.getRadius()) + "\ncontrast: " + String.format(Locale.US, "%.2f",r.getContrast());
				JOptionPane.showMessageDialog(downPanel, message);
				

			}
		}); 
		leftPanel.add(btnTrySeed);

		/*CHANGE PARAMETERS BETWEEN FILLED AND EMPTY VESSELS*/

		JRadioButton emptyButton = new JRadioButton("empty tube");
		emptyButton.setSelected(true);
		emptyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				impInsideField.setText("-0.5");
				impOutsideField.setText("-0.5");
			}
		}); 
		downPanel.add(emptyButton);

		JRadioButton filledButton = new JRadioButton("filled tube");
		filledButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				impInsideField.setText("1");
				impOutsideField.setText("-2");
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
				for(Branch b: toRemove){
					network.remove(b);
					b.restoreBranch();

				}
				Espacing_Ring.updateImgWithVol(Espacing_Ring.iC.getImage());
				showButton.doClick();
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
				for(Branch b: toRemove){
					extraBranchList.removeElement(b);
				}
				showButton.doClick();
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
								showButton.doClick();
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
							showButton.doClick();
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
						showButton.doClick();
					}
				}
			}
		};
		list2.addMouseListener(mouseListener2);


		/*FILTER BRANCHES*/
		JLabel filterLabel = new JLabel("Filter size");
		JFormattedTextField filterField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		filterField.setColumns(5);
		filterField.setText("0");
		tab2Down.add(filterLabel);
		tab2Down.add(filterField);

		final JButton filterButton = new JButton("Filter");
		filterButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				try {
					int filterSize= Integer.parseInt(filterField.getText());
					for(int i=0; i< branchList.getSize(); i++){
						Branch b = branchList.getElementAt(i);
						if(b.size()<=filterSize) {
							extraBranchList.addElement(b);
						}
					}
					showButton.doClick();
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}

			}
		}); 
		tab2Down.add(filterButton);

		final JButton btnOrderNetwork = new JButton("Order network");
		btnOrderNetwork.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				network.orderBranchPoints();
				showButton.doClick();

			}}); 
		tab2Down.add(btnOrderNetwork);


		/*****TAB3 Rings*****/	 
		tab3 = new JPanel();
		tab3.setLayout(new GridLayout(1, 2, 5, 5));
		JPanel tab3Left = new JPanel();
		JPanel tab3Right = new JPanel();
		tab3Right.setLayout(new BorderLayout());
		tab3.add(tab3Left);
		tab3.add(tab3Right);

		JPanel actionPanel = new JPanel();
		actionPanel.setLayout(new BorderLayout());
		tab3Left.add(actionPanel);

		JPanel firstRow = new JPanel();
		firstRow.setLayout(new FlowLayout(FlowLayout.LEFT));
		JPanel secondRow = new JPanel();
		secondRow.setLayout(new FlowLayout(FlowLayout.LEFT));
		actionPanel.add(firstRow, BorderLayout.NORTH);
		actionPanel.add(secondRow, BorderLayout.CENTER);

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
								showButton.doClick();	
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
						}
						else{
							//the ring is last of first of the branch
							motherBranch.remove(toRemove);
						}}	
					ringList.removeElement(toRemove);
					updateRingsUsed();
				}
				Espacing_Ring.updateImgWithVol(Espacing_Ring.iC.getImage());
				showButton.doClick();
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
				for(Ring toRemove: ringsToRemove){	
					ringList.removeElement(toRemove);
				}
				showButton.doClick();
			}
		}); 
		selectRingPanel.add(btnCleanRing);


		JLabel widthLabel = new JLabel("Width of new branch");
		JFormattedTextField widthField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		widthField.setColumns(5);
		widthField.setText("0");
		firstRow.add(widthLabel);
		firstRow.add(widthField);

		final JButton btnJoinRings = new JButton("Join two rings");
		btnJoinRings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				if(ringList.getSize()==2){
					try {
						double width= Double.parseDouble(widthField.getText());
						Ring start = ringList.getElementAt(0);
						Ring end = ringList.getElementAt(1);
						Branch.createBranchBetweenTwoRings(start, end, width);
						showButton.doClick();

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
								Branch.createBranchBetweenRingAndPoint(start, end, width);
								Espacing_Ring.iC.removeMouseListener(this);
								showButton.doClick();

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

		final JButton btnSkeleton = new JButton("Generate skeleton");
		btnSkeleton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				Volume skeleton = new Volume(Espacing_Ring.vol.nx, Espacing_Ring.vol.ny, Espacing_Ring.vol.nz);
				network.generateSkeleton(skeleton);
				skeleton.show("Skeleton");

			}
		}); 
		tab4row2.add(btnSkeleton);

		final JButton btnBinary = new JButton("Generate mask");
		btnBinary.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				Volume binary = new Volume(Espacing_Ring.vol.nx, Espacing_Ring.vol.ny, Espacing_Ring.vol.nz);
				network.createMask(binary, 0.5);
				//binary.show("Binary");

			}
		}); 
		tab4row2.add(btnBinary);


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
						System.out.println("getCurrentDirectory(): " 
								+  chooser.getCurrentDirectory());
						System.out.println("getSelectedFile() : " 
								+  chooser.getSelectedFile());
						network.exportData(chooser.getSelectedFile().getPath()+ File.separator );
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
						String objectName = chooser.getSelectedFile().getPath() + "/" + nameToSave + ".ser";
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
				chooser.setDialogTitle("Choose .xml file");
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
						Parameters.exportParams(chooser.getSelectedFile().getPath()+"/VascRing3_Params.csv");
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
					Gui.toUseParameters = Parameters.importParams(objectName);
					for (Parameters p : Gui.toUseParameters){
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
				Gui.synch = true;
				for(Parameters param: Gui.toUseParameters){
					Espacing_Ring.start(network, param);
					IJ.log("Left: " + Gui.ringsRunning.size());

				}
				Gui.synch = false;
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
				String openImage = WindowManager.getCurrentImage().getTitle();

				if (chooser.showOpenDialog(tab4) == JFileChooser.APPROVE_OPTION){
					Gui.synch = true;
					CSVUtils.setDEFAULT_SEPARATOR(sepField.getText().charAt(0));
					for(Parameters param: Gui.toUseParameters){
						Espacing_Ring.start(network, param);

						nameToSave = openImage != null ? openImage+"_"+n : ""+n ;
						String objectName = chooser.getSelectedFile().getPath() + "/" + nameToSave + ".ser";
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
					Gui.synch = false;
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
		JLabel firstLabel = new JLabel("Keep after first loop");
		firstField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		firstField.setColumns(3);
		firstField.setText("100");
		Cell1.add(firstLabel);
		Cell1.add(firstField);
		tab5Upper.add(Cell1);

		JPanel Cell2 = new JPanel();
		Cell2.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel secondLabel = new JLabel("Keep after second loop");
		secondField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		secondField.setColumns(3);
		secondField.setText("5");
		Cell2.add(secondLabel);
		Cell2.add(secondField);
		tab5Upper.add(Cell2);

		JPanel Cell3 = new JPanel();
		Cell3.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel thirdLabel = new JLabel("Keep after third loop");
		thirdField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		thirdField.setColumns(3);
		thirdField.setText("10");
		Cell3.add(thirdLabel);
		Cell3.add(thirdField);
		tab5Upper.add(Cell3);

		JPanel Cell4 = new JPanel();
		Cell4.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel checkWorstRingsLabel = new JLabel("check only X worst rings");
		checkWorstRingsField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		checkWorstRingsField.setColumns(4);
		checkWorstRingsField.setText("0.5");
		Cell4.add(checkWorstRingsLabel);
		Cell4.add(checkWorstRingsField);
		tab5Upper.add(Cell4);

		JPanel Cell5 = new JPanel();
		Cell5.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel maxInLabel = new JLabel("Max inside");
		maxInField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		maxInField.setColumns(4);
		maxInField.setText("0.8");
		Cell5.add(maxInLabel);
		Cell5.add(maxInField);
		tab5Center.add(Cell5);

		JPanel Cell6 = new JPanel();
		Cell6.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel minMemLabel = new JLabel("Min membrane");
		minMemField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		minMemField.setColumns(4);
		minMemField.setText("0.8");
		Cell6.add(minMemLabel);
		Cell6.add(minMemField);
		tab5Center.add(Cell6);

		JPanel Cell7 = new JPanel();
		Cell7.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel maxMemLabel = new JLabel("Max membrane");
		maxMemField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		maxMemField.setColumns(4);
		maxMemField.setText("1.2");
		Cell7.add(maxMemLabel);
		Cell7.add(maxMemField);
		tab5Center.add(Cell7);


		JPanel Cell8 = new JPanel();
		Cell8.setLayout(new FlowLayout(FlowLayout.LEFT));
		JLabel minOutLabel = new JLabel("Min outside");
		minOutField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		minOutField.setColumns(4);
		minOutField.setText("1.2");
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


		/*TABS*/

		JTabbedPane tabPane = new JTabbedPane();
		tabPane.addTab( "Start", tab1);
		tabPane.addTab( "Branches", tab2);
		tabPane.addTab( "Rings", tab3);
		tabPane.addTab( "Export", tab4);
		tabPane.addTab( "Advanced Settings", tab5);
		getContentPane().add(tabPane);


		/* LOWER PANEL */

		final JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.LEFT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		loadedImageLabel = new JLabel("Loaded image: " + Espacing_Ring.imageName);
		buttonPane.add(loadedImageLabel);

		runningLabel = new JLabel("Running: " + Gui.ringsRunning.size());
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
				Gui.updateRingsUsed();
				if(Espacing_Ring.vol == null){
					IJ.log("Saving the image as volume");
					Espacing_Ring.imp = WindowManager.getCurrentImage();
					Espacing_Ring.vol = new Volume(Espacing_Ring.imp );
					Espacing_Ring.imageName = Espacing_Ring.imp.getTitle();
					Gui.updateLoadedImage();
					Espacing_Ring.workingVol = new Volume(Espacing_Ring.imp );
					Espacing_Ring.imp  = new ImagePlus("VascRing3D", Espacing_Ring.vol.createImageStackFrom3DArray());
					Espacing_Ring.imp.setDisplayMode(IJ.COLOR);
					Espacing_Ring.iC = new ImageCanvas(Espacing_Ring.imp);
					Espacing_Ring.imgS = new StackWindow (Espacing_Ring.imp, Espacing_Ring.iC);
					Espacing_Ring.iC.setVisible(true);
				}
				if(Espacing_Ring.imgS.isVisible() == false) {
					IJ.log("Restoring image");
					Espacing_Ring.generateView(true);
				}
				//Espacing_Ring.drawNetwork(network);
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
				Gui.ringsUsed = new ArrayList<Ring>();
				Gui.updateLoadedImage();
			}
		}); 
		buttonPane.add(resetButton, FlowLayout.LEFT);

	}

	public static void updateRingsUsed() {



		ringsUsed = new ArrayList<Ring>();

		for(Branch b: network) {
			for(Ring r: b) {
				if(!Gui.ringsUsed.contains(r)) {
					r.setBranches(new ArrayList<Branch>());
					Gui.ringsUsed.add(r);
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
				if(!Gui.ringsUsed.contains(r)) Gui.ringsUsed.add(r);
			}
		}
	}

	public static void updateRunning() {
		runningLabel.setText("Running: " + Gui.ringsRunning.size());
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

	public void removeImageListeners(){
		for (MouseListener ml : activatedListeners){
			Espacing_Ring.iC.removeMouseListener(ml);
		}
		activatedListeners = new ArrayList<MouseListener>();
	}




}

