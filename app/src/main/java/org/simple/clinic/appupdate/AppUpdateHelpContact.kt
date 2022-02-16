package org.simple.clinic.appupdate

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppUpdateHelpContact(
    @Json(name = "display_text")
    val displayText: String,
    val url: String
)
