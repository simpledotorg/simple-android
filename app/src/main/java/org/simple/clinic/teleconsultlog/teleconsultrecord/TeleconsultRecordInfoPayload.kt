package org.simple.clinic.teleconsultlog.teleconsultrecord

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant

@JsonClass(generateAdapter = true)
data class TeleconsultRecordInfoPayload(
    @Json(name = "recorded_at")
    val recordedAt: Instant,

    @Json(name = "teleconsultation_type")
    val teleconsultationType: TeleconsultationType,

    @Json(name = "patient_took_medicines")
    val patientTookMedicines: Answer,

    @Json(name = "patient_consented")
    val patientConsented: Answer,

    @Json(name = "medical_officer_number")
    val medicalOfficerNumber: String
)
