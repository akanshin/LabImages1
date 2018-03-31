package ru.artemiyk.labimages.action.segmentation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import ru.artemiyk.labimages.LabImages;
import ru.artemiyk.labimages.action.Action;
import ru.artemiyk.labimages.pixelutils.PixelRGB;

public class MeanShiftAction extends Action {

	private BufferedImage imageToRead;

	public MeanShiftAction() {
		PointCIEDE.reset();
	}

	@Override
	public void produce(BufferedImage image) {
		if (image == null) {
			return;
		}

		imageToRead = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

		Thread thread = new Thread() {
			@Override
			public void run() {
				int iterationCount = 1;

				int minimum = 0;
				int maximum = iterationCount * image.getWidth() * image.getHeight();
				MeanShiftAction.super.setProgressRange(minimum, maximum);

				MeanShiftAction.super.showProgress(true);

				final double R = 5;
				final double dist = 7.0;

				double[][] pixR = new double[imageToRead.getWidth()][imageToRead.getHeight()];
				double[][] pixG = new double[imageToRead.getWidth()][imageToRead.getHeight()];
				double[][] pixB = new double[imageToRead.getWidth()][imageToRead.getHeight()];

				for (; iterationCount > 0; iterationCount--) {
					Graphics2D g = imageToRead.createGraphics();
					g.drawImage(image, 0, 0, null);
					g.dispose();

					ExecutorService threadPool = Executors.newFixedThreadPool(LabImages.THREAD_COUNT);
					List<Future<Void>> futureList = new ArrayList<>();
					for (int i = 0; i < imageToRead.getWidth(); i++) {
						final int iClone = i;

						Supplier<Void> supplier = new Supplier<Void>() {
							public int iii = iClone;

							@Override
							public Void get() {
								for (int j = 0; j < imageToRead.getHeight(); j++) {
									PointCIEDE point = new PointCIEDE(iii, j, imageToRead.getRGB(iii, j));

									int count = 0;
									double newR = 0.0;
									double newG = 0.0;
									double newB = 0.0;

									for (int x = (int) (-R); x <= (int) R; x++) {
										for (int y = (int) (-R); y <= (int) R; y++) {
											int ii = iii + x;
											int jj = j + y;
											if (Math.sqrt(x * x + y * y) <= R && ii >= 0 && ii < imageToRead.getWidth()
													&& jj >= 0 && jj < imageToRead.getHeight()) {
												PointCIEDE point1 = new PointCIEDE(ii, jj, imageToRead.getRGB(ii, jj));
												if (point.easycmp(point1) <= dist) {
													PixelRGB point1RGB = new PixelRGB(imageToRead.getRGB(ii, jj));
													count++;
													newR += point1RGB.getRed();
													newG += point1RGB.getGreen();
													newB += point1RGB.getBlue();
												}
											}
										}
									}

									newR /= (double) count;
									newG /= (double) count;
									newB /= (double) count;

									pixR[iii][j] = newR;
									pixG[iii][j] = newG;
									pixB[iii][j] = newB;

									Color origColor = new Color(imageToRead.getRGB(iii, j));
									Color newColor = new Color((int) pixR[iii][j], (int) pixG[iii][j],
											(int) pixB[iii][j], origColor.getAlpha());
									image.setRGB(iii, j, newColor.getRGB());

									MeanShiftAction.super.incrementProgress();
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

						}
					}

					threadPool.shutdown();
				}
				MeanShiftAction.super.showProgress(false);
			}
		};

		thread.start();
	}
}
