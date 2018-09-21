package org.simple.clinic.user

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.simple.clinic.ClinicApp
import javax.inject.Inject

class LoggedInUserHttpInterceptor @Inject constructor() : Interceptor {

  // Ugly hack to avoid a cyclic dependency between this class and UserSession.
  private val userSession
    get() = ClinicApp.appComponent.userSession()

  override fun intercept(chain: Interceptor.Chain?): Response {
    val originalRequest = chain!!.request()

    val user = userSession.loggedInUserImmediate()
    val (accessToken) = userSession.accessToken()

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
        .build()
  }
}
