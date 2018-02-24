package ru.artemiyk.labimages.pixelutils;

import java.awt.Color;

public class PixelHSV {
	private double hue;
	private double saturation;
	private double value;

	public PixelHSV() {

	}

	public PixelHSV(int rgb) {
		int red = (rgb & 0x00ff0000) >> 16;
		int green = (rgb & 0x0000ff00) >> 8;
		int blue = rgb & 0x000000ff;
		calculateHSV(red, green, blue);
	}

	public PixelHSV(Color color) {
		int red = color.getRed();
		int green = color.getGreen();
		int blue = color.getBlue();
		calculateHSV(red, green, blue);
	}

	private void calculateHSV(int red, int green, int blue) {
		double dRed = (double) red / 255.0;
		double dGreen = (double) green / 255.0;
		double dBlue = (double) blue / 255.0;

		double max = Math.max(dRed, Math.max(dGreen, dBlue));
		double min = Math.min(dRed, Math.min(dGreen, dBlue));

		if (max == dRed && green >= blue) {
			hue = (int) (60 * (dGreen - dBlue) / (max - min));
		} else if (max == dRed && dGreen < dBlue) {
			hue = (int) (60 * (dGreen - dBlue) / (max - min) + 360);
		} else if (max == dGreen) {
			hue = (int) (60 * (dBlue - dRed) / (max - min) + 120);
		} else if (max == dBlue) {
			hue = (int) (60 * (dRed - dGreen) / (max - min) + 240);
		}

		saturation = 1.0 - min / max;
		value = max;
	}

	public double getHue() {
		return hue;
	}

	public void setHue(double hue) {
		this.hue = hue;
	}

	public double getSaturation() {
		return saturation;
	}

	public void setSaturation(double saturation) {
		this.saturation = saturation;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public int getRGB() {
		double c = value * saturation;
		double x = c * (1 - Math.abs((hue / 60) % 2 - 1));
		double m = value - c;

		double r = 0;
		double g = 0;
		double b = 0;

		if (0 <= hue && hue < 60) {
			r = c;
			g = x;
		} else if (60 <= hue && hue < 150) {
			r = x;
			g = c;
		} else if (120 <= hue && hue < 180) {
			g = c;
			b = x;
		} else if (180 <= hue && hue < 240) {
			g = x;
			b = c;
		} else if (240 <= hue && hue < 300) {
			r = x;
			b = c;
		} else if (300 <= hue && hue < 360) {
			r = c;
			b = x;
		}

		int red = (int) ((r + m) * 255);
		red = red > 255 ? 255 : red;
		red = red < 0 ? 0 : red;

		int green = (int) ((g + m) * 255);
		green = green > 255 ? 255 : green;
		green = green < 0 ? 0 : green;

		int blue = (int) ((b + m) * 255);
		blue = blue > 255 ? 255 : blue;
		blue = blue < 0 ? 0 : blue;


		return new Color(red, green, blue).getRGB();
	}
	
	public static void getRGB(double[] hsv, double[] rgb) {
		double c = hsv[1] * hsv[2];
		double x = c * (1 - Math.abs((hsv[0] / 60) % 2 - 1));
		double m = hsv[2] - c;

		double r = 0;
		double g = 0;
		double b = 0;

		if (0 <= hsv[0] && hsv[0] < 60) {
			r = c;
			g = x;
		} else if (60 <= hsv[0] && hsv[0] < 150) {
			r = x;
			g = c;
		} else if (120 <= hsv[0] && hsv[0] < 180) {
			g = c;
			b = x;
		} else if (180 <= hsv[0] && hsv[0] < 240) {
			g = x;
			b = c;
		} else if (240 <= hsv[0] && hsv[0] < 300) {
			r = x;
			b = c;
		} else if (300 <= hsv[0] && hsv[0] < 360) {
			r = c;
			b = x;
		}

		rgb[0] = (r + m) * 255;
		rgb[1] = (g + m) * 255;
		rgb[2] = (b + m) * 255;
	}
	
	public static void getHSV(int rgb, double[] hsv) {
		int red = (rgb & 0x00ff0000) >> 16;
		int green = (rgb & 0x0000ff00) >> 8;
		int blue = rgb & 0x000000ff;
	
		double dRed = (double) red / 255.0;
		double dGreen = (double) green / 255.0;
		double dBlue = (double) blue / 255.0;

		double max = Math.max(dRed, Math.max(dGreen, dBlue));
		double min = Math.min(dRed, Math.min(dGreen, dBlue));
		
		if (max == dRed && green >= blue) {
			hsv[0] = (int) (60 * (dGreen - dBlue) / (max - min));
		} else if (max == dRed && dGreen < dBlue) {
			hsv[0] = (int) (60 * (dGreen - dBlue) / (max - min) + 360);
		} else if (max == dGreen) {
			hsv[0] = (int) (60 * (dBlue - dRed) / (max - min) + 120);
		} else if (max == dBlue) {
			hsv[0] = (int) (60 * (dRed - dGreen) / (max - min) + 240);
		}

		hsv[1] = 1.0 - min / max;
		hsv[2] = max;
	}
}
