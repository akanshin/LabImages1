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
import javax.swing.JScrollPane;

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

	private String histogramName;
	private String xAxisName;
	private String yAxisName;
	private int bins = 100;
	private BufferedImage image;
	private int x, y;
	private int datasetWidth, datasetHeight;

	private PlotPanel verticalPlotPanel;
	private PlotPanel horizontalPlotPanel;
	private PlotPanel allPlotPanel;

	private int frameWidth = 900;
	private int frameHeight = 350;

	private Thread thread1, thread2, thread3;

	private class PlotPanel extends JPanel implements ComponentListener {
		private static final long serialVersionUID = 1L;

		private Image image;

		public void setImage(Image image) {
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
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		allPlotPanel = new PlotPanel();
		verticalPlotPanel = new PlotPanel();
		horizontalPlotPanel = new PlotPanel();

		JPanel plotPanels = new JPanel(new GridLayout(0, 3, 0, 0));
		plotPanels.add(allPlotPanel);
		plotPanels.add(verticalPlotPanel);
		plotPanels.add(horizontalPlotPanel);

		JScrollPane scrollPane = new JScrollPane(plotPanels, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(scrollPane, BorderLayout.CENTER);
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

		ExecutorService threadPool = Executors.newFixedThreadPool(8);

		long startPoint = System.currentTimeMillis();

		final double[] dataset = new double[datasetWidth * datasetHeight];
		final double[] verticalDataset = new double[datasetWidth * datasetHeight];
		final double[] horizontalDataset = new double[datasetWidth * datasetHeight];

		List<Future<Void>> futureList = new ArrayList<>();
		for (int i = 0; i < datasetHeight; i++) {
			final int iClone = i;

			Supplier<Void> supplier = new Supplier<Void>() {
				public int ii = iClone;
				@Override
				public Void get() {
					for (int j = 0; j < datasetWidth; j++) {
						double[] lab =  PixelCIELAB.getLAB(image.getRGB(x + j, y + ii));
						int index = j + ii * datasetWidth;
						dataset[index] = lab[0];
						verticalDataset[index] = lab[1];
						horizontalDataset[index] = lab[2];
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
		
		long collectPoint = System.currentTimeMillis();

		thread1 = new Thread() {
			public void run() {
				HistogramDataset jfDatasetAll = new HistogramDataset();
				jfDatasetAll.setType(HistogramType.RELATIVE_FREQUENCY);
				jfDatasetAll.addSeries("L", dataset, 50, 0.0, 100.0);
				JFreeChart jfChartAll = ChartFactory.createHistogram("", "", "", jfDatasetAll, PlotOrientation.VERTICAL,
						true, true, true);

				Paint[] paintArray = { new Color(0x80ff0000, true) };
				jfChartAll.getPlot()
						.setDrawingSupplier(new DefaultDrawingSupplier(paintArray,
								DefaultDrawingSupplier.DEFAULT_FILL_PAINT_SEQUENCE,
								DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
								DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
								DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
								DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE));

				final XYPlot plot = jfChartAll.getXYPlot();
				BarRenderer.setDefaultBarPainter(new StandardBarPainter());
				((XYBarRenderer) plot.getRenderer()).setBarPainter(new StandardXYBarPainter());

				allPlotPanel.setImage(jfChartAll.createBufferedImage(frameWidth / 3, frameHeight - 35));
			}
		};

		thread2 = new Thread() {
			public void run() {
				HistogramDataset jfDatasetVertical = new HistogramDataset();
				jfDatasetVertical.setType(HistogramType.RELATIVE_FREQUENCY);
				jfDatasetVertical.addSeries("A", verticalDataset, 50, -128.0, 128.0);
				JFreeChart jfChartVertical = ChartFactory.createHistogram("", "", "", jfDatasetVertical,
						PlotOrientation.VERTICAL, true, true, false);

				Paint[] paintArray = { new Color(0x8000ff00, true) };
				jfChartVertical.getPlot()
						.setDrawingSupplier(new DefaultDrawingSupplier(paintArray,
								DefaultDrawingSupplier.DEFAULT_FILL_PAINT_SEQUENCE,
								DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
								DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
								DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
								DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE));

				final XYPlot plot = jfChartVertical.getXYPlot();
				BarRenderer.setDefaultBarPainter(new StandardBarPainter());
				((XYBarRenderer) plot.getRenderer()).setBarPainter(new StandardXYBarPainter());

				verticalPlotPanel.setImage(jfChartVertical.createBufferedImage(frameWidth / 3, frameHeight - 35));
			}
		};

		thread3 = new Thread() {
			public void run() {
				HistogramDataset jfDatasetHorizontal = new HistogramDataset();
				jfDatasetHorizontal.setType(HistogramType.RELATIVE_FREQUENCY);
				jfDatasetHorizontal.addSeries("B", horizontalDataset, 50, -128.0, 128.0);
				JFreeChart jfChartHorizontal = ChartFactory.createHistogram("", "", "", jfDatasetHorizontal,
						PlotOrientation.VERTICAL, true, true, false);

				Paint[] paintArray = { new Color(0x800000ff, true) };
				jfChartHorizontal.getPlot()
						.setDrawingSupplier(new DefaultDrawingSupplier(paintArray,
								DefaultDrawingSupplier.DEFAULT_FILL_PAINT_SEQUENCE,
								DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
								DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
								DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
								DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE));

				final XYPlot plot = jfChartHorizontal.getXYPlot();
				BarRenderer.setDefaultBarPainter(new StandardBarPainter());
				((XYBarRenderer) plot.getRenderer()).setBarPainter(new StandardXYBarPainter());

				horizontalPlotPanel.setImage(jfChartHorizontal.createBufferedImage(frameWidth / 3, frameHeight - 35));
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
		
		long plotPoint = System.currentTimeMillis();
		
		System.out.println(String.format("collect: %d\tplot: %d", collectPoint - startPoint, plotPoint - collectPoint));

		this.setVisible(true);
		System.gc();
	}

	public void setDataset(BufferedImage image, int x, int y, int datasetWidth, int datasetHeight) {
		this.image = image;
		this.x = x;
		this.y = y;
		this.datasetWidth = datasetWidth;
		this.datasetHeight = datasetHeight;
	}

	public String getHistogramName() {
		return histogramName;
	}

	public void setHistogramName(String histogramName) {
		this.histogramName = histogramName;
	}

	public String getxAxisName() {
		return xAxisName;
	}

	public void setxAxisName(String xAxisName) {
		this.xAxisName = xAxisName;
	}

	public String getyAxisName() {
		return yAxisName;
	}

	public void setyAxisName(String yAxisName) {
		this.yAxisName = yAxisName;
	}

	public int getBins() {
		return bins;
	}

	public void setBins(int bins) {
		this.bins = bins;
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
}
