package org.simple.clinic.patient

import org.threeten.bp.LocalDate
import java.io.Serializable

sealed class PatientSummaryResult : Serializable {

  object Saved : PatientSummaryResult()

  object NotSaved : PatientSummaryResult()

  data class Scheduled(val patientName: String, val appointmentDate: LocalDate) : PatientSummaryResult()
}
