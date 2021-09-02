package org.simple.clinic.appconfig.api

import retrofit2.http.GET

interface StatesApi {

  @GET("v4/states")
  fun fetchStates(): List<String>
}
