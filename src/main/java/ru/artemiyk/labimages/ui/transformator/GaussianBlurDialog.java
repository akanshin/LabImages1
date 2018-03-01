package ru.artemiyk.labimages.ui.transformator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ru.artemiyk.labimages.LabImages;
import ru.artemiyk.labimages.filter.EProgressState;
import ru.artemiyk.labimages.filter.FilterApplyer;
import ru.artemiyk.labimages.filter.GaussianKernel;
import ru.artemiyk.labimages.filter.ProgressListener;

public class GaussianBlurDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();

	private int dialogWidth = 406;
	private int dialogHeight = 128;

	private int radius = 2;

	private JSlider slider;
	private JSpinner spinner;
	private JProgressBar progressBar;

	private BufferedImage imageToRead;
	private BufferedImage imageToWrite;

	private FilterApplyer filterApplyer;
	private boolean disposeOnFinish = false;
	private boolean applying = false;
	
	private Color background = Color.WHITE;

	public GaussianBlurDialog() {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle("Gaussian filter");
		try {
			setIconImage(
					ImageIO.read(new File(getClass().getClassLoader().getResource("gaussian_blur.png").getFile())));
		} catch (Exception ex) {

		}

		Rectangle mainWindowRect = LabImages.getInstance().getMainWindow().getBounds();
		int dialogX = mainWindowRect.x + mainWindowRect.width / 2 - dialogWidth / 2;
		int dialogY = mainWindowRect.y + mainWindowRect.height / 2 - dialogHeight / 2;
		setBounds(dialogX, dialogY, 406, 121);

		setResizable(false);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new TitledBorder(null, "Radius", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		contentPanel.setBackground(background);

		slider = new JSlider();
		slider.setBackground(background);
		slider.setPaintTicks(true);
		slider.setBounds(10, 21, 260, 23);
		slider.setValue(radius);
		slider.setMaximum(40);
		contentPanel.add(slider);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				radius = slider.getValue();
				if (spinner != null) {
					spinner.setValue(radius);
				}

				calculate();
			}
		});

		spinner = new JSpinner();
		spinner.setBackground(background);
		spinner.setBounds(new Rectangle(10, 10, 70, 20));
		spinner.setBounds(280, 21, 70, 20);
		spinner.setMaximumSize(new Dimension(70, 20));
		spinner.setMinimumSize(new Dimension(70, 20));
		spinner.setModel(new SpinnerNumberModel(radius, 0, 40, 1));
		contentPanel.add(spinner);
		spinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				radius = (Integer) spinner.getValue();
				if (slider != null) {
					slider.setValue(radius);
				}

				calculate();
			}
		});

		JButton defaultButton = new JButton("");
		defaultButton.setBackground(background);
		defaultButton.setBounds(new Rectangle(360, 21, 20, 20));
		defaultButton.setMinimumSize(new Dimension(16, 16));
		defaultButton.setMaximumSize(new Dimension(16, 16));
		defaultButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("left_arrow.png")));
		contentPanel.add(defaultButton);
		defaultButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				radius = 2;
				if (slider != null) {
					slider.setValue(radius);
				}
				if (spinner != null) {
					spinner.setValue(radius);
				}
				
				calculate();
			}
		});
		
		JPanel buttonPane = new JPanel();
		buttonPane.setBackground(background);
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		buttonPane.setLayout(new BorderLayout(0, 0));

		progressBar = new JProgressBar();
		progressBar.setBackground(background);
		progressBar.setMinimumSize(new Dimension(10, 14));
		progressBar.setMaximumSize(new Dimension(32767, 14));
		progressBar.setIndeterminate(true);
		progressBar.setVisible(false);
		buttonPane.add(progressBar);

		JPanel panel = new JPanel();
		panel.setBackground(background);
		buttonPane.add(panel, BorderLayout.EAST);

		JButton okButton = new JButton("OK");
		okButton.setBackground(background);
		panel.add(okButton);
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
	}

	public void setImages(BufferedImage imageToRead, BufferedImage imageToWrite) {
		this.imageToRead = imageToRead;
		this.imageToWrite = imageToWrite;

		progressBar.setMinimum(0);
		progressBar.setMaximum(imageToRead.getWidth() * imageToRead.getHeight());

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
		
		filterApplyer.setKernel(new GaussianKernel(radius));

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
					GaussianBlurDialog.this.dispose();
				}
			}
		};
		thread.start();
	}

}
