package org.simple.clinic.home.patients.links

interface PatientsTabLinkUi {
  fun showOrHideMonthlyScreeningReportsView(isVisible: Boolean)
  fun showOrHideLinkView(isVisible: Boolean)
  fun showOrHidePatientLineListDownload(isVisible: Boolean)
}
