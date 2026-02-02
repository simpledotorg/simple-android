package org.simple.clinic.patient.medicalRecords

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.patient.CompleteMedicalRecord
import org.simple.clinic.patient.onlinelookup.api.CompleteMedicalRecordPayload

@JsonClass(generateAdapter = true)
data class CompleteMedicalRecordsPushRequest(

    @Json(name = "patients")
    val patients: List<CompleteMedicalRecordPayload>
)
