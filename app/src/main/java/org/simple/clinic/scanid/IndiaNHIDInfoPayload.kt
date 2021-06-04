package org.simple.clinic.scanid

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientPrefillInfo
import java.time.LocalDate

@JsonClass(generateAdapter = true)
data class IndiaNHIDInfoPayload(

    @Json(name = "hidn")
    val healthIdNumber: String,

    @Json(name = "hid")
    val healthIdUserName: String,

    @Json(name = "name")
    val fullName: String,

    @Json(name = "gender")
    val indiaNHIDGender: IndiaNHIDGender,

    @Json(name = "statelgd")
    val state: String?,

    @Json(name = "distlgd")
    val district: String?,

    @Json(name = "dob")
    @IndiaNHIDDateOfBirth
    val dateOfBirth: LocalDate,

    @Json(name = "address")
    val address: String
) {
  fun toPatientPrefillInfo(): PatientPrefillInfo {
    return PatientPrefillInfo(
        fullName = fullName,
        gender = Gender.fromIndiaNHIDToGender(indiaNHIDGender),
        dateOfBirth = dateOfBirth,
        address = address
    )
  }
}
