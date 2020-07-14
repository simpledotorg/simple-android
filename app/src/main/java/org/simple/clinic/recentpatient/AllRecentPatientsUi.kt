package org.simple.clinic.recentpatient

import org.simple.clinic.recentpatientsview.RecentPatientItemType

interface AllRecentPatientsUi : AllRecentPatientsUiActions {
  fun updateRecentPatients(allItemTypes: List<RecentPatientItemType>)
}
