package ru.artemiyk.labimages.ui.plot;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Paint;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

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

		thread1 = new Thread() {
			public void run() {
				double[] dataset = new double[datasetWidth * datasetHeight];
				for (int i = 0; i < datasetHeight; i++) {
					for (int j = 0; j < datasetWidth; j++) {
						dataset[j + i * datasetWidth] = (new PixelCIELAB(image.getRGB(x + j, y + i))).getL();
					}
				}
				HistogramDataset jfDatasetAll = new HistogramDataset();
				jfDatasetAll.setType(HistogramType.RELATIVE_FREQUENCY);
				jfDatasetAll.addSeries(histogramName, dataset, 50, 0.0, 100.0);
				JFreeChart jfChartAll = ChartFactory.createHistogram(histogramName, xAxisName, yAxisName, jfDatasetAll,
						PlotOrientation.VERTICAL, true, true, true);

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

				allPlotPanel.setImage(
						jfChartAll.createBufferedImage(frameWidth / 3, frameHeight > 50 ? frameHeight - 50 : 0));
			}
		};

		thread2 = new Thread() {
			public void run() {
				double[] verticalDataset = new double[datasetWidth];
				for (int i = 0; i < datasetHeight; i++) {
					for (int j = 0; j < datasetWidth; j++) {
						verticalDataset[j] += (new PixelCIELAB(image.getRGB(x + j, y + i))).getL();
					}
				}
				for (int j = 0; j < datasetWidth; j++) {
					verticalDataset[j] /= (double) datasetHeight;
				}

				HistogramDataset jfDatasetVertical = new HistogramDataset();
				jfDatasetVertical.setType(HistogramType.RELATIVE_FREQUENCY);
				jfDatasetVertical.addSeries(histogramName, verticalDataset, 50, 0.0, 100.0);
				JFreeChart jfChartVertical = ChartFactory.createHistogram(histogramName + " (vertical)", xAxisName,
						yAxisName, jfDatasetVertical, PlotOrientation.VERTICAL, true, true, false);

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

				verticalPlotPanel.setImage(
						jfChartVertical.createBufferedImage(frameWidth / 3, frameHeight > 50 ? frameHeight - 50 : 0));
			}
		};

		thread3 = new Thread() {
			public void run() {
				double[] horizontalDataset = new double[datasetHeight];
				for (int i = 0; i < datasetHeight; i++) {
					for (int j = 0; j < datasetWidth; j++) {
						horizontalDataset[i] += (new PixelCIELAB(image.getRGB(x + j, y + i))).getL();
					}
				}
				for (int i = 0; i < datasetHeight; i++) {
					horizontalDataset[i] /= (double) datasetWidth;
				}

				HistogramDataset jfDatasetHorizontal = new HistogramDataset();
				jfDatasetHorizontal.setType(HistogramType.RELATIVE_FREQUENCY);
				jfDatasetHorizontal.addSeries(histogramName, horizontalDataset, 50, 0.0, 100.0);
				JFreeChart jfChartHorizontal = ChartFactory.createHistogram(histogramName + " (horizontal)", xAxisName,
						yAxisName, jfDatasetHorizontal, PlotOrientation.VERTICAL, true, true, false);

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

				horizontalPlotPanel.setImage(
						jfChartHorizontal.createBufferedImage(frameWidth / 3, frameHeight > 50 ? frameHeight - 50 : 0));
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
