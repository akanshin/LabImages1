package ru.artemiyk.labimages.action.filter;

public class SobelKernel2 extends KernelBase {
  private double directionAngle = 0.0;

  public SobelKernel2(double angle) {
    super(ColorModel.eRGB);

    directionAngle = angle;

    createKernel(3, 3);
    fillKernel();
    setGrayscale(false);
    setNormalize(false);
  }

  @Override
  protected void fillKernel() {
    KernelBase kernel = new SobelKernel(directionAngle);
    KernelBase otherKernel = new SobelKernel(directionAngle + 90.0);

    for (int y = begin(1); y <= end(1); y++) {
      for (int x = begin(0); x <= end(0); x++) {
        double val1 = kernel.getValue(x, y);
        double val2 = otherKernel.getValue(x, y);
        // double val = Math.sqrt(val1 * val1 + val2 * val2);
        double val = 0.5 * Math.abs(val1) + 0.5 * Math.abs(val2) + 1;
        setValue(x, y, val);
      }
    }
  }
}
