package org.simple.clinic.help

import io.reactivex.Single
import retrofit2.http.GET

interface HelpApi {

  @GET("v2/help.html")
  fun help(): Single<String>
}
