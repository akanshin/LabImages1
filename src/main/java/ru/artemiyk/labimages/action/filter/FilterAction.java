package ru.artemiyk.labimages.action.filter;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import ru.artemiyk.labimages.action.Action;
import ru.artemiyk.labimages.ui.IFilterDialog;

public class FilterAction extends Action {

	private IFilterDialog filterDialog = null;
	
	public FilterAction(IFilterDialog filterDialog) {
		this.filterDialog = filterDialog;
	}
	
	@Override
	public void produce(BufferedImage image) {
		BufferedImage originalImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = originalImage.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		
		filterDialog.setImages(originalImage, image);
		filterDialog.setVisible(true);
	}

}
