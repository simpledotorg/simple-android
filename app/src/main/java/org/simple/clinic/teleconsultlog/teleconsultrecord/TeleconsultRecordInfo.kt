package org.simple.clinic.teleconsultlog.teleconsultrecord

import java.time.Instant

data class TeleconsultRecordInfo(

    val recordedAt: Instant,

    val teleconsultationType: TeleconsultationType,

    val patientTookMedicines: Answer,

    val patientConsented: Answer,

    val medicalOfficerNumber: String?
)
