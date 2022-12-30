package org.simple.clinic.monthlyReports.questionnaire.component

import android.os.Parcelable
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
open class BaseComponent : Parcelable

