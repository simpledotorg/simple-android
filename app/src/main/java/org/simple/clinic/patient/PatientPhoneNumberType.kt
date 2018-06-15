package org.simple.clinic.patient

import com.squareup.moshi.Json
import org.simple.clinic.util.RoomEnumTypeConverter

enum class PatientPhoneNumberType {

  @Json(name = "mobile")
  MOBILE,

  @Json(name = "landline")
  LANDLINE;

  class RoomTypeConverter : RoomEnumTypeConverter<PatientPhoneNumberType>(PatientPhoneNumberType::class.java)
}
