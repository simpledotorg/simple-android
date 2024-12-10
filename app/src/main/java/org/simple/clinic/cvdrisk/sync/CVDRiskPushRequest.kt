package org.simple.clinic.cvdrisk.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CVDRiskPushRequest(

    @Json(name = "cvd_risks")
    val cvdRisks: List<CVDRiskPayload>
)
