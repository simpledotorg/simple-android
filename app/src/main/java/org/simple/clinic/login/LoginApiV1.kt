package org.simple.clinic.login

import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginApiV1 {

  companion object {
    const val version = "v1"
  }

  @POST("$version/login")
  fun login(
      @Body body: LoginRequest
  ): Single<LoginResponse>

}
