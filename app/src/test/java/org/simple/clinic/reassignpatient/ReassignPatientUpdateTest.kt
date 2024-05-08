package org.simple.clinic.reassignpatient

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.sharedTestCode.TestData
import java.util.Optional
import java.util.UUID

class ReassignPatientUpdateTest {

  private val patientUuid = UUID.fromString("b66a9d8e-fdf1-494f-b9d7-6e7cc5679cbe")
  private val updateSpec = UpdateSpec(ReassignPatientUpdate())
  private val model = ReassignPatientModel.create(patientUuid)

  @Test
  fun `when assigned facility is loaded, then update the model`() {
    val facility = TestData.facility(
        uuid = UUID.fromString("06f5e0af-9464-4636-807a-dbb9984061fd"),
        name = "UHC Doha"
    )

    updateSpec
        .given(model)
        .whenEvent(AssignedFacilityLoaded(Optional.of(facility)))
        .then(assertThatNext(
            hasModel(model.assignedFacilityUpdated(facility)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when reassign patient not now is clicked, then close sheet`() {
    updateSpec
        .given(model)
        .whenEvent(NotNowClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CloseSheet(ReassignPatientSheetClosedFrom.NOT_NOW))
        ))
  }

  @Test
  fun `when reassign patient change is clicked, then open select facility sheet`() {
    updateSpec
        .given(model)
        .whenEvent(ChangeClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenSelectFacilitySheet)
        ))
  }

  @Test
  fun `when new assign facility is selected, then change the assigned facility`() {
    val assignedFacility = TestData.facility(
        uuid = UUID.fromString("06f5e0af-9464-4636-807a-dbb9984061fd"),
        name = "UHC Doha"
    )

    updateSpec
        .given(model)
        .whenEvent(NewAssignedFacilitySelected(assignedFacility))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ChangeAssignedFacility(model.patientUuid, assignedFacility.uuid))
        ))
  }

  @Test
  fun `when assigned facility is changed, then close the sheet`() {
    updateSpec
        .given(model)
        .whenEvent(AssignedFacilityChanged)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CloseSheet(ReassignPatientSheetClosedFrom.CHANGE))
        ))
  }
}
