package ru.artemiyk.labimages.filter;

public class GaborKernel extends KernelBase {

	private double lambda;
	private double theta;
	private double psi;
	private double sigma;
	private double gamma;

	public GaborKernel(double lambda, double theta, int psi, int sigma, double gamma, int kernelSize) {
		this.lambda = lambda;
		this.theta = theta;
		this.psi =  Math.PI * (double) psi / 180.0;
		this.sigma = (double) sigma;
		this.gamma = gamma;
		
		if (kernelSize % 2 == 1) {
			kernelSize++;
		}

		createKernel(kernelSize);
		fillKernel();
		setGrayscale(false);
		setNormalize(false);
	}

	@Override
	protected void fillKernel() {
		if ((sigma <= Double.MIN_VALUE && sigma >= -Double.MIN_VALUE) || lambda < 1.0) {
			setValue(0, 0, 1.0);
			return;
		}

		for (int y = begin(); y <= end(); y++) {
			for (int x = begin(); x <= end(); x++) {
				double xx = xX(x, y, theta);
				double yy = yY(x, y, theta);

				double expDegree = 0.5 * (xx * xx + gamma * gamma * yy * yy) / (sigma * sigma);
				double cosArg = 2.0 * Math.PI * xx / lambda + psi;

				double valExp = Math.exp(-expDegree);
				double valCos = Math.cos(cosArg);
				setValue(x, y, valExp * valCos);
			}
		}
	}

	private static double xX(int x, int y, double theta) {
		double xx = (double) x;
		double yy = (double) y;
		return xx * Math.cos(theta) + yy + Math.sin(theta);
	}

	private static double yY(int x, int y, double theta) {
		double xx = (double) x;
		double yy = (double) y;
		return -xx * Math.sin(theta) + yy + Math.cos(theta);
	}
}
