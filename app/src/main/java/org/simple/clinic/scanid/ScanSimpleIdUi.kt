package org.simple.clinic.scanid

interface ScanSimpleIdUi {
  fun showSearchingForPatient()
  fun hideSearchingForPatient()
  fun hideScanError()
  fun showScanError()
  fun hideEnteredCodeContainerView()
  fun setToolBarTitle(openedFrom: OpenedFrom)
}
