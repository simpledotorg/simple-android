package org.simple.clinic.login

import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.forgotpin.ForgotPinResponse
import org.simple.clinic.forgotpin.ResetPinRequest
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.UUID

interface LoginApiV1 {

  companion object {
    const val version = "v1"
  }

  @POST("$version/login")
  fun login(@Body body: LoginRequest): Single<LoginResponse>

  @POST("$version/users/{userId}/request_otp")
  fun requestLoginOtp(@Path("userId") userId: UUID): Completable

  @POST("$version/me/reset_password")
  fun resetPin(@Body request: ResetPinRequest): Single<ForgotPinResponse>
}
