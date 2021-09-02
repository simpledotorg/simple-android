package org.simple.clinic.appconfig

import okhttp3.HttpUrl.Companion.toHttpUrl
import org.simple.clinic.appconfig.api.StatesApi
import retrofit2.Retrofit
import java.net.ConnectException
import javax.inject.Inject

class StatesFetcher @Inject constructor(
    private val retrofitBuilder: Retrofit.Builder
) {

  @Throws(ConnectException::class)
  fun fetchStates(deployment: Deployment): List<State> {
    val statesApi = createStatesApi(deployment)
    return statesApi
        .fetchStates()
        .map { state ->
          State(
              displayName = state,
              deployment = deployment
          )
        }
  }

  private fun createStatesApi(deployment: Deployment): StatesApi {
    val baseUrl = deployment.endPoint.toString().removeSuffix("/")
    val endpoint = "$baseUrl/".toHttpUrl()

    return retrofitBuilder
        .baseUrl(endpoint)
        .build()
        .create(StatesApi::class.java)
  }
}
