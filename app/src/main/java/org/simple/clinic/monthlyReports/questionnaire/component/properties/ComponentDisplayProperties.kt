package org.simple.clinic.monthlyReports.questionnaire.component.properties

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class ComponentDisplayProperties(
    @Json(name = "orientation")
    val orientation: OrientationType
) : Parcelable
