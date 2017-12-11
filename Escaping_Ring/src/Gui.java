import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.text.NumberFormat;
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

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

import ij.IJ;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.StackWindow;

public class Gui extends JDialog {

	static DefaultListModel<Branch> branchList = new DefaultListModel<Branch>();
	DefaultListModel<Branch> extraBranchList = new DefaultListModel<Branch>();
	DefaultListModel<Ring> ringList = new DefaultListModel<Ring>();
	JList<Branch> list;
	static Network network = new Network(branchList);
	double step;
	double impInside;
	double impOutside;
	double threshold;
	double firstLoop, secondLoop, thirdLoop;
	double maxIn, minMem, maxMem, minOut, maxOut;
	double branchFacilitator;
	static JLabel runningLabel;
	static JLabel meanContrastLabel;
	JFormattedTextField firstField = null;
	JFormattedTextField secondField = null;
	JFormattedTextField thirdField = null;
	JFormattedTextField maxInField = null;
	JFormattedTextField minMemField = null;
	JFormattedTextField maxMemField = null;
	JFormattedTextField minOutField = null;
	JFormattedTextField maxOutField = null; 
	Point3D end;

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

	/**
	 * Create the dialog.
	 */
	public Gui() {
		JPanel tab1;
		JPanel tab2;
		JPanel tab3;
		JPanel tab4;
		JPanel tab5;
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
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				Espacing_Ring.start(network, step, impInside, impOutside, threshold, branchFacilitator, firstLoop, secondLoop, thirdLoop,
						maxIn, minMem, maxMem, minOut, maxOut);

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

	    
		/*****TAB2*****/
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
		//listLabel.setLabelFor(listPanel2);
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
				//Espacing_Ring.workingVol.show("working");
			}
		}); 
		buttonListPanel.add(btnDelete);

		final JButton showBranches = new JButton("Show branches");
		showBranches.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				Espacing_Ring.showResult(extraBranchList, step);	
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
								double thisDistance=target.distance(ring.c);
								if(thisDistance<minDistance){
									minDistance = thisDistance;
									closestBranch = branch;
								}
							}
						}

						if(closestBranch.isEmpty()==false){
							if(extraBranchList.contains(closestBranch)==false)
								extraBranchList.addElement(closestBranch);
						}

					}
				};
				//Espacing_Ring.iC.setImageUpdated();
				//Espacing_Ring.iC.setVisible(true);
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


		/*****TAB3*****/	 
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

				//Espacing_Ring.iC = new ImageCanvas(Espacing_Ring.threeChannels);
				//Espacing_Ring.imgS = new StackWindow (Espacing_Ring.threeChannels, iC);
				Espacing_Ring.iC.setVisible(true);
				MouseListener mouseListenerImage = new MouseAdapter() {
					public void mouseClicked(MouseEvent mouseEvent) {
						Point location = Espacing_Ring.iC.getCursorLoc();
						int x = location.x;
						int y = location.y;
						//z to solve, it sets only after moving the slice
						int z = Espacing_Ring.iC.getImage().getSlice();
						Point3D target = new Point3D(x, y, z);

						double minDistance = Double.MAX_VALUE;
						Ring closestRing = null;
						Branch closestBranch = null;
						for(Branch branch : network){
							for(Ring ring : branch){
								double thisDistance=target.distance(ring.c);
								if(thisDistance<minDistance){
									minDistance = thisDistance;
									closestRing = ring;
									closestBranch = branch;
								}
							}
						}

						if(closestRing!=null){
							if(ringList.contains(closestRing)==false)
								closestRing.setBranch(closestBranch);
							ringList.addElement(closestRing);
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
			}
		}); 
		selectRingPanel.add(showRings);

		final JButton btnDeleteRing = new JButton("Delete rings");
		btnDeleteRing.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				for(int i=0; i< ringList.getSize(); i++){
					Ring toRemove = ringList.getElementAt(i);
					Branch motherBranch = toRemove.getBranch();
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
					}	
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
						Branch motherBranch = start.getBranch();
						motherBranch.createBranchBetweenTwoRings(start, end, width);
						
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
							Branch motherBranch = start.getBranch();
							try {
								double width= Double.parseDouble(widthField.getText());
								motherBranch.createBranchBetweenRingAndPoint(start, end, width);
								
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
		

		/*****TAB4*****/	 
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
		
		final JButton btnCSV = new JButton("Generate csv");
		btnCSV.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {

				try {
					//IJ.log("trying to generate csv");
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
				    
				    
					
					//IJ.log("Succes");
				} catch (IOException e) {
					//IJ.log("failed to generate csv");
					e.printStackTrace();
				}		
			}
		}); 
		tab4.add(btnCSV);
		
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
		firstField.setText("20");
		tab5Upper.add(firstLabel);
		tab5Upper.add(firstField);
		
		JLabel secondLabel = new JLabel("Keep after second loop");
		secondField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		secondField.setColumns(5);
		secondField.setText("15");
		tab5Upper.add(secondLabel);
		tab5Upper.add(secondField);
		
		JLabel thirdLabel = new JLabel("Keep after third loop");
		thirdField = new JFormattedTextField(NumberFormat.getNumberInstance(Locale.US));
		thirdField.setColumns(5);
		thirdField.setText("20");
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
		
		runningLabel = new JLabel("Running: " + Branch.running);
		buttonPane.add(runningLabel);
		
		meanContrastLabel = new JLabel("Mean: " + network.getMeanContrast());
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

		final JButton showButton = new JButton("Show");
		showButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {

				//Espacing_Ring.showResult(network, step);	
				Espacing_Ring.drawNetwork(network);
				if(Espacing_Ring.imgS.isVisible() == false) {
					//doesnt work
					IJ.log("trying to restore image");
					Espacing_Ring.generateView(true);
					Espacing_Ring.drawNetwork(network);
				}
				Espacing_Ring.iC.repaint();
			}
		}); 
		buttonPane.add(showButton);

		final JButton btn2= new JButton("Reset");
		btn2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				branchList.clear();
				extraBranchList.clear();
				ringList.clear();
				network.clear();	
				network.resetContrast();
				Espacing_Ring.vol = null;
				Espacing_Ring.workingVol = null;
			}
		}); 
		buttonPane.add(btn2);

	}
	
	public static void updateRunning() {
		runningLabel.setText("Running: " + Branch.running);
	}

	public static void updateMeanContrast() {
		meanContrastLabel.setText("Mean: " + network.getMeanContrast());
	}
}

