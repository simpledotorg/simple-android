package org.simple.clinic.patient.onlinelookup

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Duration

@JsonClass(generateAdapter = true)
data class RecordRetention(
    @Json(name = "type")
    val type: RetentionType,

    @Json(name = "duration_seconds")
    @SecondsDuration
    val retainFor: Duration?
)
