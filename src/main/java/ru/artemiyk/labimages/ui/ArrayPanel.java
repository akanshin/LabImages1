package ru.artemiyk.labimages.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ru.artemiyk.labimages.pixelutils.PixelCIELAB;
import ru.artemiyk.labimages.pixelutils.PixelHSV;

public class ArrayPanel extends JPanel implements AdjustmentListener {
	private static final long serialVersionUID = 1L;

	private JScrollPane scrollPane;

	private BufferedImage image;
	private BufferedImage subimage;
	private int x1, y1, x2, y2;
	private final int pixelSize = 70;
	private EDrawableInfo info = EDrawableInfo.eRGB;

	public enum EDrawableInfo {
		eRGB, eHSV, eLAB
	};

	public ArrayPanel() {

	}

	public void setDrawableInfo(EDrawableInfo info) {
		this.info = info;
		this.repaint();
	}

	public void setImage(BufferedImage image, int x1, int y1, int x2, int y2) {
		this.image = image;
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;

		subimage = image.getSubimage(x1, y1, x2 - x1, y2 - y1);
		this.setPreferredSize(new Dimension(subimage.getWidth() * pixelSize, subimage.getHeight() * pixelSize));

		this.repaint();
	}

	public void paintComponent(Graphics g0) {
		super.paintComponent(g0);
		Graphics2D g = (Graphics2D) g0;
		if (g == null) {
			return;
		}

		g.clearRect(0, 0, this.getWidth(), this.getHeight());
		g.drawImage(subimage, 0, 0, subimage.getWidth() * pixelSize, subimage.getHeight() * pixelSize, null);

		Rectangle visibleRect = null;
		if (scrollPane != null) {
			visibleRect = scrollPane.getViewport().getVisibleRect();
			visibleRect.x = scrollPane.getHorizontalScrollBar().getValue();
			visibleRect.y = scrollPane.getVerticalScrollBar().getValue();
		}

		if (visibleRect == null) {
			visibleRect = new Rectangle(0, 0, this.getWidth(), this.getHeight());
		}

		int x1Visible = visibleRect.x / pixelSize;
		int y1Visible = visibleRect.y / pixelSize;
		int x2Visible = (visibleRect.x + visibleRect.width) / pixelSize + 1;
		int y2Visible = (visibleRect.y + visibleRect.height) / pixelSize + 1;

		x2Visible = (x1 + x2Visible) > x2 ? x2 - x1 : x2Visible;
		y2Visible = (y1 + y2Visible) > y2 ? y2 - y1 : y2Visible;

		if (info == EDrawableInfo.eRGB) {
			int y0 = y1Visible * pixelSize;
			for (int y = y1Visible; y < y2Visible; y++, y0 += pixelSize) {
				int x0 = x1Visible * pixelSize;
				for (int x = x1Visible; x < x2Visible; x++, x0 += pixelSize) {
					Color pixelColor = new Color(image.getRGB(x1 + x, y1 + y));

					g.setColor(getContrastColor(pixelColor));
					drawPixelBorder(g, x0, y0);
					drawString(g, String.format("(%d, %d)\nR = %d\nG = %d\nB = %d", x1 + x, y1 + y, pixelColor.getRed(),
							pixelColor.getGreen(), pixelColor.getBlue()), x0 + 5, y0 + 5);
				}
			}
		} else if (info == EDrawableInfo.eHSV) {
			int y0 = y1Visible * pixelSize;
			for (int y = y1Visible; y < y2Visible; y++, y0 += pixelSize) {
				int x0 = x1Visible * pixelSize;
				for (int x = x1Visible; x < x2Visible; x++, x0 += pixelSize) {
					Color pixelColor = new Color(image.getRGB(x1 + x, y1 + y));
					PixelHSV hsv = new PixelHSV(pixelColor);

					g.setColor(getContrastColor(pixelColor));
					drawPixelBorder(g, x0, y0);
					drawString(g, String.format("(%d, %d)\nH = %d\nS = %d\nV = %d", x1 + x, y1 + x, (int) hsv.getHue(),
							(int) (hsv.getSaturation() * 100), (int) (hsv.getValue() * 100)), x0 + 5, y0 + 5);
				}
			}
		} else if (info == EDrawableInfo.eLAB) {
			int y0 = y1Visible * pixelSize;
			for (int y = y1Visible; y < y2Visible; y++, y0 += pixelSize) {
				int x0 = x1Visible * pixelSize;
				for (int x = x1Visible; x < x2Visible; x++, x0 += pixelSize) {
					Color pixelColor = new Color(image.getRGB(x1 + x, y1 + y));
					PixelCIELAB lab = new PixelCIELAB(pixelColor);

					g.setColor(getContrastColor(pixelColor));
					drawPixelBorder(g, x0, y0);
					drawString(g, String.format("(%d, %d)\nL = %d\nA = %d\nB = %d", x1 + x, y1 + y, (int) lab.getL(),
							(int) lab.getA(), (int) lab.getB()), x0 + 5, y0 + 5);
				}
			}
		}
	}

	public Color getContrastColor(Color color) {
		PixelHSV hsv = new PixelHSV(color);
		hsv.setHue(hsv.getHue() > 90 ? hsv.getHue() - 90 : hsv.getHue() + 270);
		hsv.setSaturation(0);
		hsv.setValue((hsv.getValue() * 100) >= 50 ? 0 : 100);
		return new Color(hsv.getRGB());
	}

	public void drawPixelBorder(Graphics g, int x, int y) {
		g.drawRect(x, y, pixelSize, pixelSize);
	}

	public void drawString(Graphics g, String text, int x, int y) {
		for (String line : text.split("\n"))
			g.drawString(line, x, y += g.getFontMetrics().getHeight());
	}

	public void setScrollPane(JScrollPane scrollPane) {
		this.scrollPane = scrollPane;
		scrollPane.getHorizontalScrollBar().addAdjustmentListener(this);
		scrollPane.getVerticalScrollBar().addAdjustmentListener(this);
	}

	public void adjustmentValueChanged(AdjustmentEvent arg0) {
		this.repaint();
	}
}
