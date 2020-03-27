package org.simple.clinic.login.activateuser

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.user.LoggedInUserPayload
import java.util.UUID

@JsonClass(generateAdapter = true)
data class ActivateUserRequest(

    @Json(name = "user")
    val body: Body
) {

  companion object {
    fun create(userUuid: UUID, pin: String): ActivateUserRequest {
      return ActivateUserRequest(Body(userUuid, pin))
    }
  }

  @JsonClass(generateAdapter = true)
  data class Body(

      @Json(name = "id")
      val id: UUID,

      @Json(name = "password")
      val pin: String
  )
}

@JsonClass(generateAdapter = true)
data class ActivateUserResponse(

    @Json(name = "user")
    val user: LoggedInUserPayload
)


