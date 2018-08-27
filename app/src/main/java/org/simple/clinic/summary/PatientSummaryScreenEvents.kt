package org.simple.clinic.summary

import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class PatientSummaryScreenCreated(val patientUuid: UUID, val caller: PatientSummaryCaller) : UiEvent

class PatientSummaryBackClicked : UiEvent {
  override val analyticsName = "Patient Summary:Back Clicked"
}

class PatientSummaryDoneClicked : UiEvent {
  override val analyticsName = "Patient Summary:Done Clicked"
}

class PatientSummaryNewBpClicked : UiEvent {
  override val analyticsName = "Patient Summary:New BP Clicked"
}

class PatientSummaryUpdateDrugsClicked : UiEvent {
  override val analyticsName = "Patient Summary:Update Drugs Clicked"
}
