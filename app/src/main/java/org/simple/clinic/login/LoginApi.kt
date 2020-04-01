package org.simple.clinic.login

import io.reactivex.Single
import org.simple.clinic.forgotpin.ForgotPinResponse
import org.simple.clinic.forgotpin.ResetPinRequest
import org.simple.clinic.login.activateuser.ActivateUserRequest
import org.simple.clinic.login.activateuser.ActivateUserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface LoginApi {

  @POST("v3/login")
  fun login(@Body body: LoginRequest): Single<LoginResponse>

  @POST("v3/users/me/reset_password")
  fun resetPin(@Body request: ResetPinRequest): Single<ForgotPinResponse>

  @POST("v4/users/activate")
  fun activate(@Body request: ActivateUserRequest): Call<ActivateUserResponse>

  @GET("v4/users/me")
  fun self(): Call<CurrentUserResponse>
}
