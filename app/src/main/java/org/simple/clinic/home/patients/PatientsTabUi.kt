package org.simple.clinic.home.patients

interface PatientsTabUi {
  fun hideSyncIndicator()
  fun showSyncIndicator()
  fun showSimpleVideo()
  fun showIllustration()
  fun showUserStatusAsPendingVerification()
  fun hideUserAccountStatus()
  fun showUserStatusAsWaitingForApproval()
  fun renderAppUpdateReason(appStalenessInMonths: Int)
  fun showCriticalAppUpdateCard()
  fun showDrugStockReminderCard()
  fun showPatientLineListDownload(facilityName: String)
  fun hidePatientLineListDownload()
}
