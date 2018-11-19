package org.simple.clinic.patient

import java.io.Serializable
import java.util.UUID

sealed class PatientSummaryResult : Serializable {

  data class Saved(val patientUuid: UUID) : PatientSummaryResult()

  object NotSaved : PatientSummaryResult()

  data class Scheduled(val patientUuid: UUID) : PatientSummaryResult()
}
