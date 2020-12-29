package org.simple.clinic.instantsearch

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData

class InstantSearchUpdateTest {

  private val updateSpec = UpdateSpec(InstantSearchUpdate())
  private val defaultModel = InstantSearchModel.create()

  @Test
  fun `when current facility is loaded, then update the model and load all patients`() {
    val facility = TestData.facility()

    updateSpec
        .given(defaultModel)
        .whenEvent(CurrentFacilityLoaded(facility))
        .then(assertThatNext(
            hasModel(defaultModel.facilityLoaded(facility)),
            hasEffects(LoadAllPatients(facility))
        ))
  }
}
