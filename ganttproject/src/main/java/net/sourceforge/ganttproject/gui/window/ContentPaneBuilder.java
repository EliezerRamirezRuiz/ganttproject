/*
GanttProject is an opensource project management tool. License: GPL3
Copyright (C) 2011 Dmitry Barashev

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.sourceforge.ganttproject.gui.window;

import biz.ganttproject.app.FXToolbarBuilder;
import biz.ganttproject.app.ViewPane;
import biz.ganttproject.lib.fx.VBoxBuilder;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Priority;
import net.java.balloontip.BalloonTip;
import net.java.balloontip.styles.EdgedBalloonStyle;
import net.sourceforge.ganttproject.gui.GanttStatusBar;
import net.sourceforge.ganttproject.gui.NotificationComponent;
import net.sourceforge.ganttproject.gui.NotificationComponent.AnimationView;

import javax.swing.*;
import java.awt.*;

/**
 * Builds a main frame's content pane and creates an animation host for the
 * notification slider.
 *
 * @author dbarashev (Dmitry Barashev)
 */
public class ContentPaneBuilder {
  //private final GanttTabbedPane myTabbedPane;
  private final GanttStatusBar myStatusBar;
  private final AnimationHostImpl myAnimationHost = new AnimationHostImpl();
  private final JFXPanel myMenuPanel;
  private final ViewPane myViewPane;

  public ContentPaneBuilder(ViewPane viewPane, GanttStatusBar statusBar) {
    //myTabbedPane = tabbedPane;
    myStatusBar = statusBar;
    myMenuPanel = new JFXPanel();
    myViewPane = viewPane;
  }

  public void build(FXToolbarBuilder toolbar, Container contentPane) {
    var jfxPanel = new JFXPanel();
    var vbox = new VBoxBuilder();
    var menuBar = new MenuBar();
    var fileMenu = new Menu("File");
    fileMenu.getItems().add(new MenuItem("Open..."));
    fileMenu.getItems().add(new MenuItem("New..."));
    menuBar.getMenus().add(fileMenu);
    vbox.add(menuBar);
    vbox.add(toolbar.build().getToolbar$ganttproject());
    vbox.add(myViewPane.createComponent(), null, Priority.ALWAYS);
    JPanel contentPanel = new JPanel(new BorderLayout());

//    JPanel topPanel = new JPanel(new BorderLayout());
//
//    topPanel.add(myMenuPanel, BorderLayout.NORTH);
//    topPanel.add(toolbar, BorderLayout.CENTER);
//    contentPanel.add(topPanel, BorderLayout.NORTH);
    //contentPanel.add(myTabbedPane, BorderLayout.CENTER);
    Platform.runLater(() -> {
      var scene = new Scene(vbox.getVbox());
      jfxPanel.setScene(scene);
    });
    contentPanel.add(jfxPanel, BorderLayout.CENTER);
//    contentPanel.add(myStatusBar, BorderLayout.SOUTH);
    contentPane.add(contentPanel);
  }

//  public void setMenu(MenuBar menu) {
//    Scene menuBarScene = new Scene(menu, javafx.scene.paint.Color.TRANSPARENT);
//    myMenuPanel.setScene(menuBarScene);
//  }

  public class AnimationHostImpl implements AnimationView {
    private BalloonTip myBalloon;
    private Runnable myOnHide;

    @Override
    public boolean isReady() {
      return myStatusBar.isShowing();
    }

    @Override
    public void setComponent(JComponent component, JComponent owner, final Runnable onHide) {
      myBalloon = new BalloonTip(owner, component, new EdgedBalloonStyle(Color.WHITE, Color.BLACK),
          BalloonTip.Orientation.LEFT_ABOVE, BalloonTip.AttachLocation.ALIGNED, 30, 10, false);
      myOnHide = onHide;
      myBalloon.setVisible(true);
    }

    @Override
    public void close() {
      myBalloon.setVisible(false);
      myOnHide.run();
    }

    @Override
    public boolean isVisible() {
      return myBalloon != null && myBalloon.isVisible();
    }
  }

  public NotificationComponent.AnimationView getAnimationHost() {
    return myAnimationHost;
  }
}
