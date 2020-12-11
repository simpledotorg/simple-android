package org.simple.clinic.shortcodesearchresult

import java.util.UUID

interface UiActions {
  fun openPatientSummary(patientUuid: UUID)
  fun openPatientSearch()
}
