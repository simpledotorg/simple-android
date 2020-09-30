package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import org.simple.clinic.patient.Patient
import org.simple.clinic.teleconsultlog.teleconsultrecord.Answer
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecord
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultationType
import org.simple.clinic.widgets.UiEvent

sealed class TeleconsultRecordEvent : UiEvent

object BackClicked : TeleconsultRecordEvent() {
  override val analyticsName: String = "Teleconsult Record:Back Clicked"
}

data class TeleconsultRecordLoaded(val teleconsultRecord: TeleconsultRecord?) : TeleconsultRecordEvent()

object TeleconsultRecordCreated : TeleconsultRecordEvent()

data class DoneClicked(
    val teleconsultationType: TeleconsultationType,
    val patientTookMedicines: Answer,
    val patientConsented: Answer
) : TeleconsultRecordEvent() {
  override val analyticsName: String = "Teleconsult Record:Done Clicked with Teleconsultation Type: $teleconsultationType"
}

data class PatientDetailsLoaded(
    val patient: Patient
) : TeleconsultRecordEvent()

data class TeleconsultRecordValidated(
    val teleconsultRecordExists: Boolean
) : TeleconsultRecordEvent() {
  override val analyticsName: String = "Teleconsult Record:Validated"
}

object PatientPrescriptionsCloned : TeleconsultRecordEvent()
