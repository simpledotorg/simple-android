package org.simple.clinic.qrscan

import org.simple.clinic.patient.Gender
import javax.inject.Inject

class AadhaarQrCodeParser @Inject constructor(private val xmlParserFactory: XmlParser.Factory) {

  fun parse(qrCode: String): ParseResult {
    val xmlParser: XmlParser

    try {
      xmlParser = xmlParserFactory.parse(qrCode)
    } catch (e: InvalidXmlException) {
      return UnknownQr()
    }
    if (!xmlParser.hasNode("PrintLetterBarcodeData")) {
      return UnknownQr()
    }

    val read: (String) -> String? = { tag -> xmlParser.readStrings("//PrintLetterBarcodeData/@$tag").firstOrNull() }

    return AadhaarQrData(
        fullName = read("name"),
        gender = parseGenderCode(read("gender")),
        dateOfBirth = read("dob"),
        villageOrTownOrCity = read("vtc"),
        district = read("dist"),
        state = read("state"))
  }

  private fun parseGenderCode(genderCode: String?): Gender? {
    return when (genderCode) {
      "M" -> Gender.MALE
      "F" -> Gender.FEMALE
      "T" -> Gender.TRANSGENDER
      null -> null
      else -> {
        throw AssertionError("Unknown gender code in aadhaar: $genderCode")
      }
    }
  }
}

sealed class ParseResult

class UnknownQr : ParseResult()

data class AadhaarQrData(
    val fullName: String?,
    val gender: Gender?,
    val dateOfBirth: String?,
    val villageOrTownOrCity: String?,
    val district: String?,
    val state: String?
) : ParseResult()
