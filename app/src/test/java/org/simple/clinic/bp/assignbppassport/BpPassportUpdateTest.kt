package org.simple.clinic.bp.assignbppassport

import com.nhaarman.mockitokotlin2.mock
import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.facility.Facility
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.businessid.Identifier

class BpPassportUpdateTest {

  private val updateSpec = UpdateSpec(BpPassportUpdate())
  private val identifier = Identifier("1111111", Identifier.IdentifierType.BpPassport)
  private val defaultModel = BpPassportModel.create(identifier)
  private val ongoingPatientEntry = OngoingNewPatientEntry(
      identifier = identifier
  )
  private val facility = mock<Facility>()

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
  fun `when the ongoing patient entry is saved, then fetch current facility`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(NewOngoingPatientEntrySaved)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(FetchCurrentFacility)
            )
        )
  }

  @Test
  fun `when the current facility is fetched, then open patient entry screen`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(CurrentFacilityRetrieved(facility))
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(OpenPatientEntryScreen(facility))
            )
        )
  }
}
