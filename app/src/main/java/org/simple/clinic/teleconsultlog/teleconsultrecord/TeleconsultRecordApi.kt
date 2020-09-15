package org.simple.clinic.teleconsultlog.teleconsultrecord

import retrofit2.http.Body
import retrofit2.http.POST

interface TeleconsultRecordApi {

  @POST("v4/teleconsultations/sync")
  fun push(
      @Body body: TeleconsultRecordPayload
  )
}
