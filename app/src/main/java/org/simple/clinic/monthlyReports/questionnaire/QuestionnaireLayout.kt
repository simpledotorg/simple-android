package org.simple.clinic.monthlyReports.questionnaire

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class QuestionnaireLayout(
    @Json(name = "item")
    val components: List<QuestionnaireLayoutComponent>
) : Parcelable
