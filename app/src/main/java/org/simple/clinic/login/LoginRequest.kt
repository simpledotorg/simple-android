package org.simple.clinic.login

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(

    @Json(name = "user")
    val user: UserPayload
)

@JsonClass(generateAdapter = true)
data class UserPayload(

    @Json(name = "phone_number")
    val phoneNumber: String,

    @Json(name = "password")
    val pin: String,

    @Json(name = "otp")
    val otp: String
)
