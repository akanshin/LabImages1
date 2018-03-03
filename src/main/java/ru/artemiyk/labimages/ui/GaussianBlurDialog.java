package ru.artemiyk.labimages.ui;

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
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ru.artemiyk.labimages.LabImages;
import ru.artemiyk.labimages.filter.EProgressState;
import ru.artemiyk.labimages.filter.FilterApplyer;
import ru.artemiyk.labimages.filter.GaussianKernel;
import ru.artemiyk.labimages.filter.KernelComponent;
import ru.artemiyk.labimages.filter.ProgressListener;

public class GaussianBlurDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private IntegerParameterPanel radiusPanel;

	private int dialogWidth = 406;
	private int dialogHeight = 128;

	private int radius = radiusDefault;
	private static final int radiusDefault = 2;
	private static final int radiusMinimum = 0;
	private static final int radiusMaximum = 100;
	private static final int radiusStep = 1;

	private JProgressBar progressBar;

	private BufferedImage imageToRead;
	private BufferedImage imageToWrite;

	private FilterApplyer filterApplyer;
	private boolean disposeOnFinish = false;
	private boolean applying = false;

	private Color background = Color.WHITE;

	public GaussianBlurDialog() {
		super(LabImages.getInstance().getMainWindow(), true);

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
		setBounds(dialogX, dialogY, 406, 120);

		setResizable(false);
		getContentPane().setLayout(new BorderLayout());

		radiusPanel = new IntegerParameterPanel("Radius", radius, radiusDefault, radiusMinimum, radiusMaximum,
				radiusStep);
		getContentPane().add(radiusPanel, BorderLayout.CENTER);
		radiusPanel.setBackground(background);
		radiusPanel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				radius = radiusPanel.getValue();
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
		progressBar.setMaximum(4 * imageToRead.getHeight());

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

		filterApplyer.addKernel(new GaussianKernel(radius, KernelComponent.eVertical));
		filterApplyer.addKernel(new GaussianKernel(radius, KernelComponent.eHorizontal));
		// filterApplyer.addKernel(new GaussianKernel(radius,
		// KernelComponent.eHorizontalandVertical));

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
