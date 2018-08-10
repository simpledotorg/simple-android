package org.simple.clinic.user

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.threeten.bp.Instant
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

    @Json(name = "facility_ids")
    val facilityUuids: List<UUID>,

    // FIXME: This should not default to approved. This is temporarily done.
    @Json(name = "sync_approval_status")
    val status: UserStatus = UserStatus.APPROVED_FOR_SYNCING,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant
)
