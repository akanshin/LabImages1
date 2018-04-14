package ru.artemiyk.labimages.ui;

import java.awt.image.BufferedImage;

public interface IFilterDialog {
  public void setImages(BufferedImage imageToRead, BufferedImage imageToWrite);

  public void setVisible(boolean visible);
}
