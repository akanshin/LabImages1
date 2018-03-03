package ru.artemiyk.labimages.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
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
import ru.artemiyk.labimages.filter.GaborKernel;
import ru.artemiyk.labimages.filter.ProgressListener;

public class GaborFilterDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();

	private int dialogWidth = 406;
	private int dialogHeight = 369;

	private JProgressBar progressBar;

	private BufferedImage imageToRead;
	private BufferedImage imageToWrite;

	private FilterApplyer filterApplyer;
	private boolean disposeOnFinish = false;
	private boolean applying = false;

	private Color background = Color.WHITE;

	private DoubleParameterPanel lambdaPanel;
	private double lambda = lambdaDefault;
	private static final double lambdaDefault = 2.0;
	private static final double lambdaMinimum = 1.0;
	private static final double lambdaMaximum = 10.0;
	private static final double lambdaStep = 0.1;

	private IntegerParameterPanel thetaPanel;
	private int theta = thetaDefault;
	private static final int thetaDefault = 0;
	private static final int thetaMinimum = 0;
	private static final int thetaMaximum = 360;
	private static final int thetaStep = 1;

	private IntegerParameterPanel psiPanel;
	private int psi = psiDefault;
	private static final int psiDefault = 0;
	private static final int psiMinimum = 0;
	private static final int psiMaximum = 360;
	private static final int psiStep = 1;

	private IntegerParameterPanel sigmaPanel;
	private int sigma = sigmaDefault;
	private static final int sigmaDefault = 5;
	private static final int sigmaMinimum = 0;
	private static final int sigmaMaximum = 40;
	private static final int sigmaStep = 1;

	private DoubleParameterPanel gammaPanel;
	private double gamma = gammaDefault;
	private static final double gammaDefault = 1.0;
	private static final double gammaMinimum = 0.1;
	private static final double gammaMaximum = 10.0;
	private static final double gammaStep = 0.1;

	private IntegerParameterPanel kernelSizePanel;
	private int kernelSize = kernelSizeDefault;
	private static final int kernelSizeDefault = 1;
	private static final int kernelSizeMinimum = 1;
	private static final int kernelSizeMaximum = 201;
	private static final int kernelSizeStep = 1;

	public static void main(String[] args) {
		new GaborFilterDialog();
	}

	public GaborFilterDialog() {
		super(LabImages.getInstance().getMainWindow(), true);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setTitle("Gabor filter");
		try {
			setIconImage(ImageIO.read(new File(getClass().getClassLoader().getResource("gabor_filter.png").getFile())));
		} catch (Exception ex) {

		}

		Rectangle mainWindowRect = LabImages.getInstance().getMainWindow().getBounds();
		int dialogX = mainWindowRect.x + mainWindowRect.width / 2 - dialogWidth / 2;
		int dialogY = mainWindowRect.y + mainWindowRect.height / 2 - dialogHeight / 2;
		setBounds(dialogX, dialogY, dialogWidth, dialogHeight);

		setResizable(false);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setBackground(background);
		contentPanel.setLayout(new GridLayout(6, 1, 0, 0));

		lambdaPanel = new DoubleParameterPanel("Lambda", lambda, lambdaDefault, lambdaMinimum, lambdaMaximum, lambdaStep);
		contentPanel.add(lambdaPanel);
		lambdaPanel.setBackground(background);
		lambdaPanel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				lambda = lambdaPanel.getValue();
				calculate();
			}
		});
		
		thetaPanel = new IntegerParameterPanel("Theta", theta, thetaDefault, thetaMinimum, thetaMaximum, thetaStep);
		contentPanel.add(thetaPanel);
		thetaPanel.setBackground(background);
		thetaPanel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				theta = thetaPanel.getValue();
				calculate();
			}
		});

		psiPanel = new IntegerParameterPanel("Psi", psi, psiDefault,
				psiMinimum, psiMaximum, psiStep);
		contentPanel.add(psiPanel);
		psiPanel.setBackground(background);
		psiPanel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				psi = psiPanel.getValue();
				calculate();
			}
		});
		
		sigmaPanel = new IntegerParameterPanel("Sigma", sigma, sigmaDefault,
				sigmaMinimum, sigmaMaximum, sigmaStep);
		contentPanel.add(sigmaPanel);
		sigmaPanel.setBackground(background);
		sigmaPanel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				sigma = sigmaPanel.getValue();
				calculate();
			}
		});
		
		gammaPanel = new DoubleParameterPanel("Gamma", gamma, gammaDefault, gammaMinimum, gammaMaximum, gammaStep);
		contentPanel.add(gammaPanel);
		gammaPanel.setBackground(background);
		gammaPanel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				gamma = gammaPanel.getValue();
				calculate();
			}
		});
		
		kernelSizePanel = new IntegerParameterPanel("Kernel size", kernelSize, kernelSizeDefault,
				kernelSizeMinimum, kernelSizeMaximum, kernelSizeStep);
		contentPanel.add(kernelSizePanel);
		kernelSizePanel.setBackground(background);
		kernelSizePanel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				kernelSize = kernelSizePanel.getValue();
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

		filterApplyer.addKernel(new GaborKernel(lambda, theta, psi, sigma, gamma, kernelSize));

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
					GaborFilterDialog.this.dispose();
				}
			}
		};
		thread.start();
	}
}
