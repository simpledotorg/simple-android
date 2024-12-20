package org.simple.clinic.patientattribute.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PatientAttributePushRequest(

    @Json(name = "patient_attributes")
    val patientAttributes: List<PatientAttributePayload>
)
