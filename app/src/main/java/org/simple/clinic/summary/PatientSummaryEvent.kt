package org.simple.clinic.summary

import org.simple.clinic.widgets.UiEvent

sealed class PatientSummaryEvent : UiEvent

data class PatientSummaryProfileLoaded(val patientSummaryProfile: PatientSummaryProfile) : PatientSummaryEvent()

class PatientSummaryBackClicked : PatientSummaryEvent() {
  override val analyticsName = "Patient Summary:Back Clicked"
}

class PatientSummaryDoneClicked : PatientSummaryEvent() {
  override val analyticsName = "Patient Summary:Done Clicked"
}
