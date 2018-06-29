package org.simple.clinic.user

import okhttp3.Interceptor
import okhttp3.Response
import org.simple.clinic.ClinicApp
import javax.inject.Inject

class LoggedInUserHttpInterceptor @Inject constructor() : Interceptor {

  // Ugly hack to avoid a cyclic dependency between this class and UserSession.
  private val userSession by lazy { ClinicApp.appComponent.userSession() }

  override fun intercept(chain: Interceptor.Chain?): Response {
    val originalRequest = chain!!.request()
    val updatedRequest = originalRequest.newBuilder()

    if (userSession.isUserLoggedIn()) {
      val (loggedInUser) = userSession.loggedInUser().blockingFirst()
      val (accessToken) = userSession.accessToken()

      updatedRequest.addHeader("Authorization", "Bearer ${accessToken!!}")
      updatedRequest.addHeader("X-USER-ID", loggedInUser!!.uuid.toString())
    }

    return chain.proceed(updatedRequest.build())
  }
}
