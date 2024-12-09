package org.simple.clinic.patientattribute.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.sync.DataPullResponse

@JsonClass(generateAdapter = true)
data class PatientAttributePullResponse(

    @Json(name = "patient_attributes")
    override val payloads: List<PatientAttributePayload>,

    @Json(name = "process_token")
    override val processToken: String

) : DataPullResponse<PatientAttributePayload>

