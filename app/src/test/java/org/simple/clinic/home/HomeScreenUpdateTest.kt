package org.simple.clinic.home

import com.spotify.mobius.test.NextMatchers
import com.spotify.mobius.test.UpdateSpec
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.util.Optional
import java.util.UUID

class HomeScreenUpdateTest {

  private val defaultModel = HomeScreenModel.create()
  private val updateSpec = UpdateSpec(HomeScreenUpdate())

  @Test
  fun `when the scanned identifier has a corresponding patient, the patient summary screen must be opened`() {
    val identifier = TestData.identifier("88d12415-b10d-4ebb-bf48-482ece022139", Identifier.IdentifierType.BpPassport)
    val patient = TestData.patient(uuid = UUID.fromString("614a3a62-92be-4551-92d0-beca649cfd7c"))

    val event = PatientSearchByIdentifierCompleted(
        patient = Optional.of(patient),
        identifier = identifier
    )

    updateSpec
        .given(defaultModel)
        .whenEvent(event)
        .then(UpdateSpec.assertThatNext(
            NextMatchers.hasNoModel(),
            NextMatchers.hasEffects(OpenPatientSummary(patient.uuid))
        ))
  }

  @Test
  fun `when the scanned identifier does not have a corresponding patient, the patient search screen must be opened`() {
    val identifier = TestData.identifier("88d12415-b10d-4ebb-bf48-482ece022139", Identifier.IdentifierType.BpPassport)

    val event = PatientSearchByIdentifierCompleted(
        patient = Optional.empty(),
        identifier = identifier
    )

    updateSpec
        .given(defaultModel)
        .whenEvent(event)
        .then(UpdateSpec.assertThatNext(
            NextMatchers.hasNoModel(),
            NextMatchers.hasEffects(OpenPatientSearchScreen(identifier))
        ))
  }
}
