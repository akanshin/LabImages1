package ru.artemiyk.labimages.ui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.SystemColor;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import ru.artemiyk.labimages.pixelutils.PixelCIELAB;
import ru.artemiyk.labimages.pixelutils.PixelHSV;
import ru.artemiyk.labimages.pixelutils.PixelRGB;

public class StatusBar extends JPanel {
	private static final long serialVersionUID = 1L;

	private JProgressBar progressBar;
	
	private JLabel mouseValue;
	private JLabel selectionValue;

	private JLabel valueRGB;
	private JLabel valueHSV;
	private JLabel valueLAB;

	/**
	 * Create the panel.
	 */
	public StatusBar() {
		setPreferredSize(new Dimension(10, 23));
		GridLayout layout = new GridLayout(0, 6, 0, 0);
		setLayout(layout);
		setBackground(SystemColor.control);
		
		mouseValue = new JLabel("Ready");
		this.add(mouseValue);

		selectionValue = new JLabel("");
		this.add(selectionValue);

		valueRGB = new JLabel("");
		this.add(valueRGB);

		valueHSV = new JLabel("");
		this.add(valueHSV);

		valueLAB = new JLabel("");
		this.add(valueLAB);
		
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setVisible(false);
		this.add(progressBar);
		
		this.setOpaque(false);
	}

	public void setRGB(PixelRGB rgb) {
		valueRGB.setText(String.format("RGB: (%d, %d, %d)", rgb.getRed(), rgb.getGreen(), rgb.getBlue()));
	}

	public void setHSV(PixelHSV hsv) {
		valueHSV.setText(String.format("HSV: (%d, %d, %d)", (int) hsv.getHue(), (int) (hsv.getSaturation() * 100),
				(int) (hsv.getValue() * 100)));
	}

	public void setCIELAB(PixelCIELAB cieLab) {
		valueLAB.setText(
				String.format("LAB: (%d, %d, %d)", (int) cieLab.getL(), (int) cieLab.getA(), (int) cieLab.getB()));
	}

	public void setMousePoint(int mouseX, int mouseY) {
		mouseValue.setText(String.format("Cursor: %d x %d", mouseX, mouseY));
	}

	public void setSelectedPoint(int pointX, int pointY) {
		selectionValue.setText(String.format("Point: %d x %d", pointX, pointY));
	}

	public void setSelection(int x1, int y1, int x2, int y2) {
		selectionValue.setText(String.format("Selection: %d x %d -- %d x %d", x1, y1, x2, y2));
	}

	public void clearSelection() {
		selectionValue.setText("");
		if (valueRGB.getText().isEmpty()) {
			valueRGB.setText("");
		}
		if (valueHSV.getText().isEmpty()) {
			valueHSV.setText("");
		}
		if (valueLAB.getText().isEmpty()) {
			valueLAB.setText("");
		}
	}

	public void clearPixelInfo() {
		valueRGB.setText("");
		valueHSV.setText("");
		valueLAB.setText("");
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}

	public void setProgressBar(JProgressBar progressBar) {
		this.progressBar = progressBar;
	}
}
