package ru.artemiyk.labimages;

import java.io.File;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ru.artemiyk.labimages.ui.MainWindow;

public class LabImages {

	private static LabImages instance = null;

	private MainWindow window = null;

	public static void main(String[] args) {
		LabImages.getInstance();
	}

	public LabImages() {
		try {
			// Set cross-platform Java L&F (also called "Metal")
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (UnsupportedLookAndFeelException e) {
			// handle exception
		} catch (ClassNotFoundException e) {
			// handle exception
		} catch (InstantiationException e) {
			// handle exception
		} catch (IllegalAccessException e) {
			// handle exception
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