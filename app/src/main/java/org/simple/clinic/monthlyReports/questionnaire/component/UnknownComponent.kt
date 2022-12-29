package org.simple.clinic.monthlyReports.questionnaire.component

import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
class UnknownComponent : BaseComponent()
