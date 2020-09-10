package org.simple.clinic.user

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.simple.clinic.util.toNullable
import javax.inject.Inject

class LoggedInUserHttpInterceptor @Inject constructor(
    private val userSession: UserSession
) : Interceptor {

  override fun intercept(chain: Interceptor.Chain?): Response {
    val originalRequest = chain!!.request()

    val user = userSession.loggedInUserImmediate()
    val accessToken = userSession.accessToken().toNullable()

    return if (user != null && accessToken.isNullOrBlank().not()) {
      chain.proceed(addHeaders(originalRequest, accessToken!!, user))
    } else {
      chain.proceed(originalRequest)
    }
  }

  private fun addHeaders(originalRequest: Request, accessToken: String, user: User): Request {
    return originalRequest.newBuilder()
        .addHeader("Authorization", "Bearer $accessToken")
        .addHeader("X-USER-ID", user.uuid.toString())
        .addHeader("X-FACILITY-ID", user.currentFacilityUuid.toString())
        .build()
  }

}
