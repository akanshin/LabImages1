package ru.artemiyk.labimages.filter;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import ru.artemiyk.labimages.pixelutils.PixelRGB;

public class FilterApplyer extends Thread {
	private KernelBase kernel;

	private ExecutorService threadPool;

	private BufferedImage imageToRead;
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

	public void setKernel(KernelBase kernel) {
		this.kernel = kernel;
	}

	public void setThreadPool(ExecutorService threadPool) {
		this.threadPool = threadPool;
	}

	public void setImageToRead(BufferedImage imageToRead) {
		this.imageToRead = imageToRead;
	}

	public void setImageToWrite(BufferedImage imageToWrite) {
		this.imageToWrite = imageToWrite;
	}

	public void run() {
		if (imageToRead == null || imageToWrite == null || kernel == null) {
			return;
		}

		try {
			if (threadPool == null) {
				singleThreadNormalization();
				singleThreadApplying();
			} else {
				multiThreadNormalization();
				multiThreadApplying();
			}
		} catch (InterruptedException e) {
			
		}
	}

	private void singleThreadNormalization() throws InterruptedException {
		double[] rgba = new double[4];
		for (int i = 0; i < imageToRead.getHeight(); i++) {
			for (int j = 0; j < imageToRead.getWidth(); j++) {
				applyFilter(j, i, rgba);

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

	private void multiThreadNormalization() throws InterruptedException {
		List<Future<Void>> futureList = new ArrayList<>();
		for (int i = 0; i < imageToRead.getHeight(); i++) {
			final int iClone = i;

			Supplier<Void> supplier = new Supplier<Void>() {
				public int ii = iClone;

				@Override
				public Void get() {
					double[] rgba = new double[4];

					for (int jj = 0; jj < imageToRead.getWidth(); jj++) {
						applyFilter(jj, ii, rgba);

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

	private void singleThreadApplying() throws InterruptedException {
		double[] rgba = new double[4];
		for (int i = 0; i < imageToRead.getHeight(); i++) {
			for (int j = 0; j < imageToRead.getWidth(); j++) {
				applyFilter(j, i, rgba);
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

	private void multiThreadApplying() throws InterruptedException {
		List<Future<Void>> futureList = new ArrayList<>();
		for (int i = 0; i < imageToRead.getHeight(); i++) {
			final int iClone = i;

			Supplier<Void> supplier = new Supplier<Void>() {
				public int ii = iClone;

				@Override
				public Void get() {
					double[] rgba = new double[4];

					for (int jj = 0; jj < imageToRead.getWidth(); jj++) {
						applyFilter(jj, ii, rgba);
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

	private void applyFilter(int x, int y, double[] rgba) {
		double redSum = 0;
		double greenSum = 0;
		double blueSum = 0;
		double alphaSum = 0;
		double kernelSum = 0;

		for (int kernelY = kernel.begin(); kernelY <= kernel.end(); kernelY++) {
			for (int kernelX = kernel.begin(); kernelX <= kernel.end(); kernelX++) {
				int rgb = getRgb(x + kernelX, y + kernelY);

				blueSum += (double) ((rgb >> 0) & 255) * kernel.getValue(kernelX, kernelY);
				greenSum += (double) ((rgb >> 8) & 255) * kernel.getValue(kernelX, kernelY);
				redSum += (double) ((rgb >> 16) & 255) * kernel.getValue(kernelX, kernelY);
				alphaSum += (double) ((rgb >> 24) & 255) * kernel.getValue(kernelX, kernelY);

				kernelSum += kernel.getValue(kernelX, kernelY);
			}
		}

		rgba[0] = redSum / kernelSum;
		rgba[1] = greenSum / kernelSum;
		rgba[2] = blueSum / kernelSum;
		rgba[3] = alphaSum / kernelSum;
	}

	private int getRgb(int x, int y) {
		x = x < 0 ? 0 : x;
		x = x >= imageToRead.getWidth() ? imageToRead.getWidth() - 1 : x;

		y = y < 0 ? 0 : y;
		y = y >= imageToRead.getHeight() ? imageToRead.getHeight() - 1 : y;

		return imageToRead.getRGB(x, y);
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
