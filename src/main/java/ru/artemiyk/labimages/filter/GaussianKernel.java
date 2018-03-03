package ru.artemiyk.labimages.filter;

public class GaussianKernel extends KernelBase {
	private int radius;
	private KernelComponent component;

	public GaussianKernel(int radius, KernelComponent component) {
		this.radius = radius;
		if (component == KernelComponent.eVertical) {
			createKernel(1, 1 + 3 * radius);
		} else if (component == KernelComponent.eHorizontal) {
			createKernel(1 + 3 * radius, 1);
		} else if (component == KernelComponent.eHorizontalandVertical) {
			createKernel(1 + 3 * radius, 1 + 3 * radius);
		}

		this.component = component;

		fillKernel();
		setGrayscale(false);
		setNormalize(true);
	}

	@Override
	protected void fillKernel() {
		if (radius == 0) {
			setValue(0, 0, 1.0);
			return;
		}

		for (int y = begin(1); y <= end(1); y++) {
			for (int x = begin(0); x <= end(0); x++) {
				double val = 1.0 / (2.0 * Math.PI * (double) (radius * radius));

				if (component == KernelComponent.eHorizontalandVertical) {
					val = Math.sqrt(val);
				} else {
					val = Math.sqrt(Math.sqrt(val));
				}

				double degree = -((double) (x * x + y * y) / (double) (2 * radius * radius));
				val *= Math.exp(degree);
				setValue(x, y, val);
			}
		}
	}
}
