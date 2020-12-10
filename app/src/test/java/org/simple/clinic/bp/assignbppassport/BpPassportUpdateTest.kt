package org.simple.clinic.bp.assignbppassport

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.patient.businessid.Identifier

class BpPassportUpdateTest {

  @Test
  fun `when register new patient button is clicked, then save the ongoing patient entry`() {
    val updateSpec = UpdateSpec(BpPassportUpdate())
    val identifier = Identifier("1111111", Identifier.IdentifierType.BpPassport)
    val defaultModel = BpPassportModel.create(identifier)
    val ongoingPatientEntry = TestData.ongoingPatientEntry(
        identifier = identifier
    )

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
}
