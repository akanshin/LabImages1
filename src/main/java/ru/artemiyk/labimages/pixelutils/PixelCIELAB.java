package ru.artemiyk.labimages.pixelutils;

import java.awt.Color;

public class PixelCIELAB {
  private static final double[][] mM = { { 0.5767309, 0.1855540, 0.1881852 }, { 0.2973769, 0.6273491, 0.0752741 },
      { 0.0270343, 0.0706872, 0.9911085 } };
  private static final double[][] mM1 = { { 2.0413690, -0.5649464, -0.3446944 }, { -0.9692660, 1.8760108, 0.0415560 },
      { 0.0134474, -0.1183897, 1.0154096 } };

  private static final double xn = 0.5767309 + 0.1855540 + 0.1881852;
  private static final double yn = 0.2973769 + 0.6273491 + 0.0752741;
  private static final double zn = 0.0270343 + 0.0706872 + 0.9911085;

  private static final double e = 0.008856;
  private static final double k = 903.3;

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
    } else {
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

  public static void getRGB(double[] lab, double[] rgb) {
    double fy = (lab[0] + 16.0) / 116.0;
    double fz = fy - lab[2] / 200.0;
    double fx = lab[1] / 500.0 + fy;

    double fx3 = Math.pow(fx, 3.0);
    double fy3 = Math.pow(fy, 3.0);
    double fz3 = Math.pow(fz, 3.0);

    double xr = fx3 > e ? fx3 : (116.0 * fx - 16.0) / k;
    double yr = lab[0] > k * e ? fy3 : lab[0] / k;
    double zr = fz3 > e ? fz3 : (116.0 * fz - 16) / k;

    double x = xr * xn;
    double y = yr * yn;
    double z = zr * zn;

    rgb[0] = (mM1[0][0] * x + mM1[0][1] * y + mM1[0][2] * z) * 255.0;
    rgb[1] = (mM1[1][0] * x + mM1[1][1] * y + mM1[1][2] * z) * 255.0;
    rgb[2] = (mM1[2][0] * x + mM1[2][1] * y + mM1[2][2] * z) * 255.0;
  }
}
