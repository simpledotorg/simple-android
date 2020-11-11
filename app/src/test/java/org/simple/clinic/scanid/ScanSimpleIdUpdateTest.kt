package org.simple.clinic.scanid

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.patient.businessid.Identifier.IdentifierType.BpPassport

class ScanSimpleIdUpdateTest {

  private val defaultModel = ScanSimpleIdModel.create()

  private val spec = UpdateSpec(ScanSimpleIdUpdate())

  @Test
  fun `when a valid QR code is scanned, send the scanned identifier to the parent screen`() {
    val scannedId = "9f154761-ee2f-4ee3-acd1-0038328f75ca"

    val expectedScanResult = ScannedId(Identifier(scannedId, BpPassport))

    spec
        .given(defaultModel)
        .whenEvent(ScanSimpleIdScreenQrCodeScanned(scannedId))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SendScannedIdentifierResult(expectedScanResult))
        ))
  }

  @Test
  fun `when the entered short code is valid, send the entered short code to the parent screen`() {
    val shortCode = "1234567"
    val model = defaultModel.shortCodeChanged(ShortCodeInput(shortCode))

    val expectedScanResult = EnteredShortCode(shortCode)

    spec
        .given(model)
        .whenEvent(ShortCodeValidated(ShortCodeValidationResult.Success))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SendScannedIdentifierResult(expectedScanResult))
        ))
  }
}
