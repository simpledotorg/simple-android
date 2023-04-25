package org.simple.clinic.questionnaire.component

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class RadioButtonComponentData(
    @Json(name = "id")
    val id: String,

    @Json(name = "type")
    val type: String,

    @Json(name = "text")
    val text: String,
) : BaseComponentData()
