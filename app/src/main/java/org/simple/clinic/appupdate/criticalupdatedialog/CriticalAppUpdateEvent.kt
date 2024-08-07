package org.simple.clinic.appupdate.criticalupdatedialog

import org.simple.clinic.appupdate.AppUpdateHelpContact
import org.simple.clinic.widgets.UiEvent
import java.util.Optional

sealed class CriticalAppUpdateEvent : UiEvent

data class AppUpdateHelpContactLoaded(val appUpdateHelpContact: Optional<AppUpdateHelpContact>) : CriticalAppUpdateEvent()

data object ContactHelpClicked : CriticalAppUpdateEvent() {
  override val analyticsName: String = "Critical App Update Dialog:Contact Help Clicked"
}

data object UpdateAppClicked : CriticalAppUpdateEvent() {
  override val analyticsName: String = "Critical App Update Dialog:Update App Clicked"
}

data class AppStalenessLoaded(val appStaleness: Int) : CriticalAppUpdateEvent()
