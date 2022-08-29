package org.simple.clinic.overdue.download

import io.reactivex.Single
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming

interface OverdueListDownloadApi {

  @Streaming
  @GET("v4/analytics/overdue_list.csv")
  fun download(): Single<ResponseBody>
}
