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
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class IntegerParameterPanel extends JPanel {
  private static final long serialVersionUID = 1L;

  private JSlider slider;
  private JSpinner spinner;
  private JButton defaultButton;

  private List<ChangeListener> changeListenerList = new ArrayList<ChangeListener>();

  private int value;
  private int defaultValue;
  private int minimum;
  private int maximum;
  private int step;

  public IntegerParameterPanel(String parameterName, int value, int defaultValue, int minimum, int maximum, int step) {
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
    slider = new JSlider();
    slider.setBounds(10, 19, 260, 23);
    slider.setPaintTicks(true);
    slider.setMinimum(0);
    slider.setMaximum((maximum - minimum) / step + 1);
    slider.setValue((value - minimum) / step);
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
        value = (Integer) spinner.getValue();
        if (slider != null) {
          slider.setValue((value - minimum) / step);
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
          slider.setValue((value - minimum) / step);
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

  public int getValue() {
    return value;
  }
}
