package org.simple.clinic.user

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.simple.clinic.ClinicApp
import org.simple.clinic.facility.FacilityRepository
import java.util.UUID
import javax.inject.Inject

class LoggedInUserHttpInterceptor @Inject constructor(
    val facilityRepository: FacilityRepository
) : Interceptor {

  // Ugly hack to avoid a cyclic dependency between this class and UserSession.
  private val userSession
    get() = ClinicApp.appComponent.userSession()

  override fun intercept(chain: Interceptor.Chain?): Response {
    val originalRequest = chain!!.request()

    val user = userSession.loggedInUserImmediate()
    val (accessToken) = userSession.accessToken()

    var facilityUuid: UUID? = null
    if (user != null) {
      facilityUuid = facilityRepository.currentFacilityUuid(user)
    }

    return if (user != null && accessToken.isNullOrBlank().not() && facilityUuid != null) {
      chain.proceed(addHeaders(originalRequest, accessToken!!, user, facilityUuid))
    } else {
      chain.proceed(originalRequest)
    }
  }

  private fun addHeaders(originalRequest: Request, accessToken: String, user: User, facilityUuid: UUID): Request {
    return originalRequest.newBuilder()
        .addHeader("Authorization", "Bearer $accessToken")
        .addHeader("X-USER-ID", user.uuid.toString())
        .addHeader("X-FACILITY-ID", facilityUuid.toString())
        .build()
  }

}
