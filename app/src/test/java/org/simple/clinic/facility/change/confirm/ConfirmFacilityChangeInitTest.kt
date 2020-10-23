package org.simple.clinic.facility.change.confirm

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.FirstMatchers.hasNoEffects
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.TestData
import java.util.UUID

class ConfirmFacilityChangeInitTest {

  private val spec = InitSpec(ConfirmFacilityChangeInit())

  private val defaultModel = ConfirmFacilityChangeModel.create()

  @Test
  fun `when the screen is created, load the required information`() {
    spec
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(LoadCurrentFacility)
        ))
  }

  @Test
  fun `when the screen is restored, dont load the required information`() {
    val facility = TestData.facility(uuid = UUID.fromString("3d362e26-c8ca-4bd6-8910-4fbcbcb98678"))

    val model = defaultModel.currentFacilityLoaded(facility)

    spec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasNoEffects()
        ))
  }
}
