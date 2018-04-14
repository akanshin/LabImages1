package ru.artemiyk.labimages.action;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public abstract class Action {
  private List<ProgressListener> listeners;

  public Action() {
    listeners = new ArrayList<ProgressListener>();
  }

  public void addProgressListener(ProgressListener listener) {
    if (listener == null) {
      return;
    }

    listeners.add(listener);
  }

  protected void setProgressRange(int minimum, int maximum) {
    for (ProgressListener listener : listeners) {
      listener.rangeChanged(minimum, maximum);
    }
  }

  protected void incrementProgress() {
    for (ProgressListener listener : listeners) {
      listener.progressChanged(EProgressState.eApplying);
    }
  }

  protected void showProgress(boolean show) {
    for (ProgressListener listener : listeners) {
      listener.showProgress(show);
    }
  }

  public abstract void produce(BufferedImage image);
}
