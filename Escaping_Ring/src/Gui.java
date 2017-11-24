import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.NumberFormat;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
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

	DefaultListModel<Branch> branchList = new DefaultListModel<Branch>();
	DefaultListModel<Branch> extraBranchList = new DefaultListModel<Branch>();
	DefaultListModel<Ring> ringList = new DefaultListModel<Ring>();
	JList<Branch> list;
	Network network = new Network(branchList);
	double step;
	double impInside;
	double impOutside;

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
		setBounds(100, 100, 650, 300);
		setTitle("VascRing3D");

		/*TAB1*/	 
		tab1 = new JPanel();
		tab1.setLayout(new BorderLayout());
		
		JPanel leftPanel = new JPanel();
		tab1.add(leftPanel, BorderLayout.NORTH);
		leftPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormSpecs.MIN_ROWSPEC,
				FormSpecs.MIN_ROWSPEC,
				FormSpecs.MIN_ROWSPEC,
				FormSpecs.MIN_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,}));

		JLabel stepLabel = new JLabel("Step size");
		JFormattedTextField stepField = new JFormattedTextField(NumberFormat.getNumberInstance());
		stepField.setColumns(10);
		stepField.setText("10");
		stepLabel.setLabelFor(stepField);
		leftPanel.add(stepLabel, "1, 1, left, center");
		leftPanel.add(stepField, "2, 1, left, center");

		
		JLabel impInsideLabel = new JLabel("Importance inside");
		JFormattedTextField impInsideField = new JFormattedTextField(NumberFormat.getNumberInstance());
		impInsideField.setColumns(10);
		impInsideField.setText("-0.25");
		impInsideLabel.setLabelFor(impInsideField);
		leftPanel.add(impInsideLabel, "1, 2, left, center");
		leftPanel.add(impInsideField, "2, 2, left, center");
		
		JLabel impOutsideLabel = new JLabel("Importance outside");
		JFormattedTextField impOutsideField = new JFormattedTextField(NumberFormat.getNumberInstance());
		impOutsideField.setColumns(10);
		impOutsideField.setText("-0.25");
		impOutsideLabel.setLabelFor(impOutsideField);
		leftPanel.add(impOutsideLabel, "1, 3, left, center");
		leftPanel.add(impOutsideField, "2, 3, left, center");


		final JButton btn1 = new JButton("Start");
		btn1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				try {
					step= Double.parseDouble(stepField.getText());
					impInside = Double.parseDouble(impInsideField.getText());
					impOutside = Double.parseDouble(impOutsideField.getText());
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				Espacing_Ring.start(network, step, impInside, impOutside);

			}
		}); 
		leftPanel.add(btn1, "2, 4, left, center");



		/*TAB2*/
		tab2 = new JPanel();
		tab2.setLayout(new BorderLayout());
		JPanel tab2Left = new JPanel(); 
		JPanel tab2Right = new JPanel();
		tab2Left.setLayout(new BorderLayout());
		tab2Right.setLayout(new BorderLayout());
		tab2.add(tab2Left,BorderLayout.WEST);
		tab2.add(tab2Right,BorderLayout.EAST);

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
				}
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

				final ImageCanvas iC = new ImageCanvas(WindowManager.getCurrentImage());
				final StackWindow imgS = new StackWindow (WindowManager.getCurrentImage(), iC);
				iC.setVisible(true);
				MouseListener mouseListenerImage = new MouseAdapter() {
					public void mouseClicked(MouseEvent mouseEvent) {

						Point location = iC.getCursorLoc();
						int x = location.x;
						int y = location.y;
						//z to solve, it sets only after moving the slice
						int z = iC.getImage().getSlice();
						Point3D target = new Point3D(x, y, z);

						double minDistance = Double.MAX_VALUE;
						Ring closestRing;
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

						if(closestBranch.isEmpty()==false){
							if(extraBranchList.contains(closestBranch)==false)
								extraBranchList.addElement(closestBranch);
						}

					}
				};
				iC.addMouseListener(mouseListenerImage);
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

		/*TAB3*/	 
		tab3 = new JPanel();
		tab3.setLayout(new BorderLayout());

		JList<Ring>listRing = new JList<Ring>(ringList);
		JPanel ringPanel = new JPanel();
		ringPanel.add(listRing, BorderLayout.CENTER);
		JScrollPane scrolRing = new JScrollPane(listRing);
		tab3.add(scrolRing,BorderLayout.WEST);
		tab3.add(ringPanel);
		JPanel actionPanel = new JPanel();
		actionPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		tab3.add(actionPanel, BorderLayout.EAST);

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

				final ImageCanvas iC = new ImageCanvas(WindowManager.getCurrentImage());
				final StackWindow imgS = new StackWindow (WindowManager.getCurrentImage(), iC);
				iC.setVisible(true);
				MouseListener mouseListenerImage = new MouseAdapter() {
					public void mouseClicked(MouseEvent mouseEvent) {
						Point location = iC.getCursorLoc();
						int x = location.x;
						int y = location.y;
						//z to solve, it sets only after moving the slice
						int z = iC.getImage().getSlice();
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
				iC.addMouseListener(mouseListenerImage);
			}
		}); 
		selectRingPanel.add(clickRings);


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
		actionPanel.add(btnDeleteRing);

		final JButton btnJoinRings = new JButton("Join two rings");
		btnJoinRings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				if(ringList.getSize()==2){
					Ring start = ringList.getElementAt(0);
					Ring end = ringList.getElementAt(1);
					Branch motherBranch = start.getBranch();
					motherBranch.createBranchBetweenTwoRings(start, end);
				}
				else{
					IJ.log("something went wrong " + ringList.getElementAt(0) + ringList.getElementAt(1));
				}
			}
		}); 
		actionPanel.add(btnJoinRings);

		/*TABS*/

		JTabbedPane tabPane = new JTabbedPane();
		tabPane.addTab( "Start", tab1);
		tabPane.addTab( "Branches", tab2);
		tabPane.addTab( "Rings", tab3);
		getContentPane().add(tabPane);


		/* LOWER PANEL */

		final JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		final JButton okButton = new JButton("OK");
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);

		final JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);

		final JButton showButton = new JButton("Show");
		showButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				Espacing_Ring.showResult(network, step);	
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
			}
		}); 
		buttonPane.add(btn2);

	}

}

