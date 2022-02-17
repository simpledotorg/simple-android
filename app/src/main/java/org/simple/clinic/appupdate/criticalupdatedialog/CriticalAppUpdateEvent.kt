package org.simple.clinic.appupdate.criticalupdatedialog

import org.simple.clinic.appupdate.AppUpdateHelpContact
import java.util.Optional

sealed class CriticalAppUpdateEvent

data class AppUpdateHelpContactLoaded(val appUpdateHelpContact: Optional<AppUpdateHelpContact>) : CriticalAppUpdateEvent()
