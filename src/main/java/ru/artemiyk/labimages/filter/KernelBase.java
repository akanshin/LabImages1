package ru.artemiyk.labimages.filter;

public abstract class KernelBase {
	private int kernelCenter;
	private double[][] kernel;
	private double kernelSumm;
	
	private boolean isGrayscale = false;
	
	private boolean isNormalize = true;

	public KernelBase() {

	}

	protected abstract void fillKernel();

	protected void createKernel(int kernelSize) {
		if (kernelSize % 2 == 0) {
			kernelSize++;
		}
		kernelCenter = kernelSize / 2;
		kernel = new double[kernelSize][kernelSize];
	}

	public int begin() {
		return -kernelCenter;
	}

	public int end() {
		return kernelCenter;
	}

	public double getValue(int x, int y) {
		return kernel[kernelCenter + x][kernelCenter + y];
	}

	protected void setValue(int x, int y, double val) {
		kernel[kernelCenter + x][kernelCenter + y] = val;
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
		for (int y = begin(); y <= end(); y++) {
			for (int x = begin(); x <= end(); x++) {
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
		
		for (int y = begin(); y <= end(); y++) {
			if (y != begin()) {
				builder.append(" , ");
			}
			
			builder.append("{");
			for (int x = begin(); x <= end(); x++) {
				if (x != begin()) {
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
