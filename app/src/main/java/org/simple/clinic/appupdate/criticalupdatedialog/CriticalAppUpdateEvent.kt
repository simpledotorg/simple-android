package org.simple.clinic.appupdate.criticalupdatedialog

import org.simple.clinic.appupdate.AppUpdateHelpContact
import org.simple.clinic.widgets.UiEvent
import java.util.Optional

sealed class CriticalAppUpdateEvent : UiEvent

data class AppUpdateHelpContactLoaded(val appUpdateHelpContact: Optional<AppUpdateHelpContact>) : CriticalAppUpdateEvent()

object ContactHelpClicked : CriticalAppUpdateEvent() {
  override val analyticsName: String = "Critical App Update Dialog:Contact Help Clicked"
}
