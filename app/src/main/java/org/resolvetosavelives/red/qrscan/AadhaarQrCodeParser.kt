package org.resolvetosavelives.red.qrscan

import com.jcabi.xml.XMLDocument
import org.resolvetosavelives.red.newentry.search.Gender
import javax.inject.Inject

class AadhaarQrCodeParser @Inject constructor() {

  fun parse(qrCode: String): AadhaarQrCode {
    val xml = XMLDocument(qrCode)
    val read: (String) -> String = { tag -> xml.xpath("//PrintLetterBarcodeData/@$tag").first() }

    return AadhaarQrCode(
        fullName = read("name"),
        gender = parseGenderCode(read("gender")),
        yearOfBirth = read("yob"),
        house = read("house"),
        location = read("loc"),
        villageOrTownOrCity = read("vtc"),
        district = read("dist"),
        state = read("state"),
        pincode = read("pc"))
  }

  private fun parseGenderCode(genderCode: String): Gender {
    return when (genderCode) {
      "M" -> Gender.MALE
      "F" -> Gender.FEMALE
      "T" -> Gender.TRANS
      else -> throw AssertionError("Unknown gender code in aadhaar: $genderCode")
    }
  }
}

data class AadhaarQrCode(
    val fullName: String,
    val gender: Gender,
    val yearOfBirth: String,
    val house: String,
    val location: String,
    val villageOrTownOrCity: String,
    val district: String,
    val state: String,
    val pincode: String
)
