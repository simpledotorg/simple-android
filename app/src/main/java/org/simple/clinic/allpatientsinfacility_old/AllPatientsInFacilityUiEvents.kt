package org.simple.clinic.allpatientsinfacility_old

import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class AllPatientsInFacilitySearchResultClicked(val patientUuid: UUID) : UiEvent {
  override val analyticsName: String = "All Patients In Facility:Search Result Clicked"
}

object AllPatientsInFacilityListScrolled : UiEvent {
  override val analyticsName: String = "All Patients In Facility:List Scrolled"
}
