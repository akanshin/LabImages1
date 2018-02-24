package ru.artemiyk.labimages.ui.plot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Paint;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

import ru.artemiyk.labimages.pixelutils.PixelCIELAB;

public class HistogramFrame extends JFrame implements ComponentListener {
	private static final long serialVersionUID = 1L;

	private int bins = 100;
	private BufferedImage image;
	private int x, y;
	private int datasetWidth, datasetHeight;

	private double[] datasetL;
	private double[] datasetA;
	private double[] datasetB;

	private PlotPanel plotPanelL;
	private PlotPanel plotPanelA;
	private PlotPanel plotPanelB;

	private JSlider binsSlider;
	private boolean isNeedToCollectDatasets = true;

	private int frameWidth = 950;
	private int frameHeight = 390;

	private Thread thread1, thread2, thread3;

	private class PlotPanel extends JPanel implements ComponentListener {
		private static final long serialVersionUID = 1L;

		private Image image;

		public void setImage(Image image) {
			this.image = null;
			this.image = image;
			this.repaint();
		}

		public void paintComponent(Graphics g) {
			if (g == null || image == null) {
				return;
			}

			g.clearRect(0, 0, this.getWidth(), this.getHeight());

			int panelWidth = this.getWidth();
			int panelHeight = this.getHeight();
			int originalWidth = image.getWidth(this);
			int originalHeight = image.getHeight(this);
			int imageWidth, imageHeight;
			if ((double) originalWidth / (double) originalHeight > (double) panelWidth / (double) panelHeight) {
				imageWidth = panelWidth;
				imageHeight = originalHeight * panelWidth / originalWidth;
			} else {
				imageHeight = panelHeight;
				imageWidth = originalWidth * panelHeight / originalHeight;
			}
			int imageX = panelWidth / 2 - imageWidth / 2;
			int imageY = panelHeight / 2 - imageHeight / 2;

			g.drawImage(image, imageX, imageY, imageWidth, imageHeight, this);
		}

		public void componentHidden(ComponentEvent e) {

		}

		public void componentMoved(ComponentEvent e) {

		}

		public void componentResized(ComponentEvent e) {
			this.repaint();
		}

		public void componentShown(ComponentEvent e) {

		}
	}

	public HistogramFrame() {
		this.setBounds(0, 0, frameWidth, frameHeight);
		this.setMinimumSize(new Dimension(frameWidth, frameHeight));
		this.setDefaultCloseOperation(HIDE_ON_CLOSE);

		plotPanelL = new PlotPanel();
		plotPanelA = new PlotPanel();
		plotPanelB = new PlotPanel();

		JPanel plotPanels = new JPanel(new GridLayout(0, 3, 0, 0));
		plotPanels.add(plotPanelL);
		plotPanels.add(plotPanelA);
		plotPanels.add(plotPanelB);

		this.getContentPane().add(plotPanels, BorderLayout.CENTER);

		binsSlider = new JSlider(JSlider.HORIZONTAL, 10, 100, 50);
		binsSlider.setBackground(Color.WHITE);
		binsSlider.setMajorTickSpacing(10);
		binsSlider.setPaintTicks(true);
		binsSlider.setMinorTickSpacing(2);
		binsSlider.setPaintLabels(true);
		binsSlider.setSnapToTicks(true);
		binsSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				updateBins();
			}
		});
		this.getContentPane().add(binsSlider, BorderLayout.SOUTH);
	}

	public void showHistogram() {
		if (thread1 != null && thread1.getState() == Thread.State.RUNNABLE) {
			return;
		}
		if (thread2 != null && thread2.getState() == Thread.State.RUNNABLE) {
			return;
		}
		if (thread3 != null && thread3.getState() == Thread.State.RUNNABLE) {
			return;
		}

		if (isNeedToCollectDatasets) {
			fillDataset();
		} else {
			isNeedToCollectDatasets = true;
		}

		thread1 = new Thread() {
			public void run() {
				if (datasetL == null) {
					return;
				}
				
				HistogramDataset jfDatasetL = new HistogramDataset();
				jfDatasetL.setType(HistogramType.RELATIVE_FREQUENCY);
				jfDatasetL.addSeries("L", datasetL, bins, 0.0, 100.0);
				JFreeChart jfChartL = ChartFactory.createHistogram("", "", "", jfDatasetL, PlotOrientation.VERTICAL,
						true, true, true);

				Paint[] paintArray = { new Color(0x80ff0000, true) };
				jfChartL.getPlot()
						.setDrawingSupplier(new DefaultDrawingSupplier(paintArray,
								DefaultDrawingSupplier.DEFAULT_FILL_PAINT_SEQUENCE,
								DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
								DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
								DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
								DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE));

				final XYPlot plot = jfChartL.getXYPlot();
				BarRenderer.setDefaultBarPainter(new StandardBarPainter());
				((XYBarRenderer) plot.getRenderer()).setBarPainter(new StandardXYBarPainter());

				plotPanelL.setImage(jfChartL.createBufferedImage(300, 300));
			}
		};

		thread2 = new Thread() {
			public void run() {
				if (datasetA == null) {
					return;
				}
				
				HistogramDataset jfDatasetA = new HistogramDataset();
				jfDatasetA.setType(HistogramType.RELATIVE_FREQUENCY);
				jfDatasetA.addSeries("A", datasetA, bins, -128.0, 128.0);
				JFreeChart jfChartA = ChartFactory.createHistogram("", "", "", jfDatasetA,
						PlotOrientation.VERTICAL, true, true, false);

				Paint[] paintArray = { new Color(0x8000ff00, true) };
				jfChartA.getPlot()
						.setDrawingSupplier(new DefaultDrawingSupplier(paintArray,
								DefaultDrawingSupplier.DEFAULT_FILL_PAINT_SEQUENCE,
								DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
								DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
								DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
								DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE));

				final XYPlot plot = jfChartA.getXYPlot();
				BarRenderer.setDefaultBarPainter(new StandardBarPainter());
				((XYBarRenderer) plot.getRenderer()).setBarPainter(new StandardXYBarPainter());

				plotPanelA.setImage(jfChartA.createBufferedImage(300, 300));
			}
		};

		thread3 = new Thread() {
			public void run() {
				if (datasetB == null) {
					return;
				}
				
				HistogramDataset jfDatasetB = new HistogramDataset();
				jfDatasetB.setType(HistogramType.RELATIVE_FREQUENCY);
				jfDatasetB.addSeries("B", datasetB, bins, -128.0, 128.0);
				JFreeChart jfChartB = ChartFactory.createHistogram("", "", "", jfDatasetB,
						PlotOrientation.VERTICAL, true, true, false);

				Paint[] paintArray = { new Color(0x800000ff, true) };
				jfChartB.getPlot()
						.setDrawingSupplier(new DefaultDrawingSupplier(paintArray,
								DefaultDrawingSupplier.DEFAULT_FILL_PAINT_SEQUENCE,
								DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
								DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
								DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
								DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE));

				final XYPlot plot = jfChartB.getXYPlot();
				BarRenderer.setDefaultBarPainter(new StandardBarPainter());
				((XYBarRenderer) plot.getRenderer()).setBarPainter(new StandardXYBarPainter());

				plotPanelB.setImage(jfChartB.createBufferedImage(300, 300));
			}
		};

		thread1.start();
		thread2.start();
		thread3.start();

		try {
			thread1.join();
			thread2.join();
			thread3.join();
		} catch (InterruptedException e) {

		}

		this.setVisible(true);
	}
	
	private void fillDataset() {
		ExecutorService threadPool = Executors.newFixedThreadPool(8);

		List<Future<Void>> futureList = new ArrayList<>();
		for (int i = 0; i < datasetHeight; i++) {
			final int iClone = i;

			Supplier<Void> supplier = new Supplier<Void>() {
				public int ii = iClone;

				@Override
				public Void get() {
					double[] lab = new double[3];
					for (int j = 0; j < datasetWidth; j++) {
						PixelCIELAB.getLAB(image.getRGB(x + j, y + ii), lab);
						int index = j + ii * datasetWidth;
						datasetL[index] = lab[0];
						datasetA[index] = lab[1];
						datasetB[index] = lab[2];
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

	public void setDataset(BufferedImage image, int x, int y, int datasetWidth, int datasetHeight) {
		this.image = null;
		datasetL = null;
		datasetA = null;
		datasetB = null;

		this.x = x;
		this.y = y;
		this.datasetWidth = datasetWidth;
		this.datasetHeight = datasetHeight;
		this.image = image;

		System.gc();

		datasetL = new double[datasetWidth * datasetHeight];
		datasetA = new double[datasetWidth * datasetHeight];
		datasetB = new double[datasetWidth * datasetHeight];

		isNeedToCollectDatasets = true;
	}

	public int getBins() {
		return bins;
	}

	public void setBins(int bins) {
		if (bins < 10) {
			bins = 10;
		} else if (bins > 100) {
			bins = 100;
		}

		this.bins = bins;
		binsSlider.setValue(bins);
	}

	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void componentMoved(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void componentResized(ComponentEvent arg0) {
		frameWidth = this.getWidth();
		frameHeight = this.getHeight();

		this.repaint();
	}

	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub
	}

	private void updateBins() {
		this.bins = binsSlider.getValue();
		isNeedToCollectDatasets = false;
		showHistogram();
	}
}
