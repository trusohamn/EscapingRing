import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.OvalRoi;
import ij.gui.Roi;

public class Gui extends JDialog {
	private final JPanel contentPanel = new JPanel();



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

		setBounds(100, 100, 450, 300);
		/*
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new GridLayout(0,1));
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		 */

		JPanel labelPane = new JPanel(new GridLayout(0,1));
		labelPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(labelPane, BorderLayout.CENTER);
		
		JPanel fieldPane = new JPanel(new GridLayout(0,1));
		fieldPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(fieldPane, BorderLayout.LINE_END);
		
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
				ImagePlus imp = WindowManager.getCurrentImage();
				Network network = new Network();
				if (imp == null) {
					IJ.error("No open image.");
					return;
				}

				Roi roi = imp.getRoi();
				if (roi == null) {
					IJ.error("No selected ROI.");
					return;
				}

				if (roi.getType() != Roi.OVAL){
					IJ.error("No selected Oval ROI.");
					return;
				}

				OvalRoi oval = (OvalRoi)roi;
				Rectangle rect = oval.getBounds();
				int xc = rect.x + rect.width/2;
				int yc = rect.y + rect.height/2;
				int radius = (rect.width + rect.height) / 4;	
				int zc = imp.getSlice();	
				
				double step = 10;
				try {
					step= Double.parseDouble(stepField.getText());
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				
				Ring initial = new Ring(xc, yc, zc, 0, 0, 0, radius);
				IJ.log(" Initial Ring " + initial);
				Volume test = new Volume(imp.getWidth(), imp.getHeight(), imp.getNSlices());
				//drawMeasureArea(test, initial, step);
				Volume vol = new Volume(imp);	
				Volume workingVol = new Volume(imp); //will be erased
				
				Ring adjInitial = initial.adjustFirstRing(vol, step);
				network.recalculateContrast(initial.contrast);
				
				Branch firstBranch = new Branch(network, adjInitial, vol, test, workingVol, step);
				//drawMeasureArea(test, adjInitial, step);

				for(Branch branch : network) {
					for(Ring ring : branch) {
						ring.drawMeasureArea(test, step);
					}
				}
				
				vol.showTwoChannels("Result", test);
				

			}
		}); 

		fieldPane.add(btn1);



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

	}
}

