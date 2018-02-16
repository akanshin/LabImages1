package ru.artemiyk.labimages.ui.plot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

public class ArrayFrame extends JFrame implements ComponentListener {
	private static final long serialVersionUID = 1L;

	private JPanel contentPane;

	private JRadioButton rgbRadioButton;
	private JRadioButton hsvRadioButton;
	private JRadioButton labRadioButton;
	private ArrayPanel arrayPanel;
	private JScrollPane scrollPane;

	public ArrayFrame() {
		this.setBounds(100, 100, 400, 400);
		this.setMinimumSize(new Dimension(400, 400));
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		setContentPane(contentPane);

		JToolBar toolBar = new JToolBar();
		toolBar.setBackground(Color.WHITE);
		toolBar.setFloatable(false);
		contentPane.add(toolBar, BorderLayout.NORTH);

		rgbRadioButton = new JRadioButton("RGB");
		rgbRadioButton.setBackground(Color.WHITE);
		rgbRadioButton.setSelected(true);
		rgbRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				arrayPanel.setDrawableInfo(ArrayPanel.EDrawableInfo.eRGB);
			}
		});
		toolBar.add(rgbRadioButton);

		hsvRadioButton = new JRadioButton("HSV");
		hsvRadioButton.setBackground(Color.WHITE);
		hsvRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				arrayPanel.setDrawableInfo(ArrayPanel.EDrawableInfo.eHSV);
			}
		});
		toolBar.add(hsvRadioButton);

		labRadioButton = new JRadioButton("LAB");
		labRadioButton.setBackground(Color.WHITE);
		labRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				arrayPanel.setDrawableInfo(ArrayPanel.EDrawableInfo.eLAB);
			}
		});
		toolBar.add(labRadioButton);

		ButtonGroup group = new ButtonGroup();
		group.add(rgbRadioButton);
		group.add(hsvRadioButton);
		group.add(labRadioButton);

		arrayPanel = new ArrayPanel();
		scrollPane = new JScrollPane(arrayPanel);
		arrayPanel.setScrollPane(scrollPane);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		contentPane.add(scrollPane, BorderLayout.CENTER);
	}
	
	@Override
	public void repaint() {
		super.repaint();
		scrollPane.updateUI();
		arrayPanel.repaint();
		scrollPane.repaint();
	}

	public void setImage(BufferedImage image, int x1, int y1, int x2, int y2) {
		arrayPanel.setImage(image, x1, y1, x2, y2);
		this.repaint();
	}

	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void componentMoved(ComponentEvent arg0) {
		// TODO Auto-generated method stub
	}

	public void componentResized(ComponentEvent arg0) {
		this.repaint();
	}

	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
