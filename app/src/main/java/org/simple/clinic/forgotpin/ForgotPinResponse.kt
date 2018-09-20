package org.simple.clinic.forgotpin

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.user.LoggedInUserPayload

@JsonClass(generateAdapter = true)
data class ForgotPinResponse(

    @Json(name = "access_token")
    val accessToken: String,

    @Json(name = "user")
    val loggedInUser: LoggedInUserPayload
)
