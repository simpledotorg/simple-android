package org.resolvetosavelives.red.search

import com.squareup.moshi.Json
import org.resolvetosavelives.red.util.RoomEnumTypeConverter

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
