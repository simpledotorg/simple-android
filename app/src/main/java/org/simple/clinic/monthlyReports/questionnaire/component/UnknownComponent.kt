package org.simple.clinic.monthlyReports.questionnaire.component

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UnknownComponent(
    override val type: String = ""
) : BaseComponent(type = type)
