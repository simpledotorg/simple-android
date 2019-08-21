package org.simple.clinic.shortcodesearchresult

import java.util.UUID

interface ShortCodeSearchResultUi {
  fun openPatientSummary(patientUuid: UUID)
}
