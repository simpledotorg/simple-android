package org.simple.clinic.overdue.download

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Streaming

interface OverdueListDownloadApi {

  @Streaming
  @GET("v4/analytics/overdue_list.csv")
  fun download(): Call<ResponseBody>
}
