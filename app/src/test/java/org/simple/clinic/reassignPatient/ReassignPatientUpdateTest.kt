package org.simple.clinic.reassignPatient

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.sharedTestCode.TestData
import java.util.Optional
import java.util.UUID

class ReassignPatientUpdateTest {

  private val updateSpec = UpdateSpec(ReassignPatientUpdate())
  private val model = ReassignPatientModel.create()

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
}
