package ru.artemiyk.labimages.ui;

import javax.swing.JSlider;

public class JDoubleSlider extends JSlider {
	private static final long serialVersionUID = 1L;
	private double step;
	private double min;
	private double max;

	public JDoubleSlider() {

	}

	public double getStep() {
		return step;
	}

	public void setStep(double step) {
		super.setMaximum(0);
		super.setMaximum((int) ((max - min) / step));
		this.step = step;
	}

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public double getDoubleValue() {
		return min + step * super.getValue();
	}

	public void setDoubleValue(double value) {
		super.setValue((int) ((value - min) / step));
	}

}
