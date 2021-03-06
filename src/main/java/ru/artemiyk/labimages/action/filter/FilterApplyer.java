package ru.artemiyk.labimages.action.filter;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import ru.artemiyk.labimages.action.EProgressState;
import ru.artemiyk.labimages.action.ProgressListener;
import ru.artemiyk.labimages.pixelutils.PixelCIELAB;
import ru.artemiyk.labimages.pixelutils.PixelHSV;
import ru.artemiyk.labimages.pixelutils.PixelRGB;

public class FilterApplyer extends Thread {
  private List<KernelBase> kernels = new ArrayList<KernelBase>();

  private ExecutorService threadPool;

  private BufferedImage imageToRead;
  private BufferedImage imageTemp;
  private BufferedImage imageToWrite;

  private List<ProgressListener> listeners;

  private double rgbMin = 0.0;
  private double rgbMax = 255.0;

  public FilterApplyer() {
    listeners = new ArrayList<ProgressListener>();
  }

  public void addProgressListener(ProgressListener progressListener) {
    listeners.add(progressListener);
  }

  public void addKernel(KernelBase kernel) {
    kernels.add(kernel);
  }

  public void setThreadPool(ExecutorService threadPool) {
    this.threadPool = threadPool;
  }

  public void setImageToRead(BufferedImage imageToRead) {
    this.imageToRead = imageToRead;
    imageTemp = new BufferedImage(imageToRead.getWidth(), imageToRead.getHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics g = imageTemp.createGraphics();
    g.drawImage(imageToRead, 0, 0, null);
    g.dispose();
  }

  public void setImageToWrite(BufferedImage imageToWrite) {
    this.imageToWrite = imageToWrite;
  }

  public void run() {
    if (imageToRead == null || imageToWrite == null || kernels.isEmpty()) {
      return;
    }

    try {
      if (threadPool == null) {
        for (KernelBase kernel : kernels) {
          singleThreadNormalization(kernel);
          singleThreadApplying(kernel);

          Graphics g = imageTemp.createGraphics();
          g.drawImage(imageToWrite, 0, 0, null);
          g.dispose();
        }
      } else {
        for (KernelBase kernel : kernels) {
          multiThreadNormalization(kernel);
          multiThreadApplying(kernel);

          Graphics g = imageTemp.createGraphics();
          g.drawImage(imageToWrite, 0, 0, null);
          g.dispose();
        }
      }
    } catch (InterruptedException e) {

    }
  }

  private void singleThreadNormalization(KernelBase kernel) throws InterruptedException {

    if (kernel.getColorModel() == ColorModel.eLAB) {
      rgbMin = 255.0;
      rgbMax = 0.0;
    } else if (kernel.getColorModel() == ColorModel.eRGB) {
      rgbMin = 0.0;
      rgbMax = 255.0;
    }

    double[] rgba = new double[4];
    for (int i = 0; i < imageToRead.getHeight(); i++) {
      for (int j = 0; j < imageToRead.getWidth(); j++) {
        applyFilter(j, i, rgba, kernel);

        addNormalizedValue(rgba[0]);
        addNormalizedValue(rgba[1]);
        addNormalizedValue(rgba[2]);
        addNormalizedValue(rgba[3]);
      }

      for (ProgressListener listener : listeners) {
        listener.progressChanged(EProgressState.eNormalizing);
      }

      if (Thread.interrupted()) {
        throw new InterruptedException();
      }
    }
  }

  private void multiThreadNormalization(KernelBase kernel) throws InterruptedException {
    List<Future<Void>> futureList = new ArrayList<>();
    for (int i = 0; i < imageToRead.getHeight(); i++) {
      final int iClone = i;

      Supplier<Void> supplier = new Supplier<Void>() {
        public int ii = iClone;

        @Override
        public Void get() {
          double[] rgba = new double[4];

          for (int jj = 0; jj < imageToRead.getWidth(); jj++) {
            applyFilter(jj, ii, rgba, kernel);

            addNormalizedValue(rgba[0]);
            addNormalizedValue(rgba[1]);
            addNormalizedValue(rgba[2]);
            addNormalizedValue(rgba[3]);
          }

          for (ProgressListener listener : listeners) {
            listener.progressChanged(EProgressState.eNormalizing);
          }

          return null;
        }
      };

      futureList.add(CompletableFuture.supplyAsync(supplier, threadPool));
    }

    for (Future<Void> future : futureList) {
      try {
        future.get();
      } catch (InterruptedException | ExecutionException e) {
        threadPool.shutdownNow();
        throw new InterruptedException();
      }

      if (Thread.interrupted()) {
        threadPool.shutdownNow();
        throw new InterruptedException();
      }
    }
  }

  private void normalize(double[] rgba) {
    double rgbRange = rgbMax - rgbMin;
    for (int i = 0; i < rgba.length; i++) {
      rgba[i] = (rgba[i] - rgbMin) / rgbRange * 255.0;
    }
  }

  private void singleThreadApplying(KernelBase kernel) throws InterruptedException {
    double[] rgba = new double[4];
    for (int i = 0; i < imageToRead.getHeight(); i++) {
      for (int j = 0; j < imageToRead.getWidth(); j++) {
        applyFilter(j, i, rgba, kernel);
        normalize(rgba);
        int rgb = PixelRGB.getRGB(rgba);
        imageToWrite.setRGB(j, i, rgb);
      }

      for (ProgressListener listener : listeners) {
        listener.progressChanged(EProgressState.eApplying);
      }

      if (Thread.interrupted()) {
        throw new InterruptedException();
      }
    }
  }

  private void multiThreadApplying(KernelBase kernel) throws InterruptedException {
    List<Future<Void>> futureList = new ArrayList<>();
    for (int i = 0; i < imageToRead.getHeight(); i++) {
      final int iClone = i;

      Supplier<Void> supplier = new Supplier<Void>() {
        public int ii = iClone;

        @Override
        public Void get() {
          double[] rgba = new double[4];

          for (int jj = 0; jj < imageToRead.getWidth(); jj++) {
            applyFilter(jj, ii, rgba, kernel);
            normalize(rgba);
            int rgb = PixelRGB.getRGB(rgba);
            imageToWrite.setRGB(jj, ii, rgb);
          }

          for (ProgressListener listener : listeners) {
            listener.progressChanged(EProgressState.eApplying);
          }

          return null;
        }
      };

      futureList.add(CompletableFuture.supplyAsync(supplier, threadPool));
    }

    for (Future<Void> future : futureList) {
      try {
        future.get();
      } catch (InterruptedException | ExecutionException e) {
        threadPool.shutdownNow();
        throw new InterruptedException();
      }

      if (Thread.interrupted()) {
        threadPool.shutdownNow();
        throw new InterruptedException();
      }
    }
  }

  private void applyFilter(int x, int y, double[] rgba, KernelBase kernel) {
    for (int i = 0; i < 4; i++) {
      rgba[i] = 0.0;
    }

    if (kernel.getColorModel() == ColorModel.eRGB) {
      int rgb = 0;
      double[] hsvBuf = new double[3];
      double[] rgbBuf = new double[4];

      for (int kernelY = kernel.begin(1); kernelY <= kernel.end(1); kernelY++) {
        for (int kernelX = kernel.begin(0); kernelX <= kernel.end(0); kernelX++) {
          rgb = getRgb(x + kernelX, y + kernelY);

          if (kernel.isGrayscale()) {
            PixelHSV.getHSV(rgb, hsvBuf);
            hsvBuf[1] = 0.0;
            PixelHSV.getRGB(hsvBuf, rgbBuf);
            rgbBuf[3] = (double) ((rgb >> 24) & 255);
          } else {
            rgbBuf[2] = (double) ((rgb >> 0) & 255);
            rgbBuf[1] = (double) ((rgb >> 8) & 255);
            rgbBuf[0] = (double) ((rgb >> 16) & 255);
            rgbBuf[3] = (double) ((rgb >> 24) & 255);
          }

          for (int i = 0; i < 4; i++) {
            rgba[i] += rgbBuf[i] * kernel.getValue(kernelX, kernelY);
          }
        }
      }

      if (kernel.isNormalize()) {
        for (int i = 0; i < 4; i++) {
          rgba[i] /= kernel.getSumm();
        }
      }

      // System.out.println(rgba[0] + " " + rgba[1] + " " + rgba[2] + " " + rgba[3]);
      // System.exit(0);

    } else if (kernel.getColorModel() == ColorModel.eLAB) {
      int rgb = 0;
      double[] lab = new double[3];
      double[] resultLab = new double[3];

      for (int kernelY = kernel.begin(1); kernelY <= kernel.end(1); kernelY++) {
        for (int kernelX = kernel.begin(0); kernelX <= kernel.end(0); kernelX++) {
          rgb = getRgb(x + kernelX, y + kernelY);
          PixelCIELAB.getLAB(rgb, lab);

          resultLab[0] += lab[0] * kernel.getValue(kernelX, kernelY);
        }
      }

      if (kernel.isNormalize()) {
        resultLab[0] /= kernel.getSumm();
      }

      rgb = getRgb(x, y);
      PixelCIELAB.getLAB(rgb, lab);
      resultLab[1] = lab[1];
      resultLab[2] = lab[2];

      PixelCIELAB.getRGB(resultLab, rgba);
      rgba[3] = (double) ((rgb >> 24) & 255);

    } else {
      int rgb = getRgb(x, y);

      rgba[0] = (double) ((rgb >> 0) & 0xFF);
      rgba[1] = (double) ((rgb >> 8) & 0xFF);
      rgba[2] = (double) ((rgb >> 16) & 0xFF);
      rgba[3] = (double) ((rgb >> 24) & 0xFF);
    }
  }

  private int getRgb(int x, int y) {
    x = x < 0 ? 0 : x;
    x = x >= imageTemp.getWidth() ? imageTemp.getWidth() - 1 : x;

    y = y < 0 ? 0 : y;
    y = y >= imageTemp.getHeight() ? imageTemp.getHeight() - 1 : y;

    return imageTemp.getRGB(x, y);
  }

  private synchronized void addNormalizedValue(double val) {
    if (val < rgbMin) {
      rgbMin = val;
    }
    if (val > rgbMax) {
      rgbMax = val;
    }
  }
}
