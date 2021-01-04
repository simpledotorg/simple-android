package org.simple.clinic.instantsearch

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.TestData

class InstantSearchInitTest {

  private val initSpec = InitSpec(InstantSearchInit())
  private val defaultModel = InstantSearchModel.create()

  @Test
  fun `when screen is created, then load current facility`() {
    initSpec
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(LoadCurrentFacility)
        ))
  }

  @Test
  fun `when screen is restored and facility is loaded, then validate search query`() {
    val facility = TestData.facility()
    val facilityLoadedModel = defaultModel
        .facilityLoaded(facility)
        .searchQueryChanged("Pa")

    initSpec
        .whenInit(facilityLoadedModel)
        .then(assertThatFirst(
            hasModel(facilityLoadedModel),
            hasEffects(ValidateSearchQuery("Pa"))
        ))
  }
}
