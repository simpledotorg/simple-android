package org.simple.clinic.patient

import com.squareup.moshi.Json
import org.simple.clinic.util.RoomEnumTypeConverter

enum class PatientStatus {

  @Json(name = "active")
  ACTIVE,

  @Json(name = "dead")
  DEAD,

  @Json(name = "migrated")
  MIGRATED,

  @Json(name = "unresponsive")
  UNRESPONSIVE,

  @Json(name = "inactive")
  INACTIVE;

  class RoomTypeConverter : RoomEnumTypeConverter<PatientStatus>(PatientStatus::class.java)
}
