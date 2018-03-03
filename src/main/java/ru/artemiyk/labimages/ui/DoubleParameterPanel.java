package ru.artemiyk.labimages.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DoubleParameterPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	private JDoubleSlider slider;
	private JSpinner spinner;
	private JButton defaultButton;

	private List<ChangeListener> changeListenerList = new ArrayList<ChangeListener>();

	private double value;
	private double defaultValue;
	private double minimum;
	private double maximum;
	private double step;

	public DoubleParameterPanel(String parameterName, double value, double defaultValue, double minimum, double maximum,
			double step) {
		setBorder(new TitledBorder(null, parameterName, TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(null);

		this.value = value;
		this.defaultValue = defaultValue;
		this.minimum = minimum;
		this.maximum = maximum;
		this.step = step;

		buildSlider();
		buildSpinner();
		buildDefaultButton();
	}

	private void buildSlider() {
		slider = new JDoubleSlider();
		slider.setBounds(10, 19, 260, 23);
		slider.setPaintTicks(true);
		slider.setMin(minimum);
		slider.setMax(maximum);
		slider.setStep(step);
		slider.setDoubleValue(value);
		this.add(slider);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				int slValue = slider.getValue();
				value = minimum + step * slValue;

				if (spinner != null) {
					spinner.setValue(value);
				}

				updateListeners(arg0);
			}
		});
	}

	private void buildSpinner() {
		spinner = new JSpinner();
		spinner.setBounds(new Rectangle(10, 10, 70, 20));
		spinner.setBounds(280, 17, 70, 20);
		spinner.setMaximumSize(new Dimension(70, 20));
		spinner.setMinimumSize(new Dimension(70, 20));
		spinner.setModel(new SpinnerNumberModel(value, minimum, maximum, step));
		this.add(spinner);
		spinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				value = (Double) spinner.getValue();
				if (slider != null) {
					slider.setDoubleValue(value);
				}

				updateListeners(arg0);
			}
		});
	}

	private void buildDefaultButton() {
		defaultButton = new JButton("");
		defaultButton.setBounds(new Rectangle(360, 17, 20, 20));
		defaultButton.setMinimumSize(new Dimension(16, 16));
		defaultButton.setMaximumSize(new Dimension(16, 16));
		defaultButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("left_arrow.png")));
		this.add(defaultButton);
		defaultButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				value = defaultValue;
				if (slider != null) {
					slider.setDoubleValue(value);
				}
				if (spinner != null) {
					spinner.setValue(value);
				}

				updateListeners(null);
			}
		});
	}

	private void updateListeners(ChangeEvent changeEvent) {
		for (ChangeListener listener : changeListenerList) {
			listener.stateChanged(changeEvent);
		}
	}

	public void addChangeListener(ChangeListener changeListener) {
		changeListenerList.add(changeListener);
	}

	@Override
	public void setBackground(Color color) {
		super.setBackground(color);
		if (slider != null) {
			slider.setBackground(color);
		}
		if (spinner != null) {
			spinner.setBackground(color);
		}
		if (defaultButton != null) {
			defaultButton.setBackground(color);
		}
	}

	public double getValue() {
		return value;
	}
}
