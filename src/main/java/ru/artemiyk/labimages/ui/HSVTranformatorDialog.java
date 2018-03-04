package ru.artemiyk.labimages.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ru.artemiyk.labimages.LabImages;
import ru.artemiyk.labimages.pixelutils.PixelHSV;
import ru.artemiyk.labimages.pixelutils.PixelRGB;

public class HSVTranformatorDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel = new JPanel();

	private ImagePanel imagePanel;

	private BufferedImage originalImage;

	private int dialogWidth = 585;
	private int dialogHeight = 250;

	private IntegerParameterPanel hueShiftPanel;
	private int hueShift = hueShiftDefault;
	private static final int hueShiftDefault = 0;
	private static final int hueShiftMinimum = -180;
	private static final int hueShiftMaximum = 180;
	private static final int hueShiftStep = 1;

	private IntegerParameterPanel satShiftPanel;
	private int satShift = satShiftDefault;
	private static final int satShiftDefault = 0;
	private static final int satShiftMinimum = -100;
	private static final int satShiftMaximum = 100;
	private static final int satShiftStep = 1;

	private IntegerParameterPanel valShiftPanel;
	private int valShift = valShiftDefault;
	private static final int valShiftDefault = 0;
	private static final int valShiftMinimum = -100;
	private static final int valShiftMaximum = 100;
	private static final int valShiftStep = 1;

	private Color background = Color.WHITE;

	private double rgbMax = 255.0;
	private double rgbMin = 0.0;

	private JProgressBar progressBar;
	private ExecutorService threadPool;

	private class ImagePanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private BufferedImage image;

		public void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			if (g2d == null) {
				return;
			}

			g2d.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), this);
		}

		public void setImage(BufferedImage image) {
			this.image = image;
			this.repaint();
		}
	}

	public HSVTranformatorDialog(BufferedImage image) {
		super(LabImages.getInstance().getMainWindow(), true);

		this.originalImage = image;
		threadPool = Executors.newFixedThreadPool(LabImages.THREAD_COUNT);

		setTitle("Change HSV");
		try {
			setIconImage(ImageIO.read(new File(getClass().getClassLoader().getResource("hsv.png").getFile())));
		} catch (Exception ex) {

		}

		Rectangle mainWindowRect = LabImages.getInstance().getMainWindow().getBounds();
		int dialogX = mainWindowRect.x + mainWindowRect.width / 2 - dialogWidth / 2;
		int dialogY = mainWindowRect.y + mainWindowRect.height / 2 - dialogHeight / 2;
		setBounds(dialogX, dialogY, dialogWidth, dialogHeight);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		contentPanel.setBackground(Color.WHITE);

		imagePanel = new ImagePanel();
		imagePanel.setBounds(10, 10, 160, 160);
		contentPanel.add(imagePanel);

		JPanel parametersPanel = new JPanel();
		parametersPanel.setBounds(175, 5, 400, 168);
		parametersPanel.setLayout(new GridLayout(3, 1, 0, 0));
		contentPanel.add(parametersPanel);

		hueShiftPanel = new IntegerParameterPanel("Hue", hueShift, hueShiftDefault, hueShiftMinimum, hueShiftMaximum,
				hueShiftStep);
		parametersPanel.add(hueShiftPanel, BorderLayout.CENTER);
		hueShiftPanel.setBackground(background);
		hueShiftPanel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				hueShift = hueShiftPanel.getValue();
				updatePreview();
			}
		});

		satShiftPanel = new IntegerParameterPanel("Saturation", satShift, satShiftDefault, satShiftMinimum,
				satShiftMaximum, satShiftStep);
		parametersPanel.add(satShiftPanel, BorderLayout.CENTER);
		satShiftPanel.setBackground(background);
		satShiftPanel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				satShift = satShiftPanel.getValue();
				updatePreview();
			}
		});

		valShiftPanel = new IntegerParameterPanel("Value", valShift, valShiftDefault, valShiftMinimum, valShiftMaximum,
				valShiftStep);
		parametersPanel.add(valShiftPanel, BorderLayout.CENTER);
		valShiftPanel.setBackground(background);
		valShiftPanel.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				valShift = valShiftPanel.getValue();
				updatePreview();
			}
		});

		updatePreview();
		
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());
		southPanel.setBackground(Color.WHITE);
		getContentPane().add(southPanel, BorderLayout.SOUTH);

		progressBar = new JProgressBar();
		progressBar.setVisible(false);
		progressBar.setBackground(Color.WHITE);
		progressBar.setStringPainted(true);
		progressBar.setMaximumSize(new Dimension(1000, 20));
		southPanel.add(progressBar, BorderLayout.CENTER);

		JPanel buttonPane = new JPanel();
		buttonPane.setBackground(Color.WHITE);
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		southPanel.add(buttonPane, BorderLayout.EAST);

		JButton okButton = new JButton("OK");
		okButton.setBackground(Color.WHITE);
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOk();
			}
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.setBackground(Color.WHITE);
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
	}

	private void onOk() {
		BufferedImage clonedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(),
				originalImage.getType());
		Graphics g = clonedImage.createGraphics();
		g.drawImage(originalImage, 0, 0, null);
		g.dispose();

		progressBar.setMaximum(2 * clonedImage.getHeight());
		progressBar.setMinimum(0);
		progressBar.setVisible(true);
		Thread thread = new Thread() {
			@Override
			public void run() {
				normalize(clonedImage, progressBar);
				applyChange(clonedImage, progressBar);

				LabImages.getInstance().getMainWindow().getImagePanel().setImage(clonedImage);
				HSVTranformatorDialog.this.dispose();
			}
		};
		thread.start();
	}

	private void onCancel() {
		this.dispose();
	}

	private void updatePreview() {
		int panelWidth = imagePanel.getWidth();
		int panelHeight = imagePanel.getHeight();

		BufferedImage scaledImage = new BufferedImage(panelWidth, panelHeight, originalImage.getType());
		Graphics g = scaledImage.createGraphics();
		g.drawImage(originalImage, 0, 0, panelWidth, panelHeight, null);
		g.dispose();

		normalize(scaledImage, null);
		applyChange(scaledImage, null);

		imagePanel.setImage(scaledImage);
	}

	private int getChangedRGB(int rgb, double[] hsvBuf, double[] rgbBuf) {
		PixelHSV.getHSV(rgb, hsvBuf);

		hsvBuf[0] += (double) hueShift;
		hsvBuf[0] = hsvBuf[0] >= 360.0 ? hsvBuf[0] - 360.0 : hsvBuf[0];
		hsvBuf[0] = hsvBuf[0] < 0.0 ? hsvBuf[0] + 360.0 : hsvBuf[0];

		if (satShift >= 0) {
			hsvBuf[1] += (1.0 - hsvBuf[1]) * (double) satShift / 100.0;
		} else {
			hsvBuf[1] += hsvBuf[1] * (double) satShift / 100.0;
		}

		if (valShift >= 0) {
			hsvBuf[2] += (1.0 - hsvBuf[2]) * (double) valShift / 100.0;
		} else {
			hsvBuf[2] += hsvBuf[2] * (double) valShift / 100.0;
		}

		PixelHSV.getRGB(hsvBuf, rgbBuf);

		double rgbRange = rgbMax - rgbMin;
		for (int i = 0; i < 3; i++) {
			rgbBuf[i] = (rgbBuf[i] - rgbMin) / rgbRange * 255.0;
		}

		return PixelRGB.getRGB(rgbBuf);
	}

	private void applyChange(BufferedImage image, JProgressBar pBar) {
		if (pBar != null) {
			pBar.setString("Applying");
		}

		final int width = image.getWidth();
		final int height = image.getHeight();

		List<Future<Void>> futureList = new ArrayList<>();
		for (int i = 0; i < height; i++) {
			final int iClone = i;

			Supplier<Void> supplier = new Supplier<Void>() {
				public int ii = iClone;

				@Override
				public Void get() {
					double[] rgb = new double[3];
					double[] hsv = new double[3];

					for (int j = 0; j < width; j++) {
						image.setRGB(j, ii, getChangedRGB(image.getRGB(j, ii), hsv, rgb));
					}

					if (pBar != null) {
						progressIncrement();
					}

					return null;
				}
			};

			futureList.add(CompletableFuture.supplyAsync(supplier, threadPool));
		}

		for (Future<Void> future : futureList) {
			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {

			}
		}
	}

	private void normalize(BufferedImage image, JProgressBar pBar) {
		if (pBar != null) {
			pBar.setString("Normalizing");
		}

		final int width = image.getWidth();
		final int height = image.getHeight();

		List<Future<Void>> futureList = new ArrayList<>();
		for (int i = 0; i < height; i++) {
			final int iClone = i;

			Supplier<Void> supplier = new Supplier<Void>() {
				public int ii = iClone;

				@Override
				public Void get() {
					double min = 0.0;
					double max = 255.0;
					double[] rgb = new double[3];
					double[] hsv = new double[3];
					for (int j = 0; j < width; j++) {
						PixelHSV.getHSV(image.getRGB(j, ii), hsv);

						hsv[0] += (double) hueShift;
						hsv[0] = hsv[0] >= 360.0 ? hsv[0] - 360.0 : hsv[0];
						hsv[0] = hsv[0] < 0.0 ? hsv[0] + 360.0 : hsv[0];

						if (satShift >= 0) {
							hsv[1] += (1.0 - hsv[1]) * (double) satShift / 100.0;
						} else {
							hsv[1] += hsv[1] * (double) satShift / 100.0;
						}

						if (valShift >= 0) {
							hsv[2] += (1.0 - hsv[2]) * (double) valShift / 100.0;
						} else {
							hsv[2] += hsv[2] * (double) valShift / 100.0;
						}

						PixelHSV.getRGB(hsv, rgb);

						for (int rgbIndex = 0; rgbIndex < 3; rgbIndex++) {
							if (rgb[rgbIndex] > max) {
								max = rgb[rgbIndex];
							} else if (rgb[rgbIndex] < min) {
								min = rgb[rgbIndex];
							}
						}
					}

					setMinRgb(min);
					setMaxRgb(max);

					if (pBar != null) {
						progressIncrement();
					}

					return null;
				}
			};

			futureList.add(CompletableFuture.supplyAsync(supplier, threadPool));
		}

		for (Future<Void> future : futureList) {
			try {
				future.get();
			} catch (InterruptedException | ExecutionException e) {

			}
		}
	}

	private synchronized void progressIncrement() {
		int val = progressBar.getValue();
		val++;
		if (progressBar.getMaximum() > val) {
			progressBar.setValue(val);
		}
	}

	private synchronized void setMinRgb(double val) {
		if (val < rgbMin) {
			rgbMin = val;
		}
	}

	private synchronized void setMaxRgb(double val) {
		if (val > rgbMax) {
			rgbMax = val;
		}
	}
}
