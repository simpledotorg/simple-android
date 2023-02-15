package org.simple.clinic.questionnaire.component

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import org.simple.clinic.questionnaire.component.properties.InputFieldType
import org.simple.clinic.questionnaire.component.properties.InputFieldValidations

@JsonClass(generateAdapter = true)
@Parcelize
data class InputFieldComponentData(
    @Json(name = "id")
    val id: String,

    @Json(name = "link_id")
    val linkId: String,

    @Json(name = "text")
    val text: String,

    @Json(name = "type")
    val type: InputFieldType,

    @Json(name = "validations")
    val validations: InputFieldValidations
) : BaseComponentData()
