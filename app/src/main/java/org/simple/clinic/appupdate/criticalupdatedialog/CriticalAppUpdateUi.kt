package org.simple.clinic.appupdate.criticalupdatedialog

import org.simple.clinic.ContactType

interface CriticalAppUpdateUi {
  fun showHelp()
  fun hideHelp()
  fun renderCriticalAppUpdateReason(appStalenessInMonths: Int)
  fun renderCriticalSecurityAppUpdateReason()
  fun showSupportContactPhoneNumber(number: String, contactType: ContactType)
}
