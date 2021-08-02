package org.simple.clinic.simplevideo

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SimpleVideo(
    val url: String,
    val duration: String
)
