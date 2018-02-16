package ru.artemiyk.labimages.pixelutils;

import java.awt.Color;

public class PixelRGB {
	int red;
	int green;
	int blue;
	
	int rgb;
	
	public PixelRGB() {

	}

	public PixelRGB(int rgb) {
		this.rgb = rgb;
		red = (rgb & 0x00ff0000) >> 16;
		green = (rgb & 0x0000ff00) >> 8;
		blue = rgb & 0x000000ff;
	}

	public PixelRGB(Color color) {
		red = color.getRed();
		green = color.getGreen();
		blue = color.getBlue();
		rgb = color.getRGB();
	}

	public int getRed() {
		return red;
	}

	public void setRed(int red) {
		this.red = red;
		rgb |= (red << 16) & 0x00ff0000;
	}

	public int getGreen() {
		return green;
	}

	public void setGreen(int green) {
		this.green = green;
		rgb |= (green << 8) & 0x0000ff00;
	}

	public int getBlue() {
		return blue;
	}

	public void setBlue(int blue) {
		this.blue = blue;
		rgb |= blue & 0x000000ff;
	}

	public int getRGB() {
		return rgb;
	}

	public void setRGB(int rgb) {
		this.rgb = rgb;
	}
}
