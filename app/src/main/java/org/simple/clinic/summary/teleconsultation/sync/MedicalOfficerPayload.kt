package org.simple.clinic.summary.teleconsultation.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
data class MedicalOfficerPayload(

    @Json(name = "id")
    val id: UUID,

    @Json(name = "full_name")
    val fullName: String,

    @Json(name = "teleconsultation_phone_number")
    val phoneNumber: String
)
