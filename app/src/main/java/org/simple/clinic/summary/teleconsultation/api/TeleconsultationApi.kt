package org.simple.clinic.summary.teleconsultation.api

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.UUID

interface TeleconsultationApi {

  @GET("v4/facility_teleconsultations/{facility_id}")
  fun get(
      @Path("facility_id") facilityUuid: UUID
  ): Single<TeleconsultationsResponse>
}
