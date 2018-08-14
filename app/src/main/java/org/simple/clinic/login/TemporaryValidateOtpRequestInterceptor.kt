package org.simple.clinic.login

import com.squareup.moshi.Moshi
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import org.simple.clinic.user.LoggedInUserPayload
import org.simple.clinic.user.UserStatus
import org.threeten.bp.Instant
import java.util.UUID
import javax.inject.Inject

// TODO: Integrate APIs and remove this class when they are ready
class TemporaryValidateOtpRequestInterceptor @Inject constructor(moshi: Moshi) : Interceptor {

  private val validateLoginRequestAdapter = moshi.adapter(ValidateLoginOtpRequest::class.java)
  private val loginResponseAdapter = moshi.adapter(LoginResponse::class.java)

  override fun intercept(chain: Interceptor.Chain): Response {
    val originalRequest = chain.request()

    val urlString = originalRequest.url().toString()

    return when {
      urlString.endsWith("v1/requestOtp") -> tempSendOtpResponse(originalRequest)
      urlString.endsWith("v1/validateLoginOtp") -> tempValidateLoginOtp(originalRequest)
      else -> chain.proceed(originalRequest)
    }
  }

  private fun tempValidateLoginOtp(request: Request): Response {
    // Some sleep to simulate a real network call
    Thread.sleep(1000L)

    val buffer = Buffer()
    request.body()!!.writeTo(buffer)

    val validateLoginOtpRequest = validateLoginRequestAdapter.fromJson(buffer)!!

    val loginResponse = LoginResponse(
        accessToken = "access_token",
        loggedInUser = LoggedInUserPayload(
            UUID.fromString(validateLoginOtpRequest.userId),
            fullName = "",
            phoneNumber = "",
            pinDigest = validateLoginOtpRequest.passwordDigest,
            facilityUuids = listOf(UUID.randomUUID()),
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            status = UserStatus.DISAPPROVED_FOR_SYNCING
        )
    )

    val responseBody = ResponseBody.create(MediaType.parse("application/json"), loginResponseAdapter.toJson(loginResponse))

    return Response.Builder()
        .request(request)
        .code(200)
        .body(responseBody)
        .build()
  }

  private fun tempSendOtpResponse(request: Request): Response {
    // Some sleep to simulate a real network call
    Thread.sleep(1000L)

    val responseBody = ResponseBody.create(MediaType.parse("text/plain"), "OK")

    return Response.Builder()
        .request(request)
        .code(200)
        .body(responseBody)
        .build()
  }
}
