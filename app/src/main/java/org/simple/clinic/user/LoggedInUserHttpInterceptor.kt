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

    val userDetails = userSession.userFacilityDetails()
    val accessToken = userSession.accessToken().toNullable()

    return if (userDetails != null && accessToken.isNullOrBlank().not()) {
      chain.proceed(addHeaders(originalRequest, accessToken!!, userDetails))
    } else {
      chain.proceed(originalRequest)
    }
  }

  private fun addHeaders(originalRequest: Request, accessToken: String, userFacilityDetails: UserFacilityDetails): Request {
    return originalRequest.newBuilder()
        .addHeader("Authorization", "Bearer $accessToken")
        .addHeader("X-USER-ID", userFacilityDetails.userId.toString())
        .addHeader("X-FACILITY-ID", userFacilityDetails.currentFacilityId.toString())
        .build()
  }

}
