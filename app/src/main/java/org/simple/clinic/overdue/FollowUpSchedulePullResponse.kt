package org.simple.clinic.overdue

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.sync.DataPullResponse
import org.threeten.bp.Instant

@JsonClass(generateAdapter = true)
data class FollowUpSchedulePullResponse(

    @Json(name = "follow_up_schedules")
    override val payloads: List<FollowUpSchedulePayload>,

    @Json(name = "processed_since")
    override val processedSinceTimestamp: Instant

) : DataPullResponse<FollowUpSchedulePayload>
