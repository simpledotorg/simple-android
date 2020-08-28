package org.simple.clinic.reports

import retrofit2.Call
import retrofit2.http.GET

interface ReportsApi {

  @GET("v3/analytics/user_analytics.html")
  fun userAnalytics(): Call<String>
}
