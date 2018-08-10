package org.simple.clinic.registration

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.user.LoggedInUser

@JsonClass(generateAdapter = true)
data class RegistrationResponse(

    @Json(name = "user")
    val loggedInUser: LoggedInUser
)
