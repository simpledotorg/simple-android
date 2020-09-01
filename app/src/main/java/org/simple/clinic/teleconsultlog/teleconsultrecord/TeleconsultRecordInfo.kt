package org.simple.clinic.teleconsultlog.teleconsultrecord

import org.simple.clinic.medicalhistory.Answer

data class TeleconsultRecordInfo(

    val recordedAt: String,

    val teleconsultationType: TeleconsultationType,

    val patientTookMedicines: Answer,

    val patientConsented: Answer,

    val medicalOfficerNumber: String
)
