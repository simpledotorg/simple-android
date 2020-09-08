package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import org.simple.clinic.teleconsultlog.teleconsultrecord.Answer
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecordWithPrescribedDrugs
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultationType

sealed class TeleconsultRecordEvent

object BackClicked : TeleconsultRecordEvent()

data class TeleconsultRecordWithPrescribedDrugsLoaded(val teleconsultRecordWithPrescribedDrugs: TeleconsultRecordWithPrescribedDrugs?) : TeleconsultRecordEvent()

object TeleconsultRecordCreated : TeleconsultRecordEvent()

data class DoneClicked(
    val teleconsultationType: TeleconsultationType,
    val patientTookMedicines: Answer,
    val patientConsented: Answer
) : TeleconsultRecordEvent()
