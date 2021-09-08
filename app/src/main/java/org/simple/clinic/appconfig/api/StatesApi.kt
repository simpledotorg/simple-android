package org.simple.clinic.appconfig.api

import org.simple.clinic.appconfig.StatesPayload
import retrofit2.Call
import retrofit2.http.GET

interface StatesApi {

  @GET("v4/states")
  fun fetchStates(): Call<StatesPayload>
}
