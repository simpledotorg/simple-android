package org.simple.clinic.login

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.user.LoggedInUserPayload

@JsonClass(generateAdapter = true)
data class LoginResponse(

    @Json(name = "access_token")
    val accessToken: String,

    @Json(name = "user")
    val loggedInUser: LoggedInUserPayload
)

@JsonClass(generateAdapter = true)
data class LoginErrorResponse(

    @Json(name = "errors")
    val errors: LoginErrors
) {

  fun firstError() = errors.errorStrings.first()
}

@JsonClass(generateAdapter = true)
data class LoginErrors(

    @Json(name = "user")
    val errorStrings: List<String>
)
