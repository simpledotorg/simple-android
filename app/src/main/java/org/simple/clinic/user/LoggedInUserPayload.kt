package org.simple.clinic.user

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant
import java.util.UUID

@JsonClass(generateAdapter = true)
data class LoggedInUserPayload(

    @Json(name = "id")
    val uuid: UUID,

    @Json(name = "full_name")
    val fullName: String,

    @Json(name = "phone_number")
    val phoneNumber: String,

    @Json(name = "password_digest")
    val pinDigest: String,

    @Json(name = "registration_facility_id")
    val registrationFacilityId: UUID,

    @Json(name = "sync_approval_status")
    val status: UserStatus,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant,

    @Json(name = "teleconsult_phone_number")
    val teleconsultPhoneNumber: String?
)
