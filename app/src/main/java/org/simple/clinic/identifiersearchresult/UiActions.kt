package org.simple.clinic.identifiersearchresult

import java.util.UUID

interface UiActions {
  fun openPatientSummary(patientUuid: UUID)
  fun openPatientSearch()
}
