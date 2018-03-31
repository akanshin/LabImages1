package ru.artemiyk.labimages.action.filter;

public class GaborKernel extends KernelBase {

	private double lambda;
	private double theta;
	private double psi;
	private double sigma;
	private double gamma;
	private int kernelSize;

	public GaborKernel(double lambda, int theta, int psi, int sigma, double gamma, int kernelSize) {
		super(ColorModel.eLAB);
		this.lambda = lambda;
		this.theta = Math.PI * (double) theta / 180.0;
		this.psi =  Math.PI * (double) psi / 180.0;
		this.sigma = (double) sigma;
		this.gamma = gamma;
		this.kernelSize = kernelSize;

		createKernel(kernelSize, kernelSize);
		fillKernel();
		setGrayscale(true);
		setNormalize(true);
	}

	@Override
	protected void fillKernel() {
		if ((sigma <= Double.MIN_VALUE && sigma >= -Double.MIN_VALUE) || lambda < 1.0) {
			setValue(0, 0, 1.0);
			return;
		}

		for (int y = begin(1); y <= end(1); y++) {
			for (int x = begin(0); x <= end(0); x++) {
				double xx = xX(x, y, theta);
				double yy = yY(x, y, theta) / gamma;

				double expDegree = 0.5 * (xx * xx + yy * yy) / (sigma * sigma);
				double cosArg = 2 * Math.PI * xx / lambda + psi;

				double valExp = Math.exp(-expDegree);
				double valCos = Math.cos(cosArg);
				setValue(x, y, valExp * valCos);
			}
		}
	}

	private static double xX(int x, int y, double theta) {
		double xx = (double) x;
		double yy = (double) y;
		return xx * Math.cos(theta) + yy * Math.sin(theta);
	}

	private static double yY(int x, int y, double theta) {
		double xx = (double) x;
		double yy = (double) y;
		return -xx * Math.sin(theta) + yy * Math.cos(theta);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(super.toString() + "\n");
		builder.append("lambda=" + lambda + "\n");
		builder.append("theta=" + theta + "\n");
		builder.append("psi=" + psi + "\n");
		builder.append("sigma=" + sigma + "\n");
		builder.append("gamma=" + gamma + "\n");
		builder.append("kernelSize=" + kernelSize + "\n");
		
		return builder.toString();
	}
}
