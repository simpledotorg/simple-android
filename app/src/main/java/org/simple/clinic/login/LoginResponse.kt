package org.simple.clinic.login

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.user.LoggedInUser

@JsonClass(generateAdapter = true)
data class LoginResponse(

    @Json(name = "access_token")
    val accessToken: String,

    @Json(name = "user")
    val loggedInUser: LoggedInUser,

    @Json(name = "errors")
    val errors: LoginError?
)

@JsonClass(generateAdapter = true)
data class LoginError(

    @Json(name = "user")
    val errorStrings: List<String>
)
