package org.simple.clinic.patient.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PatientPushRequest(

    @Json(name = "patients")
    val patients: List<PatientPayload>
)
