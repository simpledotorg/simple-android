package org.simple.clinic.monthlyReports.questionnaire.component

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class SubHeaderComponentData(
    @Json(name = "link_id")
    val id: String?,

    @Json(name = "text")
    val text: String
) : BaseComponentData()

