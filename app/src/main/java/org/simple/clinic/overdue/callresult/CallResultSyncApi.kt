package org.simple.clinic.overdue.callresult

import org.simple.clinic.sync.DataPushResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface CallResultSyncApi {

  @POST("v4/call_results/sync")
  fun push(
      @Body request: CallResultPushRequest
  ): Call<DataPushResponse>
}
