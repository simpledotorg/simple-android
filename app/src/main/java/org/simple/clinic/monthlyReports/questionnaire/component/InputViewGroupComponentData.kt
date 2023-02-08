package org.simple.clinic.monthlyReports.questionnaire.component

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import org.simple.clinic.monthlyReports.questionnaire.component.properties.ComponentDisplayProperties

@JsonClass(generateAdapter = true)
@Parcelize
data class InputViewGroupComponentData(
    @Json(name = "id")
    val id: String?,

    @Json(name = "type")
    val type: String,

    @Json(name = "display_properties")
    val displayProperties: ComponentDisplayProperties?,

    @Json(name = "item")
    val children: List<InputFieldComponentData>?
) : BaseComponentData()
