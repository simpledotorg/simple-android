package org.simple.clinic.facilitypicker

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.facilitypicker.PickFrom.AllFacilities
import org.simple.clinic.facilitypicker.PickFrom.InCurrentGroup
import java.time.Duration

class FacilityPickerInitTest {

  private val defaultModel = FacilityPickerModel.create()

  private lateinit var spec: InitSpec<FacilityPickerModel, FacilityPickerEffect>

  @Test
  fun `when the screen is created for fetching from all facilities, all facilities must be loaded`() {
    setup(AllFacilities)

    spec
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(LoadFacilitiesWithQuery("") as FacilityPickerEffect)
        ))
  }

  @Test
  fun `when the screen is created for fetching from current facility group, facilities in current group must be loaded`() {
    setup(InCurrentGroup)

    spec
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(LoadFacilitiesInCurrentGroup("") as FacilityPickerEffect)
        ))

  }

  private fun setup(pickFrom: PickFrom) {
    val init = FacilityPickerInit(
        pickFrom = pickFrom,
        locationUpdateInterval = Duration.ZERO,
        locationTimeout = Duration.ZERO,
        discardLocationOlderThan = Duration.ZERO
    )

    spec = InitSpec(init)
  }
}
