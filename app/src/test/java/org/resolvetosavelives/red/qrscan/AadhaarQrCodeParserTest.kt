package org.resolvetosavelives.red.qrscan

import org.intellij.lang.annotations.Language
import org.junit.Test
import org.resolvetosavelives.red.newentry.search.Gender

class AadhaarQrCodeParserTest {

  @Language("xml")
  private val XML_1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><PrintLetterBarcodeData uid=\"424119752477\" name=\"Saket Narayan\" gender=\"M\" yob=\"1993\" co=\"S/O Sujesh Chandra Narayan\" house=\"HOUSE NO- HI/ 112\" loc=\"HARMU HOUSING COLONY, POST- HARMU\" vtc=\"Harmu\" dist=\"Ranchi\" state=\"Jharkhand\" pc=\"834002\"/>"

  private val EXPECTED_1 = AadhaarQrData(
      fullName = "Saket Narayan",
      gender = Gender.MALE,
      dateOfBirth = "",
      villageOrTownOrCity = "Harmu",
      district = "Ranchi",
      state = "Jharkhand")

  @Language("xml")
  private val XML_2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <PrintLetterBarcodeData uid=\"995845029482\" name=\"Ankur Sethi\" gender=\"M\" yob=\"1990\" co=\"S/O: Praveen Sethi\" house=\"House No-E-1061\" vtc=\"Saraswati Vihar\" po=\"Saraswati Vihar\" dist=\"North West Delhi\" subdist=\"Saraswati Vihar\" state=\"Delhi\" pc=\"110034\" dob=\"01/12/1990\"/>"

  private val EXPECTED_2 = AadhaarQrData(
      fullName = "Ankur Sethi",
      gender = Gender.MALE,
      dateOfBirth = "01/12/1990",
      villageOrTownOrCity = "Saraswati Vihar",
      district = "North West Delhi",
      state = "Delhi")

  @Test
  fun parse() {
    val xmlParserFactory = JcabiXmlParser.Factory()

    val parser = AadhaarQrCodeParser(xmlParserFactory)

    val parsed1 = parser.parse(XML_1)
    assert(parsed1 == EXPECTED_1)

    val parsed2 = parser.parse(XML_2)
    assert(parsed2 == EXPECTED_2)
  }
}
