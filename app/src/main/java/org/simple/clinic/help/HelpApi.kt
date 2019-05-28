package org.simple.clinic.help

import io.reactivex.Single
import org.simple.clinic.CURRENT_API_VERSION
import retrofit2.http.GET

interface HelpApi {

  @GET("$CURRENT_API_VERSION/help.html")
  fun help(): Single<String>
}
