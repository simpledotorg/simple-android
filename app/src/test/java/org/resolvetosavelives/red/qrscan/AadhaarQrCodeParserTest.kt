package org.resolvetosavelives.red.qrscan

import org.junit.Before
import org.junit.Test
import org.resolvetosavelives.red.newentry.search.Gender

class AadhaarQrCodeParserTest {

  private lateinit var aadharParser: AadhaarQrCodeParser

  @Before
  fun setUp() {
    val xmlParserFactory = JcabiXmlParser.Factory()
    aadharParser = AadhaarQrCodeParser(xmlParserFactory)
  }

  @Test
  fun `when an aadhaar xml is received then it should be correctly parsed`() {
    val parsed1 = aadharParser.parse("<?xml version=\"1.0\" encoding=\"UTF-8\"?><PrintLetterBarcodeData uid=\"424119752477\" " +
        "name=\"Saket Narayan\" gender=\"M\" yob=\"1993\" co=\"S/O Sujesh Chandra Narayan\" house=\"HOUSE NO- HI/ 112\" " +
        "loc=\"HARMU HOUSING COLONY, POST- HARMU\" vtc=\"Harmu\" dist=\"Ranchi\" state=\"Jharkhand\" pc=\"834002\"/>")

    assert(parsed1 == AadhaarQrData(
        fullName = "Saket Narayan",
        gender = Gender.MALE,
        dateOfBirth = null,
        villageOrTownOrCity = "Harmu",
        district = "Ranchi",
        state = "Jharkhand"))

    val parsed2 = aadharParser.parse("<?xml version=\"1.0\" encoding=\"UTF-8\"?> <PrintLetterBarcodeData uid=\"995845029482\" " +
        "name=\"Ankur Sethi\" gender=\"M\" yob=\"1990\" co=\"S/O: Praveen Sethi\" house=\"House No-E-1061\" vtc=\"Saraswati Vihar\" " +
        "po=\"Saraswati Vihar\" dist=\"North West Delhi\" subdist=\"Saraswati Vihar\" state=\"Delhi\" pc=\"110034\" dob=\"01/12/1990\"/>")

    assert(parsed2 == AadhaarQrData(
        fullName = "Ankur Sethi",
        gender = Gender.MALE,
        dateOfBirth = "01/12/1990",
        villageOrTownOrCity = "Saraswati Vihar",
        district = "North West Delhi",
        state = "Delhi"))
  }

  @Test
  fun `when a non-aadhaar xml is received then an unknown rsult should be returned`() {
    val parsed = aadharParser.parse("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources><string name=\"key\">value</string></resources>")
    assert(parsed is UnknownQr)
  }

  @Test
  fun `when a non-xml string is received then an unknown result should be returned`() {
    val parsed = aadharParser.parse("https://wikipedia.com")
    assert(parsed is UnknownQr)
  }
}
