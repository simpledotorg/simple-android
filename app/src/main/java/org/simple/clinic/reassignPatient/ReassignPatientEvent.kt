package org.simple.clinic.reassignPatient

import org.simple.clinic.facility.Facility
import org.simple.clinic.widgets.UiEvent
import java.util.Optional

sealed class ReassignPatientEvent : UiEvent

data class AssignedFacilityLoaded(val facility: Optional<Facility>) : ReassignPatientEvent()

data object NotNowClicked : ReassignPatientEvent() {
  override val analyticsName = "Reassign Patient:Not Now Clicked"
}

data object ChangeClicked : ReassignPatientEvent() {
  override val analyticsName = "Reassign Patient:Change Clicked"
}

data object AssignedFacilityChanged : ReassignPatientEvent()

data class NewAssignedFacilitySelected(val facility: Facility) : ReassignPatientEvent()
