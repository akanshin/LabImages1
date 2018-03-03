package ru.artemiyk.labimages.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ru.artemiyk.labimages.LabImages;
import ru.artemiyk.labimages.filter.EProgressState;
import ru.artemiyk.labimages.filter.FilterApplyer;
import ru.artemiyk.labimages.filter.ProgressListener;
import ru.artemiyk.labimages.filter.SobelKernel;

public class SobelFilterDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private IntegerParameterPanel anglePanel;
	
	private int dialogWidth = 406;
	private int dialogHeight = 128;

	private int angle = angleDefault;
	private static final int angleDefault = 0;
	private static final int angleMinimum = 0;
	private static final int angleMaximum = 360;
	private static final int angleStep = 1;

	private JProgressBar progressBar;
	
	private BufferedImage imageToRead;
	private BufferedImage imageToWrite;
	
	private FilterApplyer filterApplyer;
	private boolean disposeOnFinish = false;
	private boolean applying = false;
	
	private Color background = Color.WHITE;

	public SobelFilterDialog() {
		super(LabImages.getInstance().getMainWindow(), true);
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle("Sobel filter");
		try {
			setIconImage(
					ImageIO.read(new File(getClass().getClassLoader().getResource("sobel_filter.png").getFile())));
		} catch (Exception ex) {

		}
		
		Rectangle mainWindowRect = LabImages.getInstance().getMainWindow().getBounds();
		int dialogX = mainWindowRect.x + mainWindowRect.width / 2 - dialogWidth / 2;
		int dialogY = mainWindowRect.y + mainWindowRect.height / 2 - dialogHeight / 2;
		setBounds(dialogX, dialogY, dialogWidth, dialogHeight);
		
		setResizable(false);

		getContentPane().setLayout(new BorderLayout());
		
		anglePanel = new IntegerParameterPanel("Angle", angle, angleDefault, angleMinimum, angleMaximum, angleStep);
		getContentPane().add(anglePanel, BorderLayout.CENTER);
		anglePanel.setBackground(background);
		anglePanel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				angle = anglePanel.getValue();
				calculate();
			}
		});

		JPanel buttonPane = new JPanel();
		buttonPane.setBackground(background);
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		buttonPane.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		panel.setBackground(background);
		buttonPane.add(panel, BorderLayout.EAST);

		JButton okButton = new JButton("OK");
		panel.add(okButton);
		okButton.setBackground(background);
		okButton.setActionCommand("OK");
		getRootPane().setDefaultButton(okButton);
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				onOk();
			}
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.setBackground(background);
		panel.add(cancelButton);
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				onCancel();
			}
		});
		
		progressBar = new JProgressBar();
		progressBar.setBackground(background);
		progressBar.setIndeterminate(true);
		progressBar.setVisible(false);
		buttonPane.add(progressBar, BorderLayout.CENTER);

	}
	
	public void setImages(BufferedImage imageToRead, BufferedImage imageToWrite) {
		this.imageToRead = imageToRead;
		this.imageToWrite = imageToWrite;

		progressBar.setMinimum(0);
		progressBar.setMaximum(2 * imageToRead.getHeight());

		calculate();
	}
	
	private void onOk() {
		progressBar.setIndeterminate(false);
		disposeOnFinish = true;
		if (!applying) {
			dispose();
		}
	}

	private void onCancel() {
		this.dispose();

		progressBar.setVisible(false);

		LabImages.getInstance().getMainWindow().getImagePanel().revertImageFilterChanges();
	}

	private synchronized void progressIncrement() {
		int val = progressBar.getValue();
		val++;
		if (progressBar.getMaximum() > val) {
			progressBar.setValue(val);
		}
	}

	private void calculate() {
		if (filterApplyer != null) {
			filterApplyer.interrupt();
			try {
				filterApplyer.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		filterApplyer = new FilterApplyer();
		filterApplyer.setThreadPool(Executors.newFixedThreadPool(8));
		filterApplyer.setImageToRead(imageToRead);
		filterApplyer.setImageToWrite(imageToWrite);
		filterApplyer.addProgressListener(new ProgressListener() {
			@Override
			public void progressChanged(EProgressState progressState) {
				progressIncrement();
				
				progressBar.setStringPainted(true);
				if (progressState == EProgressState.eNormalizing) {
					progressBar.setString("Normalizing");
				} else if (progressState == EProgressState.eApplying) {
					progressBar.setString("Applying");
				}
				
				LabImages.getInstance().getMainWindow().getImagePanel().repaint();
			}
		});
		
		filterApplyer.addKernel(new SobelKernel((double) angle));

		filterApplyer.start();
		
		Thread thread = new Thread() {
			@Override
			public void run() {
				applying = true;
				try {
					progressBar.setValue(0);
					progressBar.setVisible(true);
					filterApplyer.join();
					progressBar.setVisible(false);
				} catch (InterruptedException e) {
					
				}
				
				applying = false;
				
				if (disposeOnFinish) {
					SobelFilterDialog.this.dispose();
				}
			}
		};
		thread.start();
	}
}
