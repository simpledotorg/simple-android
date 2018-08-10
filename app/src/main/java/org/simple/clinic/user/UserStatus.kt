package org.simple.clinic.user

import com.squareup.moshi.Json
import org.simple.clinic.util.RoomEnumTypeConverter

enum class UserStatus {

  @Json(name = "requested")
  WAITING_FOR_APPROVAL,

  @Json(name = "allowed")
  APPROVED_FOR_SYNCING,

  @Json(name = "denied")
  DISAPPROVED_FOR_SYNCING;

  class RoomTypeConverter : RoomEnumTypeConverter<UserStatus>(UserStatus::class.java)
}
