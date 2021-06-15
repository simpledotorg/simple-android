package org.simple.clinic.patient

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Languages(
    val languageLocaleCode: String,
    val languageVideoUrl: String
)
