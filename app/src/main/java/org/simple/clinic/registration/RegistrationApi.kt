package org.simple.clinic.registration

import io.reactivex.Single
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface RegistrationApi {

  @POST("v3/users/register")
  fun createUser(
      @Body body: RegistrationRequest
  ): Single<RegistrationResponse>

  @POST("v4/users/find")
  fun findUserByPhoneNumber(
      @Body request: FindUserRequest
  ): Call<FindUserResponse>
}
