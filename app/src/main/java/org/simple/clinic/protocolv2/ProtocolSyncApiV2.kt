package org.simple.clinic.protocolv2

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface ProtocolSyncApiV2 {

  companion object {
    const val version = "v2"
  }

  @GET("$version/protocols/sync")
  fun pull(
      @Query("limit") recordsToPull: Int,
      @Query("process_token") lastPullToken: String? = null
  ): Single<ProtocolPullResponse>

}
