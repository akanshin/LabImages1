package ru.artemiyk.labimages.action.segmentation;

import ru.artemiyk.labimages.pixelutils.PixelCIELAB;

public class PointCIEDE {
	private static int globalId = 0;
	private int x;
	private int y;
	private int id;
	private double[] lab = new double[3];

	public PointCIEDE(int x, int y, int rgb) {
		this.x = x;
		this.y = y;
		this.id = globalId++;
		PixelCIELAB.getLAB(rgb, this.lab);
	}

	public static void reset() {
		globalId = 0;
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getId() {
		return id;
	}

	public double[] getLab() {
		return lab;
	}

	public double[] getLCH() {
		double h = Math.atan2(lab[1], lab[2]);
		if (h > 0) {
			h = (h / Math.PI) * 180.0;
		} else {
			h = 360.0 - (Math.abs(h) / Math.PI) * 180.0;
		}

		double[] lch = new double[3];
		lch[0] = lab[0];
		lch[1] = Math.sqrt(Math.pow(lab[1], 2) + Math.pow(lab[2], 2));
		lch[2] = h;

		return lch;
	}
	
	private double degreeToRadian(double degree) {
		return (degree * (Math.PI / 180.0));
	}
	
	public double compare(PointCIEDE point) {
		if (point == null) {
			return Double.NaN;
		}
		
		double[] lab1 = this.lab;
		double[] lab2 = point.getLab();
		
		double k_L = 1.0, k_C = 1.0, k_H = 1.0;
		double deg360InRad = degreeToRadian(360.0);
		double deg180InRad = degreeToRadian(180.0);
		double pow25To7 = 6103515625.0; /* pow(25, 7) */
		
		/*
		 * Step 1 
		 */
		/* Equation 2 */
		double C1 = Math.sqrt((lab1[1] * lab1[1]) + (lab1[2] * lab1[2]));
		double C2 = Math.sqrt((lab2[1] * lab2[1]) + (lab2[2] * lab2[2]));
		/* Equation 3 */
		double barC = (C1 + C2) / 2.0;
		/* Equation 4 */
		double G = 0.5 * (1 - Math.sqrt(Math.pow(barC, 7) / (Math.pow(barC, 7) + pow25To7)));
		/* Equation 5 */
		double a1Prime = (1.0 + G) * lab1[1];
		double a2Prime = (1.0 + G) * lab2[1];
		/* Equation 6 */
		double CPrime1 = Math.sqrt((a1Prime * a1Prime) + (lab1[2] * lab1[2]));
		double CPrime2 = Math.sqrt((a2Prime * a2Prime) + (lab2[2] * lab2[2]));
		/* Equation 7 */
		double hPrime1;
		if (lab1[2] == 0 && a1Prime == 0)
			hPrime1 = 0.0;
		else {
			hPrime1 = Math.atan2(lab1[2], a1Prime);
			/* 
			 * This must be converted to a hue angle in degrees between 0 
			 * and 360 by addition of 2􏰏 to negative hue angles.
			 */
			if (hPrime1 < 0)
				hPrime1 += deg360InRad;
		}
		double hPrime2;
		if (lab2[2] == 0 && a2Prime == 0)
			hPrime2 = 0.0;
		else {
			hPrime2 = Math.atan2(lab2[2], a2Prime);
			/* 
			 * This must be converted to a hue angle in degrees between 0 
			 * and 360 by addition of 2􏰏 to negative hue angles.
			 */
			if (hPrime2 < 0)
				hPrime2 += deg360InRad;
		}
		
		/*
		 * Step 2
		 */
		/* Equation 8 */
		double deltaLPrime = lab2[0] - lab1[0];
		/* Equation 9 */
		double deltaCPrime = CPrime2 - CPrime1;
		/* Equation 10 */
		double deltahPrime;
		double CPrimeProduct = CPrime1 * CPrime2;
		if (CPrimeProduct == 0)
			deltahPrime = 0;
		else {
			/* Avoid the fabs() call */
			deltahPrime = hPrime2 - hPrime1;
			if (deltahPrime < -deg180InRad)
				deltahPrime += deg360InRad;
			else if (deltahPrime > deg180InRad)
				deltahPrime -= deg360InRad;
		}
		/* Equation 11 */
		double deltaHPrime = 2.0 * Math.sqrt(CPrimeProduct) *
				Math.sin(deltahPrime / 2.0);
		
		/*
		 * Step 3
		 */
		/* Equation 12 */
		double barLPrime = (lab1[0] + lab2[0]) / 2.0;
		/* Equation 13 */
		double barCPrime = (CPrime1 + CPrime2) / 2.0;
		/* Equation 14 */
		double barhPrime, hPrimeSum = hPrime1 + hPrime2;
		if (CPrime1 * CPrime2 == 0) {
			barhPrime = hPrimeSum;
		} else {
			if (Math.abs(hPrime1 - hPrime2) <= deg180InRad)
				barhPrime = hPrimeSum / 2.0;
			else {
				if (hPrimeSum < deg360InRad)
					barhPrime = (hPrimeSum + deg360InRad) / 2.0;
				else
					barhPrime = (hPrimeSum - deg360InRad) / 2.0;
			}
		}
		/* Equation 15 */
		double T = 1.0 - (0.17 * Math.cos(barhPrime - degreeToRadian(30.0))) +
		    (0.24 * Math.cos(2.0 * barhPrime)) +
		    (0.32 * Math.cos((3.0 * barhPrime) + degreeToRadian(6.0))) - 
		    (0.20 * Math.cos((4.0 * barhPrime) - degreeToRadian(63.0)));
		/* Equation 16 */
		double deltaTheta = degreeToRadian(30.0) *
				Math.exp(-Math.pow((barhPrime - degreeToRadian(275.0)) / degreeToRadian(25.0), 2.0));
		/* Equation 17 */
		double R_C = 2.0 * Math.sqrt(Math.pow(barCPrime, 7.0) /
		    (Math.pow(barCPrime, 7.0) + pow25To7));
		/* Equation 18 */
		double S_L = 1 + ((0.015 * Math.pow(barLPrime - 50.0, 2.0)) /
				Math.sqrt(20 + Math.pow(barLPrime - 50.0, 2.0)));
		/* Equation 19 */
		double S_C = 1 + (0.045 * barCPrime);
		/* Equation 20 */
		double S_H = 1 + (0.015 * barCPrime * T);
		/* Equation 21 */
		double R_T = (-Math.sin(2.0 * deltaTheta)) * R_C;
		
		/* Equation 22 */
		double deltaE = Math.sqrt(
				Math.pow(deltaLPrime / (k_L * S_L), 2.0) +
				Math.pow(deltaCPrime / (k_C * S_C), 2.0) +
				Math.pow(deltaHPrime / (k_H * S_H), 2.0) + 
		    (R_T * (deltaCPrime / (k_C * S_C)) * (deltaHPrime / (k_H * S_H))));
		
		return (deltaE);
	}

	public double easycmp(PointCIEDE point) {
		double[] Lab1 = lab;
		double[] Lab2 = point.getLab();
		return Math.sqrt(
				Math.pow(Lab1[0] - Lab2[0], 2.0) + Math.pow(Lab1[1] - Lab2[1], 2.0) + Math.pow(Lab1[2] - Lab2[2], 2.0));
	}
}
