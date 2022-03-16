package org.simple.clinic.appupdate.criticalupdatedialog

interface CriticalAppUpdateUi {
  fun showHelp()
  fun hideHelp()
  fun renderCriticalAppUpdateReason(appStalenessInMonths: Int)
  fun renderCriticalSecurityAppUpdateReason()
}
