package org.simple.clinic.overdue

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FollowUpSchedulePushRequest(

    @Json(name = "follow_up_schedules")
    val schedules: List<FollowUpSchedulePayload>
)

