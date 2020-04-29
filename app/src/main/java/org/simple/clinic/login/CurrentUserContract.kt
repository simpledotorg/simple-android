package org.simple.clinic.login

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.user.LoggedInUserPayload

@JsonClass(generateAdapter = true)
data class CurrentUserResponse(

    @Json(name = "user")
    val user: LoggedInUserPayload
)
