package ru.artemiyk.labimages.ui.transformator;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ru.artemiyk.labimages.LabImages;
import ru.artemiyk.labimages.imageutils.ImageUtils;
import ru.artemiyk.labimages.pixelutils.PixelHSV;

public class HSVTranformatorDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private final JPanel contentPanel = new JPanel();

	private ImagePanel imagePanel;

	private BufferedImage originalImage;

	private JSlider valueSlider;
	private JSlider saturationSlider;
	private JSlider hueSlider;

	private JSpinner hueSpinner;
	private JSpinner saturationSpinner;
	private JSpinner valueSpinner;

	private int hueShift;
	private int satShift;
	private int valShift;

	private JProgressBar progressBar;

	private class ImagePanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private BufferedImage image;

		public void paintComponent(Graphics g) {
			Graphics2D g2d = (Graphics2D) g;
			if (g2d == null) {
				return;
			}

			int imageWidth = this.image.getWidth(this);
			int imageHeight = this.image.getHeight(this);
			int panelWidth = this.getWidth();
			int panelHeight = this.getHeight();

			int imageX = panelWidth / 2 - imageWidth / 2;
			int imageY = panelHeight / 2 - imageHeight / 2;

			g2d.setColor(this.getBackground());
			g2d.fillRect(0, 0, this.getWidth(), this.getHeight());
			g2d.drawImage(image, imageX, imageY, imageWidth, imageHeight, this);
		}

		public void setImage(BufferedImage image) {
			this.image = image;
			this.repaint();
		}
	}

	public HSVTranformatorDialog(BufferedImage image) {
		this.originalImage = image;

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);
		setBounds(100, 100, 510, 273);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);

		imagePanel = new ImagePanel();
		imagePanel.setBounds(10, 10, 160, 160);
		contentPanel.add(imagePanel);

		hueSlider = new JSlider();
		hueSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				hueShift = hueSlider.getValue();
				if (hueSpinner != null) {
					hueSpinner.setValue(hueShift);
				}
				updatePreview();
			}
		});
		hueSlider.setValue(0);
		hueSlider.setToolTipText("Hue");
		hueSlider.setPaintTicks(true);
		hueSlider.setPaintLabels(true);
		hueSlider.setMaximum(180);
		hueSlider.setMinimum(-180);
		hueSlider.setBounds(180, 33, 200, 26);
		contentPanel.add(hueSlider);

		JLabel hueLabel = new JLabel("Hue");
		hueLabel.setBounds(180, 11, 46, 19);
		contentPanel.add(hueLabel);

		JLabel saturationLabel = new JLabel("Saturation");
		saturationLabel.setBounds(180, 70, 64, 19);
		contentPanel.add(saturationLabel);

		saturationSlider = new JSlider();
		saturationSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				satShift = saturationSlider.getValue();
				if (saturationSpinner != null) {
					saturationSpinner.setValue(satShift);
				}
				updatePreview();
			}
		});
		saturationSlider.setValue(0);
		saturationSlider.setToolTipText("Saturation");
		saturationSlider.setPaintTicks(true);
		saturationSlider.setPaintLabels(true);
		saturationSlider.setMinimum(-100);
		saturationSlider.setBounds(182, 89, 200, 26);
		contentPanel.add(saturationSlider);

		JLabel valueLabel = new JLabel("Value");
		valueLabel.setBounds(180, 126, 46, 19);
		contentPanel.add(valueLabel);

		valueSlider = new JSlider();
		valueSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				valShift = valueSlider.getValue();
				if (valueSpinner != null) {
					valueSpinner.setValue((Integer) valShift);
				}
				updatePreview();
			}
		});
		valueSlider.setValue(0);
		valueSlider.setMinimum(-100);
		valueSlider.setToolTipText("Value");
		valueSlider.setPaintTicks(true);
		valueSlider.setPaintLabels(true);
		valueSlider.setBounds(180, 145, 200, 26);
		contentPanel.add(valueSlider);

		JLabel lblPreview = new JLabel("Preview");
		lblPreview.setBounds(10, 169, 46, 14);
		contentPanel.add(lblPreview);

		hueSpinner = new JSpinner();
		hueSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				hueShift = (Integer) hueSpinner.getValue();
				if (hueSlider != null) {
					hueSlider.setValue(hueShift);
				}
				updatePreview();
			}
		});
		hueSpinner.setModel(new SpinnerNumberModel(0, -180, 180, 1));
		hueSpinner.setBounds(390, 29, 82, 26);
		contentPanel.add(hueSpinner);
		JButton hueDefaultButton = new JButton();
		hueDefaultButton.setToolTipText("Set zero");
		hueDefaultButton.setBounds(475, 29, 26, 26);
		hueDefaultButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("left_arrow.png")));
		contentPanel.add(hueDefaultButton);
		hueDefaultButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				hueShift = 0;
				hueSlider.setValue(0);
				hueSpinner.setValue(0);
				updatePreview();
			}
		});

		saturationSpinner = new JSpinner();
		saturationSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				satShift = (Integer) saturationSpinner.getValue();
				if (saturationSlider != null) {
					saturationSlider.setValue(satShift);
				}
				updatePreview();
			}
		});
		saturationSpinner.setModel(new SpinnerNumberModel(0, -100, 100, 1));
		saturationSpinner.setBounds(392, 85, 80, 26);
		contentPanel.add(saturationSpinner);
		JButton satDefaultButton = new JButton();
		satDefaultButton.setToolTipText("Set zero");
		satDefaultButton.setBounds(475, 85, 26, 26);
		satDefaultButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("left_arrow.png")));
		contentPanel.add(satDefaultButton);
		satDefaultButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				satShift = 0;
				saturationSlider.setValue(0);
				saturationSpinner.setValue(0);
				updatePreview();
			}
		});

		valueSpinner = new JSpinner();
		valueSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				valShift = (Integer) valueSpinner.getValue();
				if (valueSlider != null) {
					valueSlider.setValue(valShift);
				}
				updatePreview();
			}
		});
		valueSpinner.setModel(new SpinnerNumberModel(0, -100, 100, 1));
		valueSpinner.setBounds(390, 141, 82, 27);
		contentPanel.add(valueSpinner);
		JButton valDefaultButton = new JButton();
		valDefaultButton.setToolTipText("Set zero");
		valDefaultButton.setBounds(475, 141, 26, 26);
		valDefaultButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("left_arrow.png")));
		contentPanel.add(valDefaultButton);
		valDefaultButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				valShift = 0;
				valueSlider.setValue(0);
				valueSpinner.setValue(0);
				updatePreview();
			}
		});

		progressBar = new JProgressBar();
		progressBar.setBounds(10, 186, 462, 14);
		progressBar.setVisible(false);
		contentPanel.add(progressBar);

		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);

		JButton okButton = new JButton("OK");
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onOk();
			}
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				onCancel();
			}
		});
	}

	private void onOk() {
		final BufferedImage clonedImage = ImageUtils.deepCopy(originalImage);
		progressBar.setMaximum(clonedImage.getHeight());
		progressBar.setMinimum(0);
		progressBar.setVisible(true);
		Thread thread = new Thread() {
			@Override
			public void run() {
				for (int y = 0; y < clonedImage.getHeight(); y++) {
					for (int x = 0; x < clonedImage.getWidth(); x++) {
						clonedImage.setRGB(x, y, getChangedRGB(clonedImage.getRGB(x, y)));
					}
					progressBar.setValue(y + 1);
				}
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

		int imageWidth = 0;
		int imageHeight = 0;

		int originalWidth = originalImage.getWidth(this);
		int originalHeight = originalImage.getHeight(this);
		if ((double) originalWidth / (double) originalHeight > (double) panelWidth / (double) panelHeight) {
			imageWidth = panelWidth;
			imageHeight = originalHeight * panelWidth / originalWidth;
		} else {
			imageHeight = panelHeight;
			imageWidth = originalWidth * panelHeight / originalHeight;
		}

		BufferedImage scaledImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics g = scaledImage.createGraphics();
		g.drawImage(originalImage, 0, 0, imageWidth, imageHeight, null);
		g.dispose();

		// BufferedImage scaledImage = ImageUtils
		// .toBufferedImage(originalImage.getScaledInstance(imageWidth, imageHeight,
		// BufferedImage.SCALE_FAST));

		for (int y = 0; y < scaledImage.getHeight(); y++) {
			for (int x = 0; x < scaledImage.getWidth(); x++) {
				scaledImage.setRGB(x, y, getChangedRGB(scaledImage.getRGB(x, y)));
			}
		}

		imagePanel.setImage(scaledImage);
	}

	private int getChangedRGB(int rgb) {
		PixelHSV hsv = new PixelHSV(rgb);
		double hue = hsv.getHue();
		double sat = hsv.getSaturation();
		double val = hsv.getValue();

		hue += (double) hueShift;
		hue = hue >= 360.0 ? hue - 360.0 : hue;
		hue = hue < 0.0 ? hue + 360.0 : hue;

		if (satShift >= 0) {
			sat += (1.0 - sat) * (double) satShift / 100.0;
		} else {
			sat += sat * (double) satShift / 100.0;
		}

		if (valShift >= 0) {
			val += (1.0 - val) * (double) valShift / 100.0;
		} else {
			val += val * (double) valShift / 100.0;
		}

		hsv.setHue(hue);
		hsv.setSaturation(sat);
		hsv.setValue(val);

		return hsv.getRGB();
	}
}
