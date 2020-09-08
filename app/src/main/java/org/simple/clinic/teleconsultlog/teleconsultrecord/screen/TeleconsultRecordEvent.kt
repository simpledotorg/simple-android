package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import org.simple.clinic.teleconsultlog.teleconsultrecord.Answer
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecordWithPrescribedDrugs
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultationType
import org.simple.clinic.widgets.UiEvent

sealed class TeleconsultRecordEvent : UiEvent

object BackClicked : TeleconsultRecordEvent() {
  override val analyticsName: String = "Teleconsult Record:Back Clicked"
}

data class TeleconsultRecordWithPrescribedDrugsLoaded(val teleconsultRecordWithPrescribedDrugs: TeleconsultRecordWithPrescribedDrugs?) : TeleconsultRecordEvent()

object TeleconsultRecordCreated : TeleconsultRecordEvent()

data class DoneClicked(
    val teleconsultationType: TeleconsultationType,
    val patientTookMedicines: Answer,
    val patientConsented: Answer
) : TeleconsultRecordEvent() {
  override val analyticsName: String = "Teleconsult Record:Done Clicked"
}
