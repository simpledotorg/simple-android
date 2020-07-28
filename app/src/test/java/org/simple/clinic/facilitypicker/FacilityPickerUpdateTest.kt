package org.simple.clinic.facilitypicker

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.facilitypicker.PickFrom.AllFacilities
import org.simple.clinic.facilitypicker.PickFrom.InCurrentGroup

class FacilityPickerUpdateTest {

  private val defaultModel = FacilityPickerModel.create()

  private lateinit var spec: UpdateSpec<FacilityPickerModel, FacilityPickerEvent, FacilityPickerEffect>

  @Test
  fun `when the screen is created for fetching from all facilities, search queries should bne done in all facilities`() {
    setup(AllFacilities)

    val query = "HC"

    spec
        .given(defaultModel)
        .whenEvent(SearchQueryChanged(query))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(LoadFacilitiesWithQuery(query) as FacilityPickerEffect)
        ))
  }

  @Test
  fun `when the screen is created for fetching from current facility group, search queries should bne done only in current facility group`() {
    setup(InCurrentGroup)

    val query = "HC"

    spec
        .given(defaultModel)
        .whenEvent(SearchQueryChanged(query))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(LoadFacilitiesInCurrentGroup(query) as FacilityPickerEffect)
        ))
  }

  private fun setup(pickFrom: PickFrom) {
    val update = FacilityPickerUpdate(pickFrom)

    spec = UpdateSpec(update)
  }
}
