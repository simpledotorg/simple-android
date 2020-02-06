package org.simple.clinic.summary

import java.util.UUID

interface PatientSummaryUiActions {
  fun showScheduleAppointmentSheet(patientUuid: UUID)
  fun showPatientEditScreen(patientSummaryProfile: PatientSummaryProfile)
}
