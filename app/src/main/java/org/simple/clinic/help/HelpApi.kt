package org.simple.clinic.help

import retrofit2.Call
import retrofit2.http.GET

interface HelpApi {

  @GET("v3/help.html")
  fun help(): Call<String>
}
