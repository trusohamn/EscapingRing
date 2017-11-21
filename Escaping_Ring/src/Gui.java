import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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


public class Gui extends JDialog {

	DefaultListModel<String> branchList = new DefaultListModel<String>();
	JList<String> list;
	Network network = new Network(branchList);
	double step;

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
		
		
		
		setBounds(100, 100, 450, 300);
		setTitle("VascRing3D");

		/*TAB1*/	 
		tab1 = new JPanel();
		tab1.setLayout(new BorderLayout());


		JPanel labelPane = new JPanel(new GridLayout(0,1));
		labelPane.setLayout(new FlowLayout(FlowLayout.LEFT));
		tab1.add(labelPane, BorderLayout.WEST);
		
		JPanel fieldPane = new JPanel(new GridLayout(0,1));
		fieldPane.setLayout(new FlowLayout(FlowLayout.LEFT));
		tab1.add(fieldPane, BorderLayout.CENTER);
		
		//setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		JLabel stepLabel = new JLabel("Step size");
		JFormattedTextField stepField = new JFormattedTextField(NumberFormat.getNumberInstance());
		stepField.setColumns(10);
		stepLabel.setLabelFor(stepField);
		fieldPane.add(stepField);
		labelPane.add(stepLabel);


		final JButton btn1 = new JButton("Start");
		btn1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent arg0) {
				try {
					step= Double.parseDouble(stepField.getText());
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				Espacing_Ring.start(network, step);
				
			}
		}); 

		fieldPane.add(btn1);
		
		

		fieldPane.add(btn1);
		
		/*TAB2*/
		tab2 = new JPanel();
		tab2.setLayout(new BorderLayout());

		list = new JList<String>(branchList);
		JPanel listPanel = new JPanel();
		listPanel.add(list, BorderLayout.CENTER);
		JScrollPane scrol = new JScrollPane(list);
		tab2.add(scrol,BorderLayout.WEST);
		tab2.add(listPanel);

		
		/*TABS*/
		
		JTabbedPane tabPane = new JTabbedPane();
		tabPane.addTab( "Start", tab1);
		tabPane.addTab( "Branches", tab2);
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
				network.clear();	
			}
		}); 
		buttonPane.add(btn2);

	}
	
}

