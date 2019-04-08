package org.simple.clinic.patient.businessid

import com.squareup.moshi.Json
import java.util.UUID

sealed class BusinessIdMetaData {

  data class BpPassportV1(

      @Json(name = "assigning_user_id")
      val assigningUserUuid: UUID,

      @Json(name = "assigning_facility_id")
      val assigningFacilityUuid: UUID
  ) : BusinessIdMetaData()
}
