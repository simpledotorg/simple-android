package org.simple.clinic.teleconsultlog.teleconsultrecord.screen

import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecordWithPrescribedDrugs

sealed class TeleconsultRecordEvent

object BackClicked : TeleconsultRecordEvent()

data class TeleconsultRecordWithPrescribedDrugsLoaded(val teleconsultRecordWithPrescribedDrugs: TeleconsultRecordWithPrescribedDrugs?) : TeleconsultRecordEvent()

object TeleconsultRecordCreated : TeleconsultRecordEvent()
