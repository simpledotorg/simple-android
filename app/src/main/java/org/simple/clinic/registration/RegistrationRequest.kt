package org.simple.clinic.registration

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.user.LoggedInUserPayload

@JsonClass(generateAdapter = true)
data class RegistrationRequest(

    @Json(name = "user")
    val user: LoggedInUserPayload
)
