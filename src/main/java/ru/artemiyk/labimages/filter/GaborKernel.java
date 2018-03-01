package ru.artemiyk.labimages.filter;

public class GaborKernel extends KernelBase {
	
	private double lambda = 1.0;
	private double theta = 0.0;
	private double psi = 0.0;
	private double sigma = 1.0;
	private double gamma = 1.0;

	public GaborKernel(double lambda, double theta, double psi, double sigma, double gamma) {
		this.lambda = lambda;
		this.theta = theta;
		this.psi = psi;
		this.sigma = sigma;
		this.gamma = gamma;
		
		if (lambda <= Double.MIN_VALUE && lambda >= -Double.MIN_VALUE) {
			throw new IllegalArgumentException();
		}
		
		if (sigma <= Double.MIN_VALUE && sigma >= -Double.MIN_VALUE) {
			throw new IllegalArgumentException();
		}
		
		if (gamma <= Double.MIN_VALUE && gamma >= -Double.MIN_VALUE) {
			throw new IllegalArgumentException();
		}
		
		createKernel(1 + 3 * (int) sigma);
		fillKernel();
	}
	
	@Override
	protected void fillKernel() {
		for (int y = begin(); y <= end(); y++) {
			for (int x = begin(); x <= end(); x++) {
				double xx = xX(x, y, theta);
				double yy = yY(x, y, theta);
				
				double valExp = Math.exp(-(xx * xx + gamma * gamma * yy * yy) / (2.0 * sigma * sigma));
				double valCos = Math.cos(2.0 * Math.PI * xx / lambda + psi);
				setValue(x, y, valExp * valCos);
			}
		}
	}

	private static double xX(int x, int y, double theta) {
		return (double) x * Math.cos(theta) + (double) y + Math.sin(theta);
	}
	
	private static double yY(int x, int y, double theta) {
		return (double) -x * Math.sin(theta) + (double) y + Math.cos(theta);
	}
}
