package ru.artemiyk.labimages.action.segmentation;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import ru.artemiyk.labimages.action.Action;

public class SplitAndMergeAction extends Action {
	private ArrayList<Integer> ids = new ArrayList<Integer>();
	private ArrayList<PointCIEDE> points = new ArrayList<PointCIEDE>();
	private BufferedImage imageToRead;
	
	private double distance = 3.2;

	public SplitAndMergeAction() {
		PointCIEDE.reset();
	}
	
	@Override
	public void produce(BufferedImage image) {
		if (image == null) {
			return;
		}
		
		int minimum = 0;
		int maximum = 3 * image.getWidth() * image.getHeight();
		super.setProgressRange(minimum, maximum);
		
		imageToRead = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
		Graphics2D g = imageToRead.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		
		Thread thread = new Thread() {
			@Override
			public void run() {
				SplitAndMergeAction.super.showProgress(true);
				
				for (int i = 0; i < image.getWidth(); i++) {
					for (int j = 0; j < image.getHeight(); j++) {
						PointCIEDE point = new PointCIEDE(i, j, image.getRGB(i, j));
						ids.add(point.getId());
						points.add(point);
						SplitAndMergeAction.super.incrementProgress();
					}
				}
				
				splitImage(points.get(0), imageToRead.getWidth(), imageToRead.getHeight());
		        
				for (int i = 0; i < imageToRead.getWidth() - 1; i++) {
		            for (int j = 0; j < imageToRead.getHeight() - 1; j++) {
		                int id = i * imageToRead.getHeight() + j, id1 = id + 1, id2 = id + imageToRead.getHeight();
		                if (points.get(id).compare(points.get(id1)) <= distance)
		                    unionSet(id, id1);
		                if (points.get(id).compare(points.get(id2)) <= distance)
		                    unionSet(id, id2);
		                
		                SplitAndMergeAction.super.incrementProgress();
		            }
		        }
				
		        Color color[] = new Color[ids.size()];
		        
		        for (int i = 0; i < ids.size(); i++) {
		            if (i == ids.get(i)) {
		                color[i] = new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
		            }
		        }
		        
		        for (int i = 0; i < imageToRead.getWidth(); i++) {
		            for (int j = 0; j < imageToRead.getHeight(); j++) {
		                int id = i * imageToRead.getHeight() + j;
		                int parentId = findSet(id);
		                image.setRGB(i, j, color[parentId].getRGB());
		                
		                SplitAndMergeAction.super.incrementProgress();
		            }
		        }
		        
		        SplitAndMergeAction.super.showProgress(false);
			}
		};
		
		thread.start();
	}

	private int findSet(int id) {
		if (id == ids.get(id)) {
			return id;
		}

		ids.set(id, findSet(ids.get(id)));
		return ids.get(id);
	}

	private void unionSet(int id1, int id2) {
		int parentId1 = findSet(id1);
		int parentId2 = findSet(id2);
		ids.set(parentId1, parentId2);
	}

	private void splitImage(PointCIEDE point, int width, int height) {
		boolean flag = true;
		for (int x1 = point.getX(); x1 < point.getX() + width; x1++) {
			for (int y1 = point.getY(); y1 < point.getY() + height; y1++) {
				PointCIEDE newPoint = new PointCIEDE(x1, y1, imageToRead.getRGB(x1, y1));
				if (point.easycmp(newPoint) > 5.)
					flag = false;
			}
		}
		if (flag) {
			for (int i = point.getX(); i < point.getX() + width; i++) {
				for (int j = point.getY(); j < point.getY() + height; j++) {
					int id1 = i * imageToRead.getHeight() + j, id2 = point.getX() * imageToRead.getHeight() + point.getY();
					unionSet(id1, id2);
				}
			}
		} else {
			int w1 = width / 2, h1 = height / 2;
			PointCIEDE point1 = new PointCIEDE(point.getX(), point.getY() + h1,
					imageToRead.getRGB(point.getX(), point.getY() + h1));
			PointCIEDE point2 = new PointCIEDE(point.getX() + w1, point.getY(),
					imageToRead.getRGB(point.getX() + w1, point.getY()));
			PointCIEDE point3 = new PointCIEDE(point.getX() + w1, point.getY() + h1,
					imageToRead.getRGB(point.getX() + w1, point.getY() + h1));
			splitImage(point, w1, h1);
			splitImage(point1, w1, height - h1);
			splitImage(point2, width - w1, h1);
			splitImage(point3, width - w1, height - h1);
		}
	}
}
