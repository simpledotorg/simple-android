package org.simple.clinic.teleconsultlog.teleconsultrecord

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TeleconsultPushRequest(

    @Json(name = "teleconsultations")
    val records: List<TeleconsultRecordPayload>
)
