package org.simple.clinic.summary.assignedfacility

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.util.Optional
import java.util.UUID

class AssignedFacilityUpdateTest {

  private val patientUuid = UUID.fromString("29a85b60-0b85-4380-9d0f-7c41e9dda976")
  private val updateSpec = UpdateSpec(AssignedFacilityUpdate())
  private val model = AssignedFacilityModel.create(patientUuid)

  @Test
  fun `when assigned facility is loaded, then update the model`() {
    val facility = TestData.facility(
        uuid = UUID.fromString("f15e9f57-29e5-4495-9cd5-2ca8485b3d6c"),
        name = "PHC Obvious"
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
  fun `when change assign facility button is clicked, then open facility selection`() {
    updateSpec
        .given(model)
        .whenEvent(ChangeAssignedFacilityButtonClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenFacilitySelection as AssignedFacilityEffect)
        ))
  }

  @Test
  fun `change assigned facility when new assigned facility is selected`() {
    val facility = TestData.facility(
        uuid = UUID.fromString("c131da1e-7fef-4edd-ac9a-cb5ebf732606"),
        name = "CHC Obvious"
    )
    val newAssignedFacility = TestData.facility(
        uuid = UUID.fromString("13529930-7195-4cba-9cef-e0489de8da53"),
        name = "CHC Bagta"
    )
    val model = model.assignedFacilityUpdated(facility)

    updateSpec
        .given(model)
        .whenEvent(AssignedFacilitySelected(newAssignedFacility))
        .then(assertThatNext(
            hasModel(model.assignedFacilityUpdated(newAssignedFacility)),
            hasEffects(ChangeAssignedFacility(patientUuid, newAssignedFacility.uuid) as AssignedFacilityEffect)
        ))
  }
}
