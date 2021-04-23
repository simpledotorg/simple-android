package org.simple.clinic.patient

import com.google.common.truth.Truth.assertWithMessage
import org.junit.Test
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BangladeshNationalId
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.EthiopiaMedicalRecordNumber
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.IndiaNationalHealthId
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.Unknown


class IdentifierDisplayTest {

  @Test
  fun `all types of identifiers should have a display value`() {
    // given
    val testData = IdentifierType
        .values()
        .map(::generateIdentifierTestData)

    // then
    testData.forEach { (identifier, expectedValue) ->

      assertWithMessage("formatting Identifier of type [${identifier.type}]")
          .that(identifier.displayValue())
          .isEqualTo(expectedValue)
    }
  }

  private fun generateIdentifierTestData(type: IdentifierType): IdentifierTestData {
    return when (type) {
      BpPassport -> {
        val bpPassportId = "4b1de973-48e6-4f27-a384-65174748f0b1"
        val bpPassport = Identifier(value = bpPassportId, type = BpPassport)

        IdentifierTestData(identifier = bpPassport, expectedDisplayValue = "419\u00A07348")
      }
      BangladeshNationalId -> {
        val bangladeshNationalIdValue = "123456783456"
        val bangladeshNationalId = Identifier(value = bangladeshNationalIdValue, type = BangladeshNationalId)

        IdentifierTestData(identifier = bangladeshNationalId, expectedDisplayValue = bangladeshNationalIdValue)
      }
      is Unknown -> {
        val someOtherIdValue = "asdf-2345-rftg"
        val unknownId = Identifier(value = someOtherIdValue, type = Unknown("some-other-id"))

        IdentifierTestData(identifier = unknownId, expectedDisplayValue = someOtherIdValue)
      }
      EthiopiaMedicalRecordNumber -> {
        val ethiopiaMedicalRecordNumberValue = "87675747"
        val ethiopiaMedicalRecordNumber = Identifier(value = ethiopiaMedicalRecordNumberValue, type = EthiopiaMedicalRecordNumber)

        IdentifierTestData(identifier = ethiopiaMedicalRecordNumber, expectedDisplayValue = ethiopiaMedicalRecordNumberValue)
      }
      IndiaNationalHealthId -> {
        val indiaNationalHealthIdValue = "12121212121212"
        val indiaNationalHealthId = Identifier(value = indiaNationalHealthIdValue, type = IndiaNationalHealthId)

        IdentifierTestData(identifier = indiaNationalHealthId, expectedDisplayValue = "12\u00A01212\u00A01212\u00A01212")
      }
    }
  }

  private data class IdentifierTestData(
      val identifier: Identifier,
      val expectedDisplayValue: String
  )
}
