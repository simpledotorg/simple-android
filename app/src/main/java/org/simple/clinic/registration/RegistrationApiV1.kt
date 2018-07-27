package org.simple.clinic.registration

import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

interface RegistrationApiV1 {

  companion object {
    const val version = "v1"
  }

  @POST("$version/users")
  fun createUser(
      @Body body: RegistrationRequest
  ): Single<RegistrationResponse>
}
