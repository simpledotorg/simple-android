package org.simple.clinic.monthlyReports.questionnaire.component

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class ViewGroup(
    @Json(name = "link_id")
    val id: String,

    @Json(name = "item")
    val children: List<BaseComponent>?
) : BaseComponent()
