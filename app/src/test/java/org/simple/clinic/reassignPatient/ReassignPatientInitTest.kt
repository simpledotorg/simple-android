package org.simple.clinic.reassignPatient

import com.spotify.mobius.test.FirstMatchers
import com.spotify.mobius.test.InitSpec
import org.junit.Test
import org.simple.sharedTestCode.TestData
import java.util.UUID

class ReassignPatientInitTest {

  private val initSpec = InitSpec(ReassignPatientInit())
  private val patientUuid = UUID.fromString("e3ec6110-abe2-48c0-ad54-640e0c51c1fd")

  @Test
  fun `when the screen is created, load assigned facility`() {
    val model = ReassignPatientModel.create(patientUuid)

    initSpec
        .whenInit(model)
        .then(
            InitSpec.assertThatFirst(
                FirstMatchers.hasModel(model),
                FirstMatchers.hasEffects(LoadAssignedFacility(patientUuid))
            )
        )
  }

  @Test
  fun `when the model has assigned facility, then do not load assigned facility`() {
    val facility = TestData.facility(
        uuid = UUID.fromString("06f5e0af-9464-4636-807a-dbb9984061fd"),
        name = "UHC Doha"
    )

    val model = ReassignPatientModel
        .create(patientUuid)
        .assignedFacilityUpdated(facility)

    initSpec
        .whenInit(model)
        .then(
            InitSpec.assertThatFirst(
                FirstMatchers.hasModel(model),
                FirstMatchers.hasNoEffects()
            )
        )
  }
}
