package org.simple.clinic.home

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

class HomeScreenUpdateTest {

  private val defaultModel = HomeScreenModel.create()
  private val updateSpec = UpdateSpec(HomeScreenUpdate())

  @Test
  fun `when the patient short code is entered, the short code search screen must be opened`() {
    val shortCode = "1234567"

    updateSpec
        .given(defaultModel)
        .whenEvent(BusinessIdScanned.ByShortCode(shortCode))
        .then(UpdateSpec.assertThatNext(
            hasNoModel(),
            hasEffects(OpenShortCodeSearchScreen(shortCode))
        ))
  }

  @Test
  fun `when identifier is scanned and patient is found, then open the patient summary`() {
    val patientUuid = UUID.fromString("0b772168-b1d8-4410-8d74-efa1ca447a43")

    updateSpec
        .given(defaultModel)
        .whenEvent(BusinessIdScanned.ByPatientFound(patientUuid))
        .then(UpdateSpec.assertThatNext(
            hasNoModel(),
            hasEffects(OpenPatientSummary(patientUuid))
        ))
  }

  @Test
  fun `when identifier is scanned and patient is not found, then open the patient summary`() {
    val identifier = TestData.identifier("88d12415-b10d-4ebb-bf48-482ece022139", Identifier.IdentifierType.BpPassport)

    updateSpec
        .given(defaultModel)
        .whenEvent(BusinessIdScanned.ByPatientNotFound(identifier))
        .then(UpdateSpec.assertThatNext(
            hasNoModel(),
            hasEffects(OpenPatientSearchScreen(identifier))
        ))
  }
}
