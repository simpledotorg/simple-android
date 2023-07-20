package org.simple.clinic.questionnaire.component.properties

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class InputFieldValidations(
    @Json(name = "min")
    val min: Int? = null,

    @Json(name = "max")
    val max: Int? = null,

    @Json(name = "max_char")
    val maxChar: Int? = null,

    @Json(name = "allowed_days_in_past")
    val allowedDaysInPast: Int? = null,

    @Json(name = "allowed_days_in_future")
    val allowedDaysInFuture: Int? = null
) : Parcelable
