package org.simple.clinic.registration

import io.reactivex.Single
import org.simple.clinic.user.LoggedInUserPayload
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RegistrationApiV1 {

  companion object {
    const val version = "v3"
  }

  @GET("$version/users/find")
  fun findUser(
      @Query("phone_number") phoneNumber: String
  ): Single<LoggedInUserPayload>

  @POST("$version/users/register")
  fun createUser(
      @Body body: RegistrationRequest
  ): Single<RegistrationResponse>
}
