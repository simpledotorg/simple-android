package org.simple.clinic.monthlyReports.questionnaire.component

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LineSeparatorComponentData(
    @Json(name = "link_id")
    val id: String?,
) : BaseComponentData()
