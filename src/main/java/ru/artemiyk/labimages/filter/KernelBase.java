package ru.artemiyk.labimages.filter;

public abstract class KernelBase {
	private int kernelCenterX;
	private int kernelCenterY;
	private double[][] kernel;
	private double kernelSumm;

	private boolean isGrayscale = false;

	private boolean isNormalize = true;

	public KernelBase() {

	}

	protected abstract void fillKernel();

	protected void createKernel(int horizontalSize, int verticalSize) {
		if (verticalSize % 2 == 0) {
			verticalSize++;
		}
		if (horizontalSize % 2 == 0) {
			horizontalSize++;
		}

		kernelCenterX = horizontalSize / 2;
		kernelCenterY = verticalSize / 2;
		kernel = new double[horizontalSize][verticalSize];
	}

	public int begin(int component) {
		switch (component) {
		case 0:
			return -kernelCenterX;
		case 1:
			return -kernelCenterY;
		default:
			return -kernelCenterX;
		}
	}

	public int end(int component) {
		switch (component) {
		case 0:
			return kernelCenterX;
		case 1:
			return kernelCenterY;
		default:
			return kernelCenterX;
		}
	}

	public double getValue(int x, int y) {
		return kernel[kernelCenterX + x][kernelCenterY + y];
	}

	protected void setValue(int x, int y, double val) {
		kernel[kernelCenterX + x][kernelCenterY + y] = val;
	}

	public boolean isGrayscale() {
		return isGrayscale;
	}

	protected void setGrayscale(boolean isGrayscale) {
		this.isGrayscale = isGrayscale;
	}

	public boolean isNormalize() {
		return isNormalize;
	}

	public void setNormalize(boolean isNormalize) {
		this.isNormalize = isNormalize;
	}

	protected void summ() {
		kernelSumm = 0;
		for (int y = begin(1); y <= end(1); y++) {
			for (int x = begin(0); x <= end(0); x++) {
				kernelSumm += getValue(x, y);
			}
		}
	}

	public double getSumm() {
		return (kernelSumm > Double.MIN_VALUE || kernelSumm < -Double.MIN_VALUE) ? kernelSumm : 1.0;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{ ");

		for (int y = begin(1); y <= end(1); y++) {
			if (y != begin(1)) {
				builder.append(" , ");
			}

			builder.append("{");
			for (int x = begin(0); x <= end(0); x++) {
				if (x != begin(0)) {
					builder.append(",");
				}
				builder.append(getValue(x, y));
			}
			builder.append("}");

		}

		builder.append(" }");

		return builder.toString();
	}
}
