package ru.artemiyk.labimages.action;

public interface ProgressListener {
	public void progressChanged(EProgressState progressState);
	public void rangeChanged(int minimum, int maximum);
	public void showProgress(boolean show);
}
