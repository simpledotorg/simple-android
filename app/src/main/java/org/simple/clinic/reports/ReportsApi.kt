package org.simple.clinic.reports

import io.reactivex.Single
import retrofit2.http.GET

interface ReportsApi {

  @GET("v3/analytics/user_analytics.html")
  fun userAnalytics(): Single<String>
}
