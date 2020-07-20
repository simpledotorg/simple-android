package org.simple.clinic.summary.assignedfacility

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.FirstMatchers.hasNoEffects
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.TestData
import java.util.UUID

class AssignedFacilityInitTest {

  private val initSpec = InitSpec(AssignedFacilityInit())
  private val patientUuid = UUID.fromString("1945d937-b601-4803-92b7-422c3ae2a26a")
  private val model = AssignedFacilityModel.create(patientUuid)

  @Test
  fun `when screen is created, then load assigned facility`() {
    initSpec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(LoadAssignedFacility(patientUuid) as AssignedFacilityEffect)
        ))
  }

  @Test
  fun `when screen is restored, then assigned facility should not be loaded`() {
    val facility = TestData.facility(
        uuid = UUID.fromString("df7bd03a-6a0f-4466-a063-17bc61af2dee")
    )
    val model = model.assignedFacilityUpdated(facility = facility)

    initSpec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasNoEffects()
        ))
  }
}
