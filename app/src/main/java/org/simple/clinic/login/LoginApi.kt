package org.simple.clinic.login

import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.CURRENT_API_VERSION
import org.simple.clinic.forgotpin.ForgotPinResponse
import org.simple.clinic.forgotpin.ResetPinRequest
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.UUID

interface LoginApi {

  @POST("$CURRENT_API_VERSION/login")
  fun login(@Body body: LoginRequest): Single<LoginResponse>

  @POST("$CURRENT_API_VERSION/users/{userId}/request_otp")
  fun requestLoginOtp(@Path("userId") userId: UUID): Completable

  @POST("$CURRENT_API_VERSION/users/me/reset_password")
  fun resetPin(@Body request: ResetPinRequest): Single<ForgotPinResponse>
}
