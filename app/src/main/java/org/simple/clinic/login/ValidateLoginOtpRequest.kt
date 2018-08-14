package org.simple.clinic.login

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ValidateLoginOtpRequest(
    @Json(name = "uid")
    val userId: String,

    @Json(name = "password_digest")
    val passwordDigest: String,

    @Json(name = "otp")
    val otp: String
)
