package ru.artemiyk.labimages.filter;

public class GaussianKernel extends KernelBase {
	private int radius;
	
	public GaussianKernel(int radius) {
		this.radius = radius;
		createKernel(1 + 3 * radius);
		fillKernel();
	}
	
	@Override
	protected void fillKernel() {
		for (int y = begin(); y <= end(); y++) {
			for (int x = begin(); x <= end(); x++) {
				double val = 1.0 / (2.0 * Math.PI * (double) (radius * radius));
				double degree = -((double) (x * x + y * y) / (double) (2 *radius * radius));
				val *= Math.exp(degree);
				setValue(x, y, val);
			}
		}
	}
}
