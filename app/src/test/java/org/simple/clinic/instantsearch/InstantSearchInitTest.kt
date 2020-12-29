package org.simple.clinic.instantsearch

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test

class InstantSearchInitTest {

  @Test
  fun `when screen is created, then load current facility`() {
    val model = InstantSearchModel.create()

    InitSpec(InstantSearchInit())
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(LoadCurrentFacility)
        ))
  }
}
