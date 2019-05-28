package org.simple.clinic.registration

import io.reactivex.Single
import org.simple.clinic.user.LoggedInUserPayload
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RegistrationApi {

  @GET("users/find")
  fun findUser(
      @Query("phone_number") phoneNumber: String
  ): Single<LoggedInUserPayload>

  @POST("users/register")
  fun createUser(
      @Body body: RegistrationRequest
  ): Single<RegistrationResponse>
}
