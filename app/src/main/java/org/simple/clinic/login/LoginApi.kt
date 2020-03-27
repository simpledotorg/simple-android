package org.simple.clinic.login

import io.reactivex.Completable
import io.reactivex.Single
import org.simple.clinic.forgotpin.ForgotPinResponse
import org.simple.clinic.forgotpin.ResetPinRequest
import org.simple.clinic.login.activateuser.ActivateUserRequest
import org.simple.clinic.login.activateuser.ActivateUserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.UUID

interface LoginApi {

  @POST("v3/login")
  fun login(@Body body: LoginRequest): Single<LoginResponse>

  @POST("v3/users/{userId}/request_otp")
  fun requestLoginOtp(@Path("userId") userId: UUID): Completable

  @POST("v3/users/me/reset_password")
  fun resetPin(@Body request: ResetPinRequest): Single<ForgotPinResponse>

  @POST("v4/users/activate")
  fun activate(@Body request: ActivateUserRequest): Call<ActivateUserResponse>
}
