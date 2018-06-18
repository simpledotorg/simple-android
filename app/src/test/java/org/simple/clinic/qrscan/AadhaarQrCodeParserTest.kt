package org.simple.clinic.qrscan

import org.junit.Before
import org.junit.Test
import org.simple.clinic.patient.Gender

class AadhaarQrCodeParserTest {

  private lateinit var aadhaarParser: AadhaarQrCodeParser

  @Before
  fun setUp() {
    val xmlParserFactory = JcabiXmlParser.Factory()
    aadhaarParser = AadhaarQrCodeParser(xmlParserFactory)
  }

  @Test
  fun `when an aadhaar xml is received then it should be correctly parsed`() {
    val parsed1 = aadhaarParser.parse("<?xml version=\"1.0\" encoding=\"UTF-8\"?><PrintLetterBarcodeData uid=\"236619772007\" " +
        "name=\"Harry Potter\" gender=\"M\" yob=\"1993\" co=\"S/O James Potter\" house=\"HOUSE NO- HI/ 112\" " +
        "loc=\"Little Whinging\" vtc=\"Harmu\" dist=\"\" state=\"Kerala\" pc=\"899002\"/>")

    assert(parsed1 == AadhaarQrData(
        fullName = "Harry Potter",
        gender = Gender.MALE,
        dateOfBirth = null,
        villageOrTownOrCity = "Harmu",
        district = "",
        state = "Kerala"))

    val parsed2 = aadhaarParser.parse("<?xml version=\"1.0\" encoding=\"UTF-8\"?> <PrintLetterBarcodeData uid=\"997593729482\" " +
        "name=\"Byomkesh Bakshi\" gender=\"M\" yob=\"1950\" co=\"S/O: Sharadindu Bandhopadhay\" house=\"House 622\" vtc=\"Chinabazar\" " +
        "po=\"\" dist=\"North Kolkata\" subdist=\"\" state=\"West Bengal\" pc=\"392134\" dob=\"17/12/1950\"/>")

    assert(parsed2 == AadhaarQrData(
        fullName = "Byomkesh Bakshi",
        gender = Gender.MALE,
        dateOfBirth = "17/12/1950",
        villageOrTownOrCity = "Chinabazar",
        district = "North Kolkata",
        state = "West Bengal"))
  }

  @Test
  fun `parsing should be lenient`() {
    val parsed1 = aadhaarParser.parse("<?xml version=\"1.0\" encoding=\"UTF-8\"?><PrintLetterBarcodeData />")

    assert(parsed1 == AadhaarQrData(
        fullName = null,
        gender = null,
        dateOfBirth = null,
        villageOrTownOrCity = null,
        district = null,
        state = null))
  }

  @Test
  fun `when a non-aadhaar xml is received then an unknown rsult should be returned`() {
    val parsed = aadhaarParser.parse("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources><string name=\"key\">value</string></resources>")
    assert(parsed is UnknownQr)
  }

  @Test
  fun `when a non-xml string is received then an unknown result should be returned`() {
    val parsed = aadhaarParser.parse("https://wikipedia.com")
    assert(parsed is UnknownQr)
  }
}
