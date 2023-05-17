package org.simple.clinic.questionnaire.component.properties

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class InputFieldValidations(
    @Json(name = "min")
    val min: Int?,

    @Json(name = "max")
    val max: Int?,

    @Json(name = "max_char")
    val maxChar: Int?,
) : Parcelable
