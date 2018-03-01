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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
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

	private JDoubleSlider sliderLambda;
	private JDoubleSlider sliderTheta;
	private JSlider sliderPsi;
	private JSlider sliderSigma;
	private JDoubleSlider sliderGamma;
	private JSlider sliderKernelSize;

	private JSpinner spinnerLambda;
	private JSpinner spinnerTheta;
	private JSpinner spinnerPsi;
	private JSpinner spinnerSigma;
	private JSpinner spinnerGamma;
	private JSpinner spinnerKernelSize;

	private JProgressBar progressBar;

	private BufferedImage imageToRead;
	private BufferedImage imageToWrite;

	private FilterApplyer filterApplyer;
	private boolean disposeOnFinish = false;
	private boolean applying = false;

	private Color background = Color.WHITE;

	private double lambda = lambdaDefault;
	private static final double lambdaDefault = 2.0;
	private static final double lambdaMinimum = 1.0;
	private static final double lambdaMaximum = 10.0;
	private static final double lambdaStep = 0.1;

	private double theta = thetaDefault;
	private static final double thetaDefault = 0.0;
	private static final double thetaMinimum = 0.0;
	private static final double thetaMaximum = 10.0;
	private static final double thetaStep = 0.01;

	private int psi = psiDefault;
	private static final int psiDefault = 0;
	private static final int psiMinimum = 0;
	private static final int psiMaximum = 360;
	private static final int psiStep = 1;

	private int sigma = sigmaDefault;
	private static final int sigmaDefault = 5;
	private static final int sigmaMinimum = 0;
	private static final int sigmaMaximum = 40;
	private static final int sigmaStep = 1;

	private double gamma = gammaDefault;
	private static final double gammaDefault = 1.0;
	private static final double gammaMinimum = 0.1;
	private static final double gammaMaximum = 10.0;
	private static final double gammaStep = 0.1;

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

		buildUILambda();
		buildUITheta();
		buildUIPsi();
		buildUISigma();
		buildUIGamma();
		buildUIKernelSize();

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

	private void buildUILambda() {
		contentPanel.setLayout(new GridLayout(1, 1, 0, 0));
		JPanel panel = new JPanel();
		panel.setBackground(background);
		panel.setBorder(new TitledBorder(null, "Lambda", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setLayout(null);
		contentPanel.add(panel);

		sliderLambda = new JDoubleSlider();
		sliderLambda.setBounds(10, 21, 260, 23);
		sliderLambda.setBackground(background);
		sliderLambda.setPaintTicks(true);
		sliderLambda.setMin(lambdaMinimum);
		sliderLambda.setMax(lambdaMaximum);
		sliderLambda.setStep(lambdaStep);
		sliderLambda.setDoubleValue(lambdaDefault);
		panel.add(sliderLambda);
		sliderLambda.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				lambda = sliderLambda.getDoubleValue();
				if (spinnerLambda != null) {
					spinnerLambda.setValue(lambda);
				}

				calculate();
			}
		});

		spinnerLambda = new JSpinner();
		spinnerLambda.setBackground(background);
		spinnerLambda.setBounds(new Rectangle(10, 10, 70, 20));
		spinnerLambda.setBounds(280, 21, 70, 20);
		spinnerLambda.setMaximumSize(new Dimension(70, 20));
		spinnerLambda.setMinimumSize(new Dimension(70, 20));
		spinnerLambda.setModel(new SpinnerNumberModel(lambdaDefault, lambdaMinimum, lambdaMaximum, lambdaStep));
		panel.add(spinnerLambda);
		spinnerLambda.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				lambda = (Double) spinnerLambda.getValue();
				if (sliderLambda != null) {
					sliderLambda.setDoubleValue(lambda);
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
		panel.add(defaultButton);
		defaultButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				lambda = lambdaDefault;
				if (sliderLambda != null) {
					sliderLambda.setDoubleValue(lambda);
				}
				if (spinnerLambda != null) {
					spinnerLambda.setValue(lambda);
				}

				calculate();
			}
		});
	}

	private void buildUITheta() {
		contentPanel.setLayout(new GridLayout(2, 1, 0, 0));
		JPanel panel = new JPanel();
		panel.setBackground(background);
		panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Theta", TitledBorder.LEADING,
				TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel.setLayout(null);
		contentPanel.add(panel);

		sliderTheta = new JDoubleSlider();
		sliderTheta.setBounds(10, 21, 260, 23);
		sliderTheta.setBackground(background);
		sliderTheta.setPaintTicks(true);
		sliderTheta.setMin(thetaMinimum);
		sliderTheta.setMax(thetaMaximum);
		sliderTheta.setStep(thetaStep);
		sliderTheta.setDoubleValue(thetaDefault);
		panel.add(sliderTheta);
		sliderTheta.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				theta = sliderTheta.getDoubleValue();
				if (spinnerTheta != null) {
					spinnerTheta.setValue(theta);
				}

				calculate();
			}
		});

		spinnerTheta = new JSpinner();
		spinnerTheta.setBackground(background);
		spinnerTheta.setBounds(new Rectangle(10, 10, 70, 20));
		spinnerTheta.setBounds(280, 21, 70, 20);
		spinnerTheta.setMaximumSize(new Dimension(70, 20));
		spinnerTheta.setMinimumSize(new Dimension(70, 20));
		spinnerTheta.setModel(new SpinnerNumberModel(thetaDefault, thetaMinimum, thetaMaximum, thetaStep));
		panel.add(spinnerTheta);
		spinnerTheta.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				theta = (Double) spinnerTheta.getValue();
				if (sliderTheta != null) {
					sliderTheta.setDoubleValue(theta);
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
		panel.add(defaultButton);
		defaultButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				theta = thetaDefault;
				if (sliderTheta != null) {
					sliderTheta.setDoubleValue(theta);
				}
				if (spinnerTheta != null) {
					spinnerTheta.setValue(theta);
				}

				calculate();
			}
		});
	}

	private void buildUIPsi() {
		contentPanel.setLayout(new GridLayout(3, 1, 0, 0));
		JPanel panel = new JPanel();
		panel.setBackground(background);
		panel.setBorder(new TitledBorder(null, "Psi", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setLayout(null);
		contentPanel.add(panel);

		sliderPsi = new JSlider();
		sliderPsi.setBounds(10, 21, 260, 23);
		sliderPsi.setBackground(background);
		sliderPsi.setPaintTicks(true);
		sliderPsi.setMinimum(psiMinimum);
		sliderPsi.setMaximum(psiMaximum);
		sliderPsi.setValue(psiDefault);
		panel.add(sliderPsi);
		sliderPsi.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				psi = sliderPsi.getValue();
				if (spinnerPsi != null) {
					spinnerPsi.setValue(psi);
				}

				calculate();
			}
		});

		spinnerPsi = new JSpinner();
		spinnerPsi.setBackground(background);
		spinnerPsi.setBounds(new Rectangle(10, 10, 70, 20));
		spinnerPsi.setBounds(280, 21, 70, 20);
		spinnerPsi.setMaximumSize(new Dimension(70, 20));
		spinnerPsi.setMinimumSize(new Dimension(70, 20));
		spinnerPsi.setModel(new SpinnerNumberModel(psiDefault, psiMinimum, psiMaximum, psiStep));
		panel.add(spinnerPsi);
		spinnerPsi.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				psi = (Integer) spinnerPsi.getValue();
				if (sliderPsi != null) {
					sliderPsi.setValue(psi);
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
		panel.add(defaultButton);
		defaultButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				psi = psiDefault;
				if (sliderPsi != null) {
					sliderPsi.setValue(psi);
				}
				if (spinnerPsi != null) {
					spinnerPsi.setValue(psi);
				}

				calculate();
			}
		});
	}

	private void buildUISigma() {
		contentPanel.setLayout(new GridLayout(4, 1, 0, 0));
		JPanel panel = new JPanel();
		panel.setBackground(background);
		panel.setBorder(new TitledBorder(null, "Sigma", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setLayout(null);
		contentPanel.add(panel);

		sliderSigma = new JSlider();
		sliderSigma.setBounds(10, 21, 260, 23);
		sliderSigma.setBackground(background);
		sliderSigma.setPaintTicks(true);
		sliderSigma.setMinimum(sigmaMinimum);
		sliderSigma.setMaximum(sigmaMaximum);
		sliderSigma.setValue(sigmaDefault);
		panel.add(sliderSigma);
		sliderSigma.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				sigma = sliderSigma.getValue();
				if (spinnerSigma != null) {
					spinnerSigma.setValue(sigma);
				}

				calculate();
			}
		});

		spinnerSigma = new JSpinner();
		spinnerSigma.setBackground(background);
		spinnerSigma.setBounds(new Rectangle(10, 10, 70, 20));
		spinnerSigma.setBounds(280, 21, 70, 20);
		spinnerSigma.setMaximumSize(new Dimension(70, 20));
		spinnerSigma.setMinimumSize(new Dimension(70, 20));
		spinnerSigma.setModel(new SpinnerNumberModel(sigmaDefault, sigmaMinimum, sigmaMaximum, sigmaStep));
		panel.add(spinnerSigma);
		spinnerSigma.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				sigma = (Integer) spinnerSigma.getValue();
				if (sliderSigma != null) {
					sliderSigma.setValue(sigma);
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
		panel.add(defaultButton);
		defaultButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				sigma = sigmaDefault;
				if (sliderSigma != null) {
					sliderSigma.setValue(sigma);
				}
				if (spinnerSigma != null) {
					spinnerSigma.setValue(sigma);
				}

				calculate();
			}
		});
	}

	private void buildUIGamma() {
		contentPanel.setLayout(new GridLayout(5, 1, 0, 0));
		JPanel panel = new JPanel();
		panel.setBackground(background);
		panel.setBorder(new TitledBorder(null, "Gamma", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setLayout(null);
		contentPanel.add(panel);

		sliderGamma = new JDoubleSlider();
		sliderGamma.setBounds(10, 21, 260, 23);
		sliderGamma.setBackground(background);
		sliderGamma.setPaintTicks(true);
		sliderGamma.setMin(gammaMinimum);
		sliderGamma.setMax(gammaMaximum);
		sliderGamma.setStep(gammaStep);
		sliderGamma.setDoubleValue(gamma);
		panel.add(sliderGamma);
		sliderGamma.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				gamma = sliderGamma.getDoubleValue();
				if (spinnerGamma != null) {
					spinnerGamma.setValue(gamma);
				}

				calculate();
			}
		});

		spinnerGamma = new JSpinner();
		spinnerGamma.setBackground(background);
		spinnerGamma.setBounds(new Rectangle(10, 10, 70, 20));
		spinnerGamma.setBounds(280, 21, 70, 20);
		spinnerGamma.setMaximumSize(new Dimension(70, 20));
		spinnerGamma.setMinimumSize(new Dimension(70, 20));
		spinnerGamma.setModel(new SpinnerNumberModel(gamma, gammaMinimum, gammaMaximum, gammaStep));
		panel.add(spinnerGamma);
		spinnerGamma.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				gamma = (Double) spinnerGamma.getValue();
				if (sliderGamma != null) {
					sliderGamma.setDoubleValue(gamma);
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
		panel.add(defaultButton);
		defaultButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				gamma = gammaDefault;
				if (sliderGamma != null) {
					sliderGamma.setDoubleValue(gamma);
				}
				if (spinnerGamma != null) {
					spinnerGamma.setValue(gamma);
				}

				calculate();
			}
		});
	}

	private void buildUIKernelSize() {
		contentPanel.setLayout(new GridLayout(6, 1, 0, 0));
		JPanel panel = new JPanel();
		panel.setBackground(background);
		panel.setBorder(new TitledBorder(null, "Kernel size", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.setLayout(null);
		contentPanel.add(panel);

		sliderKernelSize = new JSlider();
		sliderKernelSize.setBounds(10, 21, 260, 23);
		sliderKernelSize.setBackground(background);
		sliderKernelSize.setPaintTicks(true);
		sliderKernelSize.setMinimum(kernelSizeMinimum);
		sliderKernelSize.setMaximum(kernelSizeMaximum);
		sliderKernelSize.setValue(kernelSizeDefault);
		panel.add(sliderKernelSize);
		sliderKernelSize.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				kernelSize = sliderKernelSize.getValue();
				if (spinnerKernelSize != null) {
					spinnerKernelSize.setValue(kernelSize);
				}

				calculate();
			}
		});

		spinnerKernelSize = new JSpinner();
		spinnerKernelSize.setBackground(background);
		spinnerKernelSize.setBounds(new Rectangle(10, 10, 70, 20));
		spinnerKernelSize.setBounds(280, 21, 70, 20);
		spinnerKernelSize.setMaximumSize(new Dimension(70, 20));
		spinnerKernelSize.setMinimumSize(new Dimension(70, 20));
		spinnerKernelSize.setModel(
				new SpinnerNumberModel(kernelSizeDefault, kernelSizeMinimum, kernelSizeMaximum, kernelSizeStep));
		panel.add(spinnerKernelSize);
		spinnerKernelSize.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				kernelSize = (Integer) spinnerKernelSize.getValue();
				if (sliderKernelSize != null) {
					sliderKernelSize.setValue(kernelSize);
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
		panel.add(defaultButton);
		defaultButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				kernelSize = kernelSizeDefault;
				if (sliderKernelSize != null) {
					sliderKernelSize.setValue(kernelSize);
				}
				if (spinnerKernelSize != null) {
					spinnerKernelSize.setValue(kernelSize);
				}

				calculate();
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

		filterApplyer.setKernel(new GaborKernel(lambda, theta, psi, sigma, gamma, kernelSize));

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
