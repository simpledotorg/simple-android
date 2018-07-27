package org.simple.clinic.user

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.threeten.bp.Instant
import java.util.UUID

@JsonClass(generateAdapter = true)
data class LoggedInUser(

    @Json(name = "id")
    val uuid: UUID,

    @Json(name = "full_name")
    val fullName: String,

    @Json(name = "phone_number")
    val phoneNumber: String,

    @Json(name = "password_digest")
    val pinDigest: String,

    @Json(name = "facility_id")
    val facilityUuid: UUID,

    @Json(name = "status")
    val status: Status,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant
) {
    enum class Status {
        @Json(name = "waiting_for_approval")
        WAITING_FOR_APPROVAL
    }
}
