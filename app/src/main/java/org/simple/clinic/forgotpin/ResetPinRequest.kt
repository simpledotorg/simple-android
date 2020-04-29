package org.simple.clinic.forgotpin

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ResetPinRequest(

    @Json(name = "password_digest")
    val passwordDigest: String
)
