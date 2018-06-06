package org.resolvetosavelives.red.patient

import com.squareup.moshi.Json
import org.resolvetosavelives.red.util.RoomEnumTypeConverter

enum class Gender {

  @Json(name = "male")
  MALE,

  @Json(name = "female")
  FEMALE,

  @Json(name = "transgender")
  TRANSGENDER;

  class RoomTypeConverter : RoomEnumTypeConverter<Gender>(Gender::class.java)
}
