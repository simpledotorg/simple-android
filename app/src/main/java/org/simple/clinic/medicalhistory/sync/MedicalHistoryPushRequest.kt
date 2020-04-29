package org.simple.clinic.medicalhistory.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MedicalHistoryPushRequest(

    @Json(name = "medical_histories")
    val histories: List<MedicalHistoryPayload>
)
