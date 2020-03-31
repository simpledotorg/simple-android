package org.simple.clinic.registration

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.user.UserStatus
import java.util.UUID

@JsonClass(generateAdapter = true)
data class FindUserRequest(

    @Json(name = "phone_number")
    val phoneNumber: String
)

@JsonClass(generateAdapter = true)
data class FindUserResponse(

    @Json(name = "user")
    val body: Body
) {

  @JsonClass(generateAdapter = true)
  data class Body(
      @Json(name = "id")
      val userUuid: UUID,

      @Json(name = "sync_approval_status")
      val status: UserStatus
  )
}
