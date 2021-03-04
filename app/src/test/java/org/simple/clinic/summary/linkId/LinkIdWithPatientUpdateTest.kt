package org.simple.clinic.summary.linkId

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

class LinkIdWithPatientUpdateTest {
  private val updateSpec = UpdateSpec<LinkIdWithPatientModel, LinkIdWithPatientEvent, LinkIdWithPatientEffect>(LinkIdWithPatientUpdate())
  private val patientUuid = UUID.fromString("e2770294-fe11-4f66-ab8d-0da30382d957")
  private val identifier = TestData.identifier(type = Identifier.IdentifierType.BpPassport, value = "435645")
  private val defaultModel = LinkIdWithPatientModel.create(patientUuid, identifier)

  @Test
  fun `when the patient details are loaded, then update the UI`() {
    val patientName = "TestName"

    updateSpec
        .given(defaultModel)
        .whenEvent(PatientNameReceived(patientName))
        .then(
            assertThatNext(
                hasModel(defaultModel.patientNameFetched(patientName)),
                hasNoEffects()
            )
        )
  }

  @Test
  fun `when identifier is added to patient, then update UI and close sheet`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(IdentifierAddedToPatient)
        .then(
            assertThatNext(
                hasModel(defaultModel.linkedIdToPatient()),
                hasEffects(CloseSheetWithLinkedId)
            )
        )
  }

  @Test
  fun `when add identifier is clicked, then update UI`() {
    val patientFetchedModel = defaultModel.patientNameFetched("Patient")

    updateSpec
        .given(patientFetchedModel)
        .whenEvent(LinkIdWithPatientAddClicked)
        .then(
            assertThatNext(
                hasModel(patientFetchedModel.linkingIdToPatient()),
                hasEffects(
                    AddIdentifierToPatient(
                        patientUuid = patientUuid,
                        identifier = identifier
                    )
                )
            )
        )
  }
}
