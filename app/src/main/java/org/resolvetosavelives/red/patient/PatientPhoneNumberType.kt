package org.resolvetosavelives.red.patient

import org.resolvetosavelives.red.util.RoomEnumTypeConverter

enum class PatientPhoneNumberType {

  MOBILE,
  LANDLINE;

  class RoomTypeConverter : RoomEnumTypeConverter<PatientPhoneNumberType>(PatientPhoneNumberType::class.java)
}
