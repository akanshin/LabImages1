package ru.artemiyk.labimages.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import ru.artemiyk.labimages.LabImages;
import ru.artemiyk.labimages.action.Action;
import ru.artemiyk.labimages.pixelutils.PixelCIELAB;
import ru.artemiyk.labimages.pixelutils.PixelHSV;
import ru.artemiyk.labimages.pixelutils.PixelRGB;

public class ImagePanel extends JPanel
		implements ComponentListener, MouseListener, MouseMotionListener, MouseWheelListener {
	private static final long serialVersionUID = 1L;

	private BufferedImage image;
	private BufferedImage viewedImage;
	private BufferedImage viewedImageCopy;
	private int imageX, imageY;
	private int imageWidth, imageHeight;
	private int panelWidth, panelHeight;

	private double scaleDelta = 0.1;

	private int mouseX, mouseY;

	private boolean isPointSelected;
	private int pointX, pointY;

	private boolean isSectionSelected;
	private int selectionX, selectionY;
	private int selectionWidth, selectionHeight;
	private boolean selectionChanged = false;

	private HistogramFrame histogram;
	private ArrayFrame array;

	private BufferedImage histPrevImage;
	private int histPrevX;
	private int histPrevY;
	private int histPrevWidth;
	private int histPrevHeight;

	private enum EAction {
		eMove, eSelect, eTouch, eNoAction
	}

	private EAction action;

	public ImagePanel() {
		super();
		this.addComponentListener(this);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);
		this.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
	}

	public void paintComponent(Graphics g) {
		if (g == null) {
			return;
		}

		g.setColor(new Color(80, 85, 90));
		g.fillRect(0, 0, this.getWidth(), this.getHeight());

		if (viewedImage == null) {
			return;
		}

		g.drawImage(viewedImage, imageX, imageY, imageWidth, imageHeight, this);

		if (isPointSelected) {
			drawTarget(g, pointX, pointY);
		} else if (isSectionSelected) {
			drawSelection(g, selectionX, selectionY, selectionWidth, selectionHeight);
		}

		LabImages.getInstance().getMainWindow()
				.setEnableSelectionButtons(isSectionSelected && (selectionWidth != 0) && (selectionHeight != 0));
	}

	public void drawTarget(Graphics g, int x, int y) {
		if (x < 0 || x > viewedImage.getWidth()) {
			return;
		}
		if (y < 0 || y > viewedImage.getHeight()) {
			return;
		}

		int nativeX = getNativePixelX(x);
		int nativeY = getNativePixelY(y);
		int nativeNextX = getNativePixelX(x + 1);
		int nativeNextY = getNativePixelY(y + 1);

		int lineLength = getNativePixelX(3) - getNativePixelX(0);

		Graphics2D g2d = (Graphics2D) g;
		g2d.setStroke(new BasicStroke(2));

		g2d.setColor(new Color(255, 255, 255));
		g2d.drawLine((nativeNextX + nativeX) / 2, nativeY, (nativeNextX + nativeX) / 2, nativeY - lineLength);
		g2d.drawLine((nativeNextX + nativeX) / 2, nativeNextY, (nativeNextX + nativeX) / 2, nativeNextY + lineLength);
		g2d.drawLine(nativeX - lineLength, (nativeNextY + nativeY) / 2, nativeX, (nativeNextY + nativeY) / 2);
		g2d.drawLine(nativeNextX + lineLength, (nativeNextY + nativeY) / 2, nativeNextX, (nativeNextY + nativeY) / 2);

		g2d.setColor(new Color(255, 0, 0));
		g2d.drawRect(nativeX, nativeY, nativeNextX - nativeX, nativeNextY - nativeY);
	}

	public void drawSelection(Graphics g, int x, int y, int width, int height) {
		if (width == 0 || height == 0) {
			return;
		}

		if (width < 0) {
			x += width;
			width = -width;
		}
		if (height < 0) {
			y += height;
			height = -height;
		}

		g.setColor(new Color(0, 0, 255, 100));
		g.drawRect(getNativePixelX(x), getNativePixelY(y), getNativePixelX(x + width) - getNativePixelX(x),
				getNativePixelY(y + height) - getNativePixelY(y));
		g.setColor(new Color(0, 0, 255, 50));
		g.fillRect(getNativePixelX(x), getNativePixelY(y), getNativePixelX(x + width) - getNativePixelX(x),
				getNativePixelY(y + height) - getNativePixelY(y));

		if (histogram != null && histogram.isVisible() && selectionChanged) {
			this.showHist();
			selectionChanged = false;
		}

		if (array != null && array.isVisible()) {
			this.showMassive();
		}

		LabImages.getInstance().getMainWindow().getStatusBar().clearPixelInfo();
		LabImages.getInstance().getMainWindow().getStatusBar().setSelection(x, y, x + width, y + height);
	}

	public void selectAll() {
		isPointSelected = false;
		isSectionSelected = true;
		selectionChanged = true;
		selectionX = 0;
		selectionY = 0;
		selectionWidth = viewedImage.getWidth();
		selectionHeight = viewedImage.getHeight();

		this.repaint();
	}

	public void setDefaultView() {
		if (viewedImage == null) {
			imageX = 0;
			imageY = 0;
			imageWidth = 0;
			imageHeight = 0;

			return;
		}

		imageWidth = viewedImage.getWidth(this);
		imageHeight = viewedImage.getHeight(this);
		panelWidth = this.getWidth();
		panelHeight = this.getHeight();

		imageX = panelWidth / 2 - imageWidth / 2;
		imageY = panelHeight / 2 - imageHeight / 2;

		this.repaint();
	}

	public void setAllView() {
		if (viewedImage == null) {
			imageX = 0;
			imageY = 0;
			imageWidth = 0;
			imageHeight = 0;

			return;
		}

		panelWidth = this.getWidth();
		panelHeight = this.getHeight();

		int originalWidth = viewedImage.getWidth(this);
		int originalHeight = viewedImage.getHeight(this);
		if ((double) originalWidth / (double) originalHeight > (double) panelWidth / (double) panelHeight) {
			imageWidth = panelWidth;
			imageHeight = originalHeight * panelWidth / originalWidth;
		} else {
			imageHeight = panelHeight;
			imageWidth = originalWidth * panelHeight / originalHeight;
		}
		imageX = panelWidth / 2 - imageWidth / 2;
		imageY = panelHeight / 2 - imageHeight / 2;

		this.repaint();
	}

	public void loadImage(File imageFile) {
		isSectionSelected = false;
		isPointSelected = false;
		if (histogram != null) {
			histogram.setVisible(false);
		}

		try {
			if (imageFile == null) {
				throw new Exception();
			}
			image = ImageIO.read(imageFile);

			if (image == null) {
				throw new Exception();
			}
		} catch (Exception e) {
			LabImages.getInstance().getMainWindow().showMessage("Invalid file");
		}

		viewedImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		Graphics g = (Graphics) viewedImage.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();

		this.setAllView();
	}
	
	public void saveImage(String format, File fileToSave) {
		try {
			if (fileToSave == null) {
				throw new Exception();
			}
			
			ImageIO.write(viewedImage, format, fileToSave);
			
		} catch(Exception e) {
			LabImages.getInstance().getMainWindow().showMessage("Invalid file");
		}
	}

	public void componentHidden(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {
	}

	public void componentShown(ComponentEvent e) {
	}

	public void componentResized(ComponentEvent e) {
		int width = this.getWidth();
		int height = this.getHeight();

		imageX = width / 2 - imageWidth / 2;
		imageY = height / 2 - imageHeight / 2;

		this.repaint();
	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public void mousePressed(MouseEvent arg0) {
		mouseX = arg0.getX();
		mouseY = arg0.getY();

		switch (arg0.getButton()) {
		case MouseEvent.BUTTON1: {
			action = EAction.eSelect;
			isPointSelected = false;
			isSectionSelected = true;
			if (mouseX < imageX || mouseX > imageX + imageWidth) {
				isSectionSelected = false;
			} else {
				selectionX = getImagePixelX(mouseX);
			}
			if (mouseY < imageY || mouseY > imageY + imageHeight) {
				isSectionSelected = false;
			} else {
				selectionY = getImagePixelY(mouseY);
			}
			selectionWidth = 0;
			selectionHeight = 0;

			break;
		}
		case MouseEvent.BUTTON2: {
			action = EAction.eMove;
			break;
		}
		case MouseEvent.BUTTON3: {
			action = EAction.eNoAction;
			break;
		}
		}

		this.repaint();
	}

	public void mouseReleased(MouseEvent arg0) {
		action = EAction.eNoAction;

		mouseX = arg0.getX();
		mouseY = arg0.getY();

		this.repaint();
	}

	public void mouseMoved(MouseEvent arg0) {
		mouseX = arg0.getX();
		mouseY = arg0.getY();

		if (viewedImage != null) {
			int imageMouseX = getImagePixelX(mouseX);
			int imageMouseY = getImagePixelY(mouseY);

			if (imageMouseX < 0) {
				imageMouseX = 0;
			} else if (imageMouseX > viewedImage.getWidth()) {
				imageMouseX = viewedImage.getWidth();
			}

			if (imageMouseY < 0) {
				imageMouseY = 0;
			} else if (imageMouseY > viewedImage.getHeight()) {
				imageMouseY = viewedImage.getHeight();
			}
			LabImages.getInstance().getMainWindow().getStatusBar().setMousePoint(imageMouseX, imageMouseY);
		}

		this.repaint();
	}

	public void mouseClicked(MouseEvent arg0) {
		mouseX = arg0.getX();
		mouseY = arg0.getY();

		if (arg0.getButton() == MouseEvent.BUTTON3) {
			action = EAction.eNoAction;
			isSectionSelected = false;
			isPointSelected = false;
			LabImages.getInstance().getMainWindow().getStatusBar().clearSelection();
		} else if (arg0.getButton() == MouseEvent.BUTTON1) {
			isPointSelected = true;
			isSectionSelected = false;
			pointX = getImagePixelX(mouseX);
			pointY = getImagePixelY(mouseY);

			if (pointX >= 0 && pointX < viewedImage.getWidth() && pointY >= 0 && pointY < viewedImage.getHeight()) {
				LabImages.getInstance().getMainWindow().getStatusBar().clearSelection();
				LabImages.getInstance().getMainWindow().getStatusBar().setSelectedPoint(pointX, pointY);
				LabImages.getInstance().getMainWindow().getStatusBar().setRGB(getRGB(pointX, pointY));
				LabImages.getInstance().getMainWindow().getStatusBar().setHSV(getHSV(pointX, pointY));
				LabImages.getInstance().getMainWindow().getStatusBar().setCIELAB(getCIELAB(pointX, pointY));
			}

		}

		this.repaint();
	}

	public void mouseDragged(MouseEvent arg0) {
		int deltaX = arg0.getX() - mouseX;
		int deltaY = arg0.getY() - mouseY;

		mouseX = arg0.getX();
		mouseY = arg0.getY();

		switch (action) {
		case eNoAction: {
			break;
		}
		case eSelect: {
			if (mouseX < imageX) {
				selectionWidth = getImagePixelX(imageX) - selectionX;
			} else if (mouseX > imageX + imageWidth) {
				selectionWidth = getImagePixelX(imageX + imageWidth) - selectionX;
			} else {
				selectionWidth = getImagePixelX(mouseX) - selectionX;
			}

			if (mouseY < imageY) {
				selectionHeight = getImagePixelY(imageY) - selectionY;
			} else if (mouseY > imageY + imageHeight) {
				selectionHeight = getImagePixelY(imageY + imageHeight) - selectionY;
			} else {
				selectionHeight = getImagePixelY(mouseY) - selectionY;
			}

			selectionChanged = true;

			break;
		}
		case eMove: {
			imageX += deltaX;
			imageY += deltaY;

			break;
		}
		default:
			break;
		}

		this.repaint();
	}

	public void mouseWheelMoved(MouseWheelEvent arg0) {
		int rotation = arg0.getWheelRotation();

		if (rotation > 0 && (imageWidth < 100 || imageHeight < 100)) {
			return;
		} else if (rotation < 0 && (getNativePixelX(1) - getNativePixelX(0) > 20)) {
			return;
		}

		imageX = imageX + (int) (rotation * (mouseX - imageX) * scaleDelta);
		imageY = imageY + (int) (rotation * (mouseY - imageY) * scaleDelta);

		imageWidth += (int) (-rotation * imageWidth * scaleDelta);
		imageHeight += (int) (-rotation * imageHeight * scaleDelta);

		this.repaint();
	}

	private int getImagePixelX(int nativePixelX) {
		double dImageX = (double) imageX;
		double dImageWidth = (double) imageWidth;
		double dOriginalImageWidth = (double) viewedImage.getWidth(null);
		double x = ((double) nativePixelX - dImageX) / dImageWidth;
		return (int) (x * dOriginalImageWidth);
	}

	private int getNativePixelX(int imagePixelX) {
		double dImageWidth = (double) imageWidth;
		double dOriginalImageWidth = (double) viewedImage.getWidth(null);
		double x = ((double) imagePixelX) / dOriginalImageWidth;
		return imageX + (int) (x * dImageWidth);
	}

	private int getImagePixelY(int nativePixelY) {
		double dImageY = (double) imageY;
		double dImageHeight = (double) imageHeight;
		double dOriginalImageHeight = (double) viewedImage.getHeight(null);
		double y = ((double) nativePixelY - dImageY) / dImageHeight;
		return (int) (y * dOriginalImageHeight);
	}

	private int getNativePixelY(int imagePixelY) {
		double dImageHeight = (double) imageHeight;
		double dOriginalImageHeight = (double) viewedImage.getHeight(null);
		double y = ((double) imagePixelY) / dOriginalImageHeight;
		return imageY + (int) (y * dImageHeight);
	}

	public PixelRGB getRGB(int x, int y) {
		if (x < 0 || x > viewedImage.getWidth()) {
			return new PixelRGB();
		}
		if (y < 0 || y > viewedImage.getHeight()) {
			return new PixelRGB();
		}

		return new PixelRGB(viewedImage.getRGB(x, y));
	}

	public PixelHSV getHSV(int x, int y) {
		if (x < 0 || x > viewedImage.getWidth()) {
			return new PixelHSV();
		}
		if (y < 0 || y > viewedImage.getHeight()) {
			return new PixelHSV();
		}

		return new PixelHSV(viewedImage.getRGB(x, y));
	}

	public PixelCIELAB getCIELAB(int x, int y) {
		if (x < 0 || x > viewedImage.getWidth()) {
			return new PixelCIELAB();
		}
		if (y < 0 || y > viewedImage.getHeight()) {
			return new PixelCIELAB();
		}

		return new PixelCIELAB(viewedImage.getRGB(x, y));
	}

	public boolean isPixelSelected() {
		return isPointSelected;
	}

	public boolean isSectionSelected() {
		return isSectionSelected;
	}

	public int getSelectedPixelX() {
		return pointX;
	}

	public int getSelectedPixelY() {
		return pointY;
	}

	public int getSelectionX() {
		return selectionX;
	}

	public int getSelectionY() {
		return selectionY;
	}

	public int getSelectionWidth() {
		return selectionWidth;
	}

	public int getSelectionHeight() {
		return selectionHeight;
	}

	public void showMassive() {
		if (!isSectionSelected) {
			return;
		}

		if (array == null) {
			array = new ArrayFrame();
		}

		int x = selectionWidth > 0 ? selectionX : selectionX + selectionWidth;
		int y = selectionHeight > 0 ? selectionY : selectionY + selectionHeight;
		int width = selectionWidth > 0 ? selectionWidth : -selectionWidth;
		int height = selectionHeight > 0 ? selectionHeight : -selectionHeight;

		array.setImage(viewedImage, x, y, x + width, y + height);
		array.repaint();
		array.setVisible(true);
	}

	public void showHist() {
		if (!isSectionSelected) {
			return;
		}

		int x = selectionWidth > 0 ? selectionX : selectionX + selectionWidth;
		int y = selectionHeight > 0 ? selectionY : selectionY + selectionHeight;
		int width = selectionWidth > 0 ? selectionWidth : -selectionWidth;
		int height = selectionHeight > 0 ? selectionHeight : -selectionHeight;

		if (histogram == null) {
			histogram = new HistogramFrame();
		}

		if (histPrevImage == viewedImage && histPrevX == x && histPrevY == y && histPrevWidth == width
				&& histPrevHeight == height && histogram.isVisible()) {
			return;
		}

		histogram.setDataset(viewedImage, x, y, width, height);
		histogram.showHistogram();

		histPrevImage = viewedImage;
		histPrevX = x;
		histPrevY = y;
		histPrevWidth = width;
		histPrevHeight = height;
	}

	public void setImage(BufferedImage newImage) {
		viewedImage = newImage;
		viewedImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		Graphics g = (Graphics) viewedImage.createGraphics();
		g.drawImage(newImage, 0, 0, null);
		g.dispose();
		this.repaint();
	}

	public void setDefaultImage() {
		viewedImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		Graphics g = (Graphics) viewedImage.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		this.repaint();
	}

	public BufferedImage getImage() {
		return viewedImage;
	}

	private void copyImage() {
		viewedImageCopy = new BufferedImage(viewedImage.getWidth(), viewedImage.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Graphics g = viewedImageCopy.createGraphics();
		g.drawImage(viewedImage, 0, 0, null);
	}

	public void revertImageFilterChanges() {
		viewedImage = viewedImageCopy;
		repaint();
	}
	
	public void produceAction(Action action) {
		if (action == null) {
			return;
		}
		
		copyImage();
		
		action.produce(viewedImage);
		
		repaint();
	}
}
