package org.simple.clinic.scanid.scannedqrcode

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.businessid.Identifier

class ScannedQrCodeUpdateTest {

  private val updateSpec = UpdateSpec(ScannedQrCodeUpdate())
  private val identifier = Identifier("1111111", Identifier.IdentifierType.BpPassport)
  private val defaultModel = ScannedQrCodeModel.create(identifier)
  private val ongoingPatientEntry = OngoingNewPatientEntry(
      identifier = identifier
  )

  @Test
  fun `when register new patient button is clicked, then save the ongoing patient entry`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(RegisterNewPatientClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(SaveNewOngoingPatientEntry(ongoingPatientEntry))
            )
        )
  }

  @Test
  fun `when the ongoing patient entry is saved, then send scanned qr code blank result`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(NewOngoingPatientEntrySaved)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(SendBlankScannedQrCodeResult(RegisterNewPatient))
            )
        )
  }
  
  @Test
  fun `when add to existing patient is clicked, then send scanned qr code blank result`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(AddToExistingPatientClicked)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(SendBlankScannedQrCodeResult(AddToExistingPatient))
            )
        )
  }
}
