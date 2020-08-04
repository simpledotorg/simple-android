package org.simple.clinic.summary.prescribeddrugs

import org.simple.clinic.drugs.PrescribedDrug
import org.simple.clinic.facility.Facility
import org.simple.clinic.widgets.UiEvent

sealed class DrugSummaryEvent : UiEvent

class PatientSummaryUpdateDrugsClicked : DrugSummaryEvent() {
  override val analyticsName = "Patient Summary:Update Drugs Clicked"
}

data class PrescribedDrugsLoaded(val prescribedDrugs: List<PrescribedDrug>) : DrugSummaryEvent()

data class CurrentFacilityLoaded(val facility: Facility) : DrugSummaryEvent()
