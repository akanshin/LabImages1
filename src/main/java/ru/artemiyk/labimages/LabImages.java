package ru.artemiyk.labimages;

import java.io.File;

import javax.swing.UIManager;

import ru.artemiyk.labimages.ui.MainWindow;

public class LabImages {

	private static LabImages instance = null;

	private MainWindow window = null;

	public static void main(String[] args) {
		LabImages.getInstance();
	}

	public LabImages() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			
		}

		this.window = new MainWindow("LabImages 1", 1280, 720);
		this.window.setVisible(true);
		this.window.getImagePanel().loadImage(new File(getClass().getClassLoader().getResource("lenna.png").getFile()));
	}

	public static LabImages getInstance() {
		if (instance == null) {
			instance = new LabImages();
		}
		return instance;
	}

	public MainWindow getMainWindow() {
		return this.window;
	}
}