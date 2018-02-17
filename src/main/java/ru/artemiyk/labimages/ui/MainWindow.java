package ru.artemiyk.labimages.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import ru.artemiyk.labimages.ui.transformator.HSVTranformatorDialog;

public class MainWindow extends JFrame {
	/**
	 * Default serial verstion uid
	 */
	private static final long serialVersionUID = 1L;

	private int width;
	private int height;
	private int displayWidth;
	private int displayHeight;

	private JToolBar toolBar;
	
	private ImagePanel imagePanel;
	private StatusBar statusBar;
	
	private JButton openButton;
	private JButton defaultViewButton;
	private JButton expandButton;
	private JButton showMassiveButton;
	private JButton showHistButton;
	private JButton changeHSVButton;
	private JButton setDefaultImageButton;
	private JButton selectAllButton;
	private JButton questionButton;
	
	private static String questionString = "Click left mouse button to select pixel\n"
			+ "Press left button and drag mouse to select area\n"
			+ "Click right button to clear selection\n"
			+ "Press wheel and drag mouse to move image\n"
			+ "Rotate mouse wheel to zoom\n\n"
			+ "Good luck! :)";
	
	private final JFileChooser fileChooser;

	public MainWindow(String windowName, int width, int height) {
		super(windowName);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.width = width;
		this.height = height;

		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		displayWidth = gd.getDisplayMode().getWidth();
		displayHeight = gd.getDisplayMode().getHeight();
		this.setBounds(displayWidth / 2 - this.width / 2, displayHeight / 2 - this.height / 2, this.width, this.height);

		this.buildToolBar();
		this.buildImagePanel();
		
		statusBar = new StatusBar();
		this.getContentPane().add(statusBar, BorderLayout.SOUTH);
		
		fileChooser = new JFileChooser();
	}
	
	public void buildImagePanel() {
		imagePanel = new ImagePanel();
		imagePanel.setBorder(BorderFactory.createBevelBorder(1));
		this.getContentPane().add(imagePanel, BorderLayout.CENTER);
	}
	
	public ImagePanel getImagePanel() {
		return imagePanel;
	}

	private void buildToolBar() {
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		//toolBar.setBackground(Color.WHITE);
		
		openButton = new JButton();
		//openButton.setBackground(Color.WHITE);
		openButton.setToolTipText("Open image");
		toolBar.add(openButton);
		openButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("open.png")));
		openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onOpen();
			}
		});
		
		toolBar.addSeparator();
		
		defaultViewButton = new JButton();
		//defaultViewButton.setBackground(Color.WHITE);
		defaultViewButton.setToolTipText("Default scale");
		toolBar.add(defaultViewButton);
		defaultViewButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("default_view.png")));
		defaultViewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				imagePanel.setDefaultView();
			}
		});
		
		expandButton = new JButton();
		//expandButton.setBackground(Color.WHITE);
		expandButton.setToolTipText("Scale to window");
		toolBar.add(expandButton);
		expandButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("expand.png")));
		expandButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				imagePanel.setAllView();
			}
		});
		
		toolBar.addSeparator();
		
		selectAllButton = new JButton();
		//selectAllButton.setBackground(Color.WHITE);
		selectAllButton.setToolTipText("Select all");
		toolBar.add(selectAllButton);
		selectAllButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("select.png")));
		selectAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				imagePanel.selectAll();
			}
		});
		
		showMassiveButton = new JButton();
		//showMassiveButton.setBackground(Color.WHITE);
		showMassiveButton.setToolTipText("Show massive");
		toolBar.add(showMassiveButton);
		showMassiveButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("show_rgb.png")));
		showMassiveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				imagePanel.showMassive();
			}
		});
		
		showHistButton = new JButton();
		//showHistButton.setBackground(Color.WHITE);
		showHistButton.setToolTipText("Show histogram");
		toolBar.add(showHistButton);
		showHistButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("hist.png")));
		showHistButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				imagePanel.showHist();
			}
		});
		
		toolBar.addSeparator();
		
		changeHSVButton = new JButton();
		//changeHSVButton.setBackground(Color.WHITE);
		changeHSVButton.setToolTipText("Change HSV");
		toolBar.add(changeHSVButton);
		changeHSVButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("hsv.png")));
		changeHSVButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				HSVTranformatorDialog hsvDialog = new HSVTranformatorDialog(imagePanel.getImage());
				hsvDialog.setVisible(true);
			}
		});
		
		setDefaultImageButton = new JButton();
		//setDefaultImageButton.setBackground(Color.WHITE);
		setDefaultImageButton.setToolTipText("Default HSV");
		toolBar.add(setDefaultImageButton);
		setDefaultImageButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("set_default.png")));
		setDefaultImageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				imagePanel.setDefaultImage();
			}
		});
		
		toolBar.addSeparator();
		
		questionButton = new JButton();
		//questionButton.setBackground(Color.WHITE);
		questionButton.setToolTipText("If you don't know what to do, click!");
		toolBar.add(questionButton);
		questionButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("question.png")));
		questionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JOptionPane.showMessageDialog(MainWindow.this, questionString);
			}
		});
		
		Container contentPane = this.getContentPane();
	    contentPane.add(toolBar, BorderLayout.NORTH);
	}
	
	private void onOpen() {
		int returnVal = fileChooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			imagePanel.loadImage(fileChooser.getSelectedFile());
		}
	}
	
	public void showMessage(String message) {
		JOptionPane.showMessageDialog(this, message, "Message", JOptionPane.ERROR_MESSAGE);
	}
	
	public StatusBar getStatusBar() {
		return statusBar;
	}
	
	public void setEnableSelectionButtons(boolean enable) {
		showMassiveButton.setEnabled(enable);
		showHistButton.setEnabled(enable);
	}
}
