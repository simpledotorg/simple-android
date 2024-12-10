package org.simple.clinic.cvdrisk.sync

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.sync.DataPullResponse

@JsonClass(generateAdapter = true)
data class CVDRiskPullResponse(

    @Json(name = "cvd_risks")
    override val payloads: List<CVDRiskPayload>,

    @Json(name = "process_token")
    override val processToken: String

) : DataPullResponse<CVDRiskPayload>
