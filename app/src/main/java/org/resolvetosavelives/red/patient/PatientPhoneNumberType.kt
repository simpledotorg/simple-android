package org.resolvetosavelives.red.patient

import com.squareup.moshi.Json
import org.resolvetosavelives.red.util.RoomEnumTypeConverter

enum class PatientPhoneNumberType {

  @Json(name = "mobile")
  MOBILE,

  @Json(name = "landline")
  LANDLINE;

  class RoomTypeConverter : RoomEnumTypeConverter<PatientPhoneNumberType>(PatientPhoneNumberType::class.java)
}
