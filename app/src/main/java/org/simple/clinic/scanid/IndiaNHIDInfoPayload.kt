package org.simple.clinic.scanid

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class IndiaNHIDInfoPayload(

    @Json(name = "hidn")
    val healthIdNumber: String,

    @Json(name = "hid")
    val healthIdUserName: String,

    @Json(name = "name")
    val fullName: String,

    @Json(name = "gender")
    val gender: String,

    @Json(name = "statelgd")
    val state: String?,

    @Json(name = "distlgd")
    val district: String?,

    @Json(name = "dob")
    val dateOfBirth: String,

    @Json(name = "address")
    val address: String
) {
  fun toPatientPrefillInfo(): PatientPrefillInfo {
    return PatientPrefillInfo(
        fullName = fullName,
        gender = gender,
        state = state,
        district = district,
        dateOfBirth = dateOfBirth,
        address = address
    )
  }

  fun toIndiaNHIDInfo(): IndiaNHIDInfo {
    return IndiaNHIDInfo(
        healthIdNumber = healthIdNumber,
        healthIdUserName = healthIdUserName
    )
  }
}
