package org.simple.clinic.login

import io.reactivex.Single
import org.simple.clinic.forgotpin.ForgotPinResponse
import org.simple.clinic.forgotpin.ResetPinRequest
import org.simple.clinic.login.activateuser.ActivateUserRequest
import org.simple.clinic.login.activateuser.ActivateUserResponse
import org.simple.clinic.registration.FindUserRequest
import org.simple.clinic.registration.FindUserResponse
import org.simple.clinic.registration.RegistrationRequest
import org.simple.clinic.registration.RegistrationResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface UsersApi {

  @POST("v3/login")
  fun login(@Body body: LoginRequest): Single<LoginResponse>

  @POST("v3/users/me/reset_password")
  fun resetPin(@Body request: ResetPinRequest): Single<ForgotPinResponse>

  @POST("v4/users/activate")
  fun activate(@Body request: ActivateUserRequest): Call<ActivateUserResponse>

  @GET("v4/users/me")
  fun self(): Call<CurrentUserResponse>

  @POST("v3/users/register")
  fun createUser(@Body body: RegistrationRequest): Single<RegistrationResponse>

  @POST("v4/users/find")
  fun findUserByPhoneNumber(@Body request: FindUserRequest): Call<FindUserResponse>
}
