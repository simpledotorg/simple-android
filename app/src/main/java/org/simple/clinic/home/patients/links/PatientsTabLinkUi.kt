package org.simple.clinic.home.patients.links

interface PatientsTabLinkUi {
  fun showOrHideMonthlyScreeningReportsView(isVisible: Boolean)
  fun showOrHideMonthlySuppliesReportsView(isVisible: Boolean)
  fun showOrHideLinkView(isVisible: Boolean)
  fun showOrHidePatientLineListDownload(isVisible: Boolean)
  fun showOrHideDrugStockReportsButton(isVisible: Boolean)
}
