package org.simple.clinic.user

import com.f2prateek.rx.preferences2.Preference
import okhttp3.Interceptor
import okhttp3.Response
import org.simple.clinic.util.Optional
import javax.inject.Inject
import javax.inject.Named

class LoggedInUserHttpInterceptor @Inject constructor(
    val loggedInUser: Preference<Optional<LoggedInUser>>,
    @Named("preference_access_token") val accessToken: Preference<Optional<String>>
) : Interceptor {

  override fun intercept(chain: Interceptor.Chain?): Response {
    val originalRequest = chain!!.request()
    val updatedRequest = originalRequest.newBuilder()

    if (accessToken.isSet && loggedInUser.isSet) {
      updatedRequest.addHeader("Authorization", "Bearer ${accessToken.get().toNullable()}")
      updatedRequest.addHeader("X-USER-ID", loggedInUser.get().toNullable()!!.uuid.toString())
    }

    return chain.proceed(updatedRequest.build())
  }
}
