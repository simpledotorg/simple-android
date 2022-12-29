package org.simple.clinic.monthlyReports.questionnaire

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class QuestionnaireLayoutComponent(
    @Json(name = "link_id")
    val id: String,

    @Json(name = "type")
    val type: QuestionnaireComponentType,

    @Json(name = "text")
    val text: String?,

    @Json(name = "item")
    val subComponents: List<QuestionnaireLayoutComponent>?,
) : Parcelable

