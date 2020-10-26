package org.simple.clinic.patient.businessid

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
data class BusinessIdMetaData(
    @Json(name = "assigning_user_id")
    val assigningUserUuid: UUID,

    @Json(name = "assigning_facility_id")
    val assigningFacilityUuid: UUID
)
