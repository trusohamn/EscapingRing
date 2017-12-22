import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

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


	public static void main(final String[] args) {
		try {
			final Gui dialog = new Gui();
			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
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


		/*****TAB1*****/	 
		tab1 = new JPanel();
		tab1.setLayout(new BorderLayout());

		JPanel leftPanel = new JPanel();
		tab1.add(leftPanel, BorderLayout.WEST);

		JPanel downPanel = new JPanel();
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
		impInsideField.setText("-0.25");
		impInsideLabel.setLabelFor(impInsideField);
		downPanel.add(impInsideLabel);
		downPanel.add(impInsideField);

		JLabel impOutsideLabel = new JLabel("outside");
		JFormattedTextField impOutsideField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		impOutsideField.setColumns(5);
		impOutsideField.setText("-0.25");
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

		/*CHANGE PARAMETERS BETWEEN FILLED AND EMPTY VESSELS*/

		JRadioButton emptyButton = new JRadioButton("empty tube");
		emptyButton.setSelected(true);
		emptyButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				impInsideField.setText("-0.25");
				impOutsideField.setText("-0.25");
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

		JRadioButton customButton = new JRadioButton("custom settings");
		filledButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
			}
		}); 
		downPanel.add(customButton);

		ButtonGroup group = new ButtonGroup();
		group.add(emptyButton);
		group.add(filledButton);
		group.add(customButton);


		/*****TAB2 Branches*****/
		tab2 = new JPanel();
		tab2.setLayout(new BorderLayout());
		JPanel tab2Left = new JPanel(); 
		JPanel tab2Right = new JPanel();
		JPanel tab2Down= new JPanel();
		tab2Left.setLayout(new BorderLayout());
		tab2Right.setLayout(new BorderLayout());
		tab2Down.setLayout(new FlowLayout(FlowLayout.LEFT));
		tab2.add(tab2Left,BorderLayout.WEST);
		tab2.add(tab2Right,BorderLayout.EAST);
		tab2.add(tab2Down,BorderLayout.SOUTH);

		list = new JList<Branch>(branchList);
		JPanel listPanel = new JPanel();
		listPanel.add(list, BorderLayout.CENTER);
		JScrollPane scrol = new JScrollPane(list);
		tab2Left.add(scrol,BorderLayout.WEST);
		tab2Left.add(listPanel);

		JList<Branch> list2 = new JList<Branch>(extraBranchList);
		JPanel listPanel2 = new JPanel();
		listPanel2.add(list2, BorderLayout.CENTER);
		JScrollPane scrol2 = new JScrollPane(list2);
		tab2Right.add(scrol2,BorderLayout.EAST);
		tab2Right.add(listPanel2);

		JLabel listLabel = new JLabel("Branches to delete");
		JPanel listLabelPanel = new JPanel();
		listLabelPanel.add(listLabel);
		tab2Right.add(listLabelPanel, BorderLayout.NORTH);

		JPanel buttonListPanel = new JPanel();
		buttonListPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		tab2Right.add(buttonListPanel, BorderLayout.AFTER_LAST_LINE);

		final JButton btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				for(int i=0; i< extraBranchList.getSize(); i++){
					Branch toRemove = extraBranchList.getElementAt(i);
					branchList.removeElement(toRemove);
					network.remove(toRemove);
					extraBranchList.removeElement(toRemove);
					toRemove.restoreBranch();
				}
			}
		}); 
		buttonListPanel.add(btnDelete);

		final JButton showBranches = new JButton("Show branches");
		showBranches.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				Espacing_Ring.showResult(extraBranchList);
				Espacing_Ring.iC.repaint();
			}
		}); 
		buttonListPanel.add(showBranches);

		final JButton clickBranches = new JButton("Click branches");
		clickBranches.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				MouseListener mouseListenerImage = new MouseAdapter() {
					public void mouseClicked(MouseEvent mouseEvent) {
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
								Espacing_Ring.showResult(extraBranchList);
								Espacing_Ring.iC.repaint();
							}
						}

					}
				};

				Espacing_Ring.iC.addMouseListener(mouseListenerImage);
			}
		}); 
		buttonListPanel.add(clickBranches); 

		MouseListener mouseListener = new MouseAdapter() {
			//adds branch to the extra list
			public void mouseClicked(MouseEvent mouseEvent) {
				JList<Branch> theList = (JList<Branch>) mouseEvent.getSource();
				if (mouseEvent.getClickCount() == 2) {
					int index = theList.locationToIndex(mouseEvent.getPoint());
					if (index >= 0) {
						Branch o = theList.getModel().getElementAt(index);
						if(extraBranchList.contains(o)==false) extraBranchList.addElement(o);
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
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}

			}
		}); 
		tab2Down.add(filterButton);


		/*****TAB3 Rings*****/	 
		tab3 = new JPanel();
		tab3.setLayout(new BorderLayout());

		JList<Ring>listRing = new JList<Ring>(ringList);
		JPanel ringPanel = new JPanel();
		ringPanel.add(listRing, BorderLayout.CENTER);
		JScrollPane scrolRing = new JScrollPane(listRing);
		tab3.add(scrolRing,BorderLayout.WEST);
		tab3.add(ringPanel);
		JPanel actionPanel = new JPanel();
		actionPanel.setLayout(new BorderLayout());
		tab3.add(actionPanel, BorderLayout.EAST);
		JPanel firstRow = new JPanel();
		firstRow.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JPanel secondRow = new JPanel();
		secondRow.setLayout(new FlowLayout(FlowLayout.RIGHT));
		actionPanel.add(firstRow, BorderLayout.NORTH);
		actionPanel.add(secondRow, BorderLayout.CENTER);

		MouseListener mouseListener3 = new MouseAdapter() {
			//removes ring from the list
			public void mouseClicked(MouseEvent mouseEvent) {
				JList<Ring> theList = (JList<Ring>) mouseEvent.getSource();
				if (mouseEvent.getClickCount() == 2) {
					int index = theList.locationToIndex(mouseEvent.getPoint());
					if (index >= 0) {
						Ring o = theList.getModel().getElementAt(index);
						ringList.removeElement(o);
					}
				}
			}
		};
		listRing.addMouseListener(mouseListener3);

		JPanel selectRingPanel = new JPanel();
		selectRingPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		tab3.add(selectRingPanel, BorderLayout.SOUTH);

		final JButton clickRings = new JButton("Select rings");
		clickRings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {

				Espacing_Ring.iC.setVisible(true);
				MouseListener mouseListenerImage = new MouseAdapter() {
					public void mouseClicked(MouseEvent mouseEvent) {
						Point location = Espacing_Ring.iC.getCursorLoc();
						int x = location.x;
						int y = location.y;
						int z = Espacing_Ring.iC.getImage().getSlice();
						Point3D target = new Point3D(x, y, z);

						double minDistance = Double.MAX_VALUE;
						Ring closestRing = null;
						//Branch closestBranch = null;
						for(Branch branch : network){
							for(Ring ring : branch){
								double thisDistance=target.distance(ring.getC());
								if(thisDistance<minDistance){
									minDistance = thisDistance;
									closestRing = ring;
									//closestBranch = branch;
								}
							}
						}

						if(closestRing!=null){
							if(ringList.contains(closestRing)==false){
								//closestRing.setBranch(closestBranch);
								ringList.addElement(closestRing);
								Espacing_Ring.showRings(ringList);	
								Espacing_Ring.iC.repaint();
							}
						}
					}
				};
				Espacing_Ring.iC.addMouseListener(mouseListenerImage);
			}
		}); 
		selectRingPanel.add(clickRings);

		final JButton showRings = new JButton("Show rings");
		showRings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {	
				Espacing_Ring.showRings(ringList);	
				Espacing_Ring.iC.repaint();
			}
		}); 
		selectRingPanel.add(showRings);

		final JButton btnDeleteRing = new JButton("Delete rings");
		btnDeleteRing.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				IJ.log(""+ringList.getSize());
				ArrayList<Ring> ringsToRemove = new ArrayList<Ring>();
				for(int i=0; i< ringList.getSize(); i++){
					ringsToRemove.add(ringList.getElementAt(i));
				}
				for(Ring toRemove: ringsToRemove){	
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
				}
			}
		}); 
		firstRow.add(btnDeleteRing);

		JLabel widthLabel = new JLabel("Width of new branch");
		JFormattedTextField widthField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		widthField.setColumns(5);
		widthField.setText("0");
		secondRow.add(widthLabel);
		secondRow.add(widthField);

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

					MouseListener mouseListenerPoint = new MouseAdapter() {
						public void mouseClicked(MouseEvent mouseEvent) {
							Point location = Espacing_Ring.iC.getCursorLoc();
							int x = location.x;
							int y = location.y;
							int z = Espacing_Ring.iC.getImage().getSlice();
							end = new Point3D(x, y, z);
							try {
								double width= Double.parseDouble(widthField.getText());
								Branch.createBranchBetweenRingAndPoint(start, end, width);

							} catch (NumberFormatException e) {
								e.printStackTrace();
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
		tab4 = new JPanel();
		tab4.setLayout(new FlowLayout(FlowLayout.LEFT));

		final JButton btnSkeleton = new JButton("Generate skeleton");
		btnSkeleton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				Volume skeleton = new Volume(Espacing_Ring.vol.nx, Espacing_Ring.vol.ny, Espacing_Ring.vol.nz);
				network.generateSkeleton(skeleton);
				skeleton.show("Skeleton");

			}
		}); 
		tab4.add(btnSkeleton);

		final JButton btnBinary = new JButton("Generate binary");
		btnBinary.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				Volume binary = new Volume(Espacing_Ring.vol.nx, Espacing_Ring.vol.ny, Espacing_Ring.vol.nz);
				network.generateBinary(binary);
				binary.show("Binary");

			}
		}); 
		tab4.add(btnBinary);

		final JButton btnCSV = new JButton("Generate csv");
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
						System.out.println("getCurrentDirectory(): " 
								+  chooser.getCurrentDirectory());
						System.out.println("getSelectedFile() : " 
								+  chooser.getSelectedFile());
						network.exportData(chooser.getSelectedFile().getPath()+"/VascRing3_Output.csv");
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
		tab4.add(btnCSV);

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
		tab4.add(btnExportXML);


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
		tab4.add(btnImportXML );

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

		tab4.add(btnExportParams);

		final JButton btnImportParams = new JButton("Import parameters");
		btnImportParams .addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				JFileChooser chooser = new JFileChooser(); 
				chooser.setCurrentDirectory(new java.io.File("."));
				chooser.setDialogTitle("Choose .csv file with parameters");
				if (chooser.showOpenDialog(tab4) == JFileChooser.APPROVE_OPTION) { 
					String objectName = chooser.getSelectedFile().getPath();
					Gui.toUseParameters = Parameters.importParams(objectName);
					for (Parameters p : Gui.toUseParameters){
						IJ.log(p.toString());
					}
				}
			}
		}); 
		tab4.add(btnImportParams);

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
		tab4.add(btnStartParams);

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
		tab4.add(btnStartParamsSave);

		final JButton btnOrderNetwork = new JButton("Order network");
		btnOrderNetwork.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
					network.orderBranchPoints();
					
			}}); 
		tab4.add(btnOrderNetwork);
		
		/***TAB5 Advanced Settings *****/
		tab5 = new JPanel();
		tab5.setLayout(new BorderLayout());
		JPanel tab5Upper = new JPanel();
		tab5.add(tab5Upper, BorderLayout.NORTH);
		tab5Upper.setLayout(new FlowLayout(FlowLayout.LEFT));
		JPanel tab5Center = new JPanel();
		tab5.add(tab5Center, BorderLayout.CENTER);
		tab5Center.setLayout(new FlowLayout(FlowLayout.LEFT));

		JLabel firstLabel = new JLabel("Keep after first loop");
		firstField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		firstField.setColumns(5);
		firstField.setText("100");
		tab5Upper.add(firstLabel);
		tab5Upper.add(firstField);

		JLabel secondLabel = new JLabel("Keep after second loop");
		secondField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		secondField.setColumns(5);
		secondField.setText("5");
		tab5Upper.add(secondLabel);
		tab5Upper.add(secondField);

		JLabel thirdLabel = new JLabel("Keep after third loop");
		thirdField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		thirdField.setColumns(5);
		thirdField.setText("10");
		tab5Upper.add(thirdLabel);
		tab5Upper.add(thirdField);

		JLabel maxInLabel = new JLabel("Max inside");
		maxInField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		maxInField.setColumns(4);
		maxInField.setText("0.8");
		tab5Center.add(maxInLabel);
		tab5Center.add(maxInField);

		JLabel minMemLabel = new JLabel("Min membrane");
		minMemField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		minMemField.setColumns(4);
		minMemField.setText("0.8");
		tab5Center.add(minMemLabel);
		tab5Center.add(minMemField);

		JLabel maxMemLabel = new JLabel("Max membrane");
		maxMemField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		maxMemField.setColumns(4);
		maxMemField.setText("1.2");
		tab5Center.add(maxMemLabel);
		tab5Center.add(maxMemField);

		JLabel minOutLabel = new JLabel("Min outside");
		minOutField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		minOutField.setColumns(4);
		minOutField.setText("1.2");
		tab5Center.add(minOutLabel);
		tab5Center.add(minOutField);

		JLabel maxOutLabel = new JLabel("Max outside");
		maxOutField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		maxOutField.setColumns(4);
		maxOutField.setText("2");
		tab5Center.add(maxOutLabel);
		tab5Center.add(maxOutField);
		
		JLabel checkWorstRingsLabel = new JLabel("check only X worst rings");
		checkWorstRingsField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		checkWorstRingsField.setColumns(4);
		checkWorstRingsField.setText("0.5");
		tab5Center.add(checkWorstRingsLabel);
		tab5Center.add(checkWorstRingsField);

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
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		loadedImageLabel = new JLabel("Loaded image: " + Espacing_Ring.imageName);
		buttonPane.add(loadedImageLabel);

		runningLabel = new JLabel("Running: " + Gui.ringsRunning.size());
		buttonPane.add(runningLabel);

		double meanContrast = network.getMeanContrast();
		if(meanContrast== -Double.MAX_VALUE) meanContrastLabel = new JLabel( "Mean: None");
		else meanContrastLabel = new JLabel( "Mean: " + String.format(Locale.US, "%.2f", meanContrast)  );
		buttonPane.add(meanContrastLabel);

		final JButton okButton = new JButton("OK");
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);

		final JButton cancelButton = new JButton("StopAll");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				Branch.stopAll(true);	
			}
		}); 
		buttonPane.add(cancelButton);


		showButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {

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
				Espacing_Ring.drawNetwork(network);
				Espacing_Ring.iC.repaint();
			}
		}); 
		buttonPane.add(showButton);

		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				branchList.clear();
				extraBranchList.clear();
				ringList.clear();
				network.clear();	
				network.resetContrast();
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
		buttonPane.add(resetButton);

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


}

