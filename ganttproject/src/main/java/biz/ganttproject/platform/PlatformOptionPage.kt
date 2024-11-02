/*
Copyright 2019 BarD Software s.r.o

This file is part of GanttProject, an open-source project management tool.

GanttProject is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

GanttProject is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with GanttProject.  If not, see <http://www.gnu.org/licenses/>.
*/
package biz.ganttproject.platform

import biz.ganttproject.FXUtil
import biz.ganttproject.FxUiComponent
import biz.ganttproject.app.DIALOG_STYLESHEET
import biz.ganttproject.app.DialogControllerPane
import biz.ganttproject.core.option.GPOptionGroup
import com.bardsoftware.eclipsito.update.UpdateMetadata
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import net.sourceforge.ganttproject.GPLogger
import net.sourceforge.ganttproject.gui.options.OptionPageProviderBase
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JPanel
import org.eclipse.core.runtime.Platform as Eclipsito

/**
 * @author dbarashev@bardsoftware.com
 */
class PlatformOptionPageProvider : OptionPageProviderBase("platform"), FxUiComponent {
  private var onSetActive: (() -> Unit)? = null

  override fun buildNode(): Node = StackPane().also {stackPane ->
    Eclipsito.getUpdater().getUpdateMetadata(UpdateOptions.updateUrl.value).thenAccept { updateMetadata ->
      onSetActive = {
        FXUtil.runLater {
          stackPane.children.add(createSceneContent(updateMetadata, null))
        }
      }
      if (isActive) {
        // We need to trigger onSetActive if we are already in the active state.
        isActive = true
      }

    }.exceptionally { ex ->
      GPLogger.logToLogger(ex)
      FXUtil.runLater {
        stackPane.children.add(createSceneContent(emptyList(), ex))
      }
      null
    }
  }

  override fun getOptionGroups(): Array<GPOptionGroup> {
    return arrayOf()
  }

  override fun hasCustomComponent(): Boolean  = true

  override fun buildPageComponent(): Component {
    val jfxPanel = JFXPanel()
    val wrapper = JPanel(BorderLayout())
    wrapper.add(jfxPanel, BorderLayout.CENTER)
    Eclipsito.getUpdater().getUpdateMetadata(UpdateOptions.updateUrl.value).thenAccept { updateMetadata ->
      onSetActive = {
        Platform.runLater {
          jfxPanel.scene = createScene(updateMetadata, null)
          jfxPanel.scene.root.layout()
        }
      }
    }.exceptionally { ex ->
      GPLogger.logToLogger(ex)
      Platform.runLater {
        jfxPanel.scene = createScene(emptyList(), ex)
      }
      null
    }
    return wrapper
  }

  override fun setActive(isActive: Boolean) {
    super.setActive(isActive)
    if (isActive) {
      onSetActive?.invoke()
      onSetActive = null
    }
  }

  private fun createScene(updateMetadata: List<UpdateMetadata>, ex: Throwable?): Scene {
    return Scene(createSceneContent(updateMetadata, ex))
  }

  private fun createSceneContent(updateMetadata: List<UpdateMetadata>, ex: Throwable?): Parent {
    val runningVersion = Eclipsito.getUpdater().installedUpdateVersions.maxOrNull() ?: "2900"
    val runningUpdateMetadata = UpdateMetadata(
      runningVersion,
      null, null, null, 0, "", false)
    val filteredUpdates = updateMetadata.filter { it > runningUpdateMetadata }

    val group = BorderPane().also {
      it.styleClass.addAll("dlg-information", "dlg", "dialog-pane", "border-etched")
      it.stylesheets.addAll(DIALOG_STYLESHEET)
    }
    val dialogBuildApi = DialogControllerPane(group)
    if (ex != null) {
      updatesFetchErrorDialog(ex, dialogBuildApi)
    } else {
      val updateModel = UpdateDialogModel(filteredUpdates, filteredUpdates, restarter = { uiFacade.quitApplication(false) })
      updatesAvailableDialog(updateModel, dialogBuildApi)
    }
    return group
  }

}

