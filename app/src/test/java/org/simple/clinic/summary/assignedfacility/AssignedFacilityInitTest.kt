package org.simple.clinic.summary.assignedfacility

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import java.util.UUID

class AssignedFacilityInitTest {

  @Test
  fun `when screen is created, then load assigned facility`() {
    val initSpec = InitSpec(AssignedFacilityInit())
    val patientUuid = UUID.fromString("1945d937-b601-4803-92b7-422c3ae2a26a")
    val model = AssignedFacilityModel.create(patientUuid)

    initSpec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(LoadAssignedFacility(patientUuid) as AssignedFacilityEffect)
        ))
  }
}
