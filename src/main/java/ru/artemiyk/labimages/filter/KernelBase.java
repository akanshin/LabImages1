package ru.artemiyk.labimages.filter;

public abstract class KernelBase {
	private int kernelCenter;
	private double[][] kernel;

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
