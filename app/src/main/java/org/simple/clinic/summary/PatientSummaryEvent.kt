package org.simple.clinic.summary

import org.simple.clinic.widgets.UiEvent

sealed class PatientSummaryEvent : UiEvent

data class PatientSummaryProfileLoaded(val patientSummaryProfile: PatientSummaryProfile) : PatientSummaryEvent()
