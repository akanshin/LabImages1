package ru.artemiyk.labimages.filter;

public class SobelKernel extends KernelBase {
	private double directionAngle = 0.0;

	public SobelKernel(double angle) {
		this.directionAngle = 2 * Math.PI * angle / 360.0;

		createKernel(3);
		fillKernel();
		setGrayscale(true);
		setNormalize(false);
	}

	@Override
	protected void fillKernel() {
		setValue(0, 0, 0.0);

		double angle = 0.0;
		double delta = Math.PI / 4.0;

		int[][] indexes = { { -1, 0 }, { -1, -1 }, { 0, -1 }, { 1, -1 }, { 1, 0 }, { 1, 1 }, { 0, 1 }, { -1, 1 } };

		for (int i = 0; i < 8; i++, angle += delta) {
			setValue(indexes[i][0], indexes[i][1], func(angle));
		}
	}

	private double func(double angle) {
		return 2.0 * Math.cos(angle + directionAngle);
	}
}
