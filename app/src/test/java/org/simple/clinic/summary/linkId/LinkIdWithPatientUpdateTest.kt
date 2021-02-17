package org.simple.clinic.summary.linkId

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.patient.businessid.Identifier
import java.util.UUID

class LinkIdWithPatientUpdateTest {
  private val updateSpec = UpdateSpec<LinkIdWithPatientModel, LinkIdWithPatientEvent, LinkIdWithPatientEffect>(LinkIdWithPatientUpdate())
  private val defaultModel = LinkIdWithPatientModel.create()
  private val patientUuid = UUID.fromString("e2770294-fe11-4f66-ab8d-0da30382d957")
  private val identifier = TestData.identifier(type = Identifier.IdentifierType.BpPassport, value = "435645")

  @Test
  fun `when the screen is shown, then load the patient details`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(LinkIdWithPatientViewShown(patientUuid, identifier))
        .then(
            assertThatNext(
                hasModel(defaultModel.linkIdWithPatientViewShown(patientUuid, identifier)),
                hasEffects(GetPatientNameFromId(patientUuid))
            )
        )
  }

  @Test
  fun `when the patient details are loaded, then update the UI`() {
    val patientName = "TestName"

    val linkIdWithPatientViewShownModel = defaultModel.linkIdWithPatientViewShown(patientUuid, identifier)
    updateSpec
        .given(linkIdWithPatientViewShownModel)
        .whenEvent(PatientNameReceived(patientName))
        .then(
            assertThatNext(
                hasModel(linkIdWithPatientViewShownModel.patientNameFetched(patientName)),
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
                hasModel(defaultModel.saved()),
                hasEffects(CloseSheetWithLinkedId)
            )
        )
  }
}
