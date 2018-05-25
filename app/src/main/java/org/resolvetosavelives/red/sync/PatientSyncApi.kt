package org.resolvetosavelives.red.sync

import io.reactivex.Single
import org.threeten.bp.Instant
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PatientSyncApi {

  @POST("api/v1/patients/sync")
  fun push(): Single<PatientPushResponse>

  @GET("api/v1/patients/sync")
  fun pull(
      @Query("latest_record_timestamp") latestRecordTimestamp: Instant,
      @Query("first_time") isFirstSync: Boolean,
      @Query("number_of_records") recordsToRetrieve: Int
  ): Single<PatientPullResponse>
}
