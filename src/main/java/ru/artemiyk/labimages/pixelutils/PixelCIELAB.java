package ru.artemiyk.labimages.pixelutils;

import java.awt.Color;

public class PixelCIELAB {
	private static final double[][] mM = {{0.5767309, 0.1855540, 0.1881852},
								          {0.2973769, 0.6273491, 0.0752741},
								          {0.0270343, 0.0706872, 0.9911085}};
	
	private static double xn = 0.5767309 + 0.1855540 + 0.1881852;
	private static double yn = 0.2973769 + 0.6273491 + 0.0752741;
	private static double zn = 0.0270343 + 0.0706872 + 0.9911085;
	
	private double l;
	private double a;
	private double b;
	
	public PixelCIELAB() {
		
	}
	
	public PixelCIELAB(int rgb) {
		int red = (rgb & 0x00ff0000) >> 16;
		int green = (rgb & 0x0000ff00) >> 8;
		int blue = rgb & 0x000000ff;
		
		calculateLAB(red, green, blue);
	}
	
	public PixelCIELAB(Color color) {
		calculateLAB(color.getRed(), color.getGreen(), color.getBlue());
	}
	
	private void calculateLAB(int red, int green, int blue) {
		double dRed = (double) red / 255.0;
		double dGreen = (double) green / 255.0;
		double dBlue = (double) blue / 255.0;
		
		double x = mM[0][0] * dRed + mM[0][1] * dGreen + mM[0][2] * dBlue;
		double y = mM[1][0] * dRed + mM[1][1] * dGreen + mM[1][2] * dBlue;
		double z = mM[2][0] * dRed + mM[2][1] * dGreen + mM[2][2] * dBlue;
		
		l = 116.0 * f(y / yn) - 16.0;
		a = 500.0 * (f(x / xn) - f(y / yn));
		b = 200.0 * (f(y / yn) - f(z / zn));
	}
	
	public double getL() {
		return l;
	}
	
	public double getA() {
		return a;
	}
	
	public double getB() {
		return b;
	}
	
	private static double f(double x) {
		if (x > Math.pow(6.0 / 29.0, 3)) {
			return Math.pow(x, 1.0 / 3.0);
		}
		else
		{
			return x * Math.pow(29.0 / 6.0, 2.0) / 3.0 + 4.0 / 29.0;
		}
	}
	
	public static void getLAB(int rgb, double[] lab) {
		double red = (double) ((rgb & 0x00ff0000) >> 16) / 255.0;
		double green = (double) ((rgb & 0x0000ff00) >> 8) / 255.0;
		double blue = (double) (rgb & 0x000000ff) / 255.0;
		
		double x = mM[0][0] * red + mM[0][1] * green + mM[0][2] * blue;
		double y = mM[1][0] * red + mM[1][1] * green + mM[1][2] * blue;
		double z = mM[2][0] * red + mM[2][1] * green + mM[2][2] * blue;
		
		lab[0] = 116.0 * f(y / yn) - 16.0;
		lab[1] = 500.0 * (f(x / xn) - f(y / yn));
		lab[2] = 200.0 * (f(y / yn) - f(z / zn));
	}
}
