package ru.artemiyk.labimages.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import ru.artemiyk.labimages.action.Action;
import ru.artemiyk.labimages.action.EProgressState;
import ru.artemiyk.labimages.action.ProgressListener;
import ru.artemiyk.labimages.action.filter.FilterAction;
import ru.artemiyk.labimages.action.segmentation.MeanShiftAction;
import ru.artemiyk.labimages.action.segmentation.SplitAndMergeAction;

public class MainWindow extends JFrame {
  /**
   * Default serial verstion uid
   */
  private static final long serialVersionUID = 1L;

  private int width;
  private int height;
  private int displayWidth;
  private int displayHeight;

  private ImagePanel imagePanel;
  private StatusBar statusBar;

  private Color toolBarColor = Color.WHITE;
  private JToolBar toolBar;
  private JButton openButton;
  private JButton saveButton;
  private JButton defaultViewButton;
  private JButton expandButton;
  private JButton showMassiveButton;
  private JButton showHistButton;
  private JButton changeHSVButton;
  private JButton gaussianBlurButton;
  private JButton gaborFilterButton;
  private JButton sobelFilterButton;
  private JButton setDefaultImageButton;
  private JButton selectAllButton;
  private JButton splitAndMergeButton;
  private JButton meanShiftButton;
  private JButton questionButton;

  private File lastOpennedFile;

  private static String questionString = "Click left mouse button to select pixel\n"
      + "Press left button and drag mouse to select area\n" + "Click right button to clear selection\n"
      + "Press wheel and drag mouse to move image\n" + "Rotate mouse wheel to zoom\n\n" + "Good luck! :)";

  private final JFileChooser fileChooser;

  public MainWindow(String windowName, int width, int height) {
    super(windowName);

    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    this.width = width;
    this.height = height;

    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    displayWidth = gd.getDisplayMode().getWidth();
    displayHeight = gd.getDisplayMode().getHeight();
    this.setBounds(displayWidth / 2 - this.width / 2, displayHeight / 2 - this.height / 2, this.width, this.height);

    this.buildToolBar();
    this.buildImagePanel();

    statusBar = new StatusBar();
    this.getContentPane().add(statusBar, BorderLayout.SOUTH);

    fileChooser = new JFileChooser();
    fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("JPEG", "jpg", "jpeg"));
    fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG", "png"));
    fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("GIF", "gif"));
  }

  public void buildImagePanel() {
    imagePanel = new ImagePanel();
    imagePanel.setBorder(BorderFactory.createBevelBorder(1));
    this.getContentPane().add(imagePanel, BorderLayout.CENTER);
  }

  public ImagePanel getImagePanel() {
    return imagePanel;
  }

  private void buildToolBar() {
    toolBar = new JToolBar();
    toolBar.setFloatable(false);
    toolBar.setBackground(toolBarColor);

    openButton = new JButton();
    openButton.setBackground(toolBarColor);
    openButton.setToolTipText("Open image");
    toolBar.add(openButton);
    openButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("open.png")));
    openButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        onOpen();
      }
    });

    saveButton = new JButton();
    saveButton.setBackground(toolBarColor);
    saveButton.setToolTipText("Save image");
    toolBar.add(saveButton);
    saveButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("save.png")));
    saveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        onSave();
      }
    });

    toolBar.addSeparator();

    defaultViewButton = new JButton();
    defaultViewButton.setBackground(toolBarColor);
    defaultViewButton.setToolTipText("Default scale");
    toolBar.add(defaultViewButton);
    defaultViewButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("default_view.png")));
    defaultViewButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        imagePanel.setDefaultView();
      }
    });

    expandButton = new JButton();
    expandButton.setBackground(toolBarColor);
    expandButton.setToolTipText("Scale to window");
    toolBar.add(expandButton);
    expandButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("expand.png")));
    expandButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        imagePanel.setAllView();
      }
    });

    toolBar.addSeparator();

    selectAllButton = new JButton();
    selectAllButton.setBackground(toolBarColor);
    selectAllButton.setToolTipText("Select all");
    toolBar.add(selectAllButton);
    selectAllButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("select.png")));
    selectAllButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        imagePanel.selectAll();
      }
    });

    showMassiveButton = new JButton();
    showMassiveButton.setBackground(toolBarColor);
    showMassiveButton.setToolTipText("Show massive");
    toolBar.add(showMassiveButton);
    showMassiveButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("show_rgb.png")));
    showMassiveButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        imagePanel.showMassive();
      }
    });

    showHistButton = new JButton();
    showHistButton.setBackground(toolBarColor);
    showHistButton.setToolTipText("Show histogram");
    toolBar.add(showHistButton);
    showHistButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("hist.png")));
    showHistButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        imagePanel.showHist();
      }
    });

    toolBar.addSeparator();

    changeHSVButton = new JButton();
    changeHSVButton.setBackground(toolBarColor);
    changeHSVButton.setToolTipText("Change HSV");
    toolBar.add(changeHSVButton);
    changeHSVButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("hsv.png")));
    changeHSVButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        HSVTranformatorDialog hsvDialog = new HSVTranformatorDialog(imagePanel.getImage());
        hsvDialog.setVisible(true);
      }
    });

    gaussianBlurButton = new JButton();
    gaussianBlurButton.setBackground(toolBarColor);
    gaussianBlurButton.setToolTipText("Gaussian blur");
    toolBar.add(gaussianBlurButton);
    gaussianBlurButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("gaussian_blur.png")));
    gaussianBlurButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        imagePanel.produceAction(new FilterAction(new GaussianBlurDialog()));
      }
    });

    gaborFilterButton = new JButton();
    gaborFilterButton.setBackground(toolBarColor);
    gaborFilterButton.setToolTipText("Gabor filter");
    toolBar.add(gaborFilterButton);
    gaborFilterButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("gabor_filter.png")));
    gaborFilterButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        imagePanel.produceAction(new FilterAction(new GaborFilterDialog()));
      }
    });

    sobelFilterButton = new JButton();
    sobelFilterButton.setBackground(toolBarColor);
    sobelFilterButton.setToolTipText("Sobel filter");
    toolBar.add(sobelFilterButton);
    sobelFilterButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("sobel_filter.png")));
    sobelFilterButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        imagePanel.produceAction(new FilterAction(new SobelFilterDialog()));
      }
    });

    splitAndMergeButton = new JButton();
    splitAndMergeButton.setBackground(toolBarColor);
    splitAndMergeButton.setToolTipText("Split and Merge");
    toolBar.add(splitAndMergeButton);
    splitAndMergeButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("split_and_merge.png")));
    splitAndMergeButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        Action action = new SplitAndMergeAction();
        action.addProgressListener(new ProgressListener() {
          @Override
          public void progressChanged(EProgressState progressState) {
            inc();
          }

          private synchronized void inc() {
            int val = statusBar.getProgressBar().getValue();
            if (val >= statusBar.getProgressBar().getMaximum()) {
              return;
            }
            val++;
            statusBar.getProgressBar().setValue(val);
          }

          @Override
          public void rangeChanged(int minimum, int maximum) {
            statusBar.getProgressBar().setMinimum(minimum);
            statusBar.getProgressBar().setMaximum(maximum);
            statusBar.getProgressBar().setValue(minimum);
          }

          @Override
          public void showProgress(boolean show) {
            splitAndMergeButton.setEnabled(!show);
            statusBar.getProgressBar().setVisible(show);
            if (!show) {
              imagePanel.repaint();
            }
          }
        });

        imagePanel.produceAction(action);
      }
    });

    meanShiftButton = new JButton();
    meanShiftButton.setBackground(toolBarColor);
    meanShiftButton.setToolTipText("Mean shift");
    toolBar.add(meanShiftButton);
    meanShiftButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("mean_shift.png")));
    meanShiftButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        Action action = new MeanShiftAction();
        action.addProgressListener(new ProgressListener() {
          @Override
          public void progressChanged(EProgressState progressState) {
            inc();
          }

          private synchronized void inc() {
            int val = statusBar.getProgressBar().getValue();
            if (val >= statusBar.getProgressBar().getMaximum()) {
              return;
            }
            val++;
            statusBar.getProgressBar().setValue(val);
          }

          @Override
          public void rangeChanged(int minimum, int maximum) {
            statusBar.getProgressBar().setMinimum(minimum);
            statusBar.getProgressBar().setMaximum(maximum);
            statusBar.getProgressBar().setValue(minimum);
          }

          @Override
          public void showProgress(boolean show) {
            meanShiftButton.setEnabled(!show);
            statusBar.getProgressBar().setVisible(show);
            if (!show) {
              imagePanel.repaint();
            }
          }
        });

        imagePanel.produceAction(action);
      }
    });

    setDefaultImageButton = new JButton();
    setDefaultImageButton.setBackground(toolBarColor);
    setDefaultImageButton.setToolTipText("Default HSV");
    toolBar.add(setDefaultImageButton);
    setDefaultImageButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("set_default.png")));
    setDefaultImageButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        imagePanel.setDefaultImage();
      }
    });

    toolBar.addSeparator();

    questionButton = new JButton();
    questionButton.setBackground(toolBarColor);
    questionButton.setToolTipText("If you don't know what to do, click!");
    toolBar.add(questionButton);
    questionButton.setIcon(new ImageIcon(getClass().getClassLoader().getResource("question.png")));
    questionButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent arg0) {
        JOptionPane.showMessageDialog(MainWindow.this, questionString);
      }
    });

    Container contentPane = this.getContentPane();
    contentPane.add(toolBar, BorderLayout.NORTH);
  }

  private void onOpen() {
    int returnVal = fileChooser.showOpenDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      imagePanel.loadImage(lastOpennedFile = fileChooser.getSelectedFile());
    }
  }

  private void onSave() {
    if (lastOpennedFile != null) {
      fileChooser.setSelectedFile(lastOpennedFile);
    }

    int returnVal = fileChooser.showSaveDialog(this);
    if (returnVal == JFileChooser.APPROVE_OPTION) {
      FileFilter filter = fileChooser.getFileFilter();
      String description = filter.getDescription();
      String extension;
      if (description.equals("JPEG")) {
        extension = "jpg";
      } else if (description.equals("PNG")) {
        extension = "png";
      } else if (description.equals("GIF")) {
        extension = "gif";
      } else {
        extension = "png";
      }

      lastOpennedFile = fileChooser.getSelectedFile();
      if (!lastOpennedFile.getPath().endsWith("." + extension)) {
        String path = lastOpennedFile.getAbsolutePath();
        path += "." + extension;
        lastOpennedFile = new File(path);
      }

      if (!lastOpennedFile.exists()) {
        try {
          lastOpennedFile.createNewFile();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      imagePanel.saveImage(extension, lastOpennedFile);
    }
  }

  public void showMessage(String message) {
    JOptionPane.showMessageDialog(this, message, "Message", JOptionPane.ERROR_MESSAGE);
  }

  public StatusBar getStatusBar() {
    return statusBar;
  }

  public void setEnableSelectionButtons(boolean enable) {
    showMassiveButton.setEnabled(enable);
    showHistButton.setEnabled(enable);
  }
}
