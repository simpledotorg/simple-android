package org.simple.clinic.recentpatient

import org.simple.clinic.recentpatientsview.RecentPatientItemType

interface AllRecentPatientsUi {
  fun updateRecentPatients(allItemTypes: List<RecentPatientItemType>)
}
