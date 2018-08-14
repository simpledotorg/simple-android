package org.simple.clinic.login

import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginApiV1 {

  companion object {
    const val version = "v1"
  }

  @POST("$version/login")
  fun login(@Body body: LoginRequest): Single<LoginResponse>

  @POST("$version/requestOtp")
  fun requestLoginOtp(@Body body: SendLoginOtpRequest): Completable

  @POST("$version/validateLoginOtp")
  fun validateLoginOtp(@Body body: ValidateLoginOtpRequest): Single<LoginResponse>
}
