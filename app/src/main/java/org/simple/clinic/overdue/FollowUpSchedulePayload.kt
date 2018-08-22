package org.simple.clinic.overdue

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.sync.SynceablePayload
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.util.UUID

@JsonClass(generateAdapter = true)
data class FollowUpSchedulePayload(
    @Json(name = "id")
    val id: UUID,

    @Json(name = "patient_id")
    val patientId: UUID,

    @Json(name = "facility_id")
    val facilityId: UUID,

    @Json(name = "next_visit")
    val nextVisit: LocalDate,

    @Json(name = "user_action")
    val userAction: FollowUpSchedule.UserAction,

    @Json(name = "action_by_user_id")
    val actionByUserId: UUID,

    @Json(name = "reason_for_action")
    val reasonForAction: FollowUpSchedule.UserActionReason,

    @Json(name = "created_at")
    val createdAt: Instant,

    @Json(name = "updated_at")
    val updatedAt: Instant

) : SynceablePayload<FollowUpSchedule>
