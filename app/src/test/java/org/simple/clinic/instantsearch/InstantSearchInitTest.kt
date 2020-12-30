package org.simple.clinic.instantsearch

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.TestData

class InstantSearchInitTest {

  private val initSpec = InitSpec(InstantSearchInit())
  private val identifier = TestData.identifier()
  private val defaultModel = InstantSearchModel.create(identifier)

  @Test
  fun `when screen is created, then load current facility`() {
    val model = InstantSearchModel.create(null)

    initSpec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(LoadCurrentFacility)
        ))
  }

  @Test
  fun `when screen is created and has additional identifier, then load current facility and open bp passport sheet`() {
    initSpec
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(LoadCurrentFacility, OpenBpPassportSheet(identifier))
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
            hasEffects(ValidateSearchQuery("Pa"), OpenBpPassportSheet(identifier))
        ))
  }
}
