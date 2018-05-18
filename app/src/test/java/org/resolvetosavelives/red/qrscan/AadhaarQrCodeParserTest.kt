package org.resolvetosavelives.red.qrscan

import org.intellij.lang.annotations.Language
import org.junit.Test
import org.resolvetosavelives.red.newentry.search.Gender

class AadhaarQrCodeParserTest {

  @Language("xml")
  private val xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><PrintLetterBarcodeData uid=\"424119752477\" name=\"Saket Narayan\" gender=\"M\" yob=\"1993\" co=\"S/O Sujesh Chandra Narayan\" house=\"HOUSE NO- HI/ 112\" loc=\"HARMU HOUSING COLONY, POST- HARMU\" vtc=\"Harmu\" dist=\"Ranchi\" state=\"Jharkhand\" pc=\"834002\"/>"

  @Test
  fun parse() {
    val parser = AadhaarQrCodeParser()
    val parsed = parser.parse(xml)

    val expected = AadhaarQrCode(
        fullName = "Saket Narayan",
        gender = Gender.MALE,
        yearOfBirth = "1993",
        house = "HOUSE NO- HI/ 112",
        location = "HARMU HOUSING COLONY, POST- HARMU",
        villageOrTownOrCity = "Harmu",
        district = "Ranchi",
        state = "Jharkhand",
        pincode = "834002")

    assert(parsed == expected)
  }
}
