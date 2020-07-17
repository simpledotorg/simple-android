package org.simple.clinic.recentpatientsview

interface LatestRecentPatientsUi {
  fun updateRecentPatients(recentPatients: List<RecentPatientItemType>)
  fun showOrHideRecentPatients(isVisible: Boolean)
}
